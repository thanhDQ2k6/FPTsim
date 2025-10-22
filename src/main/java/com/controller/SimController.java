package com.controller;

import com.repository.NhapSimRepository;
import com.service.SimService;
import com.web.dto.ImportSimsRequest;
import com.web.dto.NhapSimSummaryView;
import com.web.dto.SimCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard/sims")
@RequiredArgsConstructor
public class SimController {

    private final SimService simService;
    private final NhapSimRepository nhapSimRepo;

    @ModelAttribute("batch")
    public ImportSimsRequest initBatch() {
        return new ImportSimsRequest(); // sims đã non-null nhờ DTO
    }

    // Lịch sử phiếu nhập (header) cho cả GET và POST (PRG vẫn được)
    @ModelAttribute
    public void addImportHeaderHistory(Model model,
                                       @RequestParam(name = "hPage", defaultValue = "0") int hPage,
                                       @RequestParam(name = "hSize", defaultValue = "10") int hSize) {
        // Sắp xếp: mới nhất trước
        Sort sort = Sort.by(Sort.Direction.DESC, "ngayNhap").and(Sort.by(Sort.Direction.DESC, "id"));
        Page<NhapSimSummaryView> headers = nhapSimRepo.findSummary(PageRequest.of(hPage, hSize, sort));
        model.addAttribute("importHeaders", headers);
        model.addAttribute("hPage", hPage);
        model.addAttribute("hSize", hSize);
    }

    @GetMapping("/import")
    public String importForm(Model model,
                             @ModelAttribute("batch") ImportSimsRequest batch) {
        if (batch.getSims().isEmpty()) {
            batch.getSims().add(new SimCreateRequest()); // Thêm một SIM trống nếu chưa có
        }
        model.addAttribute("batch", batch);
        return "/views/dashboard/simsImport";
    }

    @PostMapping("/import")
    public String importBatch(@ModelAttribute("batch") ImportSimsRequest batch,
                              RedirectAttributes ra) {
        simService.importSimsBatch(batch);
        ra.addFlashAttribute("success", "Đã nhập lô SIM thành công.");
        return "redirect:/dashboard/sims/import";
    }
    
    @GetMapping("/manage")
    public String manageSims(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String nhaMang,
                            @RequestParam(required = false) String loaiSim,
                            @RequestParam(required = false) String trangThai,
                            Model model,
                            jakarta.servlet.http.HttpSession session) {
        // Get user from session
        Object user = session.getAttribute("user");
        if (!(user instanceof com.model.NguoiDung nguoiDung)) {
            return "redirect:/";
        }
        
        // Check authorization
        boolean isAdmin = nguoiDung.getVaiTro() == com.model.NguoiDung.VaiTro.Admin;
        boolean isStaff = nguoiDung.getVaiTro() == com.model.NguoiDung.VaiTro.NhanVien;
        
        if (!isAdmin && !isStaff) {
            return "redirect:/";
        }
        
        // Build sort with multiple criteria: import date desc, then by filters
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (nhaMang != null && !nhaMang.isBlank()) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "nhaMang"));
        }
        if (loaiSim != null && !loaiSim.isBlank()) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "loaiSim"));
        }
        if (trangThai != null && !trangThai.isBlank()) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "trangThai"));
        }
        
        PageRequest pageable = PageRequest.of(page, size, sort);
        
        Page<com.model.Sim> sims;
        // Apply filters based on parameters
        boolean hasFilters = (nhaMang != null && !nhaMang.isBlank()) || 
                            (loaiSim != null && !loaiSim.isBlank()) || 
                            (trangThai != null && !trangThai.isBlank());
        
        if (isAdmin) {
            // Admin sees all SIMs with optional filters
            if (hasFilters) {
                sims = simService.getFilteredSims(nhaMang, loaiSim, trangThai, pageable);
            } else {
                sims = simService.getAllSims(pageable);
            }
        } else {
            // Staff sees only SIMs they imported with optional filters
            if (hasFilters) {
                sims = simService.getFilteredSimsByStaff(nguoiDung.getEmail(), nhaMang, loaiSim, trangThai, pageable);
            } else {
                sims = simService.getSimsByStaff(nguoiDung.getEmail(), pageable);
            }
        }
        
        model.addAttribute("sims", sims);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sims.getTotalPages());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("nhaMang", nhaMang);
        model.addAttribute("loaiSim", loaiSim);
        model.addAttribute("trangThai", trangThai);
        
        return "views/dashboard/simsManage";
    }
    
    @GetMapping("/{iccid}")
    public String simDetails(@PathVariable String iccid, Model model) {
        com.model.Sim sim = simService.getSimById(iccid);
        model.addAttribute("sim", sim);
        return "views/dashboard/simDetails";
    }
    
    @PostMapping("/{iccid}/update")
    public String updateSim(@PathVariable String iccid,
                           @ModelAttribute com.web.dto.SimUpdateRequest request,
                           RedirectAttributes ra) {
        try {
            simService.updateSimIfReady(iccid, request);
            ra.addFlashAttribute("success", "SIM updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/sims/manage";
    }
    
    @PostMapping("/{iccid}/deactivate")
    public String deactivateSim(@PathVariable String iccid, RedirectAttributes ra) {
        try {
            simService.deactivateSim(iccid);
            ra.addFlashAttribute("success", "SIM deactivated");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/sims/manage";
    }
}