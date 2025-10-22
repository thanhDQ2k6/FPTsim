package com.repository;

import com.model.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    boolean existsBySim_Iccid(String iccid);

    long countBySim_Iccid(String iccid);

    void deleteBySim_Iccid(String iccid);
    
    // Find cart items by customer
    List<GioHang> findByKhachHang_Email(String email);
    
    // Count cart items by customer
    int countByKhachHang_Email(String email);
    
    // Check if item exists in cart
    boolean existsByKhachHang_EmailAndSim_Iccid(String email, String iccid);
}