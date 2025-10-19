# Kiến trúc sau khi bỏ logic DB

## Database
- Giữ: bảng, PK/UK/FK/CHECK, index.
- Bỏ: tất cả TRIGGER/PROCEDURE/EVENT.
- Không còn SIGNAL/SQLSTATE tuỳ biến; lỗi DB phổ biến: 1062 (duplicate), 1452 (FK).

## Application
- Controller mỏng (Thymeleaf MVC), Service dày (@Transactional).
- Repository: CRUD + native query khi cần (search số đẹp, upsert thống kê, deactivate ưu đãi).
- Exception handling:
  - MVC: @ControllerAdvice → render templates/error.html với status phù hợp.
  - REST (nếu có): @RestControllerAdvice → JSON ApiError.
- Scheduler: @EnableScheduling + @Scheduled thay EVENT.

## Business flows
- CartService: kiểm tra SIM SanSang, xử lý duplicate qua UNIQUE.
- OrderService: tạo đơn từ giỏ, tính tổng, xoá giỏ; cập nhật trạng thái, ghi lịch sử, cập nhật thống kê khi hoàn thành.
- SimSearchService: native query tính rank_score, filter, pagination.
- UuDaiScheduler: deactivateExpired() mỗi đêm.

## Bảo mật (dự kiến)
- Interceptor: chặn route cần đăng nhập, whitelist /login, /public, static.
- AuthService: login/logout (BCrypt), load VaiTro, set session currentUser.
- Nâng cấp Spring Security khi cần.