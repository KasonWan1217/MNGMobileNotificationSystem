package object.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;

@DynamoDBTable(tableName = "SNSAccount")
public class SNSAccount {
    private String app_reg_id;
    private String device_token;
    private String app_name;
    private String mobile_type;
    private String create_datetime;
    private String status;

    @DynamoDBHashKey(attributeName="app_reg_id")
    public String getApp_reg_id() {
        return app_reg_id;
    }
    public void setApp_reg_id(String app_reg_id) {
        this.app_reg_id = app_reg_id;
    }

    @DynamoDBAttribute(attributeName="device_token")
    public String getDevice_token() {
        return device_token;
    }
    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    @DynamoDBAttribute(attributeName="app_name")
    public String getApp_name() {
        return app_name;
    }
    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    @DynamoDBAttribute(attributeName="mobile_type")
    public String getMobile_type() {
        return mobile_type;
    }
    public void setMobile_type(String mobile_type) {
        this.mobile_type = mobile_type;
    }

    @DynamoDBAttribute(attributeName="create_datetime")
    public String getCreate_datetime() {
        return create_datetime;
    }
    public void setCreate_datetime(String create_datetime) {
        this.create_datetime = create_datetime;
    }

    @DynamoDBAttribute(attributeName="status")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
