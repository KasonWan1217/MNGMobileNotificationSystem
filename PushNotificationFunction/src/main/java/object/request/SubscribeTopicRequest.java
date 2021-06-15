package object.request;

import com.google.gson.Gson;

public class SubscribeTopicRequest {
    private String app_reg_id;
    private String channel_name;
    private String action;

    public String getApp_reg_id() {
        return app_reg_id;
    }

    public void setApp_reg_id(String app_reg_id) {
        this.app_reg_id = app_reg_id;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
