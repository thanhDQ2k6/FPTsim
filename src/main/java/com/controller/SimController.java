package com.controller;

import com.service.SimService;
import com.web.dto.ImportSimsRequest;
import com.web.dto.SimCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sim")
@RequiredArgsConstructor
public class SimController {

    private final SimService simService;

    @ModelAttribute("batch")
    public ImportSimsRequest initBatch() {
        return new ImportSimsRequest(); // sims đã non-null nhờ DTO
    }

    @GetMapping("/import")
    public String importForm(Model model,
                             @ModelAttribute("batch") ImportSimsRequest batch) {
        if (batch.getSims().isEmpty()) {
            batch.getSims().add(new SimCreateRequest()); // Thêm một SIM trống nếu chưa có
        }
        model.addAttribute("batch", batch);
        return "/views/sim/import";
    }

    @PostMapping("/import")
    public String importBatch(@ModelAttribute("batch") ImportSimsRequest batch,
                              RedirectAttributes ra) {
        simService.importSimsBatch(batch);
        ra.addFlashAttribute("success", "Đã nhập lô SIM thành công.");
        return "redirect:/sim/import";
    }
}