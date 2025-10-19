package com.controller;

import com.model.Sim;
import com.service.SimService;
import com.web.dto.SimCreateRequest;
import com.web.dto.SimUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/sim")
@RequiredArgsConstructor
public class SimController {

    private final SimService simService;

    @GetMapping("/new")
    public String newForm(Model model) {
        // Flash attribute "success" nếu có sẽ được Thymeleaf hiển thị
        return "/views/sim/new";
    }

    @PostMapping("/new")
    public String create(@RequestParam String iccid,
                         @RequestParam(required = false) String msisdn,
                         @RequestParam Sim.NhaMang nhaMang,
                         @RequestParam Sim.LoaiSim loaiSim,
                         @RequestParam BigDecimal giaBan,
                         @RequestParam BigDecimal giaNhap,
                         @RequestParam String nhaCungCap,
                         @RequestParam(required = false) String ghiChu,
                         RedirectAttributes ra) {
        var req = new SimCreateRequest();
        req.iccid = iccid;
        req.msisdn = msisdn;
        req.nhaMang = nhaMang;
        req.loaiSim = loaiSim;
        req.giaBan = giaBan;
        req.giaNhap = giaNhap;

        simService.createSimAndLogImport(req, nhaCungCap, ghiChu);

        // Đính kèm thông báo để hiển thị sau khi redirect (không bị resubmit)
        ra.addFlashAttribute("success", "Đã nhập SIM và ghi lịch sử nhập.");
        return "redirect:/sim/new"; // PRG: Redirect sang GET
    }

    @PostMapping("/{iccid}/update")
    public String update(@PathVariable String iccid,
                         @RequestParam(required = false) String msisdn,
                         @RequestParam(required = false) Sim.LoaiSim loaiSim,
                         @RequestParam(required = false) BigDecimal giaBan,
                         RedirectAttributes ra) {
        var req = new SimUpdateRequest();
        req.msisdn = msisdn;
        req.loaiSim = loaiSim;
        req.giaBan = giaBan;

        simService.updateSimIfReady(iccid, req);
        ra.addFlashAttribute("success", "Đã cập nhật SIM.");
        return "redirect:/sim/new"; // PRG
    }

    @PostMapping("/{iccid}/deactivate")
    @ResponseBody
    public String deactivate(@PathVariable String iccid) {
        simService.deactivateSim(iccid);
        return "OK";
    }
}