package object;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

public class RetrieveInboxRecordRequest {
    private String userArn;
    private String appName;
    private String push_timestamp;
    private String[] topicName;

    public RetrieveInboxRecordRequest(String json) {
        RetrieveInboxRecordRequest request = new Gson().fromJson(json, RetrieveInboxRecordRequest.class);
        this.userArn = request.getUserArn();
        this.appName = request.getAppName();
        this.push_timestamp = request.getPush_timestamp();
        this.topicName = request.getTopicName();
    }

    public String getUserArn() {
        return userArn;
    }

    public void setUserArn(String userArn) {
        this.userArn = userArn;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPush_timestamp() {
        return push_timestamp;
    }

    public void setPush_timestamp(String push_timestamp) {
        this.push_timestamp = push_timestamp;
    }

    public String[] getTopicName() {
        return topicName;
    }

    public void setTopicName(String[] topicName) {
        this.topicName = topicName;
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
