package com.web.dto;

public class ApiError {
    public String code;    // Mã lỗi ngắn gọn (BUSINESS, NOT_FOUND, DUPLICATE, FK_VIOLATION, ...)
    public String message; // Nội dung lỗi (dành cho dev/QA)
    public String source;  // Nguồn lỗi: request path (vd: /orders/HD1/status)

    public ApiError() {
    }

    public ApiError(String code, String message, String source) {
        this.code = code;
        this.message = message;
        this.source = source;
    }
}