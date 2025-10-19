package com.web.dto;

import com.model.UuDai;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UuDaiCreateRequest {
    private String maUD;
    private String moTa;
    private UuDai.LoaiGiam loaiGiam;
    private BigDecimal giaTriGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayHetHan;
}
