package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "nhapsim")
public class Nhapsim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhap", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaNV", nullable = false)
    private Nguoidung maNV;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NgayNhap")
    private Instant ngayNhap;

    @Column(name = "NhaCungCap", nullable = false)
    private String nhaCungCap;

    @Lob
    @Column(name = "GhiChu")
    private String ghiChu;

    @ColumnDefault("0")
    @Column(name = "TongSoLuong")
    private Integer tongSoLuong;

    @ColumnDefault("0.00")
    @Column(name = "TongGiaNhap", precision = 10, scale = 2)
    private BigDecimal tongGiaNhap;

    @OneToMany(mappedBy = "maNhap")
    private Set<Chitietnhapsim> chitietnhapsims = new LinkedHashSet<>();

}