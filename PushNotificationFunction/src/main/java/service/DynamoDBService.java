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
import object.db.ApplicationChannel;
import object.db.InboxRecord;
import object.InboxMessageRecord;
import object.db.SnsAccount;
import object.db.SnsAccount.Subscriptions;
import object.request.RetrieveInboxRecordRequest;
import util.CommonUtil;
import util.DBEnumValue;

import java.util.*;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;
import static util.CommonMessage.Fails_Message;

public class DynamoDBService {
    private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private static DynamoDB dynamoDB = new DynamoDB(client);
    private static DynamoDBMapper mapper = null;
    private static LambdaLogger logger = getLogger();

//    public static FunctionStatus saveData(ArrayList<Object> tableObj_List) {
//        try {
//            mapper = new DynamoDBMapper(client);
//            mapper.batchSave(tableObj_List);
//            logger.log("Store DB Complete.");
//            return new FunctionStatus(true, null);
//        } catch (AmazonDynamoDBException e) {
//            FailCaseLog failCaseLog = new FailCaseLog(e.getStatusCode(), e.getErrorMessage(), e.getMessage());
//            logger.log("\nError running the saveData batch: " + failCaseLog.convertToJsonString() + "\n");
//            return new FunctionStatus(false, e.getStatusCode(), e.getErrorMessage(), e.getMessage());
//        }
//    }
    public static FunctionStatus saveData(Object obj) {
        try {
            mapper = new DynamoDBMapper(client);
            mapper.save(obj);
            logger.log("Store DB Complete.");
            return new FunctionStatus(true, null);
        } catch (AmazonDynamoDBException e) {
            FailCaseLog failCaseLog = new FailCaseLog(e.getStatusCode(), e.getErrorMessage(), e.getMessage());
            logger.log("\nError running the saveData: " + failCaseLog.convertToJsonString() + "\n");
            return new FunctionStatus(false, e.getStatusCode(), e.getErrorMessage(), e.getMessage());
        }
    }

    public static FunctionStatus getSubscriptionsList(String app_reg_id) {
        FunctionStatus functionStatus = getSnsAccount_Subscriptions(app_reg_id);
        List<Subscriptions> arrayList_channelName = ((SnsAccount) functionStatus.getResponse().get("snsAccount")).getSubscriptions();
        HashMap<String, Object> rs = new HashMap<String, Object>();
        rs.put("arrayList_channelName", arrayList_channelName);
        return new FunctionStatus(true, rs);
    }

