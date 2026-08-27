package com.shirongbao.common.response;

public record ApiResponse<T>(int code, T data, String message) {
    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(0, data, "success"); }
    public static ApiResponse<Void> success() { return new ApiResponse<>(0, null, "success"); }
}
