package com.repository;

import com.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {
}