# Tổng quan hệ thống CSDL - FPT SIM

## Giới thiệu

Hệ thống cơ sở dữ liệu FPT SIM được thiết kế để quản lý việc kinh doanh SIM điện thoại di động, bao gồm các chức năng
chính:

- Quản lý người dùng (khách hàng, nhân viên, admin)
- Quản lý kho SIM (nhập, xuất, theo dõi trạng thái)
- Quản lý giỏ hàng và đơn hàng
- Quản lý ưu đãi và khuyến mãi
- Thống kê báo cáo doanh thu

## Kiến trúc CSDL

Cơ sở dữ liệu được thiết kế theo nguyên tắc chuẩn hóa (3NF), tối ưu cho các thao tác CRUD và truy vấn phổ biến. Hệ thống
CSDL được chia thành các nhóm chức năng chính:

1. **Quản lý người dùng**
    - Bảng: `NguoiDung`

2. **Quản lý SIM**
    - Bảng: `SIM`, `ThongTinChuSIM`

3. **Quản lý nhập hàng**
    - Bảng: `NhapSim`, `ChiTietNhapSim`

4. **Quản lý giỏ hàng và đơn hàng**
    - Bảng: `GioHang`, `HoaDon`, `HoaDonChiTiet`

5. **Quản lý ưu đãi**
    - Bảng: `UuDai`, `ApDungUuDai`

6. **Ghi lịch sử và đánh giá**
    - Bảng: `LichSuGiaoDich`, `DanhGia`

7. **Thống kê báo cáo**
    - Bảng: `ThongKeBanHang`

## Đặc điểm kỹ thuật

- **Hệ quản trị CSDL**: MySQL
- **Charset**: utf8mb4 (hỗ trợ đầy đủ Unicode)
- **Engine**: InnoDB (hỗ trợ transactions và foreign keys)
- **Bảo mật**: Password được lưu dưới dạng đã hash
- **Tự động hóa**: Sử dụng triggers và stored procedures

## Tính năng nổi bật

1. **Tự động chuyển SIM từ giỏ hàng sang đơn hàng**
    - Khi tạo đơn hàng mới, hệ thống tự động chuyển SIM từ giỏ hàng sang chi tiết đơn hàng

2. **Quản lý trạng thái SIM**
    - Tự động cập nhật trạng thái SIM (SanSang, DaBan, HoatDong) theo các thao tác nghiệp vụ

3. **Luồng trạng thái đơn hàng**
    - Kiểm tra và đảm bảo luồng trạng thái đơn hàng hợp lệ (ChoXacNhan → DangXuLy → DaHoanThanh/DaHuy)

4. **Lịch sử giao dịch**
    - Tự động ghi lại lịch sử thay đổi trạng thái đơn hàng

5. **Tìm kiếm SIM thông minh**
    - Hỗ trợ tìm kiếm SIM theo nhiều tiêu chí, bao gồm tìm số đẹp

6. **Thống kê báo cáo**
    - Hỗ trợ thống kê doanh thu, lợi nhuận theo ngày/tháng/năm

## Tổng quan mối quan hệ giữa các thực thể - FPT SIM

### 1. Nhóm Người dùng

- **NguoiDung**
    - Là khách hàng của → HoaDon
    - Là nhân viên xử lý → HoaDon
    - Sở hữu → GioHang
    - Thực hiện → NhapSim
    - Thực hiện → LichSuGiaoDich
    - Được áp dụng → ApDungUuDai

### 2. Nhóm SIM

- **SIM**
    - Được thêm vào → GioHang
    - Được bán trong → HoaDonChiTiet
    - Được nhập trong → ChiTietNhapSim
    - Có thông tin → ThongTinChuSIM

### 3. Nhóm Hóa đơn

- **HoaDon**
    - Chứa → HoaDonChiTiet
    - Có → LichSuGiaoDich
    - Áp dụng → ApDungUuDai
    - Được đánh giá → DanhGia
    - Liên kết với → ThongTinChuSIM

### 4. Nhóm Nhập hàng

- **NhapSim**
    - Bao gồm → ChiTietNhapSim

### 5. Nhóm Ưu đãi

- **UuDai**
    - Được sử dụng trong → ApDungUuDai

### Luồng dữ liệu cơ bản

1. **Khách hàng đặt hàng**:

   NguoiDung → GioHang → HoaDon → HoaDonChiTiet → SIM (cập nhật trạng thái) → ThongTinChuSIM

2. **Nhập SIM vào kho**:

   NguoiDung (nhân viên) → NhapSim → ChiTietNhapSim → SIM (thêm mới)

3. **Áp dụng ưu đãi**:

   UuDai → ApDungUuDai → HoaDon (cập nhật tổng tiền)

4. **Đánh giá đơn hàng**:

   NguoiDung → HoaDon → DanhGia

## Hướng phát triển tiếp theo

- **Tích hợp phân quyền chi tiết**: Phân quyền đến từng chức năng nghiệp vụ
- **Dashboard thống kê**: Thêm các stored procedure để tạo báo cáo thống kê nâng cao
- **Tự động gửi thông báo**: Tích hợp với hệ thống thông báo khi có sự kiện quan trọng
- **API tích hợp**: Cung cấp API để tích hợp với hệ thống khác