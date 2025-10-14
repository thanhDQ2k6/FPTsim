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
@Table(name = "hoadonchitiet")
public class Hoadonchitiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "MaHD", nullable = false)
    private Hoadon maHD;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iccid", nullable = false)
    private Sim iccid;

    @Column(name = "GiaBan", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "GiaCuoi", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaCuoi;

}