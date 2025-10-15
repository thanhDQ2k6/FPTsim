# Chi tiết cấu trúc bảng và mối quan hệ - FPT SIM

## Bảng quan hệ chi tiết

| Thực thể nguồn | Loại quan hệ | Thực thể đích  | Mô tả quan hệ                                         |
|----------------|--------------|----------------|-------------------------------------------------------|
| NguoiDung      | 1-N          | HoaDon         | Khách hàng đặt nhiều đơn hàng                         |
| NguoiDung      | 1-N          | HoaDon         | Nhân viên xử lý nhiều đơn hàng                        |
| NguoiDung      | 1-N          | GioHang        | Khách hàng có một giỏ hàng với nhiều SIM              |
| NguoiDung      | 1-N          | NhapSim        | Nhân viên thực hiện nhiều đợt nhập SIM                |
| NguoiDung      | 1-N          | LichSuGiaoDich | Người dùng thực hiện nhiều giao dịch                  |
| NguoiDung      | 1-N          | ApDungUuDai    | Khách hàng được áp dụng nhiều ưu đãi                  |
| SIM            | 1-N          | GioHang        | Một SIM có thể được thêm vào nhiều giỏ hàng           |
| SIM            | 1-N          | HoaDonChiTiet  | Một SIM có thể xuất hiện trong nhiều chi tiết hóa đơn |
| SIM            | 1-N          | ChiTietNhapSim | Một SIM có thể xuất hiện trong nhiều chi tiết nhập    |
| SIM            | 1-1          | ThongTinChuSIM | Một SIM chỉ có một thông tin chủ sở hữu               |
| HoaDon         | 1-N          | HoaDonChiTiet  | Một hóa đơn chứa nhiều chi tiết SIM                   |
| HoaDon         | 1-N          | LichSuGiaoDich | Một hóa đơn có nhiều lịch sử giao dịch                |
| HoaDon         | 1-N          | ApDungUuDai    | Một hóa đơn có thể áp dụng nhiều ưu đãi               |
| HoaDon         | 1-0..1       | DanhGia        | Một hóa đơn có thể có một đánh giá hoặc không         |
| HoaDon         | 1-N          | ThongTinChuSIM | Một hóa đơn có thể có nhiều thông tin chủ SIM         |
| NhapSim        | 1-N          | ChiTietNhapSim | Một đợt nhập chứa nhiều chi tiết SIM                  |
| UuDai          | 1-N          | ApDungUuDai    | Một ưu đãi có thể được áp dụng nhiều lần              |

### Chú thích loại quan hệ:

- **1-1**: Quan hệ một-một (mỗi bản ghi ở thực thể nguồn liên kết với chính xác một bản ghi ở thực thể đích)
- **1-N**: Quan hệ một-nhiều (mỗi bản ghi ở thực thể nguồn có thể liên kết với nhiều bản ghi ở thực thể đích)
- **1-0..1**: Quan hệ một-một tùy chọn (mỗi bản ghi ở thực thể nguồn có thể liên kết với một hoặc không có bản ghi ở
  thực thể đích)

## Cấu trúc bảng chi tiết

### 1. NguoiDung

Bảng quản lý thông tin người dùng hệ thống, bao gồm khách hàng, nhân viên và admin.

| Tên cột    | Kiểu dữ liệu | Chú thích                        |
|------------|--------------|----------------------------------|
| Email      | VARCHAR(255) | Khóa chính, email đăng nhập      |
| Password   | VARCHAR(255) | Mật khẩu đã hash                 |
| HoTen      | VARCHAR(255) | Tên đầy đủ                       |
| SDT        | VARCHAR(20)  | Số điện thoại                    |
| NgaySinh   | DATE         | Ngày sinh                        |
| DiaChi     | VARCHAR(255) | Địa chỉ                          |
| VaiTro     | ENUM         | 'KhachHang', 'NhanVien', 'Admin' |
| created_at | DATETIME     | Thời gian tạo tài khoản          |
| updated_at | DATETIME     | Thời gian cập nhật gần nhất      |

**Indexes:**

- `idx_nguoidung_hoten`: Tìm kiếm theo tên
- `idx_nguoidung_sdt`: Tìm kiếm theo số điện thoại
- `idx_nguoidung_vaitro`: Lọc theo vai trò

### 2. SIM

Bảng quản lý thông tin SIM trong hệ thống.

