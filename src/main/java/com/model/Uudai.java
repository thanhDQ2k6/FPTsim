package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "uudai")
public class Uudai {
    @Id
    @Column(name = "MaUD", nullable = false, length = 20)
    private String maUD;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "GiaTriGiam", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaTriGiam;

    @ColumnDefault("'PhanTram'")
    @Lob
    @Column(name = "LoaiGiam", nullable = false)
    private String loaiGiam;

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "NgayHetHan", nullable = false)
    private LocalDate ngayHetHan;

    @ColumnDefault("1")
    @Column(name = "TrangThai")
    private Boolean trangThai;

    @OneToMany(mappedBy = "maUD")
    private Set<Apdunguudai> apdunguudais = new LinkedHashSet<>();

}