package com.repository;

import com.model.Sim;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimRepository extends CrudRepository<Sim, String> {
    List<Sim> findAll();
    Sim findByIccid(String iccid);
    void deleteByIccid(String iccid);
}
