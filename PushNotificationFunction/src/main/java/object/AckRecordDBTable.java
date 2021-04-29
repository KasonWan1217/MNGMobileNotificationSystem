package object;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

@DynamoDBTable(tableName = "AckRecordDBTable")
public class AckRecordDBTable {
    private String targetArn;
    private String message_id;
    private String timestamp;


    public AckRecordDBTable(String json) {
        AckRecordDBTable ackRecordDBTable = new Gson().fromJson(json, AckRecordDBTable.class);
        this.targetArn = ackRecordDBTable.getTargetArn();
        this.message_id = ackRecordDBTable.getMessage_id();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = new Date();
        this.timestamp = formatter.format(date);
    }

//@DynamoDBIgnore

    @DynamoDBHashKey
    @DynamoDBAttribute(attributeName="targetArn")
    public String getTargetArn() {
        return targetArn;
    }
    public void setTargetArn(String targetArn) {
        this.targetArn = targetArn;
    }

    @DynamoDBRangeKey
    @DynamoDBAttribute(attributeName="message_id")
    public String getMessage_id() {
        return message_id;
    }
    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    @DynamoDBAttribute(attributeName="timestamp")
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
