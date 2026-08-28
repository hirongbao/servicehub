/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: ServiceHub Token 业务服务
 */
package com.shirongbao.authhub.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.authhub.dto.TokenCreateRequest;
import com.shirongbao.authhub.dto.TokenUsageStats;
import com.shirongbao.authhub.entity.ServiceToken;
import com.shirongbao.authhub.entity.TokenUsageLog;
import com.shirongbao.authhub.mapper.ServiceTokenMapper;
import com.shirongbao.authhub.mapper.TokenUsageLogMapper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ServiceTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ServiceTokenMapper mapper;
    private final TokenUsageLogMapper usageLogMapper;

    // 初始化 Token 业务服务
    public ServiceTokenService(ServiceTokenMapper mapper, TokenUsageLogMapper usageLogMapper) {
        this.mapper = mapper;
        this.usageLogMapper = usageLogMapper;
    }

    // 查询全部服务 Token，并附带使用统计
    public List<ServiceToken> list() {
        List<ServiceToken> tokens = mapper.selectList(null);
        if (!tokens.isEmpty()) {
            Map<Long, TokenUsageStats> stats = usageLogMapper.selectUsageStats().stream()
                    .collect(Collectors.toMap(TokenUsageStats::getTokenId, Function.identity()));
            for (ServiceToken token : tokens) {
                TokenUsageStats s = stats.get(token.getId());
                if (s != null) {
                    token.setUsageCount(s.getUsageCount());
                    token.setLastUsedAt(s.getLastUsedAt());
                }
            }
        }
        return tokens;
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

    // 校验并返回指定 hub 的有效服务 Token
    public ServiceToken requireActive(String tokenValue, String hub) {
        if (StringUtils.isBlank(tokenValue)) {
            throw new IllegalArgumentException("Token 无效、已禁用或已过期");
        }
        ServiceToken token = mapper.selectOne(new QueryWrapper<ServiceToken>().eq("token_value", tokenValue));
        if (token == null || token.getStatus() != 1 || !hub.equals(token.getTokenType())
                || (token.getExpiresAt() != null && !token.getExpiresAt().isAfter(LocalDateTime.now()))) {
            throw new IllegalArgumentException("Token 无效、已禁用或已过期");
        }
        return token;
    }

    // 记录一次 Token 使用日志
    public void recordUsage(ServiceToken token, String action) {
        TokenUsageLog log = new TokenUsageLog();
        log.setTokenId(token.getId());
        log.setHub(token.getTokenType());
        log.setAction(action);
        usageLogMapper.insert(log);
    }

    // 生成随机服务 Token
    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return "rb-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
