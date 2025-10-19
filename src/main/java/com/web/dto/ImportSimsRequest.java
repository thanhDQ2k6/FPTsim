package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ImportSimsRequest {
    private String nhaCungCap;
    private String ghiChu;

    // Khởi tạo mặc định để tránh NPE
    private List<SimCreateRequest> sims = new ArrayList<>();
}