package com.controller;

import com.model.DanhGia;
import com.model.HoaDon;
import com.model.NguoiDung;
import com.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for order management - customer purchase history and staff bill processing.
 */
@Controller
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Customer purchase history page.
     */
    @GetMapping("/orders")
    public String customerOrders(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model,
                                 HttpSession session) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung) || 
            nguoiDung.getVaiTro() != NguoiDung.VaiTro.KhachHang) {
            return "redirect:/";
        }
        
        Page<HoaDon> orders = orderService.getCustomerOrders(nguoiDung.getEmail(), page, size);
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("isLoggedIn", true);
        
        return "views/orders";
    }
    
    /**
     * Order details page.
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetails(@PathVariable String orderId,
                              Model model,
                              HttpSession session) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung)) {
            return "redirect:/signin";
        }
        
        HoaDon order = orderService.getOrderDetails(orderId);
        model.addAttribute("order", order);
        model.addAttribute("isLoggedIn", true);
        
        return "views/orderDetails";
    }
    
    /**
     * Add rating to completed order.
     */
    @PostMapping("/orders/{orderId}/rate")
    public String addRating(@PathVariable String orderId,
                           @RequestParam int stars,
                           @RequestParam(required = false) String comment,
                           RedirectAttributes redirectAttributes) {
        try {
            orderService.addRating(orderId, stars, comment);
            redirectAttributes.addFlashAttribute("success", "Rating added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/orders/" + orderId;
    }
    
    /**
     * View all ratings page.
     */
    @GetMapping("/ratings")
    public String viewRatings(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model,
                             HttpSession session) {
        Page<DanhGia> ratings = orderService.getAllRatings(page, size);
        
        model.addAttribute("ratings", ratings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ratings.getTotalPages());
        
        // Check if user is logged in
        Object user = session.getAttribute("user");
        model.addAttribute("isLoggedIn", user instanceof NguoiDung);
        
        return "views/ratings";
    }
    
    /**
     * Staff bill processing page - shows next order in queue.
     */
    @GetMapping("/dashboard/bills/process")
    public String processBills(Model model, HttpSession session) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung) || 
            (nguoiDung.getVaiTro() != NguoiDung.VaiTro.NhanVien && 
             nguoiDung.getVaiTro() != NguoiDung.VaiTro.Admin)) {
            return "redirect:/";
        }
        
        HoaDon nextOrder = orderService.getNextPendingOrder();
        model.addAttribute("nextOrder", nextOrder);
        
        return "views/dashboard/billsProcess";
    }
    
    /**
     * Approve order (staff action).
     */
    @PostMapping("/dashboard/bills/{orderId}/approve")
    public String approveOrder(@PathVariable String orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung) || 
            (nguoiDung.getVaiTro() != NguoiDung.VaiTro.NhanVien && 
             nguoiDung.getVaiTro() != NguoiDung.VaiTro.Admin)) {
            return "redirect:/";
        }
        
        try {
            orderService.approveOrder(orderId, nguoiDung.getEmail());
            redirectAttributes.addFlashAttribute("success", "Order approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/dashboard/bills/process";
    }
    
    /**
     * Staff's processed orders history.
     */
    @GetMapping("/dashboard/bills/history")
    public String billsHistory(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model,
                              HttpSession session) {
        Object user = session.getAttribute("user");
        if (!(user instanceof NguoiDung nguoiDung) || 
            (nguoiDung.getVaiTro() != NguoiDung.VaiTro.NhanVien && 
             nguoiDung.getVaiTro() != NguoiDung.VaiTro.Admin)) {
            return "redirect:/";
        }
        
        Page<HoaDon> orders;
        if (nguoiDung.getVaiTro() == NguoiDung.VaiTro.Admin) {
            // Admin can see all
            orders = orderService.getStaffProcessedOrders(null, page, size);
        } else {
            // Staff only sees their own
            orders = orderService.getStaffProcessedOrders(nguoiDung.getEmail(), page, size);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        
        return "views/dashboard/billsHistory";
    }
}
