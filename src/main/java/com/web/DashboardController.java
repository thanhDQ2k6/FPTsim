package com.web;

import com.model.NguoiDung;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("")
    public String home(HttpSession session, Model model) {
        Object principal = session != null ? session.getAttribute("user") : null;
        if (!(principal instanceof NguoiDung user)) {
            return "redirect:/";
        }
        if (user.getVaiTro() == NguoiDung.VaiTro.KhachHang) {
            return "redirect:/";
        }
        return "views/dashboard/home";
    }
}


