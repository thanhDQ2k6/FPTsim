# Stored Procedures và cách gọi từ Java - FPT SIM

## Tổng quan về Stored Procedures

Stored Procedures là các đoạn mã SQL được lưu trữ và thực thi trên cơ sở dữ liệu. Trong hệ thống FPT SIM, các stored procedures được sử dụng để:
- Tối ưu hóa hiệu suất truy vấn phức tạp
- Đóng gói logic nghiệp vụ phức tạp
- Giảm lượng dữ liệu truyền qua mạng
- Tăng cường bảo mật dữ liệu

## Danh sách Stored Procedures

### 1. AddToCart

**Mục đích**: Thêm SIM vào giỏ hàng của khách hàng.

**Tham số**:
- `p_makh` (IN): Email khách hàng
- `p_iccid` (IN): ICCID của SIM muốn thêm
- `p_success` (OUT): Kết quả thực hiện (boolean)
- `p_message` (OUT): Thông báo kết quả

**Mô tả**: Procedure kiểm tra trạng thái SIM trước khi thêm vào giỏ hàng. Nếu SIM không tồn tại hoặc không ở trạng thái "Sẵn sàng", procedure sẽ trả về lỗi.

```sql
DELIMITER //
CREATE PROCEDURE AddToCart(
    IN p_makh VARCHAR(255),          -- Email khách hàng
    IN p_iccid VARCHAR(255),         -- ICCID của SIM
    OUT p_success BOOLEAN,           -- Kết quả thực hiện
    OUT p_message VARCHAR(255)       -- Thông báo
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
```

### 2. CreateOrderFromCart

**Mục đích**: Tạo đơn hàng mới từ giỏ hàng của khách hàng.

**Tham số**:
- `p_makh` (IN): Email khách hàng
- `p_order_id` (OUT): Mã đơn hàng được tạo
- `p_success` (OUT): Kết quả thực hiện (boolean)
- `p_message` (OUT): Thông báo kết quả

**Mô tả**: Procedure tạo đơn hàng mới và trả về mã đơn hàng. Sau đó trigger `after_create_order_from_cart` sẽ tự động chuyển SIM từ giỏ hàng sang chi tiết đơn hàng.

```sql
DELIMITER //
CREATE PROCEDURE CreateOrderFromCart(
    IN p_makh VARCHAR(255),          -- Email khách hàng
    OUT p_order_id VARCHAR(20),      -- Mã hóa đơn được tạo
    OUT p_success BOOLEAN,           -- Kết quả thực hiện
    OUT p_message VARCHAR(255)       -- Thông báo
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
```

### 3. SearchSimAdvanced

**Mục đích**: Tìm kiếm SIM theo nhiều tiêu chí phức tạp.

**Tham số**:
- `p_search` (IN): Từ khóa tìm kiếm số điện thoại
- `p_nha_mang` (IN): Lọc theo nhà mạng
- `p_loai_sim` (IN): Lọc theo loại SIM
- `p_gia_min` (IN): Giá thấp nhất
- `p_gia_max` (IN): Giá cao nhất
- `p_so_dep` (IN): Tìm số đẹp (boolean)
- `p_limit` (IN): Số lượng kết quả tối đa
- `p_offset` (IN): Vị trí bắt đầu

**Mô tả**: Procedure thực hiện tìm kiếm SIM với nhiều tiêu chí phức tạp, bao gồm tính năng tìm số đẹp dựa trên các thuật toán đánh giá số điện thoại.

