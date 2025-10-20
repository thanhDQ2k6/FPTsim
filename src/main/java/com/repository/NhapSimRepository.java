package com.repository;

import com.model.NhapSim;
import com.web.dto.NhapSimSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NhapSimRepository extends JpaRepository<NhapSim, Integer> {

    @Query(
            value = """
                    select ns.id as id,
                           ns.ngayNhap as ngayNhap,
                           ns.nhaCungCap as nhaCungCap,
                           nv.hoTen as nhanVienHoTen,
                           coalesce(count(ct.id), 0) as soLuong,
                           coalesce(sum(ct.giaNhap), 0) as tongGiaNhap
                    from NhapSim ns
                      join ns.nhanVien nv
                      left join ns.chiTietNhapSims ct
                    group by ns.id, ns.ngayNhap, ns.nhaCungCap, nv.hoTen
                    """,
            countQuery = "select count(ns.id) from NhapSim ns"
    )
    Page<NhapSimSummaryView> findSummary(Pageable pageable);
}