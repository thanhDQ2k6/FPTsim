-- Use the correct database
USE fptsim;

-- 1) Drop all existing triggers (safe if they don't exist)
DROP TRIGGER IF EXISTS after_create_order_from_cart;
DROP TRIGGER IF EXISTS before_insert_giohang;
DROP TRIGGER IF EXISTS before_update_hoadon_check;
DROP TRIGGER IF EXISTS after_complete_order;

-- 2) Drop scheduled event (if exists)
DROP EVENT IF EXISTS update_uudai_status;

-- 3) Re-create triggers and event
DELIMITER //

-- ===== TRIGGERS CHO QUẢN LÝ GIỎ HÀNG =====
CREATE TRIGGER after_create_order_from_cart
    AFTER INSERT
    ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Nếu là đơn hàng mới tạo từ giỏ hàng
    IF NEW.MaKH IS NOT NULL THEN
        -- Chuyển tất cả SIM từ giỏ hàng sang hóa đơn chi tiết
        INSERT INTO `HoaDonChiTiet` (MaHD, iccid, GiaBan, GiaCuoi)
        SELECT NEW.MaHD, g.iccid, s.GiaBan, s.GiaBan
        FROM `GioHang` g
                 JOIN `SIM` s ON g.iccid = s.iccid
        WHERE g.MaKH = NEW.MaKH
          AND s.TrangThai = 'SanSang';

        -- Xóa các SIM đã chuyển khỏi giỏ hàng
        DELETE
        FROM `GioHang`
        WHERE MaKH = NEW.MaKH
          AND iccid IN (SELECT iccid FROM `HoaDonChiTiet` WHERE MaHD = NEW.MaHD);
    END IF;
END //
-- ===== TRIGGERS CHO QUẢN LÝ SIM =====
CREATE TRIGGER before_insert_giohang
    BEFORE INSERT
    ON `GioHang`
    FOR EACH ROW
BEGIN
    DECLARE sim_status VARCHAR(20);

    -- Kiểm tra trạng thái SIM
    SELECT TrangThai INTO sim_status FROM `SIM` WHERE iccid = NEW.iccid;

    -- Nếu SIM không sẵn sàng, không cho thêm vào giỏ hàng
    IF sim_status IS NULL OR sim_status != 'SanSang' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ERR_SIM_NOT_AVAILABLE: SIM này không còn sẵn sàng để bán',
                MYSQL_ERRNO = 10001;
    END IF;
END //
-- ===== TRIGGERS CHO QUẢN LÝ HÓA ĐƠN =====
CREATE TRIGGER before_update_hoadon_check
    BEFORE UPDATE
    ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Không cho chỉnh sửa đơn đã hoàn tất/hủy
    IF (OLD.TrangThaiDon = 'DaHoanThanh' AND NEW.TrangThaiDon != 'DaHoanThanh')
        OR (OLD.TrangThaiDon = 'DaHuy' AND NEW.TrangThaiDon != 'DaHuy') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT =
                    'ERR_ORDER_STATE_IMMUTABLE: Không thể thay đổi trạng thái của đơn hàng đã hoàn thành hoặc đã hủy',
                MYSQL_ERRNO = 10002;
    END IF;

    -- Bắt buộc có nhân viên khi chuyển sang xử lý/hoàn thành
    IF (OLD.TrangThaiDon = 'ChoXacNhan'
        AND NEW.TrangThaiDon IN ('DangXuLy', 'DaHoanThanh')
        AND NEW.MaNV IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ERR_ORDER_NEED_STAFF: Cần có nhân viên xử lý khi chuyển trạng thái đơn hàng',
                MYSQL_ERRNO = 10003;
    END IF;
END //
-- ===== TRIGGERS CHO THỐNG KÊ BÁO CÁO =====
CREATE TRIGGER after_complete_order
    AFTER UPDATE
    ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Khi đơn hàng chuyển sang trạng thái hoàn thành
    IF NEW.TrangThaiDon = 'DaHoanThanh' AND OLD.TrangThaiDon != 'DaHoanThanh' THEN
        -- Tạo hoặc cập nhật bản ghi thống kê theo ngày
        INSERT INTO `ThongKeBanHang` (Ngay, SoDonHang, DoanhThu)
        VALUES (CURRENT_DATE(), 1, NEW.TongTien)
        ON DUPLICATE KEY UPDATE SoDonHang = SoDonHang + 1,
                                DoanhThu  = DoanhThu + NEW.TongTien;
    END IF;
END //
-- ===== EVENT CẬP NHẬT TRẠNG THÁI ƯU ĐÃI =====
CREATE EVENT update_uudai_status
    ON SCHEDULE EVERY 1 DAY
        STARTS CURRENT_TIMESTAMP
    DO
    BEGIN
        UPDATE `UuDai`
        SET `TrangThai` = FALSE
        WHERE `NgayHetHan` < CURDATE()
          AND `TrangThai` = TRUE;
    END //
DELIMITER ;

-- Lưu ý: cần bật event scheduler nếu chưa bật
-- SET GLOBAL event_scheduler = ON;