-- Tạo cơ sở dữ liệu nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS fptsim;
USE fptsim;

-- Bảng người dùng (gộp User và ThongTinCaNhan)
CREATE TABLE IF NOT EXISTS `nguoidung`
(
    `Email`      VARCHAR(255)                            NOT NULL,
    `Password`   VARCHAR(255)                            NOT NULL,
    `HoTen`      VARCHAR(255)                            NOT NULL,
    `SDT`        VARCHAR(20),
    `NgaySinh`   DATE,
    `DiaChi`     VARCHAR(255),
    `VaiTro`     ENUM ('KhachHang', 'NhanVien', 'Admin') NOT NULL DEFAULT 'KhachHang',
    `created_at` DATETIME                                         DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`Email`),
    INDEX `idx_nguoidung_hoten` (`HoTen`),  -- Tìm kiếm theo tên
    INDEX `idx_nguoidung_sdt` (`SDT`),      -- Tìm kiếm theo số điện thoại
    INDEX `idx_nguoidung_vaitro` (`VaiTro`) -- Lọc theo vai trò
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng SIM
CREATE TABLE IF NOT EXISTS `sim`
(
    `iccid`      VARCHAR(255)                               NOT NULL,
    `msisdn`     VARCHAR(20) UNIQUE,         -- Số điện thoại, unique để tránh trùng lặp
    `NhaMang`    ENUM ('Viettel', 'Mobiphone', 'Vinaphone') NOT NULL,
    `GiaBan`     DECIMAL(10, 2)                             NOT NULL,
    `LoaiSim`    ENUM ('NgoaiDia', 'TraTruoc', 'TraSau')    NOT NULL,
    `TrangThai`  ENUM ('SanSang', 'DaBan', 'HoatDong', 'Chet')      NOT NULL DEFAULT 'SanSang',
    `created_at` DATETIME                                            DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME                                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`iccid`),
    INDEX `idx_sim_msisdn` (`msisdn`),       -- Tìm kiếm theo số điện thoại
    INDEX `idx_sim_nhamang` (`NhaMang`),     -- Lọc theo nhà mạng
    INDEX `idx_sim_trangthai` (`TrangThai`), -- Lọc theo trạng thái
    CHECK (GiaBan > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng nhập SIM
CREATE TABLE IF NOT EXISTS `nhapsim`
(
    `MaNhap`      INT AUTO_INCREMENT,
    `MaNV`        VARCHAR(255) NOT NULL,       -- Email của nhân viên nhập SIM
    `NgayNhap`    DATETIME       DEFAULT CURRENT_TIMESTAMP,
    `NhaCungCap`  VARCHAR(255) NOT NULL,
    `GhiChu`      TEXT,
    `TongSoLuong` INT            DEFAULT 0,    -- Tự động cập nhật bởi trigger
    `TongGiaNhap` DECIMAL(10, 2) DEFAULT 0,    -- Tự động cập nhật bởi trigger
    PRIMARY KEY (`MaNhap`),
    INDEX `idx_nhapsim_ngaynhap` (`NgayNhap`), -- Tìm kiếm theo ngày nhập
    FOREIGN KEY (`MaNV`) REFERENCES `nguoidung` (`Email`) ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Chi tiết nhập SIM
CREATE TABLE IF NOT EXISTS `chitietnhapsim`
(
    `ID`      INT AUTO_INCREMENT,
    `MaNhap`  INT            NOT NULL,
    `iccid`   VARCHAR(255)   NOT NULL,
    `GiaNhap` DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (`ID`),
    UNIQUE KEY (`MaNhap`, `iccid`), -- Mỗi SIM chỉ nhập một lần trong mỗi đợt
    INDEX `idx_chitietnhap_iccid` (`iccid`),
    FOREIGN KEY (`MaNhap`) REFERENCES `nhapsim` (`MaNhap`) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (`iccid`) REFERENCES `sim` (`iccid`) ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng giỏ hàng
CREATE TABLE IF NOT EXISTS `giohang`
(
    `ID`       INT AUTO_INCREMENT,
    `MaKH`     VARCHAR(255) NOT NULL, -- Email của khách hàng
    `iccid`    VARCHAR(255) NOT NULL,
    `NgayThem` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`ID`),
    UNIQUE KEY (`MaKH`, `iccid`),     -- Mỗi SIM chỉ được thêm một lần vào giỏ hàng của mỗi KH
    INDEX `idx_giohang_makh` (`MaKH`),
    FOREIGN KEY (`MaKH`) REFERENCES `nguoidung` (`Email`) ON DELETE CASCADE,
    FOREIGN KEY (`iccid`) REFERENCES `sim` (`iccid`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng hóa đơn
CREATE TABLE IF NOT EXISTS `hoadon`
(
    `MaHD`         VARCHAR(20)                                             NOT NULL,           -- Mã hóa đơn có thể tạo theo format riêng
    `MaKH`         VARCHAR(255)                                            NOT NULL,
    `MaNV`         VARCHAR(255),                                                               -- Có thể NULL khi đơn hàng chưa được xử lý bởi nhân viên
    `NgayTao`      DATETIME                                                         DEFAULT CURRENT_TIMESTAMP,
    `NgayCapNhat`  DATETIME                                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `TongTien`     DECIMAL(10, 2)                                                   DEFAULT 0, -- Tự động cập nhật bởi trigger
    `TrangThaiDon` ENUM ('ChoXacNhan', 'DangXuLy', 'DaHoanThanh', 'DaHuy') NOT NULL DEFAULT 'ChoXacNhan',
    `GhiChu`       TEXT,
    PRIMARY KEY (`MaHD`),
    INDEX `idx_hoadon_makh` (`MaKH`),
    INDEX `idx_hoadon_manv` (`MaNV`),
    INDEX `idx_hoadon_ngaytao` (`NgayTao`),
    INDEX `idx_hoadon_trangthai` (`TrangThaiDon`),
    FOREIGN KEY (`MaKH`) REFERENCES `nguoidung` (`Email`) ON UPDATE CASCADE,
    FOREIGN KEY (`MaNV`) REFERENCES `nguoidung` (`Email`) ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Chi tiết hóa đơn
CREATE TABLE IF NOT EXISTS `hoadonchitiet`
(
    `ID`      INT AUTO_INCREMENT,
    `MaHD`    VARCHAR(20)    NOT NULL,
    `iccid`   VARCHAR(255)   NOT NULL,
    `GiaBan`  DECIMAL(10, 2) NOT NULL, -- Giá bán tại thời điểm lập hóa đơn
    `GiaCuoi` DECIMAL(10, 2) NOT NULL, -- Giá sau khi áp dụng khuyến mãi
    PRIMARY KEY (`ID`),
    UNIQUE KEY (`MaHD`, `iccid`),      -- Mỗi SIM chỉ xuất hiện một lần trong hóa đơn
    INDEX `idx_hdct_iccid` (`iccid`),
    FOREIGN KEY (`MaHD`) REFERENCES `hoadon` (`MaHD`) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (`iccid`) REFERENCES `sim` (`iccid`) ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Thông tin chủ SIM
CREATE TABLE IF NOT EXISTS `thongtinchusim`
(
    `ID`       INT AUTO_INCREMENT,
    `iccid`    VARCHAR(255) NOT NULL,
    `HoTen`    VARCHAR(255) NOT NULL,
    `CCCD`     VARCHAR(20)  NOT NULL,
    `NgaySinh` DATE         NOT NULL,
    `DiaChi`   VARCHAR(255) NOT NULL,
    `SDT`      VARCHAR(20),
    `MaHD`     VARCHAR(20),             -- Mã hóa đơn mua SIM
    PRIMARY KEY (`ID`),
    UNIQUE KEY (`iccid`),               -- Mỗi SIM chỉ có một chủ sở hữu
    INDEX `idx_chusim_cccd` (`CCCD`),   -- Tìm kiếm theo CCCD
    INDEX `idx_chusim_hoten` (`HoTen`), -- Tìm kiếm theo tên chủ
    FOREIGN KEY (`iccid`) REFERENCES `sim` (`iccid`) ON UPDATE CASCADE,
    FOREIGN KEY (`MaHD`) REFERENCES `hoadon` (`MaHD`) ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng ưu đãi
CREATE TABLE IF NOT EXISTS `uudai`
(
    `MaUD`       VARCHAR(20)                  NOT NULL,
    `MoTa`       VARCHAR(255),
    `GiaTriGiam` DECIMAL(10, 2)               NOT NULL,
    `LoaiGiam`   ENUM ('PhanTram', 'TienMat') NOT NULL DEFAULT 'PhanTram',
    `NgayBatDau` DATE                         NOT NULL,
    `NgayHetHan` DATE                         NOT NULL,
    `TrangThai`  BOOLEAN                               DEFAULT TRUE, -- TRUE: còn hiệu lực, FALSE: hết hiệu lực
    PRIMARY KEY (`MaUD`),
    INDEX `idx_uudai_thoigian` (`NgayBatDau`, `NgayHetHan`),
    INDEX `idx_uudai_trangthai` (`TrangThai`),
    CHECK (NgayHetHan >= NgayBatDau),
    CHECK (GiaTriGiam > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng áp dụng ưu đãi
CREATE TABLE IF NOT EXISTS `ApDungUuDai`
(
    `MaApDung`     INT AUTO_INCREMENT,
    `MaKH`         VARCHAR(255)   NOT NULL,
    `MaHD`         VARCHAR(20)    NOT NULL,
    `MaUD`         VARCHAR(20)    NOT NULL,
    `NgayApDung`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `GiaTriApDung` DECIMAL(10, 2) NOT NULL, -- Giá trị thực tế được giảm
    PRIMARY KEY (`MaApDung`),
    INDEX `idx_apdung_mahd` (`MaHD`),
    FOREIGN KEY (`MaKH`) REFERENCES `nguoidung` (`Email`) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (`MaHD`) REFERENCES `hoadon` (`MaHD`) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (`MaUD`) REFERENCES `uudai` (`MaUD`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng đánh giá
CREATE TABLE IF NOT EXISTS `danhgia`
(
    `ID`          INT AUTO_INCREMENT,
    `MaHD`        VARCHAR(20) NOT NULL,
    `Sao`         INT         NOT NULL,
    `NoiDung`     TEXT,
    `NgayDanhGia` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`ID`),
    UNIQUE KEY (`MaHD`),             -- Mỗi hóa đơn chỉ được đánh giá một lần
    INDEX `idx_danhgia_sao` (`Sao`), -- Tìm kiếm theo số sao
    FOREIGN KEY (`MaHD`) REFERENCES `hoadon` (`MaHD`) ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (Sao BETWEEN 1 AND 5)      -- Đảm bảo số sao từ 1-5
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng lịch sử giao dịch
CREATE TABLE IF NOT EXISTS `lichsugiaodich`
(
    `MaGiaoDich`    INT AUTO_INCREMENT,
    `MaHD`          VARCHAR(20)                                         NOT NULL,
    `ThoiGian`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    `HanhDong`      ENUM ('TaoDon', 'ThanhToan', 'HuyDon', 'HoanThanh') NOT NULL,
    `GhiChu`        TEXT,
    `NguoiThucHien` VARCHAR(255), -- Email của người thực hiện
    PRIMARY KEY (`MaGiaoDich`),
    INDEX `idx_lichsu_mahd` (`MaHD`),
    INDEX `idx_lichsu_thoigian` (`ThoiGian`),
    INDEX `idx_lichsu_hanhdong` (`HanhDong`),
    FOREIGN KEY (`MaHD`) REFERENCES `hoadon` (`MaHD`) ON UPDATE CASCADE,
    FOREIGN KEY (`NguoiThucHien`) REFERENCES `nguoidung` (`Email`) ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Bảng lưu trữ thống kê bán hàng theo ngày (hỗ trợ báo cáo nhanh)
CREATE TABLE IF NOT EXISTS `thongkebanhang`
(
    `Ngay`      DATE NOT NULL,
    `SoDonHang` INT            DEFAULT 0,
    `DoanhThu`  DECIMAL(15, 2) DEFAULT 0,
    PRIMARY KEY (`Ngay`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;