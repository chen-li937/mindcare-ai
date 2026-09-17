package com.example.aispringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// 知识文章分类实体
@Data
@TableName("knowledge_category")
@Builder
public class KnowledgeCategory {
    //分类ID
    @TableId(type = IdType.AUTO)
    private Long id;

    //分类名称
    private String name;

    //图标
    private String icon;

    //主题色
    private String color;

    //排序
    private Integer sort;

    //1启用 0停用
    private Integer status;

    //创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;
}
