package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "nguoidung")
public class NguoiDung implements Serializable {
    private static final long serialVersionUID = 1L;
    
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

    @Enumerated(EnumType.STRING)
    @Column(name = "VaiTro", nullable = false)
    private VaiTro vaiTro = VaiTro.KhachHang;

    public enum VaiTro {
        KhachHang, NhanVien, Admin
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "khachHang")
    private Set<ApDungUuDai> apDungUuDais = new HashSet<>();

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GioHang> gioHangs = new HashSet<>();

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private Set<HoaDon> donHangDaMua = new HashSet<>();

    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    private Set<HoaDon> donHangDaXuLy = new HashSet<>();

    @OneToMany(mappedBy = "nguoiThucHien")
    private Set<LichSuGiaoDich> lichSuGiaoDichs = new HashSet<>();

    @OneToMany(mappedBy = "nhanVien")
    private Set<NhapSim> nhapSims = new HashSet<>();

    // Helper methods
    public void addToCart(Sim sim) {
        GioHang gioHang = new GioHang();
        gioHang.setKhachHang(this);
        gioHang.setSim(sim);
        gioHangs.add(gioHang);
    }

    public void removeFromCart(Sim sim) {
        gioHangs.removeIf(item -> item.getSim().equals(sim));
    }
}