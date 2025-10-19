package com.repository;

import com.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {
    Page<NguoiDung> findAll(Pageable pageable);
    Page<NguoiDung> findByVaiTro(NguoiDung.VaiTro vaiTro, Pageable pageable);
}