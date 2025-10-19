package com.service;

import com.exception.BusinessException;
import com.exception.NotFoundException;
import com.model.ApDungUuDai;
import com.model.HoaDon;
import com.model.NguoiDung;
import com.model.UuDai;
import com.repository.ApDungUuDaiRepository;
import com.repository.HoaDonRepository;
import com.repository.NguoiDungRepository;
import com.repository.UuDaiRepository;
import com.web.dto.UuDaiCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UuDaiService {

    private final UuDaiRepository uuDaiRepo;
    private final ApDungUuDaiRepository apDungRepo;

    // Chuẩn bị cho áp dụng ưu đãi
    private final HoaDonRepository hoaDonRepo;
    private final NguoiDungRepository nguoiDungRepo;

    public List<UuDai> listAll() {
        return uuDaiRepo.findAllByOrderByNgayBatDauDesc();
    }

    public List<UuDai> listActive() {
        return uuDaiRepo.findByTrangThaiTrueOrderByNgayBatDauDesc();
    }

    @Transactional
    public void create(UuDaiCreateRequest req) {
        validateCreate(req);

        if (uuDaiRepo.existsById(req.getMaUD())) {
            throw new BusinessException("DISCOUNT_EXISTS", "Mã ưu đãi đã tồn tại: " + req.getMaUD());
        }

        UuDai ud = new UuDai();
        ud.setMaUD(req.getMaUD().trim());
        ud.setMoTa(req.getMoTa());
        ud.setLoaiGiam(req.getLoaiGiam());
        ud.setGiaTriGiam(req.getGiaTriGiam());
        ud.setNgayBatDau(req.getNgayBatDau());
        ud.setNgayHetHan(req.getNgayHetHan());
        ud.setTrangThai(true); // active
        uuDaiRepo.save(ud);
    }

    @Transactional
    public void deactivate(String maUD) {
        UuDai ud = uuDaiRepo.findById(maUD)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ưu đãi: " + maUD));

        if (Boolean.FALSE.equals(ud.getTrangThai())) {
            return; // idempotent
        }
        ud.setTrangThai(false);
        uuDaiRepo.save(ud);
    }

    // OPTIONAL: Áp dụng ưu đãi cho hóa đơn (snapshot)
    // Mỗi hóa đơn chỉ 1 ưu đãi. Nếu đã có -> lỗi.
    @Transactional
    public void applyToHoaDon(String maUD,
                              String maHD,
                              String khachHangEmail,
                              BigDecimal tongTruocGiam) {
        if (tongTruocGiam == null || tongTruocGiam.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_TOTAL", "Tổng tiền trước giảm phải > 0");
        }

        UuDai ud = uuDaiRepo.findById(maUD)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ưu đãi: " + maUD));

        if (!ud.isConHieuLuc()) {
            throw new BusinessException("DISCOUNT_INACTIVE", "Ưu đãi không còn hiệu lực");
        }

        if (apDungRepo.existsByHoaDon_MaHD(maHD)) {
            throw new BusinessException("DISCOUNT_ALREADY_APPLIED", "Hóa đơn đã có ưu đãi");
        }

        HoaDon hd = hoaDonRepo.findById(maHD)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn: " + maHD));
        NguoiDung kh = nguoiDungRepo.findById(khachHangEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng: " + khachHangEmail));

        BigDecimal giam = calcDiscount(ud, tongTruocGiam);

        ApDungUuDai ap = new ApDungUuDai();
        ap.setHoaDon(hd);
        ap.setKhachHang(kh);
        ap.setUuDai(ud);
        ap.setGiaTriApDung(giam);
        // ngayApDung mặc định now trong entity
        apDungRepo.save(ap);
    }

    private BigDecimal calcDiscount(UuDai ud, BigDecimal total) {
        if (ud.getLoaiGiam() == UuDai.LoaiGiam.TienMat) {
            // Không cho vượt quá tổng
            return ud.getGiaTriGiam().min(total);
        } else { // PhanTram
            // 0 < percent <= 100; làm tròn 0.01
            BigDecimal percent = ud.getGiaTriGiam().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            BigDecimal giam = total.multiply(percent);
            giam = giam.setScale(2, RoundingMode.HALF_UP);
            // Không cho vượt quá tổng
            if (giam.compareTo(total) > 0) giam = total;
            return giam;
        }
    }

    private void validateCreate(UuDaiCreateRequest req) {
        if (req == null) throw new BusinessException("INVALID_INPUT", "Thiếu dữ liệu ưu đãi");
        if (req.getMaUD() == null || req.getMaUD().isBlank())
            throw new BusinessException("INVALID_CODE", "Thiếu mã ưu đãi");
        if (req.getLoaiGiam() == null)
            throw new BusinessException("INVALID_TYPE", "Thiếu loại giảm");
        if (req.getGiaTriGiam() == null || req.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("INVALID_VALUE", "Giá trị giảm phải > 0");
        if (req.getLoaiGiam() == UuDai.LoaiGiam.PhanTram &&
                req.getGiaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0)
            throw new BusinessException("INVALID_PERCENT", "Phần trăm giảm không quá 100");
        if (req.getNgayBatDau() == null || req.getNgayHetHan() == null)
            throw new BusinessException("INVALID_DATES", "Thiếu thời gian hiệu lực");
        if (req.getNgayBatDau().isAfter(req.getNgayHetHan()))
            throw new BusinessException("INVALID_DATE_RANGE", "Ngày bắt đầu phải trước hoặc bằng ngày hết hạn");
    }
}