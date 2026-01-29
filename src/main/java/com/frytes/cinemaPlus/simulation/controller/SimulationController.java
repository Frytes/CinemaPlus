package com.frytes.cinemaPlus.simulation.controller;

import com.frytes.cinemaPlus.simulation.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/start")
    public ResponseEntity<String> startSimulation() {
        simulationService.start();
        return ResponseEntity.ok("Simulation started 🚀");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopSimulation() {
        simulationService.stop();
        return ResponseEntity.ok("Simulation stopping... (New bots won't start) 🛑");
    }
}