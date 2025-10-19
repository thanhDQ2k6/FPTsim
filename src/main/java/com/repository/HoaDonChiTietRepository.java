package com.repository;

import com.model.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    boolean existsBySim_Iccid(String iccid);

    long countBySim_Iccid(String iccid);
}