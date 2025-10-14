package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "chitietnhapsim")
public class Chitietnhapsim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "MaNhap", nullable = false)
    private Nhapsim maNhap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iccid", nullable = false)
    private Sim iccid;

    @Column(name = "GiaNhap", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaNhap;

}