```sql
DELIMITER //
CREATE PROCEDURE SearchSimAdvanced(
    IN p_search VARCHAR(20),         -- Tìm kiếm theo số điện thoại
    IN p_nha_mang VARCHAR(50),       -- Nhà mạng
    IN p_loai_sim VARCHAR(50),       -- Loại SIM
    IN p_gia_min DECIMAL(10,2),      -- Giá thấp nhất
    IN p_gia_max DECIMAL(10,2),      -- Giá cao nhất
    IN p_so_dep BOOLEAN,             -- Tìm số đẹp (có nhiều số giống nhau hoặc số dễ nhớ)
    IN p_limit INT,                  -- Giới hạn kết quả
    IN p_offset INT                  -- Vị trí bắt đầu
)
BEGIN
    -- Tạo bảng tạm lưu kết quả với điểm số xếp hạng
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_results (
                                                          iccid VARCHAR(255),
                                                          msisdn VARCHAR(20),
                                                          nhaMang VARCHAR(50),
                                                          giaBan DECIMAL(10,2),
                                                          loaiSim VARCHAR(50),
                                                          trangThai VARCHAR(20),
                                                          rank_score INT
    );

    -- Xóa dữ liệu cũ trong bảng tạm nếu có
    DELETE FROM temp_results;

    -- Chèn kết quả tìm kiếm vào bảng tạm với điểm xếp hạng
    INSERT INTO temp_results
    SELECT
        s.iccid,
        s.msisdn,
        s.NhaMang,
        s.GiaBan,
        s.LoaiSim,
        s.TrangThai,
        CASE
            -- Tính điểm xếp hạng cho số đẹp nếu cần
            WHEN p_so_dep = TRUE THEN (
                -- Số có nhiều số lặp lại (ví dụ: 888, 999)
                (LENGTH(s.msisdn) - LENGTH(REPLACE(s.msisdn, SUBSTRING(s.msisdn, 1, 1), ''))) * 10 +
                    -- Số có dạng đối xứng (ví dụ: 12321)
                IF(SUBSTRING(s.msisdn, 1, 3) = REVERSE(SUBSTRING(s.msisdn, -3)), 50, 0) +
                    -- Số có dạng tăng dần (ví dụ: 12345)
                IF(SUBSTRING(s.msisdn, -5) IN ('12345', '23456', '34567', '45678', '56789'), 50, 0) +
                    -- Số có dạng giảm dần (ví dụ: 98765)
                IF(SUBSTRING(s.msisdn, -5) IN ('98765', '87654', '76543', '65432', '54321'), 50, 0) +
                    -- Số kết thúc bằng dãy số đẹp
                IF(SUBSTRING(s.msisdn, -4) IN ('0000', '1111', '2222', '3333', '4444', '5555', '6666', '7777', '8888', '9999'), 100, 0) +
                IF(SUBSTRING(s.msisdn, -4) IN ('0123', '1234', '2345', '3456', '4567', '5678', '6789'), 80, 0) +
                    -- Thêm điểm cho số ngắn hơn (ít số hơn)
                (11 - LENGTH(TRIM(s.msisdn))) * 5
                )
            ELSE 0
            END AS rank_score
    FROM `SIM` s
    WHERE
        s.TrangThai = 'SanSang' AND
        (p_search IS NULL OR s.msisdn LIKE CONCAT('%', p_search, '%')) AND
        (p_nha_mang IS NULL OR s.NhaMang = p_nha_mang) AND
        (p_loai_sim IS NULL OR s.LoaiSim = p_loai_sim) AND
        (p_gia_min IS NULL OR s.GiaBan >= p_gia_min) AND
        (p_gia_max IS NULL OR s.GiaBan <= p_gia_max);

    -- Trả về kết quả đã xếp hạng
    SELECT
        iccid, msisdn, nhaMang, giaBan, loaiSim, trangThai
    FROM
        temp_results
    ORDER BY
        CASE WHEN p_so_dep = TRUE THEN rank_score ELSE 0 END DESC,
        giaBan ASC
    LIMIT p_limit OFFSET p_offset;

    -- Đếm tổng số kết quả cho phân trang
    SELECT COUNT(*) AS total_count FROM temp_results;

    -- Xóa bảng tạm
    DROP TEMPORARY TABLE IF EXISTS temp_results;
END //
DELIMITER ;
```

### 4. UpdateOrderStatus

**Mục đích**: Cập nhật trạng thái đơn hàng và ghi lại lịch sử thay đổi.

**Tham số**:
- `p_mahd` (IN): Mã hóa đơn
- `p_manv` (IN): Email nhân viên xử lý
- `p_trangthai` (IN): Trạng thái mới
- `p_ghichu` (IN): Ghi chú
- `p_success` (OUT): Kết quả thực hiện (boolean)
- `p_message` (OUT): Thông báo kết quả

