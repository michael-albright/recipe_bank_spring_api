package com.ms.albright.recipe_bank.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class AwsAccessUtil {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${bucket.name}")
    private String bucketName;

    @Value("${bucket.recipe.key}")
    private String bucketPrefix;

    private final String dynamoDbTableName = "Recipes";


    public void s3Upload(String objectKey, String objectContent, InputStream inputStream) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(bucketPrefix + objectKey)
                .build();
            // Upload the file to S3 using the InputStream directly
            s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, objectContent.length()));
    }

    public String s3getObjectContent(String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(bucketPrefix + objectKey)
                .build();

        return s3Client.getObjectAsBytes(request).asUtf8String();
    }

    // Dynamo Db Access
    public void dynamoSaveItem(Map<String, AttributeValue> item) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(dynamoDbTableName)
                .item(item)
                .build();

        // Save the item in DynamoDB
        dynamoDbClient.putItem(request);
    }

    public List<Map<String, AttributeValue>> dynamoGetAllItems() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(dynamoDbTableName)
                .build();

        return dynamoDbClient.scan(scanRequest).items();
    }
}