    public static FunctionStatus getSnsAccount_checkStatus(String device_token, String active_status) {
        Table table = dynamoDB.getTable("SnsAccount");
        Index index = table.getIndex("DeviceToken-ActiveStatus-GSI");

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();

        if ("DeviceToken-ActiveStatus-GSI".equals(index.getIndexName())) {
            querySpec.withKeyConditionExpression("device_token = :v1 AND active_status = :v2")
                    .withValueMap(new ValueMap()
                            .withString(":v1", device_token)
                            .withString(":v2", active_status)
                    );
            items = index.query(querySpec);
            if (items == null) {
                logger.log("getSnsAccount_checkStatus : item is null");
            } else {
                logger.log("getSnsAccount_checkStatus : item is not null");
            }
        }

        List<ApplicationChannel> list = new ArrayList<ApplicationChannel>();
        Iterator<Item> iterator = items.iterator();
        SnsAccount snsAccount = new SnsAccount();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            logger.log("getSnsAccount_checkStatus: " + item.toJSONPretty());
            snsAccount = new Gson().fromJson(item.toJSON(), SnsAccount.class);
        }
        HashMap<String, Object> rs = new HashMap<String, Object>();
        rs.put("snsAccount", snsAccount);
        return new FunctionStatus(true, rs);
    }

    public static FunctionStatus getSnsAccount_Subscriptions(String app_reg_id) {
        Table table = dynamoDB.getTable("SnsAccount");
        Index index = table.getIndex("AppRegId-ActiveStatus-GSI");

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();

        if ("AppRegId-ActiveStatus-GSI".equals(index.getIndexName())) {
            querySpec.withKeyConditionExpression("app_reg_id = :v1 AND active_status =:v2")
                    .withValueMap(new ValueMap()
                            .withString(":v1", app_reg_id)
                            .withString(":v2", DBEnumValue.Status.Success.toString())
                    );
            items = index.query(querySpec);
            if (items == null) {
                logger.log("getSnsAccount_Subscriptions : item is null");
            } else {
                logger.log("getSnsAccount_Subscriptions : item is not null");
            }
        }

        List<ApplicationChannel> list = new ArrayList<ApplicationChannel>();
        Iterator<Item> iterator = items.iterator();
        SnsAccount snsAccount = new SnsAccount();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            logger.log("Subscriptions List: " + item.toJSONPretty());
            snsAccount = new Gson().fromJson(item.toJSON(), SnsAccount.class);
        }
        HashMap<String, Object> rs = new HashMap<String, Object>();
        rs.put("snsAccount", snsAccount);
        return new FunctionStatus(true, rs);
    }


    public static FunctionStatus getPlatformName(String app_name, String mobile_type) {
        Table table = dynamoDB.getTable("ApplicationChannel");
        Index index = table.getIndex("AppName-MobileType-GSI");

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
                logger.log("getPlatformName : item is null");
            } else {
                logger.log("getPlatformName : item is not null");
            }
        }

        List<ApplicationChannel> list = new ArrayList<ApplicationChannel>();
        Iterator<Item> iterator = items.iterator();

        ArrayList<String> arrayList_platformName = new ArrayList<String>();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            logger.log("getPlatformName: " + item.toJSON());
            ApplicationChannel tempObj = new Gson().fromJson(item.toJSON(), ApplicationChannel.class);
            arrayList_platformName.add(tempObj.getChannel_name());
        }
        HashMap<String, Object> rs = new HashMap<String, Object>();
        if (arrayList_platformName.size() > 0) {
            rs.put("arrayList_platformName", arrayList_platformName);
            return new FunctionStatus(true, rs);
        } else {
            return new FunctionStatus(false, 500, Fails_Message.PlatformName_Null_Error.toString(), Fails_Message.PlatformName_Null_Error.toString());
        }

    }

    public static FunctionStatus getSnsAccount_LatestAppRegID(String app_name) {
        Table table = dynamoDB.getTable("SnsAccount");
        Index index = table.getIndex("AppName-Sorting-GSI");

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();
        logger.log("app_naem: " +app_name);
        if ("AppName-Sorting-GSI".equals(index.getIndexName())) {
            querySpec.withKeyConditionExpression("app_name = :v1")
                    .withValueMap(new ValueMap()
                            .withString(":v1", app_name)
                    ).withScanIndexForward(false).setMaxResultSize(1);
            items = index.query(querySpec);
            if (items == null) {
                logger.log("getSnsAccount_LatestAppRegID : item is null");
            } else {
                logger.log("getSnsAccount_LatestAppRegID : item is not null");
            }
        }

        Iterator<Item> iterator = items.iterator();
        String app_reg_id = "";
        while (iterator.hasNext()) {
            app_reg_id = new Gson().fromJson(iterator.next().toJSONPretty(), SnsAccount.class).getApp_reg_id();
        }

        HashMap<String, Object> rs = new HashMap<String, Object>();
        rs.put("app_reg_id", app_reg_id);
        return new FunctionStatus(true, rs);
    }

    public static FunctionStatus getInboxMessageRecord(RetrieveInboxRecordRequest request, List<Subscriptions> arrayList_channelName) {
        ArrayList<InboxMessageRecord> inbox_msg = new ArrayList<InboxMessageRecord>();
        for (Subscriptions subscription : arrayList_channelName) {
            if (DBEnumValue.ArnType.Platform.toString().equals(subscription.getArn()))
                continue;
            String arn = CommonUtil.getSnsTopicArn(subscription.getChannel_name());
            inbox_msg.addAll(getInboxMessageRecord(arn, request.getMsg_timestamp()));
            logger.log("Inbox Message ArrayList Size - Topic: " + inbox_msg.size());
        }
        inbox_msg.addAll(getInboxMessageRecord(request.getApp_reg_id(), request.getMsg_timestamp()));
        logger.log("Inbox Message ArrayList Size - Total: " + inbox_msg.size());
        Collections.sort(inbox_msg, new InboxMessageRecord.SortByDate());

        HashMap<String, Object> rs = new HashMap<String, Object>();
        rs.put("inbox_msg", inbox_msg.toArray(new InboxMessageRecord[7]));
        logger.log("7 Records ArrayList: " + new Gson().toJson(rs.get("inbox_msg")));
        return new FunctionStatus(true, rs);
    }

    private static ArrayList<InboxMessageRecord> getInboxMessageRecord(String target, String msg_timestamp) {
        Table table = dynamoDB.getTable("InboxRecord");
        Index index = table.getIndex("Target-MsgTimestamp-GSI");

        ItemCollection<QueryOutcome> items = null;
        QuerySpec querySpec = new QuerySpec();

        if ("Target-MsgTimestamp-GSI".equals(index.getIndexName())) {
            querySpec.withKeyConditionExpression("target = :v1 AND msg_timestamp > :v2")
                    .withValueMap(new ValueMap()
                            .withString(":v1", target)
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
            String temp = iterator.next().toJSONPretty();
            logger.log("getInboxMessageRecord: " + temp);
            InboxRecord tempTable = new Gson().fromJson(temp, InboxRecord.class);
            list.add(tempTable);
        }

        ArrayList<InboxMessageRecord> inboxMessageRecordArray = new ArrayList<InboxMessageRecord>();
        for (InboxRecord tempTable : list) {
            InboxMessageRecord record = new Gson().fromJson(tempTable.convertToJsonString(), InboxMessageRecord.class);
            record.setMessage(new Gson().fromJson(tempTable.getMessage().convertToJsonString(), InboxMessageRecord.class));
            inboxMessageRecordArray.add(record);
        }

        return inboxMessageRecordArray;
    }

}
