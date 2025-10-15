package com.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class AuthController {

    @GetMapping("/signin")
    public String signin(HttpSession session) {
        // Demo: set a session attribute called "user" to mark the user as logged in.
        session.setAttribute("user", "demoUser");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate session to log out
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signup() {
        // Redirect to a signup page or handle signup logic; this is a placeholder.
        return "redirect:/signupPage";
    }
}
