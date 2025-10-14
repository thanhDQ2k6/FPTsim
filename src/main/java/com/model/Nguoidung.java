package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "nguoidung")
public class Nguoidung {
    @Id
    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "SDT", length = 20)
    private String sdt;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "DiaChi")
    private String diaChi;

    @ColumnDefault("'KhachHang'")
    @Lob
    @Column(name = "VaiTro", nullable = false)
    private String vaiTro;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "maKH")
    private Set<Apdunguudai> apdunguudais = new LinkedHashSet<>();

    @OneToMany(mappedBy = "maKH")
    private Set<Giohang> giohangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private Set<Hoadon> donHangDaMua = new LinkedHashSet<>();

    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    private Set<Hoadon> donHangDaXuLy = new LinkedHashSet<>();

    @OneToMany(mappedBy = "nguoiThucHien")
    private Set<Lichsugiaodich> lichsugiaodiches = new LinkedHashSet<>();

    @OneToMany(mappedBy = "maNV")
    private Set<Nhapsim> nhapsims = new LinkedHashSet<>();

}