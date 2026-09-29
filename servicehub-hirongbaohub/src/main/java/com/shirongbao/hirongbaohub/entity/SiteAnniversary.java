package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("site_anniversary")
public class SiteAnniversary {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String title;
    private LocalDate eventDate;
    private String type;
    private String icon;
    private String coverUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
