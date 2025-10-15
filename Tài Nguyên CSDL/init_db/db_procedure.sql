-- ===== PROCEDURES CHO QUẢN LÝ GIỎ HÀNG =====

-- Procedure thêm SIM vào giỏ hàng
DELIMITER //
CREATE PROCEDURE AddToCart(
    IN p_makh VARCHAR(255), -- Email khách hàng
    IN p_iccid VARCHAR(255), -- ICCID của SIM
    OUT p_success BOOLEAN, -- Kết quả thực hiện
    OUT p_message VARCHAR(255) -- Thông báo
)
BEGIN
    DECLARE sim_status VARCHAR(20);

    -- Xử lý lỗi SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            SET p_success = FALSE;
            SET p_message = 'Có lỗi xảy ra khi thêm vào giỏ hàng';
            ROLLBACK;
        END;

    START TRANSACTION;

    -- Kiểm tra trạng thái SIM
    SELECT TrangThai INTO sim_status FROM `SIM` WHERE iccid = p_iccid;

    IF sim_status IS NULL THEN
        SET p_success = FALSE;
        SET p_message = 'SIM không tồn tại';
        ROLLBACK;
    ELSEIF sim_status != 'SanSang' THEN
        SET p_success = FALSE;
        SET p_message = 'SIM này không còn sẵn sàng để bán';
        ROLLBACK;
    ELSE
        -- Thêm vào giỏ hàng
        INSERT INTO `GioHang` (MaKH, iccid)
        VALUES (p_makh, p_iccid)
        ON DUPLICATE KEY UPDATE NgayThem = CURRENT_TIMESTAMP;

        SET p_success = TRUE;
        SET p_message = 'Đã thêm SIM vào giỏ hàng thành công';
        COMMIT;
    END IF;
END //
DELIMITER ;

-- Procedure tạo đơn hàng từ giỏ hàng
DELIMITER //
CREATE PROCEDURE CreateOrderFromCart(
    IN p_makh VARCHAR(255), -- Email khách hàng
    OUT p_order_id VARCHAR(20), -- Mã hóa đơn được tạo
    OUT p_success BOOLEAN, -- Kết quả thực hiện
    OUT p_message VARCHAR(255) -- Thông báo
)
BEGIN
    DECLARE cart_count INT;

    -- Xử lý lỗi SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            SET p_success = FALSE;
            SET p_message = 'Có lỗi xảy ra khi tạo đơn hàng';
            ROLLBACK;
        END;

    -- Kiểm tra giỏ hàng có trống không
    SELECT COUNT(*) INTO cart_count FROM `GioHang` WHERE MaKH = p_makh;

    IF cart_count = 0 THEN
        SET p_success = FALSE;
        SET p_message = 'Giỏ hàng trống, không thể tạo đơn hàng';
    ELSE
        START TRANSACTION;

        -- Tạo mã hóa đơn tự động
        SET p_order_id = CONCAT(
                'HD',
                DATE_FORMAT(NOW(), '%Y%m%d'),
                LPAD((SELECT COUNT(*) + 1 FROM `HoaDon` WHERE DATE(NgayTao) = CURDATE()), 3, '0')
                         );

        -- Tạo đơn hàng mới
        INSERT INTO `HoaDon` (MaHD, MaKH, TrangThaiDon)
        VALUES (p_order_id, p_makh, 'ChoXacNhan');

        SET p_success = TRUE;
        SET p_message = 'Đã tạo đơn hàng thành công từ giỏ hàng';
        COMMIT;
    END IF;
END //
DELIMITER ;

-- ===== PROCEDURES CHO QUẢN LÝ SIM =====

