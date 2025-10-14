package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "apdunguudai")
public class Apdunguudai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaApDung", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false)
    private Nguoidung maKH;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaHD", nullable = false)
    private Hoadon maHD;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaUD", nullable = false)
    private Uudai maUD;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NgayApDung")
    private Instant ngayApDung;

    @Column(name = "GiaTriApDung", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaTriApDung;

}