# Triggers và tự động hóa - FPT SIM

## Tổng quan về triggers

Triggers là các đoạn mã SQL tự động thực thi khi có sự kiện xảy ra trên bảng dữ liệu (INSERT, UPDATE, DELETE). Trong hệ
thống FPT SIM, triggers được sử dụng để tự động hóa nhiều quy trình kinh doanh, đảm bảo tính toàn vẹn dữ liệu và giảm
tải cho code backend.

## Danh sách triggers

### 1. after_create_order_from_cart

**Bảng**: `HoaDon`  
**Sự kiện**: AFTER INSERT  
**Mô tả**: Tự động chuyển SIM từ giỏ hàng sang chi tiết hóa đơn khi tạo đơn hàng mới.

```sql
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
          AND iccid IN (SELECT iccid
                        FROM `HoaDonChiTiet`
                        WHERE MaHD = NEW.MaHD);
    END IF;
END
```

**Lợi ích cho backend**:

- Không cần viết code chuyển SIM từ giỏ hàng sang đơn hàng
- Quá trình diễn ra trong một transaction SQL, đảm bảo tính nhất quán
- Giảm lượng dữ liệu cần truyền giữa backend và database

### 2. before_insert_giohang

**Bảng**: `GioHang`  
**Sự kiện**: BEFORE INSERT  
**Mô tả**: Kiểm tra trạng thái SIM trước khi cho phép thêm vào giỏ hàng.

```sql
CREATE TRIGGER before_insert_giohang
    BEFORE INSERT
    ON `GioHang`
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
END
```

**Lợi ích cho backend**:

- Kiểm tra trạng thái SIM được thực hiện tại database layer
- Đảm bảo tính toàn vẹn dữ liệu ngay cả khi frontend không kiểm tra
- Trả về lỗi rõ ràng cho backend xử lý

### 3. before_update_hoadon_check

**Bảng**: `HoaDon`  
**Sự kiện**: BEFORE UPDATE  
**Mô tả**: Kiểm tra tính hợp lệ khi cập nhật trạng thái đơn hàng.

```sql
CREATE TRIGGER before_update_hoadon_check
    BEFORE UPDATE
    ON `HoaDon`
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
END
```

**Lợi ích cho backend**:

- Đảm bảo tính nhất quán trong luồng xử lý đơn hàng
- Ngăn chặn các thay đổi trạng thái không hợp lệ
- Đảm bảo mỗi đơn hàng phải được nhân viên xử lý trước khi hoàn thành

### 4. update_uudai_status (event)

**Loại**: EVENT  
**Lịch chạy**: Mỗi ngày một lần  
**Mô tả**: Tự động cập nhật trạng thái ưu đãi dựa trên ngày hiệu lực.

```sql
CREATE EVENT update_uudai_status
    ON SCHEDULE EVERY 1 DAY
        STARTS CURRENT_TIMESTAMP
    DO
    BEGIN
        -- Tự động cập nhật trạng thái ưu đãi dựa trên thời gian
        UPDATE `UuDai`
        SET `TrangThai` = FALSE
        WHERE `NgayHetHan` < CURDATE()
          AND `TrangThai` = TRUE;
    END
```

**Lợi ích cho backend**:

- Không cần viết code cron job để kiểm tra ưu đãi hết hạn
- Đảm bảo ưu đãi hết hạn không được áp dụng cho đơn hàng mới

### 5. after_insert_hoadonchitiet

**Bảng**: `HoaDonChiTiet`  
**Sự kiện**: AFTER INSERT  
**Mô tả**: Cập nhật trạng thái SIM và tổng tiền hóa đơn khi thêm SIM vào đơn hàng.

```sql
CREATE TRIGGER after_insert_hoadonchitiet
    AFTER INSERT
    ON `HoaDonChiTiet`
    FOR EACH ROW
BEGIN
    -- Cập nhật trạng thái SIM thành "Đã bán"
    UPDATE `SIM` SET `TrangThai` = 'DaBan' WHERE `iccid` = NEW.iccid;

    -- Cập nhật tổng tiền hóa đơn
    UPDATE `HoaDon`
    SET `TongTien` = (SELECT SUM(`GiaCuoi`)
                      FROM `HoaDonChiTiet`
                      WHERE `MaHD` = NEW.MaHD)
    WHERE `MaHD` = NEW.MaHD;
END
```

**Lợi ích cho backend**:

- Tự động cập nhật trạng thái SIM khi được bán
- Tự động tính toán tổng tiền đơn hàng
- Giảm thiểu code xử lý trong backend

