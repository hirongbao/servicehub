/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 全局异常处理，业务异常统一返回 ApiResponse 而不是 500
 */
package com.shirongbao.admin.exception;

import com.shirongbao.common.response.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常，返回具体错误消息
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.error(e.getMessage());
    }

    // 处理参数校验异常，返回第一个字段的错误消息
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage() == null ? f.getField() + " 不能为空" : f.getDefaultMessage())
                .orElse("请求参数不合法");
        return ApiResponse.error(message);
    }

    // 处理上传文件超限异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ApiResponse.error("图片大小不能超过 10MB");
    }

    // 兜底处理未知异常，不向前端暴露堆栈信息
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e) {
        return ApiResponse.error("服务暂时不可用，请稍后再试");
    }
}
