package com.example.aispringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// 知识文章实体
@Data
@TableName("knowledge_article")
@Builder
public class KnowledgeArticle {
    //文章ID
    @TableId(type = IdType.AUTO)
    private Long id;

    //分类ID
    @TableField("category_id")
    private Long categoryId;

    //标题
    private String title;

    //摘要
    private String summary;

    //封面图
    private String cover;

    //正文(Markdown)
    private String content;

    //标签(逗号分隔)
    private String tags;

    //作者
    private String author;

    //预计阅读时长(分钟)
    @TableField("read_minutes")
    private Integer readMinutes;

    //浏览量
    @TableField("view_count")
    private Integer viewCount;

    //点赞数
    @TableField("like_count")
    private Integer likeCount;

    //收藏数
    @TableField("favorite_count")
    private Integer favoriteCount;

    //1置顶
    @TableField("is_top")
    private Integer isTop;

    //1已发布 0草稿 2下架
    private Integer status;

    //发布时间
    @TableField("published_at")
    private LocalDateTime publishedAt;

    //创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;

    //更新时间
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
