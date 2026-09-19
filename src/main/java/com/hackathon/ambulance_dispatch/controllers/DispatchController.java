package com.hackathon.ambulance_dispatch.controllers;

import com.hackathon.ambulance_dispatch.models.Request;
import com.hackathon.ambulance_dispatch.repositories.RequestRepository;
import com.hackathon.ambulance_dispatch.services.BedrockTriageService;
import com.hackathon.ambulance_dispatch.services.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {
    private final RequestRepository requestRepository;
    private final BedrockTriageService bedrockTriageService;
    private final DispatchService dispatchService;

    public DispatchController(RequestRepository requestRepository,
                            BedrockTriageService bedrockTriageService,
                            DispatchService dispatchService) {
        this.requestRepository = requestRepository;
        this.bedrockTriageService = bedrockTriageService;
        this.dispatchService = dispatchService;
    }

    @PostMapping("/emergency")
    public ResponseEntity<Request> reportEmergency(@RequestBody EmergencyRequestDto dto) {
        Map<String, String> triageResult = bedrockTriageService.triageEmergency(dto.description());

        Request request = Request.builder()
                .id(UUID.randomUUID().toString())
                .emergencyDescription(dto.description())
                .severity(triageResult.get("severity"))
                .requiredAmbulanceType(triageResult.get("ambulanceType"))
                .pickupLat(dto.lat())
                .pickupLng(dto.lng())
                .status("REQUESTED")
                .createdAt(System.currentTimeMillis())
                .build();

        requestRepository.save(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> acceptRequest(@RequestBody AcceptRequestDto dto) {
        boolean assigned = dispatchService.assignDriver(dto.requestId(), dto.driverId());

        if (assigned) {
            return ResponseEntity.ok(Map.of(
                    "message", "Emergency assigned successfully",
                    "status", "ACCEPTED"
            ));
        } else {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "Request already claimed by another driver",
                    "status", "FAILED"
            ));
        }
    }

    public record EmergencyRequestDto(String description, double lat, double lng) {}

    public record AcceptRequestDto(String requestId, String driverId) {}
}
