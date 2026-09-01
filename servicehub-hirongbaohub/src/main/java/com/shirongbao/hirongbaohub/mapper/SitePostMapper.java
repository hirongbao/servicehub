/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 动态数据访问接口
 */
package com.shirongbao.hirongbaohub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.hirongbaohub.entity.SitePost;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SitePostMapper extends BaseMapper<SitePost> {
}
