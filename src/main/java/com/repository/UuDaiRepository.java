package com.repository;

import com.model.UuDai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UuDaiRepository extends JpaRepository<UuDai, String> {
    List<UuDai> findAllByOrderByNgayBatDauDesc();
    List<UuDai> findByTrangThaiTrueOrderByNgayBatDauDesc();
}