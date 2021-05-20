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

import object.FailCaseLog;
import object.FunctionStatus;
import object.db.ApplicationPlatform;
import object.db.InboxRecord;
import object.InboxMessageRecord;
import object.ResponseMessage;
import object.db.SnsSubscriptions;
import object.request.RetrieveInboxRecordRequest;
import util.CommonUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;

public class DynamoDBService {
    private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private static DynamoDB dynamoDB = new DynamoDB(client);
    private static DynamoDBMapper mapper = null;
    private static LambdaLogger logger = getLogger();

    public static FunctionStatus saveData(ArrayList<Object> tableObj_List) {
        try {
            mapper = new DynamoDBMapper(client);
            mapper.batchSave(tableObj_List);
            logger.log("Store DB Complete.");
            return new FunctionStatus(true, null);
        } catch (AmazonDynamoDBException e) {
            FailCaseLog failCaseLog = new FailCaseLog(e.getStatusCode(), e.getErrorMessage(), e.getMessage());
            logger.log("\nError running the saveData batch: " + failCaseLog.convertToJsonString() + "\n");
            return new FunctionStatus(false, e.getStatusCode(), e.getErrorMessage(), e.getMessage());
        }
    }
    public static FunctionStatus saveData(Object tableObj) {
        try {
            mapper = new DynamoDBMapper(client);
            mapper.save(tableObj);
            logger.log("Store DB Complete.");
            return new FunctionStatus(true, null);
        } catch (AmazonDynamoDBException e) {
            FailCaseLog failCaseLog = new FailCaseLog(e.getStatusCode(), e.getErrorMessage(), e.getMessage());
            logger.log("\nError running the saveData: " + failCaseLog.convertToJsonString() + "\n");
            return new FunctionStatus(false, e.getStatusCode(), e.getErrorMessage(), e.getMessage());
        }
    }


    public static FunctionStatus getApplicationPlatform(String app_name, String mobile_type) {
        ArrayList<String> arrayList_platformArn = new ArrayList<String>();
        Table table = dynamoDB.getTable("ApplicationPlatform");
        Index index = table.getIndex("AppName-MobilePlatform-GSI");

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();

        if ("AppName-MobileType-GSI".equals(index.getIndexName())) {
            querySpec.withKeyConditionExpression("app_name = :v1 AND mobile_type = :v2")
                    .withValueMap(new ValueMap()
                            .withString(":v1", app_name)
                            .withString(":v2", mobile_type)
                    );
            items = index.query(querySpec);
            if (items == null) {
                logger.log("getApplicationPlatform : item is null");
            } else {
                logger.log("getApplicationPlatform : item is not null");
            }
        }

        List<ApplicationPlatform> list = new ArrayList<ApplicationPlatform>();
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            logger.log("GsonA: " + item.toJSON());
            ApplicationPlatform tempObj = new Gson().fromJson(item.toJSON().toString(), ApplicationPlatform.class);
            arrayList_platformArn.add(tempObj.getPlatform());
        }
        HashMap<String, Object> rs = new HashMap<String, Object>();
        rs.put("platformName_arraylist", arrayList_platformArn);
        return new FunctionStatus(true, rs);
    }

    public static String getLatestAppRegID(String app_name) {
        Table table = dynamoDB.getTable("SnsSubscriptions");
        Index index = table.getIndex("AppName-Sorting-GSI");

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();

        if ("AppName-Sorting-GSI".equals(index.getIndexName())) {
            querySpec.withKeyConditionExpression("app_name = :v1")
                    .withValueMap(new ValueMap()
                            .withString(":v1", app_name)
                    );
            querySpec.withScanIndexForward(true);
            items = index.query(querySpec);
            if (items == null) {
                logger.log("getInboxMessageRecord : item is null");
            } else {
                logger.log("getInboxMessageRecord : item is not null");
            }
        }

        SnsSubscriptions account = new Gson().fromJson(items.toString(), SnsSubscriptions.class);
        logger.log("The Latest SnsSubscriptions : " + account.getApp_reg_id());

        return account.getApp_reg_id();
    }

    public static ResponseMessage getInboxMessageRecord(RetrieveInboxRecordRequest request) {
        ArrayList<InboxMessageRecord> inboxMessageRecordArray = new ArrayList<InboxMessageRecord>();

        inboxMessageRecordArray = getInboxMessageRecord(inboxMessageRecordArray, request.getAccount_id(), request.getPush_timestamp());
        for (String topic : request.getTopic_name()) {
            String arn = new CommonUtil().getSnsTopicArn(topic);
            inboxMessageRecordArray = getInboxMessageRecord(inboxMessageRecordArray, arn, request.getPush_timestamp());
            logger.log("ArrayList: " + inboxMessageRecordArray.size());
        }

        return new ResponseMessage(200, new ResponseMessage.Message(inboxMessageRecordArray.toArray(new InboxMessageRecord[inboxMessageRecordArray.size()])));
    }

    private static ArrayList<InboxMessageRecord> getInboxMessageRecord(ArrayList<InboxMessageRecord> inboxMessageRecordArray, String arn, String msg_timestamp) {
        String tableIndexName = "TargetArn-MsgTimestamp-GSI ";
        Table table = dynamoDB.getTable("InboxRecordDBTable");
        Index index = table.getIndex(tableIndexName);

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();

        if ("TargetArnIndex".equals(tableIndexName)) {
            querySpec.withKeyConditionExpression("targetArn = :v1 AND msg_timestamp > :v2")
                    .withValueMap(new ValueMap()
                            .withString(":v1", arn)
                            .withString(":v2", msg_timestamp)
                    );
            items = index.query(querySpec);
            if (items == null) {
                logger.log("getInboxMessageRecord : item is null");
            } else {
                logger.log("getInboxMessageRecord : item is not null");
            }
        }

        List<InboxRecord> list = new ArrayList<InboxRecord>();
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            logger.log("GsonA: " + item.toJSON());
            InboxRecord tempTable = new Gson().fromJson(item.toJSON().toString(), InboxRecord.class);
            list.add(tempTable);
        }

        for (InboxRecord tempTable : list) {
            InboxMessageRecord record = new Gson().fromJson(tempTable.convertToJsonString(), InboxMessageRecord.class);
            record.setMessage(new Gson().fromJson(tempTable.getMessage().convertToJsonString(), InboxMessageRecord.class));
            inboxMessageRecordArray.add(record);
        }

        return inboxMessageRecordArray;
    }

}
