package com.shirongbao.hirongbaohub.dto;

import com.shirongbao.hirongbaohub.entity.SiteComment;

public record AdminCommentResponse(
        SiteComment comment,
        String postContentSummary
) {}
