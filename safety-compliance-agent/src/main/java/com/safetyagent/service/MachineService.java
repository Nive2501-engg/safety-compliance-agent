package com.safetyagent.service;

import com.safetyagent.model.Machine;
import com.safetyagent.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineService {

    @Autowired
    private MachineRepository machineRepository;

    public Machine createMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    public List<Machine> getMachinesByCompany(String companyName) {
        return machineRepository.findByCompanyName(companyName);
    }
}