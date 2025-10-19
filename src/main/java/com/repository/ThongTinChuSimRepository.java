package com.repository;

import com.model.ThongTinChuSim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThongTinChuSimRepository extends JpaRepository<ThongTinChuSim, Integer> {
    boolean existsBySim_Iccid(String iccid);

    long countBySim_Iccid(String iccid);
}