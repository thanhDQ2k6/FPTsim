package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "lichsugiaodich")
public class LichSuGiaoDich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiaoDich", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaHD", nullable = false)
    private HoaDon hoaDon;

    @Column(name = "ThoiGian")
    private LocalDateTime thoiGian = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "HanhDong", nullable = false)
    private HanhDong hanhDong;

    public enum HanhDong {
        TaoDon, ThanhToan, HuyDon, HoanThanh
    }

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiThucHien")
    private NguoiDung nguoiThucHien;
}