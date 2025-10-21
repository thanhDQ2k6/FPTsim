package com.service;

import com.exception.BusinessException;
import com.exception.NotFoundException;
import com.model.*;
import com.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for order/bill management and checkout process.
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GioHangRepository gioHangRepository;
    private final SimRepository simRepository;
    private final UuDaiRepository uuDaiRepository;
    private final ThongTinChuSimRepository thongTinChuSimRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final DanhGiaRepository danhGiaRepository;
    
    /**
     * Create order from cart items with SIM owner information.
     */
    @Transactional
    public HoaDon createOrderFromCart(String customerEmail, String ownerName, 
                                      String ownerCccd, String ownerDateOfBirth,
                                      String ownerPhone, String ownerAddress,
                                      String discountCode) {
        NguoiDung customer = nguoiDungRepository.findById(customerEmail)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        
        List<GioHang> cartItems = gioHangRepository.findByKhachHang_Email(customerEmail);
        if (cartItems.isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Cart is empty");
        }
        
        // Validate owner info
        if (ownerName == null || ownerName.isBlank()) {
            throw new BusinessException("INVALID_OWNER", "Owner name is required");
        }
        if (ownerCccd == null || ownerCccd.isBlank()) {
            throw new BusinessException("INVALID_OWNER", "Owner CCCD is required");
        }
        if (ownerDateOfBirth == null || ownerDateOfBirth.isBlank()) {
            throw new BusinessException("INVALID_OWNER", "Owner date of birth is required");
        }
        if (ownerPhone == null || ownerPhone.isBlank()) {
            throw new BusinessException("INVALID_OWNER", "Owner phone is required");
        }
        
        // Create order
        HoaDon order = new HoaDon();
        order.setMaHD("HD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setKhachHang(customer);
        order.setTrangThaiDon(HoaDon.TrangThaiDon.ChoXacNhan);
        order.setNgayTao(LocalDateTime.now());
        order.setNgayCapNhat(LocalDateTime.now());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Add order details and SIM owner info
        for (GioHang cartItem : cartItems) {
            Sim sim = cartItem.getSim();
            
            // Validate SIM is still available
            if (sim.getTrangThai() != Sim.TrangThai.SanSang) {
                throw new BusinessException("SIM_NOT_AVAILABLE", 
                    "SIM " + sim.getMsisdn() + " is no longer available");
            }
            
            // Create order detail (each SIM is unique, no quantity needed)
            HoaDonChiTiet detail = new HoaDonChiTiet();
            detail.setHoaDon(order);
            detail.setSim(sim);
            detail.setGiaBan(sim.getGiaBan());
            detail.setGiaCuoi(sim.getGiaBan()); // Before discount
            order.getHoaDonChiTiets().add(detail);
            
            // Create SIM owner info with proper CCCD
            ThongTinChuSim ownerInfo = new ThongTinChuSim();
            ownerInfo.setSim(sim);
            ownerInfo.setHoaDon(order);
            ownerInfo.setHoTen(ownerName);
            ownerInfo.setCccd(ownerCccd); // Now using proper CCCD
            ownerInfo.setSdt(ownerPhone);
            ownerInfo.setDiaChi(ownerAddress);
            // Parse date of birth
            try {
                ownerInfo.setNgaySinh(java.time.LocalDate.parse(ownerDateOfBirth));
            } catch (Exception e) {
                throw new BusinessException("INVALID_DATE", "Invalid date of birth format");
            }
            thongTinChuSimRepository.save(ownerInfo);
            
            // Update SIM status to DaBan
            sim.setTrangThai(Sim.TrangThai.DaBan);
            simRepository.save(sim);
            
            totalAmount = totalAmount.add(sim.getGiaBan());
        }
        
        // Apply discount if provided
        BigDecimal finalTotalAmount = totalAmount;
        if (discountCode != null && !discountCode.isBlank()) {
            uuDaiRepository.findById(discountCode).ifPresent(discount -> {
                if (isDiscountValid(discount)) {
                    BigDecimal discountAmount;
                    if (discount.getLoaiGiam() == UuDai.LoaiGiam.PhanTram) {
                        discountAmount = finalTotalAmount.multiply(
                            discount.getGiaTriGiam().divide(BigDecimal.valueOf(100)));
                    } else {
                        discountAmount = discount.getGiaTriGiam();
                    }
                    BigDecimal newTotal = finalTotalAmount.subtract(discountAmount);
                    order.setTongTien(newTotal.max(BigDecimal.ZERO));
                    
                    ApDungUuDai appliedDiscount = new ApDungUuDai();
                    appliedDiscount.setHoaDon(order);
                    appliedDiscount.setUuDai(discount);
                    appliedDiscount.setKhachHang(customer);
                    order.getApDungUuDais().add(appliedDiscount);
                }
            });
        }
        
        // Set total if no discount applied
        if (order.getTongTien() == null || order.getTongTien().equals(BigDecimal.ZERO)) {
            order.setTongTien(totalAmount);
        }
        
        hoaDonRepository.save(order);
        
        // Clear cart
        gioHangRepository.deleteAll(cartItems);
        
        return order;
    }
    
    /**
     * Get customer's purchase history.
     */
    @Transactional(readOnly = true)
    public Page<HoaDon> getCustomerOrders(String customerEmail, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return hoaDonRepository.findByKhachHang_Email(customerEmail, pageable);
    }
    
    /**
     * Get next pending order from queue (for staff).
     */
    @Transactional(readOnly = true)
    public HoaDon getNextPendingOrder() {
        return hoaDonRepository.findFirstByTrangThaiDonOrderByNgayTaoAsc(
            HoaDon.TrangThaiDon.ChoXacNhan).orElse(null);
    }
    
    /**
     * Approve order (staff action).
     */
    @Transactional
    public void approveOrder(String orderId, String staffEmail) {
        HoaDon order = hoaDonRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));
        
        if (order.getTrangThaiDon() != HoaDon.TrangThaiDon.ChoXacNhan) {
            throw new BusinessException("INVALID_STATUS", "Order is not pending");
        }
        
        NguoiDung staff = nguoiDungRepository.findById(staffEmail)
            .orElseThrow(() -> new NotFoundException("Staff not found"));
        
        order.setNhanVien(staff);
        order.setTrangThaiDon(HoaDon.TrangThaiDon.DaHoanThanh);
        order.setNgayCapNhat(LocalDateTime.now());
        
        // Update SIM status to HoatDong
        for (HoaDonChiTiet detail : order.getHoaDonChiTiets()) {
            Sim sim = detail.getSim();
            sim.setTrangThai(Sim.TrangThai.HoatDong);
            simRepository.save(sim);
        }
        
        hoaDonRepository.save(order);
    }
    
    /**
     * Get orders processed by staff (or all if staffEmail is null for admin).
     */
    @Transactional(readOnly = true)
    public Page<HoaDon> getStaffProcessedOrders(String staffEmail, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayCapNhat"));
        if (staffEmail == null) {
            // Admin view - all completed orders
            return hoaDonRepository.findByTrangThaiDon(HoaDon.TrangThaiDon.DaHoanThanh, pageable);
        }
        return hoaDonRepository.findByNhanVien_EmailOrderByNgayCapNhatDesc(staffEmail, pageable);
    }
    
    /**
     * Add rating for completed order.
     */
    @Transactional
    public void addRating(String orderId, int stars, String comment) {
        HoaDon order = hoaDonRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));
        
        if (order.getTrangThaiDon() != HoaDon.TrangThaiDon.DaHoanThanh) {
            throw new BusinessException("INVALID_STATUS", "Can only rate completed orders");
        }
        
        if (danhGiaRepository.existsByHoaDon_MaHD(orderId)) {
            throw new BusinessException("ALREADY_RATED", "Order already has rating");
        }
        
        if (stars < 1 || stars > 5) {
            throw new BusinessException("INVALID_RATING", "Rating must be between 1 and 5");
        }
        
        DanhGia rating = new DanhGia();
        rating.setHoaDon(order);
        rating.setSao(stars);
        rating.setNoiDung(comment);
        rating.setNgayDanhGia(LocalDateTime.now());
        
        danhGiaRepository.save(rating);
    }
    
    /**
     * Get all ratings with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DanhGia> getAllRatings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return danhGiaRepository.findAllByOrderByNgayDanhGiaDesc(pageable);
    }
    
    /**
     * Get order details.
     */
    @Transactional(readOnly = true)
    public HoaDon getOrderDetails(String orderId) {
        return hoaDonRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));
    }
    
    private boolean isDiscountValid(UuDai discount) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return discount.getTrangThai() && 
               !now.isBefore(discount.getNgayBatDau()) && 
               !now.isAfter(discount.getNgayHetHan());
    }
}
