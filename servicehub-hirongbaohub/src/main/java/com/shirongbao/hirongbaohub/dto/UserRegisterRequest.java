package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "账号名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "账号名必须是3-20位字母、数字或下划线")
    private String accountName;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
