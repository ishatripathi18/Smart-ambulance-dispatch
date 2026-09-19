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
public class Hospital {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private int availableBeds;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
