/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 全局异常处理，业务异常统一返回 ApiResponse 而不是 500
 */
package com.shirongbao.admin.exception;

import com.shirongbao.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理静态资源不存在，如实返回 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.notFound().build();
    }

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

    // 处理请求媒体类型不支持异常
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResponse<Void> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return ApiResponse.error("请求媒体类型不支持，上传图片请使用 multipart/form-data 格式");
    }

    // 处理请求方法不支持异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.error("不支持的请求方法：" + e.getMethod());
    }

    // 处理缺失请求部件异常
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ApiResponse<Void> handleMissingRequestPart(MissingServletRequestPartException e) {
        return ApiResponse.error("缺少必填文件参数：" + e.getRequestPartName() + "，请确保表单字段名为 'file'");
    }

    // 处理缺失请求参数异常
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Void> handleMissingRequestParam(MissingServletRequestParameterException e) {
        return ApiResponse.error("缺少必填请求参数：" + e.getParameterName());
    }

    // 处理 Multipart 解析失败异常
    @ExceptionHandler(MultipartException.class)
    public ApiResponse<Void> handleMultipartException(MultipartException e) {
        return ApiResponse.error("文件上传请求解析失败，请检查请求是否为包含有效 boundary 的 multipart/form-data 格式");
    }

    // 兜底处理未知异常，记录日志并返回通用消息
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e) {
        log.error("接口处理出现未预期异常", e);
        return ApiResponse.error("服务暂时不可用，请稍后再试");
    }
}
