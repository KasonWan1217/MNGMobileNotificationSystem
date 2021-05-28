package object;

import com.google.gson.Gson;

public class ResponseMessage {

    private Integer code;
    private Message message;

    public Message getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public ResponseMessage(int code, Message message) {
        this.code = code;
        this.message = message;
    }

    public ResponseMessage(FunctionStatus obj) {
        if(obj.isStatus()) {
            this.code = 200;
            Gson gson = new Gson();
            String jsonString = gson.toJson(obj.getResponse());
            Message message = gson.fromJson(jsonString, Message.class);
            this.message = message;
        } else {
            this.code = obj.getCode();
            Message message = new Message(obj.getError_msg(), obj.getError_msg_detail());
            this.message = message;
        }
    }

    public static class Message {
        private String msg_id;
        private String app_reg_id;
        private String msg_qty;
        private String error_msg;
        private String error_msg_detail;
        private InboxMessageRecord[] inbox_msg;

        public Message() {}

        public Message combine(Message obj) {
            this.msg_id = obj.getMsg_id() == null   ? this.msg_id : obj.getMsg_id();
            this.msg_qty = obj.getMsg_qty() == null ? this.msg_qty : obj.getMsg_qty();
            this.error_msg = obj.getError_msg() == null       ? this.error_msg : obj.getError_msg();
            this.error_msg_detail = obj.getError_msg_detail() == null       ? this.error_msg_detail : obj.getError_msg_detail();
            this.inbox_msg = obj.getInbox_msg() == null     ? this.inbox_msg : obj.getInbox_msg();
            return this;
        }

        public Message(String msg_id, int msg_qty) {
            this.msg_id = msg_id;
            this.msg_qty = String.valueOf(msg_qty);
        }

        public Message(String error_msg, String error_msg_detail) {
            this.error_msg = error_msg;
            this.error_msg_detail = error_msg_detail;
        }

        public Message(InboxMessageRecord[] inbox_msg) {
            this.inbox_msg = inbox_msg;
        }

        public String getMsg_id() {
            return msg_id;
        }

        public void setMsg_id(String msg_id) {
            this.msg_id = msg_id;
        }

        public String getApp_reg_id() {
            return app_reg_id;
        }

        public void setApp_reg_id(String app_reg_id) {
            this.app_reg_id = app_reg_id;
        }

        public String getMsg_qty() {
            return msg_qty;
        }

        public void setMsg_qty(String msg_qty) {
            this.msg_qty = msg_qty;
        }

        public String getError_msg() {
            return error_msg;
        }

        public void setError_msg(String error_msg) {
            this.error_msg = error_msg;
        }

        public String getError_msg_detail() {
            return error_msg_detail;
        }

        public void setError_msg_detail(String error_msg_detail) {
            this.error_msg_detail = error_msg_detail;
        }

        public InboxMessageRecord[] getInbox_msg() {
            return inbox_msg;
        }

        public void setInbox_msg(InboxMessageRecord[] inbox_msg) {
            this.inbox_msg = inbox_msg;
        }
    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }

}
