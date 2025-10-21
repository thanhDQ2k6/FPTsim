package com.controller;

import com.model.NguoiDung;
import com.repository.NguoiDungRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the main landing page and public-facing views.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final NguoiDungRepository nguoiDungRepository;
    
    @GetMapping("/")
    public String index() {
        return "views/index";
    }
    
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung)) {
            redirectAttributes.addFlashAttribute("error", "Please sign in to view your profile");
            return "redirect:/signin";
        }
        
        // Refresh user data from database
        nguoiDung = nguoiDungRepository.findById(nguoiDung.getEmail()).orElse(nguoiDung);
        model.addAttribute("user", nguoiDung);
        return "views/profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String hoTen,
                               @RequestParam(required = false) String sdt,
                               @RequestParam(required = false) String ngaySinh,
                               @RequestParam(required = false) String diaChi,
                               @RequestParam(required = false) String newPassword,
                               @RequestParam(required = false) String confirmPassword,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung)) {
            redirectAttributes.addFlashAttribute("error", "Please sign in to update your profile");
            return "redirect:/signin";
        }
        
        try {
            // Get fresh data from database
            NguoiDung dbUser = nguoiDungRepository.findById(nguoiDung.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update basic info
            dbUser.setHoTen(hoTen);
            dbUser.setSdt(sdt);
            if (ngaySinh != null && !ngaySinh.isBlank()) {
                dbUser.setNgaySinh(java.time.LocalDate.parse(ngaySinh));
            }
            dbUser.setDiaChi(diaChi);
            
            // Update password if provided
            if (newPassword != null && !newPassword.isBlank()) {
                if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                    return "redirect:/profile";
                }
                dbUser.setPassword(newPassword); // In production, hash the password!
            }
            
            nguoiDungRepository.save(dbUser);
            
            // Update session
            session.setAttribute("user", dbUser);
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}