-- Procedure quản lý nhập SIM hàng loạt
DELIMITER //
CREATE PROCEDURE ImportSimBatch(
    IN p_manv VARCHAR(255), -- Email nhân viên nhập
    IN p_nhacungcap VARCHAR(255), -- Nhà cung cấp
    IN p_ghichu TEXT, -- Ghi chú nhập hàng
    OUT p_manhap INT, -- Mã đợt nhập được tạo
    OUT p_success BOOLEAN -- Kết quả thực hiện
)
BEGIN
    -- Xử lý lỗi SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            SET p_success = FALSE;
            ROLLBACK;
        END;

    START TRANSACTION;

    -- Tạo một đợt nhập SIM mới
    INSERT INTO `NhapSim` (MaNV, NhaCungCap, GhiChu)
    VALUES (p_manv, p_nhacungcap, p_ghichu);

    -- Lấy ID đợt nhập vừa tạo
    SET p_manhap = LAST_INSERT_ID();
    SET p_success = TRUE;
    COMMIT;
END //
DELIMITER ;

-- Procedure tìm kiếm SIM nâng cao
DELIMITER //
CREATE PROCEDURE SearchSimAdvanced(
    IN p_search VARCHAR(20), -- Tìm kiếm theo số điện thoại
    IN p_nha_mang VARCHAR(50), -- Nhà mạng
    IN p_loai_sim VARCHAR(50), -- Loại SIM
    IN p_gia_min DECIMAL(10, 2), -- Giá thấp nhất
    IN p_gia_max DECIMAL(10, 2), -- Giá cao nhất
    IN p_so_dep BOOLEAN, -- Tìm số đẹp (có nhiều số giống nhau hoặc số dễ nhớ)
    IN p_limit INT, -- Giới hạn kết quả
    IN p_offset INT -- Vị trí bắt đầu
)
BEGIN
    -- Tạo bảng tạm lưu kết quả với điểm số xếp hạng
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_results
    (
        iccid      VARCHAR(255),
        msisdn     VARCHAR(20),
        nhaMang    VARCHAR(50),
        giaBan     DECIMAL(10, 2),
        loaiSim    VARCHAR(50),
        trangThai  VARCHAR(20),
        rank_score INT
    );

    -- Xóa dữ liệu cũ trong bảng tạm nếu có
    DELETE FROM temp_results;

    -- Chèn kết quả tìm kiếm vào bảng tạm với điểm xếp hạng
    INSERT INTO temp_results
    SELECT s.iccid,
           s.msisdn,
           s.NhaMang,
           s.GiaBan,
           s.LoaiSim,
           s.TrangThai,
           IF(p_so_dep = TRUE, (
               -- Số có nhiều số lặp lại (ví dụ: 888, 999)
               (LENGTH(s.msisdn) - LENGTH(REPLACE(s.msisdn, SUBSTRING(s.msisdn, 1, 1), ''))) * 10 +
                   -- Số có dạng đối xứng (ví dụ: 12321)
               IF(SUBSTRING(s.msisdn, 1, 3) = REVERSE(SUBSTRING(s.msisdn, -3)), 50, 0) +
                   -- Số có dạng tăng dần (ví dụ: 12345)
               IF(SUBSTRING(s.msisdn, -5) IN ('12345', '23456', '34567', '45678', '56789'), 50, 0) +
                   -- Số có dạng giảm dần (ví dụ: 98765)
               IF(SUBSTRING(s.msisdn, -5) IN ('98765', '87654', '76543', '65432', '54321'), 50, 0) +
                   -- Số kết thúc bằng dãy số đẹp
               IF(SUBSTRING(s.msisdn, -4) IN
                  ('0000', '1111', '2222', '3333', '4444', '5555', '6666', '7777', '8888', '9999'), 100, 0) +
               IF(SUBSTRING(s.msisdn, -4) IN ('0123', '1234', '2345', '3456', '4567', '5678', '6789'), 80, 0) +
                   -- Thêm điểm cho số ngắn hơn (ít số hơn)
               (11 - LENGTH(TRIM(s.msisdn))) * 5
               ), 0) AS rank_score
    FROM `SIM` s
    WHERE s.TrangThai = 'SanSang'
      AND (p_search IS NULL OR s.msisdn LIKE CONCAT('%', p_search, '%'))
      AND (p_nha_mang IS NULL OR s.NhaMang = p_nha_mang)
      AND (p_loai_sim IS NULL OR s.LoaiSim = p_loai_sim)
      AND (p_gia_min IS NULL OR s.GiaBan >= p_gia_min)
      AND (p_gia_max IS NULL OR s.GiaBan <= p_gia_max);

    -- Trả về kết quả đã xếp hạng
    SELECT iccid,
           msisdn,
           nhaMang,
           giaBan,
           loaiSim,
           trangThai
    FROM temp_results
    ORDER BY IF(p_so_dep = TRUE, rank_score, 0) DESC,
             giaBan
    LIMIT p_limit OFFSET p_offset;

    -- Đếm tổng số kết quả cho phân trang
    SELECT COUNT(*) AS total_count FROM temp_results;

    -- Xóa bảng tạm
    DROP TEMPORARY TABLE IF EXISTS temp_results;
