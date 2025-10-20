package com.repository;

import com.model.Sim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimRepository extends JpaRepository<Sim, String> {
    boolean existsByMsisdn(String msisdn);
    
    // Find available SIMs for shop
    Page<Sim> findByTrangThai(Sim.TrangThai trangThai, Pageable pageable);
    
    // Find SIMs imported by specific staff
    @Query("SELECT s FROM Sim s JOIN s.chiTietNhapSims ctn JOIN ctn.nhapSim ns WHERE ns.nhanVien.email = :email")
    Page<Sim> findByImportedBy(@Param("email") String staffEmail, Pageable pageable);
    
    // Count nice phone numbers (pattern-based)
    @Query("SELECT COUNT(s) FROM Sim s WHERE s.msisdn LIKE %:pattern%")
    long countByMsisdnPattern(@Param("pattern") String pattern);
}