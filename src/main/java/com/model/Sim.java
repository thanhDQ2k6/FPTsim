package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "sim")
public class Sim {
    @Id
    @Column(name = "iccid", nullable = false)
    private String iccid;

    @Column(name = "msisdn", length = 20)
    private String msisdn;

    @Lob
    @Column(name = "NhaMang", nullable = false)
    private String nhaMang;

    @Column(name = "GiaBan", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaBan;

    @Lob
    @Column(name = "LoaiSim", nullable = false)
    private String loaiSim;

    @ColumnDefault("'SanSang'")
    @Lob
    @Column(name = "TrangThai", nullable = false)
    private String trangThai;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "iccid")
    private Set<Chitietnhapsim> chitietnhapsims = new LinkedHashSet<>();

    @OneToMany(mappedBy = "iccid")
    private Set<Giohang> giohangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "iccid")
    private Set<Hoadonchitiet> hoadonchitiets = new LinkedHashSet<>();

    @OneToOne(mappedBy = "iccid")
    private Thongtinchusim thongtinchusim;

}