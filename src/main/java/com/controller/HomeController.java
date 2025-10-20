package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the main landing page and public-facing views.
 */
@Controller
@RequestMapping("/")
public class HomeController {
    
    @GetMapping("")
    public String index() {
        return "views/index";
    }
}
