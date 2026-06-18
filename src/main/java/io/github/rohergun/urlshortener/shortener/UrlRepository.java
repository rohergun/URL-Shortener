package io.github.rohergun.urlshortener.shortener;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

import java.util.Optional;

@Repository
public class UrlRepository {
    private final DynamoDbTable<UrlMapping> table;

    public UrlRepository(DynamoDbEnhancedClient client, DynamoDbClient dynamoDbClient) {
        this.table = client.table("url_mappings", TableSchema.fromBean(UrlMapping.class));
        createTableIfNotExists(dynamoDbClient);
    }

    public void save(UrlMapping mapping) {
        table.putItem(mapping);
    }

    public Optional<UrlMapping> findByShortCode(String shortCode) {
        UrlMapping key = new UrlMapping();
        key.setShortCode(shortCode);
        return Optional.ofNullable(table.getItem(key));
    }

    private void createTableIfNotExists(DynamoDbClient client) {
        try {
            table.createTable(r -> r.provisionedThroughput(
                    ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build()
            ));
            client.waiter().waitUntilTableExists(b -> b.tableName("url_mappings"));
        } catch (ResourceInUseException ignored) {
            // table already exists, safe to continue
        }
    }
}
