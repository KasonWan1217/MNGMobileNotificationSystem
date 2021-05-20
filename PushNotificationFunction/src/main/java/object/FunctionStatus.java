package object;

import com.google.gson.Gson;

import java.util.HashMap;

public class FunctionStatus {
    private boolean status;
    private HashMap<String, Object> response;
    private Integer code;
    private String error_msg;
    private String error_msg_detail;

    public FunctionStatus(boolean status, HashMap<String, Object> response) {
        this.status = status;
        this.response = response;
    }

    public FunctionStatus(boolean status, Integer code, String error_msg, String error_msg_detail) {
        this.status = status;
        this.code = code;
        this.error_msg = error_msg;
        this.error_msg_detail = error_msg_detail;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public HashMap<String, Object> getResponse() {
        return response;
    }

    public void setResponse(HashMap<String, Object> response) {
        this.response = response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
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
