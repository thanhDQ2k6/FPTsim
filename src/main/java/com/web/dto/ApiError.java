package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String code;    // Mã lỗi ngắn gọn (BUSINESS, NOT_FOUND, DUPLICATE, FK_VIOLATION, ...)
    private String message; // Nội dung lỗi (dành cho dev/QA)
    private String source;  // Nguồn lỗi: request path (vd: /orders/HD1/status)
}