package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "danhgia")
public class Danhgia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "MaHD", nullable = false)
    private Hoadon maHD;

    @Column(name = "Sao", nullable = false)
    private Integer sao;

    @Lob
    @Column(name = "NoiDung")
    private String noiDung;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NgayDanhGia")
    private Instant ngayDanhGia;

}