package com.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
public class EmployeeAuthController {

    @PostMapping("/signin")
    public String signinPost(@RequestParam String username, @RequestParam String password, HttpSession session) {
        // Demo: simple check, if username and password are not empty, log in as employee.
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            session.setAttribute("employee", username);
            return "redirect:/dashboard";
        } else {
            return "redirect:/dashboard?error";
        }
    }

    @GetMapping("/signout")
    public String signout(HttpSession session) {
        // Invalidate session to log out employee
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/dashboard";
    }
}