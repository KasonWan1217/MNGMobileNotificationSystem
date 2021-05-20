package object.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;

@DynamoDBTable(tableName = "ApplicationTopic")
public class ApplicationTopic {
    private String topic;
    private String app_name;
    private String create_datetime;

    @DynamoDBHashKey
    @DynamoDBAttribute(attributeName="topic")
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @DynamoDBRangeKey
    @DynamoDBAttribute(attributeName="app_name")
    public String getApp_name() {
        return app_name;
    }
    public void setApp_name(String app_name) {
        this.app_name = app_name;
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
