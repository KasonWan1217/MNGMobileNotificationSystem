package object;

import com.google.gson.Gson;

public class FailCaseLog {
    private Integer status;
    private String error_msg;
    private String error_msg_detail;

    public FailCaseLog(Integer status, String error_msg, String error_msg_detail) {
        this.status = status;
        this.error_msg = error_msg;
        this.error_msg_detail = error_msg_detail;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String convertToJsonString() {
        return new Gson().toJson(this);
    }
}
