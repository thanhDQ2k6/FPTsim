package com.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface NhapSimSummaryView {
    Integer getId();
    LocalDateTime getNgayNhap();
    String getNhaCungCap();
    String getNhanVienHoTen();
    Long getSoLuong();
    BigDecimal getTongGiaNhap();
}