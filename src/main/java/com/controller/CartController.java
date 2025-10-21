package com.controller;

import com.model.GioHang;
import com.model.NguoiDung;
import com.model.HoaDon;
import com.repository.GioHangRepository;
import com.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for shopping cart and checkout functionality.
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final GioHangRepository gioHangRepository;
    private final OrderService orderService;

    @GetMapping("")
    public String viewCart(Model model, HttpSession session) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung)) {
            return "redirect:/signin";
        }
        
        List<GioHang> cartItems = gioHangRepository.findByKhachHang_Email(nguoiDung.getEmail());
        model.addAttribute("cartItems", cartItems);
        return "views/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam int itemId, HttpSession session) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung)) {
            return "redirect:/signin";
        }
        
        gioHangRepository.deleteById(itemId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam String ownerName,
                          @RequestParam String ownerCccd,
                          @RequestParam String ownerDateOfBirth,
                          @RequestParam String ownerPhone,
                          @RequestParam String ownerAddress,
                          @RequestParam(required = false) String discountCode,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung)) {
            return "redirect:/signin";
        }
        
        try {
            HoaDon order = orderService.createOrderFromCart(
                nguoiDung.getEmail(), ownerName, ownerCccd, ownerDateOfBirth, 
                ownerPhone, ownerAddress, discountCode);
            
            redirectAttributes.addFlashAttribute("success", 
                "Order created successfully: " + order.getMaHD());
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}
