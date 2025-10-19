package com.repository;

import com.model.ThongKeBanHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ThongKeBanHangRepository extends JpaRepository<ThongKeBanHang, LocalDate> {
}