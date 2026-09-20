package com.hackathon.ambulance_dispatch.services;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CascadeDispatchService {

    public void startCascade(String requestId, List<String> rankedAmbulanceIds) {
        System.out.println("Triggered cascade dispatch stub for request " + requestId + " with " + rankedAmbulanceIds.size() + " ambulances");
    }
}
