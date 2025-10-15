package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "thongkebanhang", indexes = {
        @Index(name = "idx_thongke_doanhthu", columnList = "DoanhThu DESC")
})
public class ThongKeBanHang {
    @Id
    @Column(name = "Ngay", nullable = false)
    private LocalDate ngay;

    @Column(name = "SoDonHang")
    private Integer soDonHang = 0;

    @Column(name = "DoanhThu", precision = 15, scale = 2)
    private BigDecimal doanhThu = BigDecimal.ZERO;

    // Constructor mặc định
    public ThongKeBanHang() {
    }

    // Constructor với tham số
    public ThongKeBanHang(LocalDate ngay) {
        this.ngay = ngay;
        this.soDonHang = 0;
    }

    // Phương thức helper để tăng số đơn hàng
    public void tangSoDonHang() {
        this.soDonHang = this.soDonHang == null ? 1 : this.soDonHang + 1;
    }

    // Phương thức helper để tăng doanh thu
    public void tangDoanhThu(BigDecimal doanhThuThem) {
        if (doanhThuThem == null) return;

        if (this.doanhThu == null) {
            this.doanhThu = doanhThuThem;
        } else {
            this.doanhThu = this.doanhThu.add(doanhThuThem);
        }
    }
}