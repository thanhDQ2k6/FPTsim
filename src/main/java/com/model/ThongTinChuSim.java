package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "thongtinchusim", indexes = {
        @Index(name = "idx_thongtin_cccd", columnList = "CCCD"),
        @Index(name = "idx_thongtin_hoten", columnList = "HoTen")
})
public class ThongTinChuSim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iccid", nullable = false, unique = true)
    private Sim sim;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "CCCD", nullable = false, length = 20)
    private String cccd;

    @Column(name = "NgayCap")
    private LocalDate ngayCap;

    @Column(name = "NgaySinh", nullable = false)
    private LocalDate ngaySinh;

    @Column(name = "DiaChi", nullable = false)
    private String diaChi;

    @Column(name = "SDT", length = 20)
    private String sdt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHD")
    private HoaDon hoaDon;
}