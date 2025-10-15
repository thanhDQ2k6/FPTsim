package com.controller;

import com.model.Sim;
import com.service.SimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// http://localhost:8080/sim/list

@Controller
@RequestMapping("/sim")
public class SimController {

    @Autowired
    private SimService simService;

    @GetMapping("/list")
    public String listSim(Model model) {
        model.addAttribute("list", simService.findAll());
        return "sim/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("sim", new Sim());
        return "sim/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("sim") Sim sim) {
        simService.save(sim);
        return "redirect:/sim/list";
    }

    @GetMapping("/edit/{iccid}")
    public String edit(@PathVariable("iccid") String iccid, Model model) {
        Sim sim = simService.findById(iccid);
        if (sim == null) {
            return "redirect:/sim/list";
        }
        model.addAttribute("sim", sim);
        return "sim/form";
    }

    @GetMapping("/delete/{iccid}")
    public String delete(@PathVariable("iccid") String iccid) {
        simService.deleteById(iccid);
        return "redirect:/sim/list";
    }
}
