package com.controller;

import com.model.GioHang;
import com.model.NguoiDung;
import com.model.Sim;
import com.repository.GioHangRepository;
import com.service.ShopService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Controller for customer shop - browsing and adding SIMs to cart.
 */
@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {
    
    private final ShopService shopService;
    private final GioHangRepository gioHangRepository;
    
    @GetMapping("")
    public String shop(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "12") int size,
                      @RequestParam(required = false) String nhaMang,
                      @RequestParam(required = false) String loaiSim,
                      @RequestParam(required = false) String sortBy,
                      Model model,
                      HttpSession session) {
        
        // Check if customer is logged in
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung) || 
            nguoiDung.getVaiTro() != NguoiDung.VaiTro.KhachHang) {
            return "redirect:/";
        }
        
        Page<Sim> sims = shopService.getAvailableSims(nhaMang, loaiSim, sortBy, page, size);
        
        model.addAttribute("sims", sims);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sims.getTotalPages());
        model.addAttribute("nhaMang", nhaMang);
        model.addAttribute("loaiSim", loaiSim);
        model.addAttribute("sortBy", sortBy);
        
        // Get cart count
        int cartCount = gioHangRepository.countByKhachHang_Email(nguoiDung.getEmail());
        model.addAttribute("cartCount", cartCount);
        
        // Add isLoggedIn for layout
        model.addAttribute("isLoggedIn", true);
        
        return "views/shop";
    }
    
    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam String iccid,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung) || 
            nguoiDung.getVaiTro() != NguoiDung.VaiTro.KhachHang) {
            return "redirect:/signin";
        }
        
        // Check if SIM exists and is available
        Sim sim = shopService.getSimDetails(iccid);
        if (sim == null || sim.getTrangThai() != Sim.TrangThai.SanSang) {
            redirectAttributes.addFlashAttribute("error", "SIM not available");
            return "redirect:/shop";
        }
        
        // Check if already in cart
        if (gioHangRepository.existsByKhachHang_EmailAndSim_Iccid(
                nguoiDung.getEmail(), iccid)) {
            redirectAttributes.addFlashAttribute("error", "SIM already in cart");
            return "redirect:/shop";
        }
        
        // Add to cart (no quantity field in GioHang model, each SIM is unique)
        GioHang cartItem = new GioHang();
        cartItem.setKhachHang(nguoiDung);
        cartItem.setSim(sim);
        cartItem.setNgayThem(LocalDateTime.now());
        gioHangRepository.save(cartItem);
        
        redirectAttributes.addFlashAttribute("success", "Added to cart");
        return "redirect:/shop";
    }
}
