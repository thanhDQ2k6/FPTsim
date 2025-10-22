package com.repository;

import com.model.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, String> {
    
    // Find orders by customer
    Page<HoaDon> findByKhachHang_Email(String email, Pageable pageable);
    
    // Find orders by status
    Page<HoaDon> findByTrangThaiDon(HoaDon.TrangThaiDon status, Pageable pageable);
    
    // Find pending orders (queue for staff)
    List<HoaDon> findByTrangThaiDonOrderByNgayTaoAsc(HoaDon.TrangThaiDon status);
    
    // Find first pending order
    Optional<HoaDon> findFirstByTrangThaiDonOrderByNgayTaoAsc(HoaDon.TrangThaiDon status);
    
    // Find orders processed by staff
    Page<HoaDon> findByNhanVien_EmailOrderByNgayCapNhatDesc(String email, Pageable pageable);
    
    // Count orders by status
    long countByTrangThaiDon(HoaDon.TrangThaiDon status);
    
    // Get orders for reporting
    @Query("SELECT h FROM HoaDon h WHERE h.ngayTao >= :startDate AND h.trangThaiDon = :status")
    List<HoaDon> findRecentCompletedOrders(@Param("startDate") java.time.LocalDateTime startDate, 
                                           @Param("status") HoaDon.TrangThaiDon status);
    
    // Find completed orders without ratings for a customer
    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.email = :email AND h.trangThaiDon = :status AND NOT EXISTS (SELECT d FROM DanhGia d WHERE d.hoaDon = h) ORDER BY h.ngayCapNhat DESC")
    Page<HoaDon> findCompletedOrdersWithoutRatingByCustomer(@Param("email") String email, 
                                                              @Param("status") HoaDon.TrangThaiDon status,
                                                              Pageable pageable);
}