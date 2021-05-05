package service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import com.google.gson.Gson;

import object.db.InboxRecordTable;
import object.InboxMessageRecord;
import object.ResponseMessage;
import object.RetrieveInboxRecordRequest;
import util.CommonUtil;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;

public class DynamoDBService {
    private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private static DynamoDB dynamoDB = new DynamoDB(client);
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

    public static ResponseMessage getInboxMessageRecord(RetrieveInboxRecordRequest request) {
        String tableIndexName = "TargetArnIndex";
        ArrayList<InboxMessageRecord> inboxMessageRecordArray = new ArrayList<InboxMessageRecord>();

        inboxMessageRecordArray = getInboxMessageRecord(inboxMessageRecordArray, request.getUserArn(), request.getPush_timestamp());
        for (String topic : request.getTopicName()) {
            String arn = new CommonUtil().getSnsTopicArn(topic);
            inboxMessageRecordArray = getInboxMessageRecord(inboxMessageRecordArray, arn, request.getPush_timestamp());
            logger.log("ArrayList: " + inboxMessageRecordArray.size());
        }

        return new ResponseMessage(200, new ResponseMessage.Message(inboxMessageRecordArray.toArray(new InboxMessageRecord[inboxMessageRecordArray.size()])));
    }

    private static ArrayList<InboxMessageRecord> getInboxMessageRecord(ArrayList<InboxMessageRecord> inboxMessageRecordArray, String arn, String push_timestamp) {
        String tableIndexName = "TargetArnIndex";
        Table table = dynamoDB.getTable("InboxRecordDBTable");
        Index index = table.getIndex(tableIndexName);

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();

        if ("TargetArnIndex".equals(tableIndexName)) {
            querySpec.withKeyConditionExpression("push_timestamp > :v1 AND targetArn = :v2")
                    .withValueMap(new ValueMap()
                            .withString(":v1", push_timestamp)
                            .withString(":v2", arn)
                    );
            items = index.query(querySpec);
            if (items == null) {
                logger.log("getInboxMessageRecord : item is null");
            } else {
                logger.log("getInboxMessageRecord : item is not null");
            }
        }

        List<InboxRecordTable> list = new ArrayList<InboxRecordTable>();
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            logger.log("GsonA: " + item.toJSON());
            InboxRecordTable tempTable = new Gson().fromJson(item.toJSON().toString(), InboxRecordTable.class);
            list.add(tempTable);
        }

        for (InboxRecordTable tempTable : list) {
            InboxMessageRecord record = new Gson().fromJson(tempTable.convertToJsonString(), InboxMessageRecord.class);
            record.setMessage(new Gson().fromJson(tempTable.getMessage().convertToJsonString(), InboxMessageRecord.class));
            inboxMessageRecordArray.add(record);
        }

        return inboxMessageRecordArray;
    }

}
