package com.hackathon.ambulance_dispatch.config;

import com.hackathon.ambulance_dispatch.models.Ambulance;
import com.hackathon.ambulance_dispatch.models.Hospital;
import com.hackathon.ambulance_dispatch.repositories.AmbulanceRepository;
import com.hackathon.ambulance_dispatch.repositories.HospitalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class DataSeeder implements CommandLineRunner {
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;

    public DataSeeder(AmbulanceRepository ambulanceRepository, HospitalRepository hospitalRepository) {
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (ambulanceRepository.findAvailableAmbulances().isEmpty()) {
            seedAmbulances();
        }

        if (hospitalRepository.findAll().isEmpty()) {
            seedHospitals();
        }

        System.out.println("Database seeding complete.");
    }

    private void seedAmbulances() {
        Ambulance amb01 = Ambulance.builder()
                .id("Amb-01")
                .vehicleNumber("AMB-001")
                .type("BASIC")
                .currentLat(26.4520)
                .currentLng(80.3350)
                .isAvailable(true)
                .build();

        Ambulance amb02 = Ambulance.builder()
                .id("Amb-02")
                .vehicleNumber("AMB-002")
                .type("ADVANCED")
                .currentLat(26.4600)
                .currentLng(80.3200)
                .isAvailable(true)
                .build();

        Ambulance amb03 = Ambulance.builder()
                .id("Amb-03")
                .vehicleNumber("AMB-003")
                .type("ADVANCED")
                .currentLat(26.4400)
                .currentLng(80.3400)
                .isAvailable(true)
                .build();

        Ambulance amb04 = Ambulance.builder()
                .id("Amb-04")
                .vehicleNumber("AMB-004")
                .type("BASIC")
                .currentLat(26.4700)
                .currentLng(80.3100)
                .isAvailable(false)
                .build();

        ambulanceRepository.save(amb01);
        ambulanceRepository.save(amb02);
        ambulanceRepository.save(amb03);
        ambulanceRepository.save(amb04);

        System.out.println("Ambulances seeded successfully.");
    }

    private void seedHospitals() {
        Hospital hosp01 = Hospital.builder()
                .id("Hosp-01")
                .name("City Emergency Trauma Center")
                .latitude(26.4650)
                .longitude(80.3450)
                .availableBeds(12)
                .build();

        Hospital hosp02 = Hospital.builder()
                .id("Hosp-02")
                .name("Metro Life Hospital")
                .latitude(26.4380)
                .longitude(80.3150)
                .availableBeds(5)
                .build();

        hospitalRepository.save(hosp01);
        hospitalRepository.save(hosp02);

        System.out.println("Hospitals seeded successfully.");
    }
}
