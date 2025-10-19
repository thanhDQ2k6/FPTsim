package com.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    @RequestMapping("")
    public String dashboard() {
        return "views/dashboard/home";
    }

    @RequestMapping("/accounts")
    public String accounts() {
        return "views/dashboard/accounts";
    }

    @RequestMapping("/sims/manage")
    public String simsManage() {
        return "views/dashboard/simsManage";
    }
}