### 6. after_insert_chitietnhapsim

**Bảng**: `ChiTietNhapSim`  
**Sự kiện**: AFTER INSERT  
**Mô tả**: Cập nhật tổng số lượng và tổng giá nhập SIM.

```sql
CREATE TRIGGER after_insert_chitietnhapsim
    AFTER INSERT
    ON `ChiTietNhapSim`
    FOR EACH ROW
BEGIN
    -- Cập nhật tổng số lượng và tổng giá nhập trong bảng NhapSim
    UPDATE `NhapSim`
    SET `TongSoLuong` = `TongSoLuong` + 1,
        `TongGiaNhap` = `TongGiaNhap` + NEW.GiaNhap
    WHERE `MaNhap` = NEW.MaNhap;
END
```

**Lợi ích cho backend**:

- Tự động cập nhật tổng số lượng và tổng giá trị nhập hàng
- Không cần viết code tính toán tổng kết đợt nhập

### 7. after_insert_hoadon

**Bảng**: `HoaDon`  
**Sự kiện**: AFTER INSERT  
**Mô tả**: Tự động ghi lịch sử khi tạo hóa đơn mới.

```sql
CREATE TRIGGER after_insert_hoadon
    AFTER INSERT
    ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Thêm vào lịch sử giao dịch
    INSERT INTO `LichSuGiaoDich` (`MaHD`, `HanhDong`, `GhiChu`, `NguoiThucHien`)
    VALUES (NEW.MaHD, 'TaoDon', 'Tạo đơn hàng mới', NEW.MaKH);
END
```

**Lợi ích cho backend**:

- Tự động ghi lịch sử giao dịch khi tạo đơn hàng
- Đảm bảo mọi đơn hàng đều có lịch sử đầy đủ

### 8. after_update_trangthai_hoadon

**Bảng**: `HoaDon`  
**Sự kiện**: AFTER UPDATE  
**Mô tả**: Ghi lịch sử khi thay đổi trạng thái đơn hàng và xử lý khi hủy đơn.

```sql
CREATE TRIGGER after_update_trangthai_hoadon
    AFTER UPDATE
    ON `HoaDon`
    FOR EACH ROW
BEGIN
    -- Chỉ thêm lịch sử nếu trạng thái đơn hàng thay đổi
    IF NEW.TrangThaiDon != OLD.TrangThaiDon THEN
        -- Xác định hành động dựa trên trạng thái mới
        DECLARE action ENUM('TaoDon', 'ThanhToan', 'HuyDon', 'HoanThanh');
        DECLARE note VARCHAR(255);

        IF NEW.TrangThaiDon = 'DangXuLy' THEN
            SET action = 'ThanhToan';
            SET note = 'Đơn hàng đang được xử lý';
        ELSEIF NEW.TrangThaiDon = 'DaHoanThanh' THEN
            SET action = 'HoanThanh';
            SET note = 'Đơn hàng đã hoàn thành';
        ELSEIF NEW.TrangThaiDon = 'DaHuy' THEN
            SET action = 'HuyDon';
            SET note = 'Đơn hàng đã bị hủy';
        ELSE
            SET action = 'ThanhToan';
            SET note = CONCAT('Cập nhật trạng thái từ ', OLD.TrangThaiDon, ' sang ', NEW.TrangThaiDon);
        END IF;

        -- Thêm vào lịch sử giao dịch
        INSERT INTO `LichSuGiaoDich` (`MaHD`, `HanhDong`, `GhiChu`, `NguoiThucHien`)
        VALUES (NEW.MaHD, action, note, NEW.MaNV);

        -- Nếu đơn hàng bị hủy, cập nhật trạng thái SIM về "Sẵn sàng"
        IF NEW.TrangThaiDon = 'DaHuy' THEN
            UPDATE `SIM` s
                JOIN `HoaDonChiTiet` hdct ON s.iccid = hdct.iccid
            SET s.TrangThai = 'SanSang'
            WHERE hdct.MaHD = NEW.MaHD;
        END IF;
    END IF;
END
```

**Lợi ích cho backend**:

- Tự động ghi lịch sử thay đổi trạng thái
- Tự động xử lý khi đơn hàng bị hủy
- Không cần code backend cho việc khôi phục trạng thái SIM khi hủy đơn

## Quy trình nghiệp vụ được tự động hóa

### 1. Quy trình thanh toán đơn hàng

