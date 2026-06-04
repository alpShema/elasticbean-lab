package spring_eb_app;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Service
public class DynamoDBService {

    private final DynamoDbClient client;
    private final String tableName;

    public DynamoDBService() {
        // Region and table name come from Elastic Beanstalk environment variables
        String region = System.getenv("AWS_REGION") != null
                ? System.getenv("AWS_REGION") : "us-east-1";

        this.tableName = System.getenv("DYNAMODB_TABLE") != null
                ? System.getenv("DYNAMODB_TABLE") : "eb-app-visitors";

        this.client = DynamoDbClient.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * Write a visitor record to DynamoDB
     */
    public String recordVisit(String visitorId) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("visitorId", AttributeValue.builder().s(visitorId).build());
            item.put("timestamp", AttributeValue.builder().s(new Date().toString()).build());
            item.put("message", AttributeValue.builder().s("Visited from Elastic Beanstalk").build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            client.putItem(request);
            return "Visit recorded for: " + visitorId;

        } catch (Exception e) {
            return "DynamoDB error: " + e.getMessage();
        }
    }

    /**
     * Read all visitor records from DynamoDB
     */
    public List<Map<String, String>> getVisitors() {
        List<Map<String, String>> results = new ArrayList<>();
        try {
            ScanRequest request = ScanRequest.builder()
                    .tableName(tableName)
                    .limit(10)
                    .build();

            ScanResponse response = client.scan(request);

            for (Map<String, AttributeValue> item : response.items()) {
                Map<String, String> record = new HashMap<>();
                item.forEach((key, val) -> record.put(key, val.s()));
                results.add(record);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            results.add(error);
        }
        return results;
    }

    public String getTableName() {
        return tableName;
    }
}
