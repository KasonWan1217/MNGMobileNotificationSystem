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

    public ResponseMessage(int code, Message message) {
        this.code = code;
        this.message = message;
    }

    public static class Message {
        private String message_id;
        private String message_qty;
        private String errorMsg;

        public String getMessage_id() {
            return message_id;
        }

        public void setMessage_id(String message_id) {
            this.message_id = message_id;
        }

        public String getMessage_qty() {
            return message_qty;
        }

        public void setMessage_qty(String message_qty) {
            this.message_qty = message_qty;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public String getErrorMsg_Detail() {
            return errorMsg_Detail;
        }

        public void setErrorMsg_Detail(String errorMsg_Detail) {
            this.errorMsg_Detail = errorMsg_Detail;
        }

        private String errorMsg_Detail;


        public Message(String message_id, int message_qty) {
            this.message_id = message_id;
            this.message_qty = String.valueOf(message_qty);
        }

        public Message(String errorMsg, String errorMsg_Detail) {
            this.errorMsg = errorMsg;
            this.errorMsg_Detail = errorMsg_Detail;
        }

    }

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
