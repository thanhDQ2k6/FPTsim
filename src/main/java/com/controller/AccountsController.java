package com.controller;

import com.exception.BusinessException;
import com.model.NguoiDung;
import com.repository.NguoiDungRepository;
import com.web.dto.AccountView;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
                       @RequestParam(value = "size", defaultValue = "10") int size) {
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
        return "views/dashboard/accounts";
    }

    /**
     * Show create new account form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        if (!isStaff(session)) return "redirect:/";
        return "views/dashboard/accountCreate";
    }

    /**
     * Create new account
     */
    @PostMapping("/create")
    public String createAccount(@RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String role,
                               @RequestParam(required = false) String fullName,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String dob,
                               @RequestParam(required = false) String address,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (!isStaff(session)) return "redirect:/";
        
        // Check if email already exists
        if (userRepo.existsById(email)) {
            ra.addFlashAttribute("error", "Email already exists");
            return "redirect:/dashboard/accounts/new";
        }
        
        try {
            NguoiDung user = new NguoiDung();
            user.setEmail(email);
            user.setPassword(password);
            user.setHoTen(fullName != null ? fullName : email);
            user.setSdt(phone);
            if (dob != null && !dob.isBlank()) {
                user.setNgaySinh(LocalDate.parse(dob));
            }
            user.setDiaChi(address);
            user.setVaiTro(parseRole(role));
            
            userRepo.save(user);
            ra.addFlashAttribute("success", "Account created successfully");
            return "redirect:/dashboard/accounts";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create account: " + e.getMessage());
            return "redirect:/dashboard/accounts/new";
        }
    }

    /**
     * Show edit account form
     */
    @GetMapping("/{email}/edit")
    public String showEditForm(@PathVariable("email") String email, Model model, HttpSession session, RedirectAttributes ra) {
        if (!isStaff(session)) return "redirect:/";
        
        NguoiDung user = userRepo.findById(email).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Account not found");
            return "redirect:/dashboard/accounts";
        }
        
        model.addAttribute("account", user);
        return "views/dashboard/accountEdit";
    }

    /**
     * Update account
     */
    @PostMapping("/{email}/edit")
    public String updateAccount(@PathVariable("email") String email,
                               @RequestParam(required = false) String password,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false) String fullName,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String dob,
                               @RequestParam(required = false) String address,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (!isStaff(session)) return "redirect:/";
        
        NguoiDung user = userRepo.findById(email).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Account not found");
            return "redirect:/dashboard/accounts";
        }
        
        try {
            // Update fields
            if (password != null && !password.isBlank()) {
                user.setPassword(password);
            }
            if (fullName != null) {
                user.setHoTen(fullName);
            }
            if (phone != null) {
                user.setSdt(phone);
            }
            if (dob != null && !dob.isBlank()) {
                user.setNgaySinh(LocalDate.parse(dob));
            }
            if (address != null) {
                user.setDiaChi(address);
            }
            if (role != null && !role.isBlank()) {
                user.setVaiTro(parseRole(role));
            }
            
            userRepo.save(user);
            ra.addFlashAttribute("success", "Account updated successfully");
            return "redirect:/dashboard/accounts";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update account: " + e.getMessage());
            return "redirect:/dashboard/accounts/" + email + "/edit";
        }
    }

    /**
     * Delete account
     */
    @PostMapping("/{email}/delete")
    public String deleteAccount(@PathVariable("email") String email,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (!isStaff(session)) return "redirect:/";
        
        // Prevent deleting currently logged-in account
        Object principal = session.getAttribute("user");
        if (principal instanceof NguoiDung current && email.equalsIgnoreCase(current.getEmail())) {
            ra.addFlashAttribute("error", "Cannot delete your own account");
            return "redirect:/dashboard/accounts/" + email + "/edit";
        }
        
        try {
            userRepo.deleteById(email);
            ra.addFlashAttribute("success", "Account deleted successfully");
            return "redirect:/dashboard/accounts";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to delete account: " + e.getMessage());
            return "redirect:/dashboard/accounts/" + email + "/edit";
        }
    }

    private boolean isStaff(HttpSession session) {
        Object principal = session != null ? session.getAttribute("user") : null;
        if (!(principal instanceof NguoiDung user)) return false;
        return user.getVaiTro() == NguoiDung.VaiTro.Admin || user.getVaiTro() == NguoiDung.VaiTro.NhanVien;
    }

    private NguoiDung.VaiTro parseRole(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> NguoiDung.VaiTro.Admin;
            case "staff", "nhanvien" -> NguoiDung.VaiTro.NhanVien;
            case "user", "khachhang", "customer" -> NguoiDung.VaiTro.KhachHang;
            default -> NguoiDung.VaiTro.KhachHang;
        };
    }
}

