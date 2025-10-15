package com.repository;

import com.model.ThongKeBanHang;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;

public interface ThongKeBanHangRepository extends Repository<ThongKeBanHang, LocalDate> {
}