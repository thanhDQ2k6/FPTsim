package com.controller;

import com.model.NguoiDung;
import com.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller handling user authentication (sign in, sign up, sign out).
 */
@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @GetMapping("/signin")
    public String showSignInForm() {
        return "forms/signIn";
    }

    @PostMapping("/signin")
    public String signIn(@RequestParam String email,
                         @RequestParam String password,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Optional<NguoiDung> user = authService.authenticate(email, password);
        if (user.isPresent()) {
            session.setAttribute("user", user.get());
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/signin";
        }
    }

    @GetMapping("/signup")
    public String showSignUpForm() {
        return "forms/signUp";
    }

    @PostMapping("/signup")
    public String signUp(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(required = false) String fullName,
                        @RequestParam(required = false) String phone,
                        @RequestParam(required = false) String dob,
                        @RequestParam(required = false) String address,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            LocalDate dateOfBirth = (dob != null && !dob.isBlank()) ? LocalDate.parse(dob) : null;
            NguoiDung user = authService.registerCustomer(
                email, 
                password, 
                fullName != null ? fullName : email, 
                phone, 
                dateOfBirth, 
                address
            );
            session.setAttribute("user", user);
            redirectAttributes.addFlashAttribute("success", "Account created successfully!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
