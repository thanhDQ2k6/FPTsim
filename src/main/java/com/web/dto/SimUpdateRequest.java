package com.web.dto;

import com.model.Sim;

import java.math.BigDecimal;

// cho phép khi SanSang
public class SimUpdateRequest {
    public String msisdn;
    public Sim.LoaiSim loaiSim;
    public BigDecimal giaBan;
}