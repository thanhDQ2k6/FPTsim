package com.repository;

import com.model.DanhGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    
    // Find rating by order
    Optional<DanhGia> findByHoaDon_MaHD(String maHD);
    
    // Find all ratings with pagination
    Page<DanhGia> findAllByOrderByNgayDanhGiaDesc(Pageable pageable);
    
    // Check if order has rating
    boolean existsByHoaDon_MaHD(String maHD);
}