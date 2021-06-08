package object.db;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

@DynamoDBTable(tableName = "SnsAccount")
public class SnsAccount {
    private String app_reg_id;
    private String device_token;
    private String app_name;
    private String mobile_type;
    private String create_datetime;
    private String active_status;
    private List<Subscriptions> subscriptions;

    private String app_id;

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

    @DynamoDBAttribute(attributeName="active_status")
    public String getActive_status() {
        return active_status;
    }
    public void setActive_status(String active_status) {
        this.active_status = active_status;
    }

    @DynamoDBAttribute(attributeName="subscriptions")
    public List<Subscriptions> getSubscriptions() {
        return subscriptions;
    }
    public void setSubscriptions(List<Subscriptions> subscriptions) {
        this.subscriptions = subscriptions;
    }
    public void addSubscriptions(Subscriptions subscription) {
        if (this.subscriptions == null) {
            List<Subscriptions> list = new ArrayList<Subscriptions>();
            list.add(subscription);
            this.subscriptions = list;
        } else {
            this.subscriptions.add(subscription);
        }
    }

    @DynamoDBIgnore
    public String getApp_id() {
        return app_id;
    }
    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    @DynamoDBDocument
    public static class Subscriptions {
        private String channel_name;
        private String arn;
        private String channel_type;
        private String create_datetime;

        public Subscriptions(String channel_name, String arn, String channel_type, String create_datetime) {
            this.channel_name = channel_name;
            this.arn = arn;
            this.channel_type = channel_type;
            this.create_datetime = create_datetime;
        }

        public String getChannel_name() {
            return channel_name;
        }
        public void setChannel_name(String channel_name) {
            this.channel_name = channel_name;
        }

        public String getArn() {
            return arn;
        }
        public void setArn(String arn) {
            this.arn = arn;
        }

        public String getChannel_type() {
            return channel_type;
        }
        public void setChannel_type(String channel_type) {
            this.channel_type = channel_type;
        }

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

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
