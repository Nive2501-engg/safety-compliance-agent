package com.safetyagent.repository;

import com.safetyagent.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByCompanyName(String companyName);
}