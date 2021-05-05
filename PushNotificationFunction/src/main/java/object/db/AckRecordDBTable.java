package object.db;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

@DynamoDBTable(tableName = "AckRecordDBTable")
public class AckRecordDBTable {
    private String targetArn;
    private String message_id;
    private String read_timestamp;
    private String remark;


    public AckRecordDBTable(String json) {
        AckRecordDBTable ackRecordDBTable = new Gson().fromJson(json, AckRecordDBTable.class);
        this.targetArn = ackRecordDBTable.getTargetArn();
        this.message_id = ackRecordDBTable.getMessage_id();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = new Date();
        this.read_timestamp = formatter.format(date);
        this.remark = ackRecordDBTable.getRemark();
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

    @DynamoDBAttribute(attributeName="read_timestamp")
    public String getRead_timestamp() {
        return read_timestamp;
    }

    public void setRead_timestamp(String read_timestamp) {
        this.read_timestamp = read_timestamp;
    }

    @DynamoDBAttribute(attributeName="remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
