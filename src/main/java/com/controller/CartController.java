package com.controller;

import com.model.CartItem;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for shopping cart functionality.
 * Currently uses session-based cart storage for demonstration purposes.
 * In production, this should be replaced with database-backed cart using GioHang entity.
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    @GetMapping("")
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> items = getCartItems(session);
        model.addAttribute("cartItems", items);
        return "views/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam("index") int index, HttpSession session) {
        List<CartItem> items = getCartItems(session);
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            session.setAttribute("cartItems", items);
        }
        return "redirect:/cart";
    }

    @PostMapping("/deleteSelected")
    public String deleteSelected(@RequestParam("indices") List<Integer> indices, HttpSession session) {
        List<CartItem> items = getCartItems(session);
        // Sort indices in descending order to remove from end first
        indices.sort((a, b) -> b - a);
        for (int index : indices) {
            if (index >= 0 && index < items.size()) {
                items.remove(index);
            }
        }
        session.setAttribute("cartItems", items);
        return "redirect:/cart";
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCartItems(HttpSession session) {
        List<CartItem> items = (List<CartItem>) session.getAttribute("cartItems");
        if (items == null) {
            items = new ArrayList<>();
            session.setAttribute("cartItems", items);
        }
        return items;
    }
}
