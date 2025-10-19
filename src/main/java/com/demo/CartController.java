package com.demo;

import com.model.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private List<CartItem> demoItems() {
        return new ArrayList<>(Arrays.asList(
                new CartItem("0123456789", "Viettel", "prepaid", false),
                new CartItem("0987654321", "Mobifone", "postpaid", false),
                new CartItem("0111111111", "Vinaphone", "domestic", false),
                new CartItem("0222222222", "Vietnamobile", "foreign", false)
        ));
    }

    @GetMapping("")
    public String cart(Model model, HttpSession session) {
        // initialize session-backed cart once for demo purposes
        List<CartItem> items = (List<CartItem>) session.getAttribute("cartItems");
        if (items == null) {
            items = demoItems();
            session.setAttribute("cartItems", items);
        }
        model.addAttribute("cartItems", items);
        return "views/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("index") int index, HttpSession session) {
        List<CartItem> items = (List<CartItem>) session.getAttribute("cartItems");
        if (items != null) {
            if (index >= 0 && index < items.size()) {
                items.remove(index);
                // update session attribute (not strictly required for same list instance)
                session.setAttribute("cartItems", items);
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/deleteSelected")
    public String deleteSelected(@RequestParam("indices") List<Integer> indices, HttpSession session) {
        List<CartItem> items = (List<CartItem>) session.getAttribute("cartItems");
        if (items != null) {
            // Sort indices descending to remove from end
            indices.sort((a, b) -> b - a);
            for (int index : indices) {
                if (index >= 0 && index < items.size()) {
                    items.remove(index);
                }
            }
            session.setAttribute("cartItems", items);
        }
        return "redirect:/cart";
    }
}