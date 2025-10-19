package com.controller;

import com.model.UuDai;
import com.service.UuDaiService;
import com.web.dto.UuDaiCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/uudai")
@RequiredArgsConstructor
public class UuDaiController {

    private final UuDaiService uuDaiService;

    @ModelAttribute("req")
    public UuDaiCreateRequest createRequest() {
        return new UuDaiCreateRequest();
    }

    @GetMapping
    public String index(Model model) {
        List<UuDai> list = uuDaiService.listAll();
        model.addAttribute("uudais", list);
        return "/views/uudai/index";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute("req") UuDaiCreateRequest req,
                         RedirectAttributes ra) {
        uuDaiService.create(req);
        ra.addFlashAttribute("success", "Đã tạo ưu đãi mới.");
        return "redirect:/uudai";
    }

    @PostMapping("/{maUD}/deactivate")
    public String deactivate(@PathVariable String maUD,
                             RedirectAttributes ra) {
        uuDaiService.deactivate(maUD);
        ra.addFlashAttribute("success", "Đã ngừng kích hoạt ưu đãi: " + maUD);
        return "redirect:/uudai";
    }
}