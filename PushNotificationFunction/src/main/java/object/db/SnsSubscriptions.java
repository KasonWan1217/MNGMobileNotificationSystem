package object.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;

@DynamoDBTable(tableName = "SnsSubscriptions")
public class SnsSubscriptions {
    private String app_reg_id;
    private String arn;
    private String arn_type;
    private String create_datetime;

    @DynamoDBHashKey(attributeName="app_reg_id")
    public String getApp_reg_id() {
        return app_reg_id;
    }
    public void setApp_reg_id(String app_reg_id) {
        this.app_reg_id = app_reg_id;
    }

    @DynamoDBRangeKey(attributeName="arn")
    public String getArn() {
        return arn;
    }
    public void setArn(String arn) {
        this.arn = arn;
    }

    @DynamoDBAttribute(attributeName="arn_type")
    public String getArn_type() {
        return arn_type;
    }
    public void setArn_type(String arn_type) {
        this.arn_type = arn_type;
    }

    @DynamoDBAttribute(attributeName="create_datetime")
    public String getCreate_datetime() {
        return create_datetime;
    }
    public void setCreate_datetime(String create_datetime) {
        this.create_datetime = create_datetime;
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}