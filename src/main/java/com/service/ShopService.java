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
     * Get available SIMs for shop with multi-sort and pagination.
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
        
        // Always sort by created date desc first
        orders.add(Sort.Order.desc("createdAt"));
        
        // Add additional sort
        if ("price".equals(sortBy)) {
            orders.add(Sort.Order.asc("giaBan"));
        } else if ("price-desc".equals(sortBy)) {
            orders.add(Sort.Order.desc("giaBan"));
        }
        
        // Add multi-sort for filtering
        if (nhaMang != null && !nhaMang.isBlank()) {
            orders.add(Sort.Order.asc("nhaMang"));
        }
        if (loaiSim != null && !loaiSim.isBlank()) {
            orders.add(Sort.Order.asc("loaiSim"));
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        
        // For now, just get all available SIMs
        // TODO: Add specification for dynamic filtering
        return simRepository.findByTrangThai(Sim.TrangThai.SanSang, pageable);
    }
    
    /**
     * Get SIM details for shop.
     */
    @Transactional(readOnly = true)
    public Sim getSimDetails(String iccid) {
        return simRepository.findById(iccid).orElse(null);
    }
}
