package com.hackathon.ambulance_dispatch.services;

import com.hackathon.ambulance_dispatch.models.Ambulance;
import com.hackathon.ambulance_dispatch.repositories.AmbulanceRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService {
    private final AmbulanceRepository ambulanceRepository;

    public MatchingService(AmbulanceRepository ambulanceRepository) {
        this.ambulanceRepository = ambulanceRepository;
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

    public double calculateEtaMinutes(double distanceKm) {
        final double AVERAGE_SPEED_KMH = 40.0;
        return (distanceKm / AVERAGE_SPEED_KMH) * 60;
    }

    public List<Ambulance> findRankedAmbulances(double patientLat, double patientLng, String requiredType) {
        List<Ambulance> availableAmbulances = ambulanceRepository.findAvailableAmbulances();

        List<Ambulance> filtered = availableAmbulances.stream()
                .filter(ambulance -> {
                    if ("ADVANCED".equals(requiredType)) {
                        return "ADVANCED".equals(ambulance.getType());
                    }
                    return true;
                })
                .sorted(Comparator.comparingDouble(ambulance ->
                        calculateDistance(patientLat, patientLng, ambulance.getCurrentLat(), ambulance.getCurrentLng())
                ))
                .collect(Collectors.toList());

        return filtered;
    }
}
