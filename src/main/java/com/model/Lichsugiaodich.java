package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lichsugiaodich")
public class Lichsugiaodich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiaoDich", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaHD", nullable = false)
    private Hoadon maHD;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ThoiGian")
    private Instant thoiGian;

    @Lob
    @Column(name = "HanhDong", nullable = false)
    private String hanhDong;

    @Lob
    @Column(name = "GhiChu")
    private String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiThucHien")
    private Nguoidung nguoiThucHien;

}