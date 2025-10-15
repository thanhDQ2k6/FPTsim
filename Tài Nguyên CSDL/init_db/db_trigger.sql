-- ===== TRIGGERS CHO QUẢN LÝ GIỎ HÀNG =====

-- Trigger tự động chuyển SIM từ giỏ hàng sang hóa đơn khi tạo đơn hàng
DELIMITER //
CREATE TRIGGER after_create_order_from_cart
    AFTER INSERT ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Nếu là đơn hàng mới tạo từ giỏ hàng
    IF NEW.MaKH IS NOT NULL THEN
        -- Chuyển tất cả SIM từ giỏ hàng sang hóa đơn chi tiết
        INSERT INTO `HoaDonChiTiet` (MaHD, iccid, GiaBan, GiaCuoi)
        SELECT NEW.MaHD, g.iccid, s.GiaBan, s.GiaBan
        FROM `GioHang` g
                 JOIN `SIM` s ON g.iccid = s.iccid
        WHERE g.MaKH = NEW.MaKH AND s.TrangThai = 'SanSang';

        -- Xóa các SIM đã chuyển khỏi giỏ hàng
        DELETE FROM `GioHang`
        WHERE MaKH = NEW.MaKH AND iccid IN (
            SELECT iccid FROM `HoaDonChiTiet` WHERE MaHD = NEW.MaHD
        );
    END IF;
END //
DELIMITER ;

-- ===== TRIGGERS CHO QUẢN LÝ SIM =====

-- Trigger kiểm tra khi thêm SIM vào giỏ hàng
DELIMITER //
CREATE TRIGGER before_insert_giohang
    BEFORE INSERT ON `GioHang`
    FOR EACH ROW
BEGIN
    DECLARE sim_status VARCHAR(20);

    -- Kiểm tra trạng thái SIM
    SELECT TrangThai INTO sim_status FROM `SIM` WHERE iccid = NEW.iccid;

    -- Nếu SIM không sẵn sàng, không cho thêm vào giỏ hàng
    IF sim_status != 'SanSang' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'SIM này không còn sẵn sàng để bán';
    END IF;
END //
DELIMITER ;

-- ===== TRIGGERS CHO QUẢN LÝ HÓA ĐƠN =====

-- Trigger cập nhật trạng thái đơn hàng
DELIMITER //
CREATE TRIGGER before_update_hoadon_check
    BEFORE UPDATE ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Kiểm tra chuyển trạng thái đơn hàng hợp lệ
    IF (OLD.TrangThaiDon = 'DaHoanThanh' AND NEW.TrangThaiDon != 'DaHoanThanh') OR
       (OLD.TrangThaiDon = 'DaHuy' AND NEW.TrangThaiDon != 'DaHuy') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Không thể thay đổi trạng thái của đơn hàng đã hoàn thành hoặc đã hủy';
    END IF;

    -- Đảm bảo đơn hàng phải có nhân viên xử lý khi chuyển sang trạng thái tiếp theo
    IF (OLD.TrangThaiDon = 'ChoXacNhan' AND
        NEW.TrangThaiDon IN ('DangXuLy', 'DaHoanThanh') AND
        NEW.MaNV IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cần có nhân viên xử lý khi chuyển trạng thái đơn hàng';
    END IF;
END //
DELIMITER ;

-- ===== TRIGGERS CHO QUẢN LÝ ƯU ĐÃI =====

-- Trigger tự động cập nhật trạng thái ưu đãi dựa trên ngày hiệu lực
DELIMITER //
CREATE EVENT update_uudai_status
    ON SCHEDULE EVERY 1 DAY
        STARTS CURRENT_TIMESTAMP
    DO
    BEGIN
        -- Tự động cập nhật trạng thái ưu đãi dựa trên thời gian
        UPDATE `UuDai`
        SET `TrangThai` = FALSE
        WHERE `NgayHetHan` < CURDATE() AND `TrangThai` = TRUE;
    END //
DELIMITER ;

-- ===== TRIGGERS CHO THỐNG KÊ BÁO CÁO =====

-- Trigger tự động cập nhật thống kê bán hàng theo ngày
DELIMITER //
CREATE TRIGGER after_complete_order
    AFTER UPDATE ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Khi đơn hàng chuyển sang trạng thái hoàn thành
    IF NEW.TrangThaiDon = 'DaHoanThanh' AND OLD.TrangThaiDon != 'DaHoanThanh' THEN
        -- Tạo hoặc cập nhật bản ghi thống kê theo ngày
        INSERT INTO `ThongKeBanHang` (Ngay, SoDonHang, DoanhThu)
        VALUES (CURRENT_DATE(), 1, NEW.TongTien)
        ON DUPLICATE KEY UPDATE
                             SoDonHang = SoDonHang + 1,
                             DoanhThu = DoanhThu + NEW.TongTien;
    END IF;
END //
DELIMITER ;