| Tên cột    | Kiểu dữ liệu  | Chú thích                           |
|------------|---------------|-------------------------------------|
| iccid      | VARCHAR(255)  | Khóa chính, mã định danh SIM        |
| msisdn     | VARCHAR(20)   | Số điện thoại, unique               |
| NhaMang    | ENUM          | 'Viettel', 'Mobiphone', 'Vinaphone' |
| GiaBan     | DECIMAL(10,2) | Giá bán SIM                         |
| LoaiSim    | ENUM          | 'NgoaiDia', 'TraTruoc', 'TraSau'    |
| TrangThai  | ENUM          | 'SanSang', 'DaBan', 'HoatDong'      |
| created_at | DATETIME      | Thời gian tạo                       |
| updated_at | DATETIME      | Thời gian cập nhật                  |

**Indexes:**

- `idx_sim_msisdn`: Tìm kiếm theo số điện thoại
- `idx_sim_nhamang`: Lọc theo nhà mạng
- `idx_sim_trangthai`: Lọc theo trạng thái

### 3. NhapSim

Bảng quản lý thông tin nhập SIM vào kho.

| Tên cột     | Kiểu dữ liệu       | Chú thích                           |
|-------------|--------------------|-------------------------------------|
| MaNhap      | INT AUTO_INCREMENT | Khóa chính                          |
| MaNV        | VARCHAR(255)       | Email nhân viên nhập SIM            |
| NgayNhap    | DATETIME           | Thời gian nhập                      |
| NhaCungCap  | VARCHAR(255)       | Tên nhà cung cấp                    |
| GhiChu      | TEXT               | Ghi chú nhập hàng                   |
| TongSoLuong | INT                | Tổng số SIM nhập (tự động cập nhật) |
| TongGiaNhap | DECIMAL(10,2)      | Tổng giá nhập (tự động cập nhật)    |

**Indexes:**

- `idx_nhapsim_ngaynhap`: Tìm kiếm theo ngày nhập

### 4. ChiTietNhapSim

Bảng lưu chi tiết các SIM nhập trong mỗi đợt nhập.

| Tên cột | Kiểu dữ liệu       | Chú thích                           |
|---------|--------------------|-------------------------------------|
| ID      | INT AUTO_INCREMENT | Khóa chính                          |
| MaNhap  | INT                | Mã đợt nhập, tham chiếu đến NhapSim |
| iccid   | VARCHAR(255)       | ICCID của SIM, tham chiếu đến SIM   |
| GiaNhap | DECIMAL(10,2)      | Giá nhập của SIM                    |

**Constraints:**

- UNIQUE KEY (`MaNhap`, `iccid`): Mỗi SIM chỉ được nhập một lần trong mỗi đợt

### 5. GioHang

Bảng lưu thông tin SIM trong giỏ hàng của khách hàng.

| Tên cột  | Kiểu dữ liệu       | Chú thích                   |
|----------|--------------------|-----------------------------|
| ID       | INT AUTO_INCREMENT | Khóa chính                  |
| MaKH     | VARCHAR(255)       | Email khách hàng            |
| iccid    | VARCHAR(255)       | ICCID của SIM               |
| NgayThem | DATETIME           | Thời gian thêm vào giỏ hàng |

**Constraints:**

- UNIQUE KEY (`MaKH`, `iccid`): Mỗi SIM chỉ xuất hiện một lần trong giỏ hàng của mỗi khách hàng

### 6. HoaDon

Bảng quản lý thông tin hóa đơn.

| Tên cột      | Kiểu dữ liệu  | Chú thích                                        |
|--------------|---------------|--------------------------------------------------|
| MaHD         | VARCHAR(20)   | Khóa chính, mã hóa đơn                           |
| MaKH         | VARCHAR(255)  | Email khách hàng                                 |
| MaNV         | VARCHAR(255)  | Email nhân viên xử lý đơn hàng                   |
| NgayTao      | DATETIME      | Thời gian tạo đơn hàng                           |
| NgayCapNhat  | DATETIME      | Thời gian cập nhật gần nhất                      |
| TongTien     | DECIMAL(10,2) | Tổng tiền hóa đơn                                |
| TrangThaiDon | ENUM          | 'ChoXacNhan', 'DangXuLy', 'DaHoanThanh', 'DaHuy' |
| GhiChu       | TEXT          | Ghi chú đơn hàng                                 |

**Indexes:**

- `idx_hoadon_makh`: Tìm kiếm theo khách hàng
- `idx_hoadon_manv`: Tìm kiếm theo nhân viên
- `idx_hoadon_ngaytao`: Tìm kiếm theo ngày tạo
- `idx_hoadon_trangthai`: Lọc theo trạng thái

### 7. HoaDonChiTiet

Bảng lưu chi tiết các SIM trong mỗi hóa đơn.

| Tên cột | Kiểu dữ liệu       | Chú thích                         |
|---------|--------------------|-----------------------------------|
| ID      | INT AUTO_INCREMENT | Khóa chính                        |
| MaHD    | VARCHAR(20)        | Mã hóa đơn, tham chiếu đến HoaDon |
| iccid   | VARCHAR(255)       | ICCID của SIM                     |
| GiaBan  | DECIMAL(10,2)      | Giá bán gốc                       |
| GiaCuoi | DECIMAL(10,2)      | Giá sau khuyến mãi                |

