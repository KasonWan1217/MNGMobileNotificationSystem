package object.request;

import com.google.gson.Gson;

public class RetrieveInboxRecordRequest {
    private String account_id;
    private String app_name;
    private String[] topic_name;
    private String push_timestamp;

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String[] getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String[] topic_name) {
        this.topic_name = topic_name;
    }

    public String getPush_timestamp() {
        return push_timestamp;
    }

    public void setPush_timestamp(String push_timestamp) {
        this.push_timestamp = push_timestamp;
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
