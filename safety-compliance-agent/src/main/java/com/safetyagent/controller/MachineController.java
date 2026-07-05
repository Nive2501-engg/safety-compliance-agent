package com.safetyagent.controller;

import com.safetyagent.model.Machine;
import com.safetyagent.service.MachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    @Autowired
    private MachineService machineService;

    @PostMapping
    public Machine createMachine(@RequestBody Machine machine) {
        return machineService.createMachine(machine);
    }

    @GetMapping
    public List<Machine> getMachines(@RequestParam String companyName) {
        return machineService.getMachinesByCompany(companyName);
    }
}