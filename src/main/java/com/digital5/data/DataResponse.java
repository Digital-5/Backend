package com.digital5.data;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public class DataResponse {

    private HashMap<String, String> data;
    private HttpStatus status;

    public DataResponse(HttpStatus status) {
        this.status = status;
        data = new HashMap<>();
    }

    public DataResponse() {
        this.status = HttpStatus.OK;
        data = new HashMap<>();
    }

    public void addData(String key, String value) {
        data.put(key, value);
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        JSONObject dataObject = new JSONObject();
        dataObject.putAll(data);
        jsonObject.put("data", dataObject);
        jsonObject.put("status", status.toString());
        return jsonObject.toJSONString();
    }

}
