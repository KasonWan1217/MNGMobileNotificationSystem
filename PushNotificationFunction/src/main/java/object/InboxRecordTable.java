package object;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

@DynamoDBTable(tableName = "InboxRecordDBTable")
public class InboxRecordTable {
    private String message_id;
    private String timestamp;
    private String targetArn;
    private String targetArnType;
    private Message message;
    private String actionCategory;
    private int badge;
    private String sound;
    private String picture_url;
    private boolean directMsg;

    public InboxRecordTable(String json) {
        InboxRecordTable request = new Gson().fromJson(json, InboxRecordTable.class);
        this.message = request.getMessage();
        this.targetArn = request.getTargetArn();
        this.targetArnType = request.getTargetArnType();
        this.actionCategory = request.getActionCategory();
        this.badge = request.getBadge();
        this.sound = request.getSound();
        this.timestamp = request.getTimestamp();
        this.picture_url = request.getPicture_url();
        this.directMsg = request.isDirectMsg();
    }

//@DynamoDBIgnore

    @DynamoDBHashKey
    @DynamoDBAttribute(attributeName="message_id")
    public String getMessage_id() {
        return message_id;
    }
    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    @DynamoDBRangeKey
    @DynamoDBAttribute(attributeName="timestamp")
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBAttribute(attributeName="targetArn")
    public String getTargetArn() {
        return targetArn;
    }
    public void setTargetArn(String targetArn) {
        this.targetArn = targetArn;
    }

    @DynamoDBAttribute(attributeName="targetArnType")
    public String getTargetArnType() {
        return targetArnType;
    }
    public void setTargetArnType(String targetArnType) {
        this.targetArnType = targetArnType;
    }

    @DynamoDBAttribute(attributeName="message")
    public Message getMessage() {
        return message;
    }
    public void setMessage(Message message) {
        this.message = message;
    }

    @DynamoDBAttribute(attributeName="actionCategory")
    public String getActionCategory() {
        return actionCategory;
    }
    public void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory;
    }

    @DynamoDBAttribute(attributeName="badge")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public int getBadge() {
        return badge;
    }
    public void setBadge(int badge) {
        this.badge = badge;
    }

    @DynamoDBAttribute(attributeName="sound")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSound() {
        return sound;
    }
    public void setSound(String sound) {
        this.sound = sound;
    }

    @DynamoDBAttribute(attributeName="picture_url")
    public String getPicture_url() {
        return picture_url;
    }
    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    @DynamoDBAttribute(attributeName="directMsg")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean isDirectMsg() {
        return directMsg;
    }
    public void setDirectMsg(boolean directMsg) {
        this.directMsg = directMsg;
    }

    @DynamoDBDocument
    public class Message {
        private String title;
        private String subtitle;
        private String body;

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getBody() {
            return body;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
