package com.example.aispringboot.DTO.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// 知识文章列表/详情响应（字段对齐前端页面）
@Data
public class ArticleResponseDTO {
    private Long id;

    private String title;

    //分类名称
    private String category;

    //分类ID
    private Long categoryId;

    //标签数组
    private List<String> tags;

    //摘要
    private String summary;

    //正文(Markdown)
    private String content;

    //作者
    private String author;

    //预计阅读时长(分钟)
    private Integer readMinutes;

    //浏览量
    private Integer views;

    //点赞数
    private Integer likes;

    //收藏数
    private Integer favorites;

    //是否置顶
    private Boolean isTop;

    //已发布 / 草稿 / 已下架
    private String status;

    //发布时间 yyyy-MM-dd，未发布为 "—"
    private String publishTime;

    private LocalDateTime createdAt;
}
