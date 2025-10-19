package com.web.dto;

import com.model.Sim;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// cho phép khi SanSang
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimUpdateRequest {
    private String msisdn;
    private Sim.LoaiSim loaiSim;
    private BigDecimal giaBan;
}