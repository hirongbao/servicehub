/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: FileHub 文件记录数据访问接口
 */
package com.shirongbao.filehub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.filehub.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
}
