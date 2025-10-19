package com.web.dto;

import com.model.Sim;

import java.math.BigDecimal;

public class SimCreateRequest {
    public String iccid;
    public String msisdn;
    public Sim.NhaMang nhaMang;
    public Sim.LoaiSim loaiSim;
    public BigDecimal giaBan;
    public BigDecimal giaNhap; // bắt buộc khi nhập
}