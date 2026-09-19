package com.hackathon.ambulance_dispatch.controllers;

import com.hackathon.ambulance_dispatch.models.Ambulance;
import com.hackathon.ambulance_dispatch.models.Request;
import com.hackathon.ambulance_dispatch.repositories.RequestRepository;
import com.hackathon.ambulance_dispatch.services.BedrockTriageService;
import com.hackathon.ambulance_dispatch.services.CascadeDispatchService;
import com.hackathon.ambulance_dispatch.services.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestRepository requestRepository;
    private final BedrockTriageService bedrockTriageService;
    private final MatchingService matchingService;
    private final CascadeDispatchService cascadeDispatchService;

    public RequestController(RequestRepository requestRepository,
                            BedrockTriageService bedrockTriageService,
                            MatchingService matchingService,
                            CascadeDispatchService cascadeDispatchService) {
        this.requestRepository = requestRepository;
        this.bedrockTriageService = bedrockTriageService;
        this.matchingService = matchingService;
        this.cascadeDispatchService = cascadeDispatchService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createRequest(@RequestBody CreateRequestDto dto) {
        Map<String, String> triageResult = bedrockTriageService.triageEmergency(dto.description());

        Request request = Request.builder()
                .id(UUID.randomUUID().toString())
                .emergencyDescription(dto.description())
                .severity(triageResult.get("severity"))
                .requiredAmbulanceType(triageResult.get("ambulanceType"))
                .pickupLat(dto.pickupLat())
                .pickupLng(dto.pickupLng())
                .status("REQUESTED")
                .createdAt(System.currentTimeMillis())
                .build();

        requestRepository.save(request);

        String requiredAmbulanceType = triageResult.get("ambulanceType");
        List<Ambulance> rankedAmbulances = matchingService.findRankedAmbulances(
                dto.pickupLat(),
                dto.pickupLng(),
                requiredAmbulanceType
        );

        List<String> rankedIds = rankedAmbulances.stream()
                .map(Ambulance::getId)
                .collect(Collectors.toList());

        cascadeDispatchService.startCascade(request.getId(), rankedIds);

        return ResponseEntity.ok(Map.of(
                "request", request,
                "rankedAmbulanceCount", rankedIds.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Request> getRequest(@PathVariable String id) {
        Request request = requestRepository.findById(id);
        if (request != null) {
            return ResponseEntity.ok(request);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public record CreateRequestDto(String description, double pickupLat, double pickupLng) {}
}
