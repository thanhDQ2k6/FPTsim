package com.repository;

import com.model.Sim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimRepository extends JpaRepository<Sim, String>, JpaSpecificationExecutor<Sim> {
    boolean existsByMsisdn(String msisdn);
    
    // Find available SIMs for shop
    Page<Sim> findByTrangThai(Sim.TrangThai trangThai, Pageable pageable);
    
    // Find SIMs by status and provider
    Page<Sim> findByTrangThaiAndNhaMang(Sim.TrangThai trangThai, Sim.NhaMang nhaMang, Pageable pageable);
    
    // Find SIMs by status and type
    Page<Sim> findByTrangThaiAndLoaiSim(Sim.TrangThai trangThai, Sim.LoaiSim loaiSim, Pageable pageable);
    
    // Find SIMs by status, provider and type
    Page<Sim> findByTrangThaiAndNhaMangAndLoaiSim(Sim.TrangThai trangThai, Sim.NhaMang nhaMang, Sim.LoaiSim loaiSim, Pageable pageable);
    
    // Find SIMs imported by specific staff
    @Query("SELECT s FROM Sim s JOIN s.chiTietNhapSims ctn JOIN ctn.nhapSim ns WHERE ns.nhanVien.email = :email")
    Page<Sim> findByImportedBy(@Param("email") String staffEmail, Pageable pageable);
    
    // Count nice phone numbers (pattern-based)
    @Query("SELECT COUNT(s) FROM Sim s WHERE s.msisdn LIKE %:pattern%")
    long countByMsisdnPattern(@Param("pattern") String pattern);
}