- Tạo đơn hàng mới (`HoaDon`)
- Tự động chuyển SIM từ giỏ hàng sang đơn hàng (`after_create_order_from_cart`)
- Tự động cập nhật tổng tiền đơn hàng
- Tự động ghi lịch sử giao dịch

### 2. Quy trình cập nhật trạng thái đơn hàng

- Kiểm tra tính hợp lệ của trạng thái mới (`before_update_hoadon_check`)
- Đảm bảo có nhân viên xử lý khi chuyển trạng thái
- Tự động ghi lịch sử thay đổi trạng thái

### 3. Quy trình nhập SIM

- Cập nhật tổng số lượng và tổng giá nhập tự động
- Đảm bảo dữ liệu nhất quán giữa bảng chính và bảng chi tiết

### 4. Quy trình hủy đơn hàng

- Kiểm tra tính hợp lệ của việc hủy đơn
- Tự động khôi phục trạng thái SIM khi hủy đơn
- Ghi lại lịch sử hủy đơn

### 5. Quy trình áp dụng ưu đãi

- Tự động kiểm tra tính hiệu lực của ưu đãi
- Cập nhật giá cuối cùng sau khi áp dụng ưu đãi

## Lưu ý khi phát triển backend

1. **Xử lý lỗi từ trigger**:
    - Khi gặp lỗi từ trigger (SQLSTATE '45000'), backend nên bắt exception và hiển thị thông báo lỗi phù hợp
    - Ví dụ:
      ```java
      try {
          // Thêm SIM vào giỏ hàng
          gioHangRepository.save(gioHang);
      } catch (DataIntegrityViolationException ex) {
          if (ex.getMessage().contains("SIM này không còn sẵn sàng để bán")) {
              throw new BusinessException("SIM đã được bán hoặc không khả dụng");
          }
          throw ex;
      }
      ```

2. **Không trùng lặp logic**:
    - Tránh viết lại logic đã được xử lý bởi trigger
    - Sử dụng stored procedure thay vì xử lý logic phức tạp trong code Java

3. **Hiểu rõ luồng dữ liệu**:
    - Nắm vững các trigger sẽ chạy khi thực hiện các thao tác CRUD
    - Lưu ý thứ tự thực thi của các trigger

4. **Bảo trì và cập nhật**:
    - Khi thay đổi logic nghiệp vụ, cần xem xét cập nhật cả trigger và code backend
    - Ghi chú rõ ràng về mục đích của trigger để dễ bảo trì

5. **Debugging với trigger**:
    - Sử dụng bảng log tạm thời để ghi lại hoạt động của trigger
    - Ví dụ:
      ```sql
      -- Tạo bảng log
      CREATE TABLE IF NOT EXISTS `TriggerLog` (
          `ID` INT AUTO_INCREMENT,
          `TriggerName` VARCHAR(100),
          `TableName` VARCHAR(100),
          `ActionType` VARCHAR(10),
          `RecordID` VARCHAR(100),
          `LogMessage` TEXT,
          `LogTime` DATETIME DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (`ID`)
      );
      
      -- Trong trigger
      INSERT INTO `TriggerLog` (`TriggerName`, `TableName`, `ActionType`, `RecordID`, `LogMessage`)
      VALUES ('after_insert_hoadon', 'HoaDon', 'INSERT', NEW.MaHD, 'Hóa đơn được tạo');
      ```

## Khi nào nên sử dụng Trigger vs. khi nào sử dụng xử lý Java

### Nên sử dụng trigger khi:

1. Logic nghiệp vụ đơn giản và không thay đổi thường xuyên
2. Cần đảm bảo tính toàn vẹn dữ liệu ở mức database
3. Cần tự động hóa các tác vụ thường xuyên (ghi log, cập nhật số lượng)
4. Muốn giảm lượng dữ liệu truyền qua mạng

### Nên sử dụng xử lý Java khi:

1. Logic nghiệp vụ phức tạp hoặc thay đổi thường xuyên
2. Cần tích hợp với các hệ thống bên ngoài (thanh toán, email)
3. Cần xử lý dữ liệu dưới dạng batch hoặc xử lý nhiều bảng không liên quan
4. Cần kiểm soát chi tiết về luồng xử lý và xử lý lỗi

## Kết luận

Triggers trong hệ thống FPT SIM giúp tự động hóa nhiều quy trình nghiệp vụ, giảm tải cho code backend và đảm bảo tính
nhất quán dữ liệu. Hiểu rõ các trigger hiện có và cách chúng hoạt động sẽ giúp phát triển backend hiệu quả và tránh được
các lỗi phổ biến.