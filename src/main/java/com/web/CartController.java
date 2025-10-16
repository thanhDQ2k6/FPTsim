package com.web;

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
                new CartItem("Hart Hagerty", "United States", "Zemlak, Daniel and Leannon", "Desktop Support Technician", "Purple", false),
                new CartItem("Brice Swyre", "Canada", "Yost, Kerluke and Wunsch", "Software Engineer", "Blue", false),
                new CartItem("Marvin McKinney", "United Kingdom", "Robel-Cormier", "Product Manager", "Green", false),
                new CartItem("Jerome Bell", "Australia", "D'Amore and Sons", "UX Designer", "Red", false)
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
}