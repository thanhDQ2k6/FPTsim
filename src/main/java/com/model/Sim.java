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
@Table(name = "sim", indexes = {
        @Index(name = "idx_sim_msisdn", columnList = "msisdn"),
        @Index(name = "idx_sim_nhamang", columnList = "NhaMang"),
        @Index(name = "idx_sim_trangthai", columnList = "TrangThai")
})
public class Sim {
    @Id
    @Column(name = "iccid", nullable = false)
    private String iccid;

    @Column(name = "msisdn", length = 20, unique = true)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(name = "NhaMang", nullable = false)
    private NhaMang nhaMang;

    public enum NhaMang {
        Viettel, Mobiphone, Vinaphone
    }

    @Column(name = "GiaBan", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaBan;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiSim", nullable = false)
    private LoaiSim loaiSim;

    public enum LoaiSim {
        NgoaiDia, TraTruoc, TraSau
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private TrangThai trangThai = TrangThai.SanSang;

    public enum TrangThai {
        SanSang, DaBan, HoatDong, Chet
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "sim")
    private Set<ChiTietNhapSim> chiTietNhapSims = new HashSet<>();

    @OneToMany(mappedBy = "sim")
    private Set<GioHang> gioHangs = new HashSet<>();

    @OneToMany(mappedBy = "sim")
    private Set<HoaDonChiTiet> hoaDonChiTiets = new HashSet<>();

    @OneToOne(mappedBy = "sim", cascade = CascadeType.ALL, orphanRemoval = true)
    private ThongTinChuSim thongTinChuSim;

    // Phương thức để lấy giá nhập mới nhất
    public BigDecimal getGiaNhapMoiNhat() {
        return chiTietNhapSims.stream()
                .sorted((a, b) -> b.getNhapSim().getNgayNhap().compareTo(a.getNhapSim().getNgayNhap()))
                .map(ChiTietNhapSim::getGiaNhap)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}