**Mô tả**: Procedure cập nhật trạng thái đơn hàng, kiểm tra tính hợp lệ của thay đổi và ghi lại lịch sử giao dịch.

```sql
DELIMITER //
CREATE PROCEDURE UpdateOrderStatus(
    IN p_mahd VARCHAR(20),           -- Mã hóa đơn
    IN p_manv VARCHAR(255),          -- Email nhân viên xử lý
    IN p_trangthai VARCHAR(50),      -- Trạng thái mới
    IN p_ghichu TEXT,                -- Ghi chú
    OUT p_success BOOLEAN,           -- Kết quả thực hiện
    OUT p_message VARCHAR(255)       -- Thông báo
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
        SET 
            TrangThaiDon = p_trangthai,
            MaNV = CASE WHEN MaNV IS NULL THEN p_manv ELSE MaNV END,
            GhiChu = CASE WHEN p_ghichu IS NOT NULL THEN p_ghichu ELSE GhiChu END
        WHERE MaHD = p_mahd;
        
        -- Thêm vào lịch sử giao dịch
        INSERT INTO `LichSuGiaoDich` (MaHD, HanhDong, GhiChu, NguoiThucHien)
        VALUES (
            p_mahd, 
            CASE 
                WHEN p_trangthai = 'DangXuLy' THEN 'ThanhToan'
                WHEN p_trangthai = 'DaHoanThanh' THEN 'HoanThanh'
                WHEN p_trangthai = 'DaHuy' THEN 'HuyDon'
                ELSE 'ThanhToan'
            END,
            CONCAT('Chuyển trạng thái từ ', old_status, ' sang ', p_trangthai),
            p_manv
        );
        
        SET p_success = TRUE;
        SET p_message = CONCAT('Đã cập nhật trạng thái đơn hàng thành ', p_trangthai);
        COMMIT;
    END IF;
END //
DELIMITER ;
```

### 5. GetOrderDetails

**Mục đích**: Lấy thông tin chi tiết đầy đủ của một đơn hàng.

**Tham số**:
- `p_mahd` (IN): Mã hóa đơn cần lấy thông tin

**Mô tả**: Procedure trả về tất cả thông tin liên quan đến đơn hàng bao gồm: thông tin chung, chi tiết SIM, ưu đãi áp dụng, và lịch sử giao dịch.

```sql
DELIMITER //
CREATE PROCEDURE GetOrderDetails(
    IN p_mahd VARCHAR(20)            -- Mã hóa đơn cần lấy thông tin
)
BEGIN
    -- Thông tin chung về đơn hàng
    SELECT 
        h.MaHD,
        h.NgayTao,
        h.TrangThaiDon,
        h.TongTien,
        h.GhiChu,
        kh.Email AS EmailKhachHang,
        kh.HoTen AS TenKhachHang,
        kh.SDT AS SDTKhachHang,
        kh.DiaChi AS DiaChiKhachHang,
        nv.Email AS EmailNhanVien,
        nv.HoTen AS TenNhanVien
    FROM 
        `HoaDon` h
    LEFT JOIN 
        `NguoiDung` kh ON h.MaKH = kh.Email
    LEFT JOIN 
        `NguoiDung` nv ON h.MaNV = nv.Email
    WHERE 
        h.MaHD = p_mahd;
    
    -- Chi tiết các SIM trong đơn hàng
    SELECT 
        hdct.ID,
        s.iccid,
        s.msisdn,
        s.NhaMang,
        s.LoaiSim,
        hdct.GiaBan AS GiaGoc,
        hdct.GiaCuoi AS GiaSauKhuyenMai
    FROM 
        `HoaDonChiTiet` hdct
    JOIN 
        `SIM` s ON hdct.iccid = s.iccid
    WHERE 
        hdct.MaHD = p_mahd;
    
    -- Thông tin ưu đãi áp dụng
    SELECT 
        ud.MaUD,
        ud.MoTa,
        ud.LoaiGiam,
        apud.GiaTriApDung
    FROM 
        `ApDungUuDai` apud
    JOIN 
        `UuDai` ud ON apud.MaUD = ud.MaUD
    WHERE 
        apud.MaHD = p_mahd;
    
    -- Lịch sử giao dịch của đơn hàng
    SELECT 
        ls.MaGiaoDich,
        ls.ThoiGian,
        ls.HanhDong,
        ls.GhiChu,
        nd.HoTen AS NguoiThucHien
    FROM 
        `LichSuGiaoDich` ls
    LEFT JOIN 
        `NguoiDung` nd ON ls.NguoiThucHien = nd.Email
    WHERE 
        ls.MaHD = p_mahd
    ORDER BY 
        ls.ThoiGian;
END //
DELIMITER ;
```

