package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "apdunguudai")
public class ApDungUuDai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaApDung", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaHD", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaUD", nullable = false)
    private UuDai uuDai;

    @Column(name = "NgayApDung")
    private LocalDateTime ngayApDung = LocalDateTime.now();

    @Column(name = "GiaTriApDung", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaTriApDung;
}