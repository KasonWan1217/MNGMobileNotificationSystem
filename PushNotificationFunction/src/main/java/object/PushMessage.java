package object;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class PushMessage {
    @SerializedName("default")
    private String defaultValue;

    @SerializedName("APNS")
    private String apns;

    @SerializedName("APNS_SANDBOX")
    private String apns_sandbox;

    @SerializedName("GCM")
    private String gcm;

    public String getDefaultValue() {
        return defaultValue;
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

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    public PushMessage(InboxRecordTable obj){
        PushDetails details = new PushDetails(obj);
        Gson gson = new GsonBuilder().serializeNulls().create();
        String txt_APNS = gson.toJson(new APNS(new Alert(details)));
        String txt_GCM = gson.toJson(new GCM(details));
        System.out.println("txt_APNS: " + txt_APNS);
        System.out.println("txt_GCM: " + txt_GCM);

        this.defaultValue = "BEA Notification Message";
        this.apns = txt_APNS;
        this.apns_sandbox = txt_APNS;
        this.gcm = txt_GCM;
    }

    public class PushDetails {
        private String type;
        private String title;
        private String body;
        private String sound;
        private int badge;
        private String picture_url;

        public PushDetails(InboxRecordTable data) {
            this.type = data.getActionCategory();
            this.title = data.getMessage().getTitle();
            this.body = data.getMessage().getBody();
            this.sound = data.getSound();
            this.badge = data.getBadge();
            this.picture_url = data.getPicture_url();
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

        public String getPicture_url() {
            return picture_url;
        }

        public void setPicture_url(String picture_url) {
            this.picture_url = picture_url;
        }
    }
}
