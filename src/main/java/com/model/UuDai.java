package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "uudai", indexes = {
        @Index(name = "idx_uudai_thoigian", columnList = "NgayBatDau, NgayHetHan"),
        @Index(name = "idx_uudai_trangthai", columnList = "TrangThai")
})
public class UuDai {
    @Id
    @Column(name = "MaUD", nullable = false, length = 20)
    private String maUD;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "GiaTriGiam", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaTriGiam;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiGiam", nullable = false)
    private LoaiGiam loaiGiam = LoaiGiam.PhanTram;

    public enum LoaiGiam {
        PhanTram, TienMat
    }

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "NgayHetHan", nullable = false)
    private LocalDate ngayHetHan;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    @OneToMany(mappedBy = "uuDai")
    private Set<ApDungUuDai> apDungUuDais = new HashSet<>();

    // Kiểm tra ưu đãi còn hiệu lực không
    public boolean isConHieuLuc() {
        LocalDate now = LocalDate.now();
        return trangThai && !now.isBefore(ngayBatDau) && !now.isAfter(ngayHetHan);
    }
}