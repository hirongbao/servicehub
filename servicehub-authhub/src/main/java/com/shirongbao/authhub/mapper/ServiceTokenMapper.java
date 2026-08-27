/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: ServiceHub Token 数据访问接口
 */
package com.shirongbao.authhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.authhub.entity.ServiceToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceTokenMapper extends BaseMapper<ServiceToken> {
}