### 6. ThongKeDoanhThu

**Mục đích**: Thống kê doanh thu theo khoảng thời gian và nhóm theo ngày/tháng/năm.

**Tham số**:
- `p_from_date` (IN): Ngày bắt đầu
- `p_to_date` (IN): Ngày kết thúc
- `p_group_by` (IN): Nhóm theo: 'day', 'month', 'year'

**Mô tả**: Procedure thống kê doanh thu từ các đơn hàng hoàn thành trong khoảng thời gian và nhóm kết quả theo ngày, tháng hoặc năm.

```sql
DELIMITER //
CREATE PROCEDURE ThongKeDoanhThu(
    IN p_from_date DATE,             -- Ngày bắt đầu
    IN p_to_date DATE,               -- Ngày kết thúc
    IN p_group_by VARCHAR(20)        -- Nhóm theo: 'day', 'month', 'year'
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
        WHEN p_group_by = 'month' THEN
            -- Thống kê theo tháng
            SELECT 
                DATE_FORMAT(h.NgayTao, '%Y-%m') AS ThoiGian,
                COUNT(DISTINCT h.MaHD) AS SoDonHang,
                SUM(h.TongTien) AS DoanhThu,
                COUNT(DISTINCT h.MaKH) AS SoKhachHang
            FROM 
                `HoaDon` h
            WHERE 
                h.TrangThaiDon = 'DaHoanThanh' AND
                DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
            GROUP BY 
                DATE_FORMAT(h.NgayTao, '%Y-%m')
            ORDER BY 
                ThoiGian;
        
        WHEN p_group_by = 'year' THEN
            -- Thống kê theo năm
            SELECT 
                YEAR(h.NgayTao) AS ThoiGian,
                COUNT(DISTINCT h.MaHD) AS SoDonHang,
                SUM(h.TongTien) AS DoanhThu,
                COUNT(DISTINCT h.MaKH) AS SoKhachHang
            FROM 
                `HoaDon` h
            WHERE 
                h.TrangThaiDon = 'DaHoanThanh' AND
                DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
            GROUP BY 
                YEAR(h.NgayTao)
            ORDER BY 
                ThoiGian;
        
        ELSE
            -- Thống kê theo ngày (mặc định)
            SELECT 
                DATE(h.NgayTao) AS ThoiGian,
                COUNT(DISTINCT h.MaHD) AS SoDonHang,
                SUM(h.TongTien) AS DoanhThu,
                COUNT(DISTINCT h.MaKH) AS SoKhachHang
            FROM 
                `HoaDon` h
            WHERE 
                h.TrangThaiDon = 'DaHoanThanh' AND
                DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
            GROUP BY 
                DATE(h.NgayTao)
            ORDER BY 
                ThoiGian;
    END CASE;
END //
DELIMITER ;
```

### 7. ThongKeSanPhamBanChay

**Mục đích**: Thống kê SIM bán chạy theo nhà mạng và loại SIM.

**Tham số**:
- `p_from_date` (IN): Ngày bắt đầu
- `p_to_date` (IN): Ngày kết thúc
- `p_limit` (IN): Số lượng kết quả

**Mô tả**: Procedure thống kê số lượng SIM bán ra và doanh thu theo nhà mạng và loại SIM trong khoảng thời gian.

