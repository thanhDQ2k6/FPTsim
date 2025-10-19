package com.web;

import com.model.NguoiDung;
import com.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "forms/signIn";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "forms/signUp";
    }

    @PostMapping("/signin")
    public String signin(@RequestParam("email") String email,
                         @RequestParam("password") String password,
                         HttpSession session) {
        System.out.println("Attempting login for email: " + email);
        return authService.authenticate(email, password)
                .map(user -> {
                    System.out.println("Login successful for user: " + user.getEmail() + " with role: " + user.getVaiTro());
                    session.setAttribute("user", user);
                    if (user.getVaiTro() == NguoiDung.VaiTro.NhanVien || user.getVaiTro() == NguoiDung.VaiTro.Admin) {
                        System.out.println("Redirecting to dashboard for role: " + user.getVaiTro());
                        return "redirect:/dashboard";
                    }
                    System.out.println("Redirecting to home for customer role");
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    System.out.println("Login failed for email: " + email);
                    return "redirect:/?error=invalid_credentials";
                });
    }

    @PostMapping("/signup")
    public String signup(@RequestParam("email") String email,
                         @RequestParam("password") String password,
                         @RequestParam(value = "fullName", required = false) String hoTen,
                         @RequestParam(value = "phone", required = false) String sdt,
                         @RequestParam(value = "dob", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngaySinh,
                         @RequestParam(value = "address", required = false) String diaChi,
                         HttpSession session) {
        NguoiDung user = authService.registerCustomer(email, password, hoTen, sdt, ngaySinh, diaChi);
        session.setAttribute("user", user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
