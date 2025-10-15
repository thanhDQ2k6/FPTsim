package com.service;

import com.model.Sim;
import com.repository.SimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimService {

    @Autowired
    private SimRepository simRepository;

    public List<Sim> findAll() {
        return simRepository.findAll();
    }

    // đổi tên cho khớp với Repository
    public Sim findById(String iccid) {
        return simRepository.findByIccid(iccid);
    }

    public Sim save(Sim sim) {
        return simRepository.save(sim);
    }

    // đổi tên cho khớp với Repository
    public void deleteById(String iccid) {
        simRepository.deleteByIccid(iccid);
    }
}
