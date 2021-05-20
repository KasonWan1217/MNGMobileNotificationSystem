package object.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;

@DynamoDBTable(tableName = "ApplicationPlatform")
public class ApplicationPlatform {
    private String platform;
    private String app_name;
    private String mobile_type;

    @DynamoDBHashKey(attributeName="platform")
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @DynamoDBRangeKey(attributeName="app_name")
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

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
