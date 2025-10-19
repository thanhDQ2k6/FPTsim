package com.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormController {
    @GetMapping("/signin")
    public String showForm() {
        return "forms/signIn";
    }

    @GetMapping("/signup")
    public String showSignUpForm() {
        return "forms/signUp";
    }
}
