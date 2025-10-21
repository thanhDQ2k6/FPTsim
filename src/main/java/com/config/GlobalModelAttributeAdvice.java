package com.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to add common model attributes to all views.
 * Currently provides authentication status to all Thymeleaf templates.
 */
@ControllerAdvice
public class GlobalModelAttributeAdvice {
    
    /**
     * Adds authentication status to all views.
     * @param session the HTTP session
     * @return true if user is logged in, false otherwise
     */
    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user") != null;
    }
}
