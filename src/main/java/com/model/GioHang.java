package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "giohang",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MaKH", "iccid"}))
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iccid", nullable = false)
    private Sim sim;

    @Column(name = "NgayThem")
    private LocalDateTime ngayThem = LocalDateTime.now();
}