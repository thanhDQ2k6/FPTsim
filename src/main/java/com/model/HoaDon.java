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
@Table(name = "hoadon")
public class HoaDon {
    @Id
    @Column(name = "MaHD", nullable = false, length = 20)
    private String maHD;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNV")
    private NguoiDung nhanVien;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Column(name = "TongTien", precision = 10, scale = 2)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThaiDon", nullable = false)
    private TrangThaiDon trangThaiDon = TrangThaiDon.ChoXacNhan;

    public enum TrangThaiDon {
        ChoXacNhan, DangXuLy, DaHoanThanh, DaHuy
    }

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApDungUuDai> apDungUuDais = new HashSet<>();

    @OneToOne(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private DanhGia danhGia;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HoaDonChiTiet> hoaDonChiTiets = new HashSet<>();

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LichSuGiaoDich> lichSuGiaoDichs = new HashSet<>();

    @OneToMany(mappedBy = "hoaDon")
    private Set<ThongTinChuSim> thongTinChuSims = new HashSet<>();

    // Helper methods để duy trì tính nhất quán của mối quan hệ
    public void addChiTiet(HoaDonChiTiet chiTiet) {
        hoaDonChiTiets.add(chiTiet);
        chiTiet.setHoaDon(this);
    }

    public void removeChiTiet(HoaDonChiTiet chiTiet) {
        hoaDonChiTiets.remove(chiTiet);
        chiTiet.setHoaDon(null);
    }

    public void addUuDai(ApDungUuDai uuDai) {
        apDungUuDais.add(uuDai);
        uuDai.setHoaDon(this);
    }
}