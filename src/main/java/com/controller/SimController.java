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
}