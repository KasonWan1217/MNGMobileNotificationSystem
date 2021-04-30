package object;

import com.google.gson.Gson;

public class InboxMessageRecord {
    private String title;
    private String subtitle;
    private String body;
    private String message_id;
    private String push_timestamp;

    public InboxMessageRecord(String title, String subtitle, String body, String message_id, String push_timestamp) {
        this.title = title;
        this.subtitle = subtitle;
        this.body = body;
        this.message_id = message_id;
        this.push_timestamp = push_timestamp;
    }

    public void setMessage(InboxMessageRecord inboxMessageRecord) {
        this.title = inboxMessageRecord.getTitle();
        this.subtitle = inboxMessageRecord.getSubtitle();
        this.body = inboxMessageRecord.getBody();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
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
