# TODO bảo mật bằng Interceptor + AuthService

1) AuthService
- login(email, rawPassword): kiểm tra BCrypt, trả thông tin user (VaiTro).
- logout(): xoá session attribute.
- getCurrentUser(HttpSession): đọc currentUser từ session.

2) Interceptor
- PreHandle: nếu path trong danh sách bảo vệ và chưa đăng nhập → redirect /login.
- Whitelist: /login, /register (tuỳ), /css/**, /js/**, /images/**, /public/**.
- Optional: kiểm tra VaiTro cho tuyến /admin/**, /staff/**.

3) Wiring
- @Configuration addInterceptor(…): đăng ký Interceptor + patterns.
- LoginController: GET/POST /login, set session attribute khi đăng nhập thành công, redirect trang trước.
- LogoutController: POST /logout, invalidate session.

4) Mật khẩu
- Lưu BCrypt (BCryptPasswordEncoder). Không so sánh plaintext.
- Khi migrate dữ liệu: hash lại mật khẩu hiện có.

5) Views
- login.html (form), layout hiển thị tên user, link Logout.
- error.html dùng chung (đã có).

6) Kiểm thử
- Không đăng nhập vào trang bảo vệ → bị redirect.
- Đăng nhập sai mật khẩu → thông báo hợp lệ (handler hoặc trả về view).
- Đăng nhập đúng → vào được route, có thể phân quyền VaiTro.