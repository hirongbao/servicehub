/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: ServiceHub Token 业务服务
 */
package com.shirongbao.authhub.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.authhub.dto.TokenCreateRequest;
import com.shirongbao.authhub.entity.ServiceToken;
import com.shirongbao.authhub.mapper.ServiceTokenMapper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class ServiceTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ServiceTokenMapper mapper;

    // 初始化 Token 业务服务
    public ServiceTokenService(ServiceTokenMapper mapper) {
        this.mapper = mapper;
    }

    // 查询全部服务 Token
    public List<ServiceToken> list() {
        return mapper.selectList(null);
    }

    // 创建服务 Token
    public ServiceToken create(TokenCreateRequest request) {
        ServiceToken token = new ServiceToken();
        token.setTokenName(request.tokenName());
        token.setTokenType(StringUtils.isBlank(request.tokenType()) ? "FILEHUB" : request.tokenType().toUpperCase());
        token.setTokenValue(generateToken());
        token.setStatus(1);
        if (request.validDays() != null && request.validDays() > 0) {
            token.setExpiresAt(LocalDateTime.now().plusDays(request.validDays()));
        }
        mapper.insert(token);
        return token;
    }

    // 更新服务 Token 状态
    public ServiceToken updateStatus(Long id, Integer status) {
        ServiceToken token = mapper.selectById(id);
        if (token == null) {
            throw new IllegalArgumentException("Token 不存在");
        }
        token.setStatus(status == null || status == 0 ? 0 : 1);
        mapper.updateById(token);
        return token;
    }

    // 删除服务 Token
    public void delete(Long id) {
        if (mapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("Token 不存在");
        }
    }

    // 校验 FileHub 服务 Token 是否有效
    public boolean isActive(String tokenValue) {
        if (StringUtils.isBlank(tokenValue)) {
            return false;
        }
        ServiceToken token = mapper.selectOne(new QueryWrapper<ServiceToken>().eq("token_value", tokenValue));
        return token != null && token.getStatus() == 1
                && "FILEHUB".equals(token.getTokenType())
                && (token.getExpiresAt() == null || token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    // 生成随机服务 Token
    private String generateToken() {
        byte[] bytes = new byte[ thirtyTwoBytes() ];
        RANDOM.nextBytes(bytes);
        return "rb-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // 返回 Token 随机字节数
    private int thirtyTwoBytes() {
        return 32;
    }
}
