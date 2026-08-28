/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 统一响应结构
 */
package com.shirongbao.common.response;

public record ApiResponse<T>(int code, T data, String message) {
    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(0, data, "success"); }
    public static ApiResponse<Void> success() { return new ApiResponse<>(0, null, "success"); }
    public static <T> ApiResponse<T> error(String message) { return new ApiResponse<>(1, null, message); }
}
