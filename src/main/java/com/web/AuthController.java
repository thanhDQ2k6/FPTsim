package com.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.model.NguoiDung;
import com.repository.NguoiDungRepository;

@Controller
@RequestMapping
public class AuthController {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @GetMapping("/signin")
    public String signin(HttpSession session) {
        // This method will be called when the user navigates to /signin,
        // but the actual sign-in form is in a modal on the index page.
        // So, we redirect to the index page.
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate session to log out
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signup() {
        // This method will be called when the user navigates to /signup,
        // but the actual sign-up form is in a modal on the index page.
        // So, we redirect to the index page.
        return "redirect:/";
    }

    @PostMapping("/signin")
    @ResponseBody
    public java.util.Map<String, Object> signin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        // Validate cơ bản
        if (email == null || email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            result.put("success", false);
            result.put("message", "Email không hợp lệ!");
            return result;
        }
        if (password == null || password.isEmpty()) {
            result.put("success", false);
            result.put("message", "Mật khẩu không được để trống!");
            return result;
        }
        NguoiDung user = nguoiDungRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
            session.setAttribute("role", user.getVaiTro());
            result.put("success", true);
            result.put("message", "Đăng nhập thành công!");
            result.put("role", user.getVaiTro().name());
        } else {
            result.put("success", false);
            result.put("message", "Sai email hoặc mật khẩu!");
        }
        return result;
    }

    @PostMapping("/signup")
    @ResponseBody
    public java.util.Map<String, Object> signup(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam String hoTen,
                        @RequestParam(required = false) String sdt,
                        @RequestParam(required = false) String ngaySinh,
                        @RequestParam(required = false) String diaChi,
                        HttpSession session) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        // Validate cơ bản
        if (email == null || email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            result.put("success", false);
            result.put("message", "Email không hợp lệ!");
            return result;
        }
        if (password == null || password.isEmpty()) {
            result.put("success", false);
            result.put("message", "Mật khẩu không được để trống!");
            return result;
        }
        if (nguoiDungRepository.existsById(email)) {
            result.put("success", false);
            result.put("message", "Email đã tồn tại!");
            return result;
        }
        NguoiDung user = new NguoiDung();
        user.setEmail(email);
        user.setPassword(password);
        user.setHoTen(hoTen);
        user.setSdt(sdt);
        user.setDiaChi(diaChi);
        if (ngaySinh != null && !ngaySinh.isEmpty()) {
            user.setNgaySinh(java.time.LocalDate.parse(ngaySinh));
        }
        user.setVaiTro(NguoiDung.VaiTro.KhachHang);
        nguoiDungRepository.save(user);
        result.put("success", true);
        result.put("message", "Đăng ký thành công!");
        result.put("redirect", "/?signupSuccess=true"); // Redirect to index with success parameter
        return result;
    }
}
