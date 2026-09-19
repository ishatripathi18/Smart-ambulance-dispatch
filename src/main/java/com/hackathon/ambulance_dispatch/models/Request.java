package com.hackathon.ambulance_dispatch.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Request {
    private String id;
    private String emergencyDescription;
    private String severity;
    private String requiredAmbulanceType;
    private double pickupLat;
    private double pickupLng;
    private String status;
    private String assignedDriverId;
    private String assignedHospitalId;
    private long createdAt;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
