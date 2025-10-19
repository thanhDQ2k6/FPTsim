package com.service;

import com.exception.BusinessException;
import com.model.NguoiDung;
import com.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NguoiDungRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<NguoiDung> authenticate(String email, String rawPassword) {
        return userRepository.findById(email)
                .filter(user -> {
                    String stored = user.getPassword();
                    return stored != null && stored.equals(rawPassword);
                });
    }

    @Transactional
    public NguoiDung registerCustomer(String email,
                                      String rawPassword,
                                      String hoTen,
                                      String sdt,
                                      LocalDate ngaySinh,
                                      String diaChi) {
        if (userRepository.existsById(email)) {
            throw new BusinessException("EMAIL_EXISTS", "Email đã tồn tại");
        }
        NguoiDung user = new NguoiDung();
        user.setEmail(email);
        user.setPassword(rawPassword);
        user.setHoTen(hoTen);
        user.setSdt(sdt);
        user.setNgaySinh(ngaySinh);
        user.setDiaChi(diaChi);
        user.setVaiTro(NguoiDung.VaiTro.KhachHang);
        return userRepository.save(user);
    }
}


