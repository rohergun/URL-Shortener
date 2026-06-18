package io.github.rohergun.urlshortener.shortener;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class UrlMapping {
    private String shortCode;
    private String longUrl;
    private long createdAt;
    private long expiresAt;

    @DynamoDbPartitionKey
    public String getShortCode() {
        return shortCode;
    }
}
