package com.web.dto;

import com.model.Sim;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimCreateRequest {
    private String iccid;
    private String msisdn;
    private Sim.NhaMang nhaMang;
    private Sim.LoaiSim loaiSim;
    private BigDecimal giaBan;
    private BigDecimal giaNhap; // bắt buộc khi nhập
}