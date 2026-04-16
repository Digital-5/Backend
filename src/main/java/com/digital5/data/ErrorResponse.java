package com.digital5.data;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorResponse {

    private String message;
    private HttpStatus status;

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);
        jsonObject.put("status", status.toString());
        return jsonObject.toJSONString();
    }

    public ResponseEntity<String> toResponseEntity() {
        return new ResponseEntity<>(toJsonString(), status);
    }

}