package com.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AuthModelAdvice {
    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user") != null;
    }
}
