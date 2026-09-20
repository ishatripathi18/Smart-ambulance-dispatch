package com.hackathon.ambulance_dispatch.repositories;

import com.hackathon.ambulance_dispatch.models.Hospital;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.ArrayList;
import java.util.List;

@Repository
public class HospitalRepository {
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Hospital> hospitalTable;

    public HospitalRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.hospitalTable = enhancedClient.table("Hospitals", TableSchema.fromClass(Hospital.class));
    }

    public void save(Hospital hospital) {
        hospitalTable.putItem(hospital);
    }

    public Hospital findById(String id) {
        return hospitalTable.getItem(Key.builder().partitionValue(id).build());
    }

    public List<Hospital> findAll() {
        List<Hospital> hospitals = new ArrayList<>();
        hospitalTable.scan()
                .items()
                .forEach(hospitals::add);
        return hospitals;
    }

    public void updateBedStatus(String hospitalId, boolean bedReady, boolean teamNotified) {
        Hospital hospital = findById(hospitalId);
        if (hospital != null) {
            hospital.setAvailableBeds(bedReady ? hospital.getAvailableBeds() - 1 : hospital.getAvailableBeds() + 1);
            save(hospital);
        }
    }
}
