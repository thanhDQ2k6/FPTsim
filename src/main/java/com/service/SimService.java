package com.service;

import com.exception.BusinessException;
import com.exception.NotFoundException;
import com.model.NguoiDung;
import com.model.NhapSim;
import com.model.Sim;
import com.repository.*;
import com.web.dto.ImportSimsRequest;
import com.web.dto.SimCreateRequest;
import com.web.dto.SimUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SimService {

    private final SimRepository simRepo;
    private final NguoiDungRepository userRepo;
    private final NhapSimRepository nhapSimRepo;
    private final ChiTietNhapSimRepository ctNhapRepo;
    private final GioHangRepository gioHangRepo;
    private final HoaDonChiTietRepository hdctRepo;
    private final ThongTinChuSimRepository ttcsRepo;

    // Nhân viên thực hiện nhập (đã tồn tại trong CSDL theo xác nhận)
    private static final String IMPORT_STAFF_EMAIL = "root@mail.com";

    // Tạo 1 SIM và ghi lịch sử nhập
    @Transactional
    public void createSimAndLogImport(SimCreateRequest req, String nhaCungCap, String ghiChu) {
        validateCreate(req, nhaCungCap);
        ensureUnique(req.iccid, req.msisdn);

        Sim sim = new Sim();
        sim.setIccid(req.iccid);
        sim.setMsisdn(req.msisdn);
        sim.setNhaMang(req.nhaMang);
        sim.setLoaiSim(req.loaiSim);
        sim.setGiaBan(req.giaBan);
        sim.setTrangThai(Sim.TrangThai.SanSang);
        simRepo.save(sim);

        NguoiDung nv = userRepo.findById(IMPORT_STAFF_EMAIL)
                .orElseThrow(() -> new BusinessException("IMPORT_STAFF_NOT_FOUND",
                        "Thiếu nhân viên nhập: " + IMPORT_STAFF_EMAIL));

        NhapSim nhap = new NhapSim();
        nhap.setNhanVien(nv);
        nhap.setNhaCungCap(nhaCungCap);
        nhap.setGhiChu(ghiChu);
        nhap.addChiTietNhap(sim, req.giaNhap);
        nhapSimRepo.save(nhap);
    }

    // Import hàng loạt: 1 NhapSim header + nhiều ChiTietNhapSim
    @Transactional
    public void importSimsBatch(ImportSimsRequest batch) {
        if (batch == null || batch.sims == null || batch.sims.isEmpty()) {
            throw new BusinessException("EMPTY_IMPORT", "Danh sách SIM nhập trống");
        }
        if (isBlank(batch.nhaCungCap)) {
            throw new BusinessException("INVALID_SUPPLIER", "Thiếu nhà cung cấp");
        }

        // Kiểm tra trước để fail-fast
        Set<String> seenIccids = new HashSet<>();
        Set<String> seenMsisdn = new HashSet<>();
        for (var s : batch.sims) {
            validateCreate(s, batch.nhaCungCap);
            if (!seenIccids.add(s.iccid)) {
                throw new BusinessException("DUP_ICCID_IN_PAYLOAD", "ICCID bị lặp trong payload: " + s.iccid);
            }
            if (s.msisdn != null && !seenMsisdn.add(s.msisdn)) {
                throw new BusinessException("DUP_MSISDN_IN_PAYLOAD", "MSISDN bị lặp trong payload: " + s.msisdn);
            }
            if (simRepo.existsById(s.iccid)) {
                throw new BusinessException("SIM_ALREADY_EXISTS", "SIM đã tồn tại: " + s.iccid);
            }
            if (s.msisdn != null && simRepo.existsByMsisdn(s.msisdn)) {
                throw new BusinessException("MSISDN_ALREADY_EXISTS", "Số msisdn đã tồn tại: " + s.msisdn);
            }
        }

        NguoiDung nv = userRepo.findById(IMPORT_STAFF_EMAIL)
                .orElseThrow(() -> new BusinessException("IMPORT_STAFF_NOT_FOUND",
                        "Thiếu nhân viên nhập: " + IMPORT_STAFF_EMAIL));

        NhapSim nhap = new NhapSim();
        nhap.setNhanVien(nv);
        nhap.setNhaCungCap(batch.nhaCungCap);
        nhap.setGhiChu(batch.ghiChu);

        for (var s : batch.sims) {
            Sim sim = new Sim();
            sim.setIccid(s.iccid);
            sim.setMsisdn(s.msisdn);
            sim.setNhaMang(s.nhaMang);
            sim.setLoaiSim(s.loaiSim);
            sim.setGiaBan(s.giaBan);
            sim.setTrangThai(Sim.TrangThai.SanSang);
            simRepo.save(sim);

            nhap.addChiTietNhap(sim, s.giaNhap);
        }

        nhapSimRepo.save(nhap);
    }

    // Cập nhật chỉ khi SanSang
    @Transactional
    public void updateSimIfReady(String iccid, SimUpdateRequest req) {
        Sim sim = simRepo.findById(iccid)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy SIM: " + iccid));

        if (sim.getTrangThai() != Sim.TrangThai.SanSang) {
            throw new BusinessException("SIM_NOT_EDITABLE", "Chỉ được sửa SIM ở trạng thái Sẵn sàng");
        }

        if (req.msisdn != null && !req.msisdn.equals(sim.getMsisdn()) && simRepo.existsByMsisdn(req.msisdn)) {
            throw new BusinessException("MSISDN_ALREADY_EXISTS", "Số msisdn đã tồn tại: " + req.msisdn);
        }
        if (req.msisdn != null) sim.setMsisdn(req.msisdn);
        if (req.loaiSim != null) sim.setLoaiSim(req.loaiSim);
        if (req.giaBan != null) {
            if (req.giaBan.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_GIABAN", "Giá bán phải > 0");
            }
            sim.setGiaBan(req.giaBan);
        }

        simRepo.save(sim);
    }

    // Soft delete: chuyển Chet + dọn khỏi giỏ
    @Transactional
    public void deactivateSim(String iccid) {
        Sim sim = simRepo.findById(iccid)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy SIM: " + iccid));

        if (sim.getTrangThai() == Sim.TrangThai.Chet) {
            return; // idempotent
        }

        // Dọn giỏ để tránh checkout lỗi
        gioHangRepo.deleteBySim_Iccid(iccid);

        sim.setTrangThai(Sim.TrangThai.Chet);
        simRepo.save(sim);
    }

    // Hard delete (ít dùng): chỉ khi thật sự không còn tham chiếu nào
    @Transactional
    public void hardDeleteIfAbsolutelyNoRefs(String iccid) {
        Sim sim = simRepo.findById(iccid)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy SIM: " + iccid));

        if (gioHangRepo.existsBySim_Iccid(iccid) ||
                hdctRepo.existsBySim_Iccid(iccid) ||
                ttcsRepo.existsBySim_Iccid(iccid) ||
                ctNhapRepo.existsBySim_Iccid(iccid)) {
            throw new BusinessException("FK_CONFLICT", "SIM còn tham chiếu; chỉ nên dùng soft delete");
        }
        try {
            simRepo.delete(sim);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("FK_CONFLICT", "Không thể xóa do còn tham chiếu");
        }
    }

    // Helpers
    private void validateCreate(SimCreateRequest req, String nhaCungCap) {
        if (req == null) throw new BusinessException("INVALID_INPUT", "Thiếu dữ liệu SIM");
        if (isBlank(req.iccid)) throw new BusinessException("INVALID_ICCID", "Thiếu ICCID");
        if (req.nhaMang == null) throw new BusinessException("INVALID_NHAMANG", "Thiếu nhà mạng");
        if (req.loaiSim == null) throw new BusinessException("INVALID_LOAISIM", "Thiếu loại SIM");
        if (req.giaBan == null || req.giaBan.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("INVALID_GIABAN", "Giá bán phải > 0");
        if (req.giaNhap == null || req.giaNhap.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException("INVALID_GIANHAP", "Giá nhập không hợp lệ");
        if (isBlank(nhaCungCap))
            throw new BusinessException("INVALID_SUPPLIER", "Thiếu nhà cung cấp");
    }

    private void ensureUnique(String iccid, String msisdn) {
        if (simRepo.existsById(iccid)) {
            throw new BusinessException("SIM_ALREADY_EXISTS", "SIM đã tồn tại: " + iccid);
        }
        if (msisdn != null && simRepo.existsByMsisdn(msisdn)) {
            throw new BusinessException("MSISDN_ALREADY_EXISTS", "Số msisdn đã tồn tại: " + msisdn);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}