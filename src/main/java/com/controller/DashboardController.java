package com.controller;

import com.model.NguoiDung;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for dashboard pages accessible to staff and admin users.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @GetMapping("")
    public String dashboard(HttpSession session) {
        if (!isStaffOrAdmin(session)) {
            return "redirect:/";
        }
        return "views/dashboard/home";
    }

    /**
     * Check if the current user is staff or admin.
     */
    private boolean isStaffOrAdmin(HttpSession session) {
        Object principal = session != null ? session.getAttribute("user") : null;
        if (!(principal instanceof NguoiDung user)) {
            return false;
        }
        return user.getVaiTro() == NguoiDung.VaiTro.Admin 
            || user.getVaiTro() == NguoiDung.VaiTro.NhanVien;
    }
}
