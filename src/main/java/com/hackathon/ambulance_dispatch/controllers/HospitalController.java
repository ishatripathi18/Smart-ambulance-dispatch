package com.hackathon.ambulance_dispatch.controllers;

import com.hackathon.ambulance_dispatch.models.Hospital;
import com.hackathon.ambulance_dispatch.models.Request;
import com.hackathon.ambulance_dispatch.repositories.HospitalRepository;
import com.hackathon.ambulance_dispatch.repositories.RequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/hospitals")
public class HospitalController {
    private final HospitalRepository hospitalRepository;
    private final RequestRepository requestRepository;

    public HospitalController(HospitalRepository hospitalRepository, RequestRepository requestRepository) {
        this.hospitalRepository = hospitalRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping("/{id}/incoming")
    public ResponseEntity<List<Request>> getIncomingRequests(@PathVariable String id) {
        List<Request> incomingRequests = new ArrayList<>();
        
        List<Request> allPendingRequests = requestRepository.findAllPending();
        
        for (Request request : allPendingRequests) {
            if ("ACCEPTED".equals(request.getStatus()) || "EN_ROUTE".equals(request.getStatus())) {
                incomingRequests.add(request);
            }
        }

        return ResponseEntity.ok(incomingRequests);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Hospital> updateHospitalStatus(@PathVariable String id, @RequestBody Map<String, Boolean> payload) {
        Boolean bedReady = payload.getOrDefault("bedReady", false);
        Boolean teamNotified = payload.getOrDefault("teamNotified", false);

        hospitalRepository.updateBedStatus(id, bedReady, teamNotified);

        Hospital hospital = hospitalRepository.findById(id);
        if (hospital != null) {
            return ResponseEntity.ok(hospital);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getHospital(@PathVariable String id) {
        Hospital hospital = hospitalRepository.findById(id);
        if (hospital != null) {
            return ResponseEntity.ok(hospital);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
