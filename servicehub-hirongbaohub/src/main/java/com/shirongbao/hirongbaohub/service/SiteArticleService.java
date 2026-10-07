package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.hirongbaohub.entity.SiteArticle;
import com.shirongbao.hirongbaohub.mapper.SiteArticleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SiteArticleService {
    private final SiteArticleMapper mapper;

    public SiteArticleService(SiteArticleMapper mapper) {
        this.mapper = mapper;
    }

    public IPage<SiteArticle> page(int current, int size, String keyword) {
        QueryWrapper<SiteArticle> query = new QueryWrapper<SiteArticle>().orderByDesc("created_at");
        if (StringUtils.hasText(keyword)) {
            query.like("title", keyword);
        }
        return mapper.selectPage(new Page<>(current, size), query);
    }

    public IPage<SiteArticle> publishedPage(int current, int size) {
        QueryWrapper<SiteArticle> query = new QueryWrapper<SiteArticle>()
                .eq("status", 1)
                .orderByDesc("created_at");
        return mapper.selectPage(new Page<>(current, size), query);
    }

    public SiteArticle get(Long id) {
        return mapper.selectById(id);
    }

    public SiteArticle getPublished(Long id) {
        SiteArticle article = mapper.selectOne(new QueryWrapper<SiteArticle>()
                .eq("id", id)
                .eq("status", 1));
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            mapper.updateById(article);
        }
        return article;
    }

    public SiteArticle save(SiteArticle article) {
        if (article.getId() == null) {
            if (article.getViewCount() == null) {
                article.setViewCount(0);
            }
            mapper.insert(article);
        } else {
            mapper.updateById(article);
        }
        return article;
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
