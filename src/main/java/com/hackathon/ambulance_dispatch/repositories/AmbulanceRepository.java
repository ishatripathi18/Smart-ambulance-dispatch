package com.hackathon.ambulance_dispatch.repositories;

import com.hackathon.ambulance_dispatch.models.Ambulance;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AmbulanceRepository {
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbTable<Ambulance> ambulanceTable;

    public AmbulanceRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbClient dynamoDbClient) {
        this.enhancedClient = enhancedClient;
        this.dynamoDbClient = dynamoDbClient;
        this.ambulanceTable = enhancedClient.table("Ambulances", TableSchema.fromClass(Ambulance.class));
    }

    public void save(Ambulance ambulance) {
        ambulanceTable.putItem(ambulance);
    }

    public Ambulance findById(String id) {
        return ambulanceTable.getItem(Key.builder().partitionValue(id).build());
    }

    public List<Ambulance> findAvailableAmbulances() {
        List<Ambulance> availableAmbulances = new ArrayList<>();
        ambulanceTable.scan()
                .items()
                .forEach(ambulance -> {
                    if (ambulance.isAvailable()) {
                        availableAmbulances.add(ambulance);
                    }
                });
        return availableAmbulances;
    }

    public void updateAvailability(String ambulanceId, boolean isAvailable) {
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("Ambulances")
                .key(Map.of("id", AttributeValue.builder().s(ambulanceId).build()))
                .updateExpression("SET #isAvailable = :isAvailable")
                .expressionAttributeNames(Map.of("#isAvailable", "isAvailable"))
                .expressionAttributeValues(Map.of(
                        ":isAvailable", AttributeValue.builder().bool(isAvailable).build()
                ))
                .build();

        dynamoDbClient.updateItem(updateRequest);
    }

    public void updateLocation(String ambulanceId, double lat, double lng) {
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("Ambulances")
                .key(Map.of("id", AttributeValue.builder().s(ambulanceId).build()))
                .updateExpression("SET #currentLat = :lat, #currentLng = :lng")
                .expressionAttributeNames(Map.of(
                        "#currentLat", "currentLat",
                        "#currentLng", "currentLng"
                ))
                .expressionAttributeValues(Map.of(
                        ":lat", AttributeValue.builder().n(String.valueOf(lat)).build(),
                        ":lng", AttributeValue.builder().n(String.valueOf(lng)).build()
                ))
                .build();

        dynamoDbClient.updateItem(updateRequest);
    }
}
