/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 站内私信Mapper
 */
package com.shirongbao.hirongbaohub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.hirongbaohub.dto.MessageSessionResponse;
import com.shirongbao.hirongbaohub.entity.SiteMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SiteMessageMapper extends BaseMapper<SiteMessage> {

    // 获取私信会话列表
    @Select("""
        SELECT
            u.id AS otherUserId,
            u.nickname AS otherUserName,
            u.avatar_url AS otherUserAvatar,
            m.content AS lastMessageContent,
            m.created_at AS lastMessageTime,
            IFNULL(unread.unreadCount, 0) AS unreadCount
        FROM (
            SELECT
                IF(sender_id = #{userId}, receiver_id, sender_id) AS other_user_id,
                MAX(id) AS last_msg_id
            FROM site_message
            WHERE sender_id = #{userId} OR receiver_id = #{userId}
            GROUP BY IF(sender_id = #{userId}, receiver_id, sender_id)
        ) sess
        JOIN site_message m ON m.id = sess.last_msg_id
        JOIN site_user u ON u.id = sess.other_user_id
        LEFT JOIN (
            SELECT sender_id, COUNT(*) AS unreadCount
            FROM site_message
            WHERE receiver_id = #{userId} AND is_read = 0
            GROUP BY sender_id
        ) unread ON unread.sender_id = sess.other_user_id
        ORDER BY m.created_at DESC
    """)
    List<MessageSessionResponse> getSessions(@Param("userId") Long userId);
}
