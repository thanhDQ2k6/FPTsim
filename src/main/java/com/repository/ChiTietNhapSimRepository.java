package com.repository;

import com.model.ChiTietNhapSim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietNhapSimRepository extends JpaRepository<ChiTietNhapSim, Integer> {
    boolean existsBySim_Iccid(String iccid);

    long countBySim_Iccid(String iccid);
}