END //
DELIMITER ;

-- ===== PROCEDURES CHO QUẢN LÝ ĐƠN HÀNG =====

-- Procedure cập nhật trạng thái đơn hàng
DELIMITER //
CREATE PROCEDURE UpdateOrderStatus(
    IN p_mahd VARCHAR(20), -- Mã hóa đơn
    IN p_manv VARCHAR(255), -- Email nhân viên xử lý
    IN p_trangthai VARCHAR(50), -- Trạng thái mới
    IN p_ghichu TEXT, -- Ghi chú
    OUT p_success BOOLEAN, -- Kết quả thực hiện
    OUT p_message VARCHAR(255) -- Thông báo
)
BEGIN
    DECLARE old_status VARCHAR(50);

    -- Xử lý lỗi SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            SET p_success = FALSE;
            SET p_message = 'Có lỗi xảy ra khi cập nhật đơn hàng';
            ROLLBACK;
        END;

    START TRANSACTION;

    -- Kiểm tra đơn hàng tồn tại
    SELECT TrangThaiDon INTO old_status FROM `HoaDon` WHERE MaHD = p_mahd;

    IF old_status IS NULL THEN
        SET p_success = FALSE;
        SET p_message = 'Đơn hàng không tồn tại';
        ROLLBACK;
        -- Kiểm tra luồng trạng thái hợp lệ
    ELSEIF old_status = 'DaHoanThanh' OR old_status = 'DaHuy' THEN
        SET p_success = FALSE;
        SET p_message = 'Không thể thay đổi trạng thái đơn hàng đã hoàn thành hoặc đã hủy';
        ROLLBACK;
    ELSEIF p_trangthai NOT IN ('ChoXacNhan', 'DangXuLy', 'DaHoanThanh', 'DaHuy') THEN
        SET p_success = FALSE;
        SET p_message = 'Trạng thái đơn hàng không hợp lệ';
        ROLLBACK;
    ELSE
        -- Cập nhật trạng thái đơn hàng
        UPDATE `HoaDon`
        SET TrangThaiDon = p_trangthai,
            MaNV         = IF(MaNV IS NULL, p_manv, MaNV),
            GhiChu       = COALESCE(p_ghichu, GhiChu)
        WHERE MaHD = p_mahd;

        -- Thêm vào lịch sử giao dịch
        INSERT INTO `LichSuGiaoDich` (MaHD, HanhDong, GhiChu, NguoiThucHien)
        VALUES (p_mahd,
                CASE
                    WHEN p_trangthai = 'DangXuLy' THEN 'ThanhToan'
                    WHEN p_trangthai = 'DaHoanThanh' THEN 'HoanThanh'
                    WHEN p_trangthai = 'DaHuy' THEN 'HuyDon'
                    ELSE 'ThanhToan'
                    END,
                CONCAT('Chuyển trạng thái từ ', old_status, ' sang ', p_trangthai),
                p_manv);

        SET p_success = TRUE;
        SET p_message = CONCAT('Đã cập nhật trạng thái đơn hàng thành ', p_trangthai);
        COMMIT;
    END IF;
END //
DELIMITER ;

