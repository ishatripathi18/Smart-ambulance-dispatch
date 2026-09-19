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
public class Ambulance {
    private String id;
    private String vehicleNumber;
    private String type;
    private double currentLat;
    private double currentLng;
    private boolean isAvailable;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
