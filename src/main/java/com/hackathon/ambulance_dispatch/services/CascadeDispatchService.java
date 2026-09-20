package com.hackathon.ambulance_dispatch.services;

import com.hackathon.ambulance_dispatch.models.Request;
import com.hackathon.ambulance_dispatch.repositories.AmbulanceRepository;
import com.hackathon.ambulance_dispatch.repositories.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class CascadeDispatchService {
    private final RequestRepository requestRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final ScheduledExecutorService scheduler;

    public CascadeDispatchService(RequestRepository requestRepository, AmbulanceRepository ambulanceRepository) {
        this.requestRepository = requestRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    public void startCascade(String requestId, List<String> rankedAmbulanceIds) {
        if (rankedAmbulanceIds == null || rankedAmbulanceIds.isEmpty()) {
            System.out.println("Escalation fallback: No ambulances available for request " + requestId);
            return;
        }

        for (int i = 0; i < rankedAmbulanceIds.size(); i++) {
            final int index = i;
            final String ambulanceId = rankedAmbulanceIds.get(i);

            scheduler.schedule(() -> {
                Request request = requestRepository.findById(requestId);

                if (request == null || !"REQUESTED".equals(request.getStatus())) {
                    System.out.println("Cascade cancelled for request " + requestId + ": already claimed");
                    return;
                }

                System.out.println("Cascade ping " + (index + 1) + " for request " + requestId + " to ambulance " + ambulanceId);
            }, (long) i * 15, TimeUnit.SECONDS);
        }

        scheduler.schedule(() -> {
            Request request = requestRepository.findById(requestId);
            if (request != null && "REQUESTED".equals(request.getStatus())) {
                System.out.println("Escalation radius expansion: No driver accepted request " + requestId);
            }
        }, (long) rankedAmbulanceIds.size() * 15 + 5, TimeUnit.SECONDS);
    }
}

