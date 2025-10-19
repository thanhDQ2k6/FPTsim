package com.repository;

import com.model.ApDungUuDai;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApDungUuDaiRepository extends JpaRepository<ApDungUuDai, Integer> {
    boolean existsByHoaDon_MaHD(String maHD);
    ApDungUuDai findByHoaDon_MaHD(String maHD);
}