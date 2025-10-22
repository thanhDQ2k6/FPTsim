package com.controller;

import com.model.NguoiDung;
import com.model.UuDai;
import com.service.UuDaiService;
import com.web.dto.UuDaiCreateRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for managing discounts/promotions (UuDai).
 * Admin-only access for creating and managing promotional discounts.
 */
@Controller
@RequestMapping("/dashboard/discounts")
@RequiredArgsConstructor
public class UuDaiController {

    private final UuDaiService uuDaiService;

    /**
     * Check if user is admin
     */
    private boolean isAdmin(HttpSession session) {
        Object principal = session.getAttribute("user");
        if (!(principal instanceof NguoiDung user)) {
            return false;
        }
        return user.getVaiTro() == NguoiDung.VaiTro.Admin;
    }

    @ModelAttribute("req")
    public UuDaiCreateRequest createRequest() {
        return new UuDaiCreateRequest();
    }

    /**
     * Display discount management page (Admin only)
     */
    @GetMapping
    public String index(Model model, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "Access denied. Admin only.");
            return "redirect:/dashboard";
        }
        
        List<UuDai> list = uuDaiService.listAll();
        model.addAttribute("uudais", list);
        return "views/dashboard/discounts";
    }

    /**
     * Create new discount (Admin only)
     */
    @PostMapping("/new")
    public String create(@ModelAttribute("req") UuDaiCreateRequest req,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "Access denied. Admin only.");
            return "redirect:/dashboard";
        }
        
        uuDaiService.create(req);
        ra.addFlashAttribute("success", "Discount created successfully!");
        return "redirect:/dashboard/discounts";
    }

    /**
     * Deactivate discount (Admin only)
     */
    @PostMapping("/{maUD}/deactivate")
    public String deactivate(@PathVariable String maUD,
                             HttpSession session,
                             RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "Access denied. Admin only.");
            return "redirect:/dashboard";
        }
        
        uuDaiService.deactivate(maUD);
        ra.addFlashAttribute("success", "Discount deactivated: " + maUD);
        return "redirect:/dashboard/discounts";
    }
}