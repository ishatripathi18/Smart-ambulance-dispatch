package com.hackathon.ambulance_dispatch.services;

import com.hackathon.ambulance_dispatch.models.Ambulance;
import com.hackathon.ambulance_dispatch.repositories.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DispatchService {
    private final RequestRepository requestRepository;

    public DispatchService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public List<Ambulance> rankAmbulancesByProximity(double patientLat, double patientLng, List<Ambulance> availableAmbulances) {
        List<Ambulance> rankedAmbulances = new ArrayList<>(availableAmbulances);
        
        rankedAmbulances.sort(Comparator.comparingDouble(ambulance ->
                calculateDistance(patientLat, patientLng, ambulance.getCurrentLat(), ambulance.getCurrentLng())
        ));

        return rankedAmbulances;
    }

    public boolean assignDriver(String requestId, String driverId) {
        return requestRepository.lockRequestForDriver(requestId, driverId);
    }
}
