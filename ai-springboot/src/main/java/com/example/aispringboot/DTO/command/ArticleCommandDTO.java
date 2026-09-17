package com.example.aispringboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

// 文章新增/编辑请求
@Data
public class ArticleCommandDTO {
    //编辑时传入，新增为空
    private Long id;

    //分类名称（前端按名称提交，后端解析为 categoryId）
    @NotBlank(message = "文章分类不能为空")
    private String category;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 150, message = "标题长度不能超过150个字符")
    private String title;

    @Size(max = 300, message = "摘要长度不能超过300个字符")
    private String summary;

    //封面图
    private String cover;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    //标签数组
    private List<String> tags;

    @Size(max = 50, message = "作者长度不能超过50个字符")
    private String author;

    //预计阅读时长(分钟)
    private Integer readMinutes;

    //已发布 / 草稿
    private String status;

    //是否置顶
    private Boolean isTop;
}
