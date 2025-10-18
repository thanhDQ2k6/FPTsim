package com.repository;

import com.model.NguoiDung;
import org.springframework.data.repository.CrudRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface NguoiDungRepository extends CrudRepository<NguoiDung, String> {
    NguoiDung findByEmail(String email);
}