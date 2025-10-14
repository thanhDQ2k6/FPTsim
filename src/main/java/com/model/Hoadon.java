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
@Table(name = "hoadon")
public class Hoadon {
    @Id
    @Column(name = "MaHD", nullable = false, length = 20)
    private String maHD;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false)
    private Nguoidung maKH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNV")
    private Nguoidung maNV;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NgayTao")
    private Instant ngayTao;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NgayCapNhat")
    private Instant ngayCapNhat;

    @ColumnDefault("0.00")
    @Column(name = "TongTien", precision = 10, scale = 2)
    private BigDecimal tongTien;

    @ColumnDefault("'ChoXacNhan'")
    @Lob
    @Column(name = "TrangThaiDon", nullable = false)
    private String trangThaiDon;

    @Lob
    @Column(name = "GhiChu")
    private String ghiChu;

    @OneToMany(mappedBy = "maHD")
    private Set<Apdunguudai> apdunguudais = new LinkedHashSet<>();

    @OneToOne(mappedBy = "maHD")
    private Danhgia danhgia;

    @OneToMany(mappedBy = "maHD")
    private Set<Hoadonchitiet> hoadonchitiets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "maHD")
    private Set<Lichsugiaodich> lichsugiaodiches = new LinkedHashSet<>();

    @OneToMany(mappedBy = "maHD")
    private Set<Thongtinchusim> thongtinchusims = new LinkedHashSet<>();

    // Quan hệ với khách hàng
    @ManyToOne
    @JoinColumn(name = "MaKH", referencedColumnName = "Email")
    private com.model.Nguoidung khachHang;

    // Quan hệ với nhân viên
    @ManyToOne
    @JoinColumn(name = "MaNV", referencedColumnName = "Email")
    private com.model.Nguoidung nhanVien;
}