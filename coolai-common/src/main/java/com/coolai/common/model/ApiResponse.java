package com.coolai.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private long timestamp;

    private ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> fail(String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }

    public static <T> ApiResponse<T> fail(String message, String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(error);
        return response;
    }
}
