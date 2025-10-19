package com.web;

import com.exception.BusinessException;
import com.model.NguoiDung;
import com.repository.NguoiDungRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.web.dto.AccountView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/dashboard/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final NguoiDungRepository userRepo;

    @GetMapping("")
    public String list(Model model,
                       HttpSession session,
                       @RequestParam(value = "role", required = false) String role,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @RequestParam(value = "activeTab", required = false) String activeTab) {
        if (!isStaff(session)) return "redirect:/";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "hoTen"));
        Page<NguoiDung> pageData;
        if (role != null && !role.isBlank()) {
            pageData = userRepo.findByVaiTro(parseRole(role), pageable);
        } else {
            pageData = userRepo.findAll(pageable);
        }
        List<AccountView> accounts = pageData.getContent().stream()
                .map(u -> new AccountView(
                        u.getEmail(),
                        u.getEmail(),
                        u.getHoTen(),
                        u.getSdt(),
                        u.getVaiTro() != null ? u.getVaiTro().name() : null
                ))
                .collect(Collectors.toList());
        model.addAttribute("accounts", accounts);
        model.addAttribute("page", pageData);
        model.addAttribute("roleFilter", role);
        model.addAttribute("activeTab", activeTab == null ? "list" : activeTab);
        return "views/dashboard/accounts";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") String email, Model model, HttpSession session) {
        if (!isStaff(session)) return "redirect:/";
        NguoiDung u = userRepo.findById(email).orElse(null);
        model.addAttribute("detail", u);
        return list(model, session, null, 0, 10, "detail");
    }

    @GetMapping("/new")
    public String newForm(Model model, HttpSession session) {
        if (!isStaff(session)) return "redirect:/";
        model.addAttribute("detail", new NguoiDung());
        return list(model, session, null, 0, 10, "detail");
    }

    @PostMapping("")
    public String upsert(@RequestParam String action,
                         @RequestParam String email,
                         @RequestParam(required = false) String password,
                         @RequestParam(value = "fullName", required = false) String hoTen,
                         @RequestParam(value = "phone", required = false) String sdt,
                         @RequestParam(value = "dob", required = false) String dob,
                         @RequestParam(value = "address", required = false) String diaChi,
                         @RequestParam(value = "role", required = false) String role,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (!isStaff(session)) return "redirect:/";
        if ("delete".equalsIgnoreCase(action)) {
            // Prevent deleting currently logged-in account
            Object principal = session.getAttribute("user");
            if (principal instanceof NguoiDung current && email.equalsIgnoreCase(current.getEmail())) {
                ra.addFlashAttribute("error", "Không thể xoá tài khoản đang đăng nhập");
                return "redirect:/dashboard/accounts?activeTab=detail";
            }
            userRepo.findById(email).ifPresent(userRepo::delete);
            return "redirect:/dashboard/accounts";
        }
        NguoiDung user = userRepo.findById(email).orElseGet(NguoiDung::new);
        user.setEmail(email);
        if (password != null && !password.isBlank()) {
            user.setPassword(password);
        } else if (user.getPassword() == null) {
            throw new BusinessException("PASSWORD_REQUIRED", "Thiếu mật khẩu cho tài khoản mới");
        }
        if (hoTen != null) user.setHoTen(hoTen);
        if (sdt != null) user.setSdt(sdt);
        if (dob != null && !dob.isBlank()) user.setNgaySinh(LocalDate.parse(dob));
        if (diaChi != null) user.setDiaChi(diaChi);
        if (role != null && !role.isBlank()) {
            user.setVaiTro(parseRole(role));
        }
        userRepo.save(user);
        return "redirect:/dashboard/accounts";
    }

    private boolean isStaff(HttpSession session) {
        Object principal = session != null ? session.getAttribute("user") : null;
        if (!(principal instanceof NguoiDung user)) return false;
        return user.getVaiTro() == NguoiDung.VaiTro.Admin || user.getVaiTro() == NguoiDung.VaiTro.NhanVien;
    }

    private NguoiDung.VaiTro parseRole(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> NguoiDung.VaiTro.Admin;
            case "staff" -> NguoiDung.VaiTro.NhanVien;
            case "user", "khachhang", "customer" -> NguoiDung.VaiTro.KhachHang;
            default -> NguoiDung.VaiTro.KhachHang;
        };
    }
}


