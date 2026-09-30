package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank(message = "账号名或邮箱不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
