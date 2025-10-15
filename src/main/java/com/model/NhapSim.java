package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "nhapsim")
public class NhapSim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhap", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaNV", nullable = false)
    private NguoiDung nhanVien;

    @Column(name = "NgayNhap")
    private LocalDateTime ngayNhap = LocalDateTime.now();

    @Column(name = "NhaCungCap", nullable = false)
    private String nhaCungCap;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "TongSoLuong")
    private Integer tongSoLuong = 0;

    @Column(name = "TongGiaNhap", precision = 10, scale = 2)
    private BigDecimal tongGiaNhap = BigDecimal.ZERO;

    @OneToMany(mappedBy = "nhapSim", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChiTietNhapSim> chiTietNhapSims = new HashSet<>();

    // Helper methods
    public void addChiTietNhap(Sim sim, BigDecimal giaNhap) {
        ChiTietNhapSim chiTiet = new ChiTietNhapSim();
        chiTiet.setNhapSim(this);
        chiTiet.setSim(sim);
        chiTiet.setGiaNhap(giaNhap);
        chiTietNhapSims.add(chiTiet);

        // Tự động cập nhật tổng
        this.tongSoLuong++;
        this.tongGiaNhap = this.tongGiaNhap.add(giaNhap);
    }
}