package com.repository;

import com.model.Sim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimRepository extends JpaRepository<Sim, String> {
}