```sql
DELIMITER //
CREATE PROCEDURE ThongKeSanPhamBanChay(
    IN p_from_date DATE,             -- Ngày bắt đầu
    IN p_to_date DATE,               -- Ngày kết thúc
    IN p_limit INT                   -- Số lượng kết quả
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
    SELECT 
        s.NhaMang,
        s.LoaiSim,
        COUNT(*) AS SoLuongBan,
        SUM(hdct.GiaCuoi) AS DoanhThu
    FROM 
        `HoaDonChiTiet` hdct
    JOIN 
        `SIM` s ON hdct.iccid = s.iccid
    JOIN 
        `HoaDon` h ON hdct.MaHD = h.MaHD
    WHERE 
        h.TrangThaiDon = 'DaHoanThanh' AND
        DATE(h.NgayTao) BETWEEN p_from_date AND p_to_date
    GROUP BY 
        s.NhaMang, s.LoaiSim
    ORDER BY 
        SoLuongBan DESC, DoanhThu DESC
    LIMIT p_limit;
END //
DELIMITER ;
```

### 8. AuthenticateUser

**Mục đích**: Xác thực người dùng đăng nhập.

**Tham số**:
- `p_email` (IN): Email đăng nhập
- `p_password` (IN): Mật khẩu (đã hash từ frontend)
- `p_authenticated` (OUT): Kết quả xác thực (boolean)
- `p_role` (OUT): Vai trò người dùng nếu xác thực thành công

**Mô tả**: Procedure kiểm tra thông tin đăng nhập và trả về kết quả xác thực cùng vai trò của người dùng.

```sql
DELIMITER //
CREATE PROCEDURE AuthenticateUser(
    IN p_email VARCHAR(255),         -- Email đăng nhập
    IN p_password VARCHAR(255),      -- Mật khẩu (đã hash từ frontend)
    OUT p_authenticated BOOLEAN,     -- Kết quả xác thực
    OUT p_role VARCHAR(20)           -- Vai trò người dùng nếu xác thực thành công
)
BEGIN
    DECLARE stored_password VARCHAR(255);
    DECLARE user_role VARCHAR(20);
    
    -- Tìm thông tin người dùng
    SELECT Password, VaiTro INTO stored_password, user_role
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
```

## Cách gọi Stored Procedure từ Spring Boot (JPA)

### 1. Sử dụng @NamedStoredProcedureQuery

Định nghĩa procedure trong entity:

```java
@Entity
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(
        name = "AddToCart",
        procedureName = "AddToCart",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_makh", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_iccid", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_success", type = Boolean.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_message", type = String.class)
        }
    )
})
public class GioHang {
    // entity properties
}
```

Gọi procedure trong repository:

```java
@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    
    @Procedure(name = "AddToCart")
    Map<String, Object> addToCart(
        @Param("p_makh") String makh,
        @Param("p_iccid") String iccid
    );
}
```

### 2. Sử dụng JdbcTemplate

```java
@Service
public class GioHangService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public GioHangService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public Map<String, Object> addToCart(String makh, String iccid) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("AddToCart")
            .declareParameters(
                new SqlParameter("p_makh", Types.VARCHAR),
                new SqlParameter("p_iccid", Types.VARCHAR),
                new SqlOutParameter("p_success", Types.BOOLEAN),
                new SqlOutParameter("p_message", Types.VARCHAR)
            );
            
        SqlParameterSource inParams = new MapSqlParameterSource()
            .addValue("p_makh", makh)
            .addValue("p_iccid", iccid);
            
        return jdbcCall.execute(inParams);
    }
}
```

### 3. Sử dụng EntityManager

```java
@Repository
public class GioHangRepositoryImpl implements GioHangRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Map<String, Object> addToCart(String makh, String iccid) {
        StoredProcedureQuery query = entityManager
            .createStoredProcedureQuery("AddToCart")
            .registerStoredProcedureParameter("p_makh", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("p_iccid", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("p_success", Boolean.class, ParameterMode.OUT)
            .registerStoredProcedureParameter("p_message", String.class, ParameterMode.OUT)
            .setParameter("p_makh", makh)
            .setParameter("p_iccid", iccid);
            
        query.execute();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", query.getOutputParameterValue("p_success"));
        result.put("message", query.getOutputParameterValue("p_message"));
        
        return result;
    }
}
```

