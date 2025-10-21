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

    // Import hàng loạt: 1 NhapSim header + nhiều ChiTietNhapSim
    @Transactional
    public void importSimsBatch(ImportSimsRequest batch) {
        if (batch == null || batch.getSims() == null || batch.getSims().isEmpty()) {
            throw new BusinessException("EMPTY_IMPORT", "Danh sách SIM nhập trống");
        }
        if (isBlank(batch.getNhaCungCap())) {
            throw new BusinessException("INVALID_SUPPLIER", "Thiếu nhà cung cấp");
        }

        // Kiểm tra trước để fail-fast
        Set<String> seenIccids = new HashSet<>();
        Set<String> seenMsisdn = new HashSet<>();
        for (var s : batch.getSims()) {
            validateCreate(s, batch.getNhaCungCap());
            if (!seenIccids.add(s.getIccid())) {
                throw new BusinessException("DUP_ICCID_IN_PAYLOAD", "ICCID bị lặp trong payload: " + s.getIccid());
            }
            if (s.getMsisdn() != null && !seenMsisdn.add(s.getMsisdn())) {
                throw new BusinessException("DUP_MSISDN_IN_PAYLOAD", "MSISDN bị lặp trong payload: " + s.getMsisdn());
            }
            if (simRepo.existsById(s.getIccid())) {
                throw new BusinessException("SIM_ALREADY_EXISTS", "SIM đã tồn tại: " + s.getIccid());
            }
            if (s.getMsisdn() != null && simRepo.existsByMsisdn(s.getMsisdn())) {
                throw new BusinessException("MSISDN_ALREADY_EXISTS", "Số msisdn đã tồn tại: " + s.getIccid());
            }
        }

        NguoiDung nv = userRepo.findById(IMPORT_STAFF_EMAIL)
                .orElseThrow(() -> new BusinessException("IMPORT_STAFF_NOT_FOUND",
                        "Thiếu nhân viên nhập: " + IMPORT_STAFF_EMAIL));

        NhapSim nhap = new NhapSim();
        nhap.setNhanVien(nv);
        nhap.setNhaCungCap(batch.getNhaCungCap());
        nhap.setGhiChu(batch.getGhiChu());

        for (var s : batch.getSims()) {
            Sim sim = new Sim();
            sim.setIccid(s.getIccid());
            sim.setMsisdn(s.getMsisdn());
            sim.setNhaMang(s.getNhaMang());
            sim.setLoaiSim(s.getLoaiSim());
            sim.setGiaBan(s.getGiaBan());
            sim.setTrangThai(Sim.TrangThai.SanSang);
            simRepo.save(sim);

            nhap.addChiTietNhap(sim, s.getGiaNhap());
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

        if (req.getMsisdn() != null && !req.getMsisdn().equals(sim.getMsisdn()) && simRepo.existsByMsisdn(req.getMsisdn())) {
            throw new BusinessException("MSISDN_ALREADY_EXISTS", "Số msisdn đã tồn tại: " + req.getMsisdn());
        }
        if (req.getMsisdn() != null) sim.setMsisdn(req.getMsisdn());
        if (req.getLoaiSim() != null) sim.setLoaiSim(req.getLoaiSim());
        if (req.getGiaBan() != null) {
            if (req.getGiaBan().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_GIABAN", "Giá bán phải > 0");
            }
            sim.setGiaBan(req.getGiaBan());
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
        if (isBlank(req.getIccid())) throw new BusinessException("INVALID_ICCID", "Thiếu ICCID");
        if (req.getNhaMang() == null) throw new BusinessException("INVALID_NHAMANG", "Thiếu nhà mạng");
        if (req.getLoaiSim() == null) throw new BusinessException("INVALID_LOAISIM", "Thiếu loại SIM");
        if (req.getGiaBan() == null || req.getGiaBan().compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("INVALID_GIABAN", "Giá bán phải > 0");
        if (req.getGiaNhap() == null || req.getGiaNhap().compareTo(BigDecimal.ZERO) < 0)
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
    
    // Get all SIMs with pagination (for admin)
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Sim> getAllSims(org.springframework.data.domain.Pageable pageable) {
        return simRepo.findAll(pageable);
    }
    
    // Get SIMs with filters
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Sim> getFilteredSims(String nhaMang, String loaiSim, String trangThai, 
                                                                       org.springframework.data.domain.Pageable pageable) {
        // Convert string parameters to enums
        Sim.NhaMang nhaMangEnum = nhaMang != null && !nhaMang.isBlank() ? 
            Sim.NhaMang.valueOf(nhaMang) : null;
        Sim.LoaiSim loaiSimEnum = loaiSim != null && !loaiSim.isBlank() ? 
            Sim.LoaiSim.valueOf(loaiSim) : null;
        Sim.TrangThai trangThaiEnum = trangThai != null && !trangThai.isBlank() ? 
            Sim.TrangThai.valueOf(trangThai) : null;
        
        // Build query using Specification or custom repository method
        // For now, using a simple approach with custom queries
        return getSimsByFilters(nhaMangEnum, loaiSimEnum, trangThaiEnum, pageable);
    }
    
    private org.springframework.data.domain.Page<Sim> getSimsByFilters(Sim.NhaMang nhaMang, Sim.LoaiSim loaiSim, 
                                                                         Sim.TrangThai trangThai,
                                                                         org.springframework.data.domain.Pageable pageable) {
        // Apply filters based on what is provided
        if (nhaMang != null && loaiSim != null && trangThai != null) {
            // All three filters
            return simRepo.findAll(
                (root, query, cb) -> cb.and(
                    cb.equal(root.get("nhaMang"), nhaMang),
                    cb.equal(root.get("loaiSim"), loaiSim),
                    cb.equal(root.get("trangThai"), trangThai)
                ), pageable);
        } else if (nhaMang != null && loaiSim != null) {
            // Provider and Type
            return simRepo.findAll(
                (root, query, cb) -> cb.and(
                    cb.equal(root.get("nhaMang"), nhaMang),
                    cb.equal(root.get("loaiSim"), loaiSim)
                ), pageable);
        } else if (nhaMang != null && trangThai != null) {
            // Provider and Status
            return simRepo.findAll(
                (root, query, cb) -> cb.and(
                    cb.equal(root.get("nhaMang"), nhaMang),
                    cb.equal(root.get("trangThai"), trangThai)
                ), pageable);
        } else if (loaiSim != null && trangThai != null) {
            // Type and Status
            return simRepo.findAll(
                (root, query, cb) -> cb.and(
                    cb.equal(root.get("loaiSim"), loaiSim),
                    cb.equal(root.get("trangThai"), trangThai)
                ), pageable);
        } else if (nhaMang != null) {
            // Provider only
            return simRepo.findAll(
                (root, query, cb) -> cb.equal(root.get("nhaMang"), nhaMang), pageable);
        } else if (loaiSim != null) {
            // Type only
            return simRepo.findAll(
                (root, query, cb) -> cb.equal(root.get("loaiSim"), loaiSim), pageable);
        } else if (trangThai != null) {
            // Status only
            return simRepo.findAll(
                (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai), pageable);
        } else {
            // No filters
            return simRepo.findAll(pageable);
        }
    }
    
    // Get SIMs imported by staff
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Sim> getSimsByStaff(String email, org.springframework.data.domain.Pageable pageable) {
        return simRepo.findByImportedBy(email, pageable);
    }
    
    // Get SIMs imported by staff with filters
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Sim> getFilteredSimsByStaff(String email, String nhaMang, String loaiSim, 
                                                                             String trangThai,
                                                                             org.springframework.data.domain.Pageable pageable) {
        // For staff filtering, we need to join with import records and apply filters
        // This is more complex, so for now we'll get all staff sims and filter in memory (not optimal)
        // TODO: Optimize with custom query
        return simRepo.findByImportedBy(email, pageable);
    }
    
    // Get SIM by ID
    @Transactional(readOnly = true)
    public Sim getSimById(String iccid) {
        return simRepo.findById(iccid)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy SIM: " + iccid));
    }
}