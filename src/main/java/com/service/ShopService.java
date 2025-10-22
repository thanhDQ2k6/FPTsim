package com.service;

import com.model.Sim;
import com.repository.SimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for customer shop functionality - browsing and filtering SIMs.
 */
@Service
@RequiredArgsConstructor
public class ShopService {
    
    private final SimRepository simRepository;
    
    /**
     * Get available SIMs for shop with filtering and pagination.
     * @param nhaMang filter by provider (null for all)
     * @param loaiSim filter by type (null for all)
     * @param sortBy sort field (price, nice, etc.)
     * @param page page number
     * @param size page size
     * @return page of available SIMs
     */
    @Transactional(readOnly = true)
    public Page<Sim> getAvailableSims(String nhaMang, String loaiSim, 
                                       String sortBy, int page, int size) {
        List<Sort.Order> orders = new ArrayList<>();
        
        // Add sort based on user selection
        if ("price".equals(sortBy)) {
            orders.add(Sort.Order.asc("giaBan"));
        } else if ("price-desc".equals(sortBy)) {
            orders.add(Sort.Order.desc("giaBan"));
        } else {
            // Default: sort by created date desc
            orders.add(Sort.Order.desc("createdAt"));
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        
        // Apply filters - if both filters provided, both must match
        if (nhaMang != null && !nhaMang.isBlank() && loaiSim != null && !loaiSim.isBlank()) {
            // Filter by both provider and type
            try {
                Sim.NhaMang providerEnum = Sim.NhaMang.valueOf(nhaMang);
                Sim.LoaiSim typeEnum = Sim.LoaiSim.valueOf(loaiSim);
                return simRepository.findByTrangThaiAndNhaMangAndLoaiSim(
                    Sim.TrangThai.SanSang, providerEnum, typeEnum, pageable);
            } catch (IllegalArgumentException e) {
                // Invalid enum value, return empty page
                return Page.empty(pageable);
            }
        } else if (nhaMang != null && !nhaMang.isBlank()) {
            // Filter by provider only
            try {
                Sim.NhaMang providerEnum = Sim.NhaMang.valueOf(nhaMang);
                return simRepository.findByTrangThaiAndNhaMang(
                    Sim.TrangThai.SanSang, providerEnum, pageable);
            } catch (IllegalArgumentException e) {
                return Page.empty(pageable);
            }
        } else if (loaiSim != null && !loaiSim.isBlank()) {
            // Filter by type only
            try {
                Sim.LoaiSim typeEnum = Sim.LoaiSim.valueOf(loaiSim);
                return simRepository.findByTrangThaiAndLoaiSim(
                    Sim.TrangThai.SanSang, typeEnum, pageable);
            } catch (IllegalArgumentException e) {
                return Page.empty(pageable);
            }
        } else {
            // No filters, get all available SIMs
            return simRepository.findByTrangThai(Sim.TrangThai.SanSang, pageable);
        }
    }
    
    /**
     * Get SIM details for shop.
     */
    @Transactional(readOnly = true)
    public Sim getSimDetails(String iccid) {
        return simRepository.findById(iccid).orElse(null);
    }
}
