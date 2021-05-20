package object;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import object.db.InboxRecord;

public class PushMessage {
    @SerializedName("default")
    private String default_value;

    @SerializedName("APNS")
    private String apns;

    @SerializedName("APNS_SANDBOX")
    private String apns_sandbox;

    @SerializedName("GCM")
    private String gcm;

    public String getDefault_value() {
        return default_value;
    }

    public void setDefault_value(String default_value) {
        this.default_value = default_value;
    }

    public String getApns() {
        return apns;
    }

    public String getApns_sandbox() {
        return apns_sandbox;
    }

    public String getGcm() {
        return gcm;
    }

    public void setApns(String apns) {
        this.apns = apns;
    }

    public void setApns_sandbox(String apns_sandbox) {
        this.apns_sandbox = apns_sandbox;
    }

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }

    public class Alert {
        private PushDetails alert;

        public Alert(PushDetails alert) {
            this.alert = alert;
        }

        public PushDetails getAlert() {
            return alert;
        }

        public void setAlert(PushDetails alert) {
            this.alert = alert;
        }
    }

    public class APNS {
        private Alert aps;

        public APNS(Alert aps) {
            this.aps = aps;
        }

        public Alert getAps() {
            return aps;
        }

        public void setAps(Alert aps) {
            this.aps = aps;
        }
    }

    public class GCM {
        private PushDetails data;

        public PushDetails getData() {
            return data;
        }

        public void setData(PushDetails data) {
            this.data = data;
        }

        public GCM(PushDetails data) {
            this.data = data;
        }
    }

    public PushMessage(InboxRecord obj){
        PushDetails details = new PushDetails(obj);
        Gson gson = new GsonBuilder().serializeNulls().create();
        String txt_APNS = gson.toJson(new APNS(new Alert(details)));
        String txt_GCM = gson.toJson(new GCM(details));

        this.default_value = "BEA Notification Message";
        this.apns = txt_APNS;
        this.apns_sandbox = txt_APNS;
        this.gcm = txt_GCM;
    }

    public class PushDetails {
        private String msg_id;
        private String type;
        private String title;
        private String sub_title;
        private String body;
        private String sound;
        private int badge;
        private String picUrl;

        public PushDetails(InboxRecord obj) {
            this.msg_id = obj.getMsg_id();
            this.type = obj.getAction_category();
            this.title = obj.getMessage().getTitle();
            this.sub_title = obj.getMessage().getSub_title();
            this.body = obj.getMessage().getBody();
            this.sound = obj.getSound();
            this.badge = obj.getBadge();
            this.picUrl = obj.getPic_url();
        }

        public String getMsg_id() {
            return msg_id;
        }

        public void setMsg_id(String msg_id) {
            this.msg_id = msg_id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSub_title() {
            return sub_title;
        }

        public void setSub_title(String sub_title) {
            this.sub_title = sub_title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getSound() {
            return sound;
        }

        public void setSound(String sound) {
            this.sound = sound;
        }

        public int getBadge() {
            return badge;
        }

        public void setBadge(int badge) {
            this.badge = badge;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }
    }
}
