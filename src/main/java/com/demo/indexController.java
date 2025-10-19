package com.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class indexController {
    @RequestMapping("")
    public String index() {
        return "views/index";
    }
}