-- Procedure lấy chi tiết đơn hàng đầy đủ
DELIMITER //
CREATE PROCEDURE GetOrderDetails(
    IN p_mahd VARCHAR(20) -- Mã hóa đơn cần lấy thông tin
)
BEGIN
    -- Thông tin chung về đơn hàng
    SELECT h.MaHD,
           h.NgayTao,
           h.TrangThaiDon,
           h.TongTien,
           h.GhiChu,
           kh.Email  AS EmailKhachHang,
           kh.HoTen  AS TenKhachHang,
           kh.SDT    AS SDTKhachHang,
           kh.DiaChi AS DiaChiKhachHang,
           nv.Email  AS EmailNhanVien,
           nv.HoTen  AS TenNhanVien
    FROM `HoaDon` h
             LEFT JOIN
         `NguoiDung` kh ON h.MaKH = kh.Email
             LEFT JOIN
         `NguoiDung` nv ON h.MaNV = nv.Email
    WHERE h.MaHD = p_mahd;

    -- Chi tiết các SIM trong đơn hàng
    SELECT hdct.ID,
           s.iccid,
           s.msisdn,
           s.NhaMang,
           s.LoaiSim,
           hdct.GiaBan  AS GiaGoc,
           hdct.GiaCuoi AS GiaSauKhuyenMai
    FROM `HoaDonChiTiet` hdct
             JOIN
         `SIM` s ON hdct.iccid = s.iccid
    WHERE hdct.MaHD = p_mahd;

    -- Thông tin ưu đãi áp dụng
    SELECT ud.MaUD,
           ud.MoTa,
           ud.LoaiGiam,
           apud.GiaTriApDung
    FROM `ApDungUuDai` apud
             JOIN
         `UuDai` ud ON apud.MaUD = ud.MaUD
    WHERE apud.MaHD = p_mahd;

    -- Lịch sử giao dịch của đơn hàng
    SELECT ls.MaGiaoDich,
           ls.ThoiGian,
           ls.HanhDong,
           ls.GhiChu,
           nd.HoTen AS NguoiThucHien
    FROM `LichSuGiaoDich` ls
             LEFT JOIN
         `NguoiDung` nd ON ls.NguoiThucHien = nd.Email
    WHERE ls.MaHD = p_mahd
    ORDER BY ls.ThoiGian;
END //
DELIMITER ;

-- ===== PROCEDURES CHO THỐNG KÊ BÁO CÁO =====

-- Procedure thống kê doanh thu theo khoảng thời gian
DELIMITER //
CREATE PROCEDURE ThongKeDoanhThu(
    IN p_from_date DATE, -- Ngày bắt đầu
    IN p_to_date DATE, -- Ngày kết thúc
    IN p_group_by VARCHAR(20) -- Nhóm theo: 'day', 'month', 'year'
)
BEGIN
    -- Nếu không có ngày bắt đầu, mặc định là đầu tháng hiện tại
    IF p_from_date IS NULL THEN
        SET p_from_date = DATE_FORMAT(NOW(), '%Y-%m-01');
    END IF;

    -- Nếu không có ngày kết thúc, mặc định là ngày hiện tại
    IF p_to_date IS NULL THEN
        SET p_to_date = CURDATE();
    END IF;

    -- Thống kê theo từng loại nhóm
    CASE
        WHEN p_group_by = 'month' THEN -- Thống kê theo tháng
        SELECT DATE_FORMAT(h.NgayTao, '%Y-%m') AS ThoiGian,
               COUNT(DISTINCT h.MaHD)          AS SoDonHang,
               SUM(h.TongTien)                 AS DoanhThu,
               COUNT(DISTINCT h.MaKH)          AS SoKhachHang
        FROM `HoaDon` h
        WHERE h.TrangThaiDon = 'DaHoanThanh'
          AND DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
        GROUP BY DATE_FORMAT(h.NgayTao, '%Y-%m')
        ORDER BY ThoiGian;

        WHEN p_group_by = 'year' THEN -- Thống kê theo năm
        SELECT YEAR(h.NgayTao)        AS ThoiGian,
               COUNT(DISTINCT h.MaHD) AS SoDonHang,
               SUM(h.TongTien)        AS DoanhThu,
               COUNT(DISTINCT h.MaKH) AS SoKhachHang
        FROM `HoaDon` h
        WHERE h.TrangThaiDon = 'DaHoanThanh'
          AND DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
        GROUP BY YEAR(h.NgayTao)
        ORDER BY ThoiGian;

        ELSE -- Thống kê theo ngày (mặc định)
        SELECT DATE(h.NgayTao)        AS ThoiGian,
               COUNT(DISTINCT h.MaHD) AS SoDonHang,
               SUM(h.TongTien)        AS DoanhThu,
               COUNT(DISTINCT h.MaKH) AS SoKhachHang
        FROM `HoaDon` h
        WHERE h.TrangThaiDon = 'DaHoanThanh'
          AND DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
        GROUP BY DATE(h.NgayTao)
        ORDER BY ThoiGian;
        END CASE;
