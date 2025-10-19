package com.repository;

import com.model.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    boolean existsBySim_Iccid(String iccid);

    long countBySim_Iccid(String iccid);

    void deleteBySim_Iccid(String iccid);
}