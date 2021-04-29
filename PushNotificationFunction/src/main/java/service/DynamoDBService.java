package service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import object.ResponseMessage;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;

public class DynamoDBService {
    private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private static DynamoDBMapper mapper = null;
    private static LambdaLogger logger = getLogger();

    public static ResponseMessage insertData(Object table) {
        try {
            mapper = new DynamoDBMapper(client);
            mapper.save(table);
            logger.log("Store DB Complete.");
            return new ResponseMessage(200, null);
        } catch (AmazonDynamoDBException e) {
            System.err.println("Error running the DynamoDBMapperBatchWriteExample: " + e);
            e.printStackTrace();
            return new ResponseMessage(e.getStatusCode(), new ResponseMessage.Message(e.getErrorMessage(), e.getMessage()));
        }
    }

}