END //
DELIMITER ;

-- Procedure thống kê sản phẩm bán chạy
DELIMITER //
CREATE PROCEDURE ThongKeSanPhamBanChay(
    IN p_from_date DATE, -- Ngày bắt đầu
    IN p_to_date DATE, -- Ngày kết thúc
    IN p_limit INT -- Số lượng kết quả
)
BEGIN
    -- Nếu không có ngày bắt đầu, mặc định là đầu tháng hiện tại
    IF p_from_date IS NULL THEN
        SET p_from_date = DATE_FORMAT(NOW(), '%Y-%m-01');
    END IF;

    -- Nếu không có ngày kết thúc, mặc định là ngày hiện tại
    IF p_to_date IS NULL THEN
        SET p_to_date = CURDATE();
    END IF;

    -- Nếu không có giới hạn, mặc định là 10
    IF p_limit IS NULL OR p_limit <= 0 THEN
        SET p_limit = 10;
    END IF;

    -- Thống kê SIM bán chạy theo nhà mạng
    SELECT s.NhaMang,
           s.LoaiSim,
           COUNT(*)          AS SoLuongBan,
           SUM(hdct.GiaCuoi) AS DoanhThu
    FROM `HoaDonChiTiet` hdct
             JOIN
         `SIM` s ON hdct.iccid = s.iccid
             JOIN
         `HoaDon` h ON hdct.MaHD = h.MaHD
    WHERE h.TrangThaiDon = 'DaHoanThanh'
      AND DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
    GROUP BY s.NhaMang, s.LoaiSim
    ORDER BY SoLuongBan DESC, DoanhThu DESC
    LIMIT p_limit;
END //
DELIMITER ;

-- ===== PROCEDURES CHO XÁC THỰC VÀ PHÂN QUYỀN =====

-- Procedure xác thực đăng nhập
DELIMITER //
CREATE PROCEDURE AuthenticateUser(
    IN p_email VARCHAR(255), -- Email đăng nhập
    IN p_password VARCHAR(255), -- Mật khẩu (đã hash từ frontend)
    OUT p_authenticated BOOLEAN, -- Kết quả xác thực
    OUT p_role VARCHAR(20) -- Vai trò người dùng nếu xác thực thành công
)
BEGIN
    DECLARE stored_password VARCHAR(255);
    DECLARE user_role VARCHAR(20);

    -- Tìm thông tin người dùng
    SELECT Password, VaiTro
    INTO stored_password, user_role
    FROM `NguoiDung`
    WHERE Email = p_email;

    -- Kiểm tra xác thực
    IF stored_password IS NULL THEN
        SET p_authenticated = FALSE;
        SET p_role = NULL;
    ELSEIF stored_password = p_password THEN
        SET p_authenticated = TRUE;
        SET p_role = user_role;
    ELSE
        SET p_authenticated = FALSE;
        SET p_role = NULL;
    END IF;
END //
DELIMITER ;