## Ví dụ triển khai Service Layer đầy đủ

### 1. Service Layer cho quản lý giỏ hàng

```java
@Service
@Transactional
public class CartService {

    private final JdbcTemplate jdbcTemplate;
    private final GioHangRepository gioHangRepository;

    @Autowired
    public CartService(JdbcTemplate jdbcTemplate, GioHangRepository gioHangRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.gioHangRepository = gioHangRepository;
    }

    public CartResult addToCart(String customerId, String iccid) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("AddToCart")
                .declareParameters(
                    new SqlParameter("p_makh", Types.VARCHAR),
                    new SqlParameter("p_iccid", Types.VARCHAR),
                    new SqlOutParameter("p_success", Types.BOOLEAN),
                    new SqlOutParameter("p_message", Types.VARCHAR)
                );
                
            SqlParameterSource inParams = new MapSqlParameterSource()
                .addValue("p_makh", customerId)
                .addValue("p_iccid", iccid);
                
            Map<String, Object> result = jdbcCall.execute(inParams);
            
            boolean success = (Boolean) result.get("p_success");
            String message = (String) result.get("p_message");
            
            return new CartResult(success, message);
        } catch (Exception e) {
            return new CartResult(false, "Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    public OrderResult createOrderFromCart(String customerId) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("CreateOrderFromCart")
                .declareParameters(
                    new SqlParameter("p_makh", Types.VARCHAR),
                    new SqlOutParameter("p_order_id", Types.VARCHAR),
                    new SqlOutParameter("p_success", Types.BOOLEAN),
                    new SqlOutParameter("p_message", Types.VARCHAR)
                );
                
            SqlParameterSource inParams = new MapSqlParameterSource()
                .addValue("p_makh", customerId);
                
            Map<String, Object> result = jdbcCall.execute(inParams);
            
            boolean success = (Boolean) result.get("p_success");
            String message = (String) result.get("p_message");
            String orderId = (String) result.get("p_order_id");
            
            return new OrderResult(success, message, orderId);
        } catch (Exception e) {
            return new OrderResult(false, "Lỗi hệ thống: " + e.getMessage(), null);
        }
    }
    
    // DTO cho kết quả thêm giỏ hàng
    @Data
    @AllArgsConstructor
    public static class CartResult {
        private boolean success;
        private String message;
    }
    
    // DTO cho kết quả tạo đơn hàng
    @Data
    @AllArgsConstructor
    public static class OrderResult {
        private boolean success;
        private String message;
        private String orderId;
    }
}
```

### 2. Service Layer cho quản lý đơn hàng