**Constraints:**

- UNIQUE KEY (`MaHD`, `iccid`): Mỗi SIM chỉ xuất hiện một lần trong một hóa đơn

### 8. ThongTinChuSIM

Bảng lưu thông tin chủ sở hữu của SIM.

| Tên cột  | Kiểu dữ liệu       | Chú thích             |
|----------|--------------------|-----------------------|
| ID       | INT AUTO_INCREMENT | Khóa chính            |
| iccid    | VARCHAR(255)       | ICCID của SIM         |
| HoTen    | VARCHAR(255)       | Tên chủ SIM           |
| CCCD     | VARCHAR(20)        | Số căn cước công dân  |
| NgayCap  | DATE               | Ngày cấp CCCD         |
| NgaySinh | DATE               | Ngày sinh             |
| DiaChi   | VARCHAR(255)       | Địa chỉ               |
| SDT      | VARCHAR(20)        | Số điện thoại liên hệ |
| MaHD     | VARCHAR(20)        | Mã hóa đơn mua SIM    |

**Constraints:**

- UNIQUE KEY (`iccid`): Mỗi SIM chỉ có một chủ sở hữu

**Indexes:**

- `idx_chusim_cccd`: Tìm kiếm theo CCCD
- `idx_chusim_hoten`: Tìm kiếm theo tên

### 9. UuDai

Bảng quản lý thông tin ưu đãi.

| Tên cột    | Kiểu dữ liệu  | Chú thích                               |
|------------|---------------|-----------------------------------------|
| MaUD       | VARCHAR(20)   | Khóa chính, mã ưu đãi                   |
| MoTa       | VARCHAR(255)  | Mô tả ưu đãi                            |
| GiaTriGiam | DECIMAL(10,2) | Giá trị giảm                            |
| LoaiGiam   | ENUM          | 'PhanTram', 'TienMat'                   |
| NgayBatDau | DATE          | Ngày bắt đầu hiệu lực                   |
| NgayHetHan | DATE          | Ngày hết hiệu lực                       |
| TrangThai  | BOOLEAN       | TRUE: còn hiệu lực, FALSE: hết hiệu lực |

**Constraints:**

- CHECK (NgayHetHan >= NgayBatDau)
- CHECK (GiaTriGiam > 0)

**Indexes:**

- `idx_uudai_thoigian`: Tìm kiếm theo thời gian hiệu lực
- `idx_uudai_trangthai`: Lọc theo trạng thái

### 10. ApDungUuDai

Bảng lưu thông tin áp dụng ưu đãi vào hóa đơn.

| Tên cột      | Kiểu dữ liệu       | Chú thích                 |
|--------------|--------------------|---------------------------|
| MaApDung     | INT AUTO_INCREMENT | Khóa chính                |
| MaKH         | VARCHAR(255)       | Email khách hàng          |
| MaHD         | VARCHAR(20)        | Mã hóa đơn                |
| MaUD         | VARCHAR(20)        | Mã ưu đãi                 |
| NgayApDung   | DATETIME           | Thời gian áp dụng         |
| GiaTriApDung | DECIMAL(10,2)      | Giá trị thực tế được giảm |

### 11. DanhGia

Bảng lưu đánh giá của khách hàng.

| Tên cột     | Kiểu dữ liệu       | Chú thích             |
|-------------|--------------------|-----------------------|
| ID          | INT AUTO_INCREMENT | Khóa chính            |
| MaHD        | VARCHAR(20)        | Mã hóa đơn            |
| Sao         | INT                | Số sao đánh giá (1-5) |
| NoiDung     | TEXT               | Nội dung đánh giá     |
| NgayDanhGia | DATETIME           | Thời gian đánh giá    |

**Constraints:**

- UNIQUE KEY (`MaHD`): Mỗi hóa đơn chỉ được đánh giá một lần
- CHECK (Sao BETWEEN 1 AND 5)

### 12. LichSuGiaoDich

Bảng lưu lịch sử giao dịch liên quan đến hóa đơn.

| Tên cột       | Kiểu dữ liệu       | Chú thích                                    |
|---------------|--------------------|----------------------------------------------|
| MaGiaoDich    | INT AUTO_INCREMENT | Khóa chính                                   |
| MaHD          | VARCHAR(20)        | Mã hóa đơn                                   |
| ThoiGian      | DATETIME           | Thời gian giao dịch                          |
| HanhDong      | ENUM               | 'TaoDon', 'ThanhToan', 'HuyDon', 'HoanThanh' |
| GhiChu        | TEXT               | Ghi chú giao dịch                            |
| NguoiThucHien | VARCHAR(255)       | Email người thực hiện                        |

