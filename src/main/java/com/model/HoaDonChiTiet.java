package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "hoadonchitiet",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MaHD", "iccid"}))
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaHD", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iccid", nullable = false)
    private Sim sim;

    @Column(name = "GiaBan", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "GiaCuoi", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaCuoi;
}