```java
@Service
@Transactional
public class OrderService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OrderStatusResult updateOrderStatus(String orderId, String employeeId, String status, String note) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("UpdateOrderStatus")
                .declareParameters(
                    new SqlParameter("p_mahd", Types.VARCHAR),
                    new SqlParameter("p_manv", Types.VARCHAR),
                    new SqlParameter("p_trangthai", Types.VARCHAR),
                    new SqlParameter("p_ghichu", Types.VARCHAR),
                    new SqlOutParameter("p_success", Types.BOOLEAN),
                    new SqlOutParameter("p_message", Types.VARCHAR)
                );
                
            SqlParameterSource inParams = new MapSqlParameterSource()
                .addValue("p_mahd", orderId)
                .addValue("p_manv", employeeId)
                .addValue("p_trangthai", status)
                .addValue("p_ghichu", note);
                
            Map<String, Object> result = jdbcCall.execute(inParams);
            
            boolean success = (Boolean) result.get("p_success");
            String message = (String) result.get("p_message");
            
            return new OrderStatusResult(success, message);
        } catch (Exception e) {
            return new OrderStatusResult(false, "Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    public OrderDetailsDTO getOrderDetails(String orderId) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GetOrderDetails")
                .declareParameters(
                    new SqlParameter("p_mahd", Types.VARCHAR)
                )
                .returningResultSet("orderInfo", new OrderInfoRowMapper())
                .returningResultSet("orderItems", new OrderItemsRowMapper())
                .returningResultSet("appliedDiscounts", new DiscountsRowMapper())
                .returningResultSet("orderHistory", new OrderHistoryRowMapper());
                
            SqlParameterSource inParams = new MapSqlParameterSource()
                .addValue("p_mahd", orderId);
                
            Map<String, Object> results = jdbcCall.execute(inParams);
            
            // Parse results
            List<OrderInfoDTO> orderInfoList = (List<OrderInfoDTO>) results.get("orderInfo");
            OrderInfoDTO orderInfo = orderInfoList.isEmpty() ? null : orderInfoList.get(0);
            
            List<OrderItemDTO> orderItems = (List<OrderItemDTO>) results.get("orderItems");
            List<DiscountDTO> appliedDiscounts = (List<DiscountDTO>) results.get("appliedDiscounts");
            List<OrderHistoryDTO> orderHistory = (List<OrderHistoryDTO>) results.get("orderHistory");
            
            if (orderInfo == null) {
                return null; // Order not found
            }
            
            return new OrderDetailsDTO(orderInfo, orderItems, appliedDiscounts, orderHistory);
        } catch (Exception e) {
            throw new RuntimeException("Error getting order details: " + e.getMessage(), e);
        }
    }
    
    // DTO classes and Row Mappers implementation
    // ...
    
    @Data
    @AllArgsConstructor
    public static class OrderStatusResult {
        private boolean success;
        private String message;
    }
}
```

## Best Practices khi sử dụng Stored Procedures

1. **Xử lý lỗi phù hợp**:
   ```java
   try {
       Map<String, Object> result = gioHangRepository.addToCart(email, iccid);
       boolean success = (Boolean) result.get("success");
       String message = (String) result.get("message");
       
       if (!success) {
           throw new BusinessException(message);
       }
   } catch (DataAccessException ex) {
       throw new SystemException("Lỗi khi gọi stored procedure", ex);
   }
   ```

2. **Tránh gọi procedure trong vòng lặp**:
    - Nên sử dụng batch processing hoặc gọi procedure một lần với nhiều dữ liệu

3. **Cache kết quả procedure khi phù hợp**:
    - Sử dụng Spring Cache để lưu trữ kết quả của các procedure không thay đổi thường xuyên
   ```java
   @Cacheable(value = "simSearchResults", key = "#search + '-' + #nhaMang + '-' + #page")
   public SearchResultDTO searchSims(String search, String nhaMang, int page) {
       // Gọi stored procedure
   }
   ```

4. **Monitoring và logging**:
    - Ghi log thời gian thực thi của procedure để phát hiện vấn đề hiệu năng
   ```java
   long startTime = System.currentTimeMillis();
   Map<String, Object> result = jdbcCall.execute(inParams);
   long executionTime = System.currentTimeMillis() - startTime;
   log.info("Procedure {} executed in {} ms", procedureName, executionTime);
   ```

5. **Sử dụng Connection Pooling**:
    - Đảm bảo HikariCP hoặc một connection pool khác được cấu hình đúng để xử lý nhiều procedure calls
   ```properties
   spring.datasource.hikari.maximum-pool-size=10
   spring.datasource.hikari.connection-timeout=30000
   ```

6. **Quản lý transaction**:
    - Sử dụng `@Transactional` khi gọi nhiều procedure liên quan đến nhau
   ```java
   @Transactional
   public OrderResult processOrder(String customerId, PaymentInfo paymentInfo) {
       OrderResult orderResult = createOrderFromCart(customerId);
       if (orderResult.isSuccess()) {
           paymentResult = processPayment(orderResult.getOrderId(), paymentInfo);
       }
       return orderResult;
   }
   ```

7. **Kiểm tra hiệu suất**:
    - Sử dụng EXPLAIN để kiểm tra các truy vấn trong procedure có được tối ưu không
    - Xem xét việc sử dụng index cho các trường thường xuyên tìm kiếm

8. **Phân trang kết quả**:
    - Đối với procedure trả về nhiều dữ liệu, luôn cung cấp tham số phân trang (LIMIT và OFFSET)