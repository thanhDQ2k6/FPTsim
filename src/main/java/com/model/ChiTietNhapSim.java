package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "chitietnhapsim")
public class ChiTietNhapSim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaNhap", nullable = false)
    private NhapSim nhapSim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iccid", nullable = false)
    private Sim sim;

    @Column(name = "GiaNhap", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaNhap;
}