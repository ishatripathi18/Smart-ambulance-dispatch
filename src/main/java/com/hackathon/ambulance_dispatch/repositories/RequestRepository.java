package com.hackathon.ambulance_dispatch.repositories;

import com.hackathon.ambulance_dispatch.models.Request;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.Update;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RequestRepository {
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbTable<Request> requestTable;

    public RequestRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbClient dynamoDbClient) {
        this.enhancedClient = enhancedClient;
        this.dynamoDbClient = dynamoDbClient;
        this.requestTable = enhancedClient.table("Requests", TableSchema.fromClass(Request.class));
    }

    public void save(Request request) {
        requestTable.putItem(request);
    }

    public Request findById(String id) {
        return requestTable.getItem(id);
    }

    public List<Request> findAllPending() {
        List<Request> pendingRequests = new ArrayList<>();
        requestTable.scan()
                .items()
                .forEach(request -> {
                    if ("REQUESTED".equals(request.getStatus())) {
                        pendingRequests.add(request);
                    }
                });
        return pendingRequests;
    }

    public boolean lockRequestForDriver(String requestId, String driverId) {
        try {
            UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                    .tableName("Requests")
                    .key(java.util.Map.of("id", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                            .s(requestId)
                            .build()))
                    .updateExpression("SET #status = :newStatus, #assignedDriverId = :driverId")
                    .expressionAttributeNames(java.util.Map.of(
                            "#status", "status",
                            "#assignedDriverId", "assignedDriverId"
                    ))
                    .expressionAttributeValues(java.util.Map.of(
                            ":currentStatus", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                                    .s("REQUESTED")
                                    .build(),
                            ":newStatus", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                                    .s("ACCEPTED")
                                    .build(),
                            ":driverId", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                                    .s(driverId)
                                    .build()
                    ))
                    .conditionExpression("#status = :currentStatus")
                    .build();

            dynamoDbClient.updateItem(updateRequest);
            return true;
        } catch (ConditionalCheckFailedException e) {
            return false;
        }
    }
}
