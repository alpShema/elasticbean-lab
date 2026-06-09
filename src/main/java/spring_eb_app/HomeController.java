package spring_eb_app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class HomeController {

    // App version — change this to prove a new deployment went live
    private static final String APP_VERSION = "1.0.0";

    @Autowired
    private DynamoDBService dynamoDBService;

    /**
     * GET /
     * Home endpoint — confirms the app is live and shows version
     */
    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "running");
        response.put("message", "this is the lab app deployed on AWS Elastic Beanstalk");
        response.put("version", APP_VERSION);
        response.put("timestamp", new Date().toString());
        response.put("platform", "Elastic Beanstalk + Java 17");
        return response;
    }

    /**
     * GET /health
     * Health check endpoint — Elastic Beanstalk uses this to verify the app is up
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "healthy");
        response.put("version", APP_VERSION);
        return response;
    }

    /**
     * GET /data
     * Reads visitor records from DynamoDB — proves external service integration
     */
    @GetMapping("/data")
    public Map<String, Object> getData() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("source", "DynamoDB");
        response.put("table", dynamoDBService.getTableName());
        response.put("visitors", dynamoDBService.getVisitors());
        return response;
    }

    /**
     * POST /data?id=someVisitorId
     * Writes a visitor record to DynamoDB
     */
    @PostMapping("/data")
    public Map<String, String> postData(@RequestParam(defaultValue = "anonymous") String id) {
        Map<String, String> response = new LinkedHashMap<>();
        String result = dynamoDBService.recordVisit(id);
        response.put("result", result);
        response.put("version", APP_VERSION);
        return response;
    }
}
