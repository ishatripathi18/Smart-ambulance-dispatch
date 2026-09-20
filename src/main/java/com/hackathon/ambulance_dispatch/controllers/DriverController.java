package com.hackathon.ambulance_dispatch.controllers;

import com.hackathon.ambulance_dispatch.models.Request;
import com.hackathon.ambulance_dispatch.repositories.AmbulanceRepository;
import com.hackathon.ambulance_dispatch.repositories.HospitalRepository;
import com.hackathon.ambulance_dispatch.repositories.RequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/driver")
public class DriverController {
    private final RequestRepository requestRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;

    public DriverController(RequestRepository requestRepository,
                          AmbulanceRepository ambulanceRepository,
                          HospitalRepository hospitalRepository) {
        this.requestRepository = requestRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> acceptRequest(@RequestBody Map<String, String> payload) {
        String requestId = payload.get("requestId");
        String driverId = payload.get("driverId");
        String ambulanceId = payload.get("ambulanceId");

        boolean assigned = requestRepository.lockRequestForDriver(requestId, driverId);

        if (assigned) {
            ambulanceRepository.updateAvailability(ambulanceId, false);
            return ResponseEntity.ok(Map.of(
                    "message", "Request accepted successfully",
                    "status", "ACCEPTED"
            ));
        } else {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "Request already accepted by another driver",
                    "status", "FAILED"
            ));
        }
    }

    @PatchMapping("/status")
    public ResponseEntity<Map<String, String>> updateStatus(@RequestBody Map<String, String> payload) {
        String requestId = payload.get("requestId");
        String status = payload.get("status");

        if (!isValidStatus(status)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid status. Valid statuses are: EN_ROUTE, ARRIVED, COMPLETED",
                    "status", "FAILED"
            ));
        }

        requestRepository.updateStatus(requestId, status);
        return ResponseEntity.ok(Map.of(
                "message", "Status updated successfully",
                "status", status
        ));
    }

    @GetMapping("/active-trip/{driverId}")
    public ResponseEntity<?> getActiveTrip(@PathVariable String driverId) {
        Request activeRequest = requestRepository.findByAssignedDriverId(driverId);

        if (activeRequest != null) {
            return ResponseEntity.ok(activeRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isValidStatus(String status) {
        return "EN_ROUTE".equals(status) || "ARRIVED".equals(status) || "COMPLETED".equals(status);
    }
}