**Indexes:**

- `idx_lichsu_mahd`: Tìm kiếm theo hóa đơn
- `idx_lichsu_thoigian`: Tìm kiếm theo thời gian
- `idx_lichsu_hanhdong`: Lọc theo hành động

### 13. ThongKeBanHang

Bảng lưu thống kê bán hàng theo ngày.

| Tên cột   | Kiểu dữ liệu  | Chú thích                         |
|-----------|---------------|-----------------------------------|
| Ngay      | DATE          | Khóa chính, ngày thống kê         |
| SoDonHang | INT           | Số đơn hàng hoàn thành trong ngày |
| DoanhThu  | DECIMAL(15,2) | Doanh thu trong ngày              |

## Mối quan hệ chi tiết

### NguoiDung - HoaDon (Khách hàng)

- **Kiểu quan hệ**: One-to-Many
- **Khóa ngoại**: HoaDon.MaKH → NguoiDung.Email
- **Mô tả**: Một người dùng (khách hàng) có thể có nhiều hóa đơn

### NguoiDung - HoaDon (Nhân viên)

- **Kiểu quan hệ**: One-to-Many
- **Khóa ngoại**: HoaDon.MaNV → NguoiDung.Email
- **Mô tả**: Một nhân viên có thể xử lý nhiều hóa đơn

### NguoiDung - GioHang

- **Kiểu quan hệ**: One-to-Many
- **Khóa ngoại**: GioHang.MaKH → NguoiDung.Email
- **Mô tả**: Một khách hàng có thể có nhiều SIM trong giỏ hàng

### SIM - GioHang

- **Kiểu quan hệ**: One-to-Many
- **Khóa ngoại**: GioHang.iccid → SIM.iccid
- **Mô tả**: Một SIM có thể được thêm vào giỏ hàng của nhiều khách hàng

### HoaDon - HoaDonChiTiet

- **Kiểu quan hệ**: One-to-Many
- **Khóa ngoại**: HoaDonChiTiet.MaHD → HoaDon.MaHD
- **Mô tả**: Một hóa đơn có thể có nhiều SIM

### SIM - HoaDonChiTiet

- **Kiểu quan hệ**: One-to-Many
- **Khóa ngoại**: HoaDonChiTiet.iccid → SIM.iccid
- **Mô tả**: Một SIM có thể xuất hiện trong nhiều hóa đơn khác nhau

### SIM - ThongTinChuSIM

- **Kiểu quan hệ**: One-to-One
- **Khóa ngoại**: ThongTinChuSIM.iccid → SIM.iccid
- **Mô tả**: Mỗi SIM chỉ có một chủ sở hữu

### HoaDon - DanhGia

- **Kiểu quan hệ**: One-to-One
- **Khóa ngoại**: DanhGia.MaHD → HoaDon.MaHD
- **Mô tả**: Mỗi hóa đơn chỉ có một đánh giá

### UuDai - ApDungUuDai

- **Kiểu quan hệ**: One-to-Many
- **Khóa ngoại**: ApDungUuDai.MaUD → UuDai.MaUD
- **Mô tả**: Một ưu đãi có thể được áp dụng cho nhiều hóa đơn

## Các ràng buộc đặc biệt

1. **Ràng buộc số sao đánh giá**
    - Bảng DanhGia có ràng buộc CHECK (Sao BETWEEN 1 AND 5)

2. **Ràng buộc thời gian ưu đãi**
    - Bảng UuDai có ràng buộc CHECK (NgayHetHan >= NgayBatDau)

3. **Ràng buộc giá trị**
    - Bảng SIM có ràng buộc CHECK (GiaBan > 0)
    - Bảng UuDai có ràng buộc CHECK (GiaTriGiam > 0)

4. **Ràng buộc tính duy nhất**
    - Mỗi SIM chỉ được thêm một lần vào giỏ hàng của mỗi khách hàng
    - Mỗi SIM chỉ xuất hiện một lần trong một hóa đơn
    - Mỗi hóa đơn chỉ được đánh giá một lần
    - Mỗi SIM chỉ có một chủ sở hữu

## Lưu ý khi phát triển

1. **Tham chiếu khóa ngoại**: Đảm bảo tham chiếu đúng trước khi thêm/xóa dữ liệu
2. **Sử dụng transaction**: Thực hiện các thao tác phức tạp trong transaction để đảm bảo tính toàn vẹn
3. **Sử dụng trigger**: Nhiều logic nghiệp vụ được xử lý bởi trigger, cần hiểu rõ trước khi phát triển
4. **Phân trang dữ liệu lớn**: Sử dụng LIMIT và OFFSET để phân trang khi truy vấn nhiều dữ liệu