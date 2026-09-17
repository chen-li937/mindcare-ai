package com.example.aispringboot.controller;

import com.example.aispringboot.DTO.command.ArticleCommandDTO;
import com.example.aispringboot.DTO.response.ArticleResponseDTO;
import com.example.aispringboot.common.PageResult;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.entity.KnowledgeCategory;
import com.example.aispringboot.service.ArticleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 知识文章管理接口
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    //分页查询
    @GetMapping("/page")
    public Result<PageResult<ArticleResponseDTO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return articleService.page(page, pageSize, keyword, category, status);
    }

    //新增文章
    @PostMapping
    public Result<ArticleResponseDTO> save(@Valid @RequestBody ArticleCommandDTO commandDTO) {
        return articleService.save(commandDTO);
    }

    //更新文章
    @PutMapping("/{id}")
    public Result<ArticleResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ArticleCommandDTO commandDTO) {
        commandDTO.setId(id);
        return articleService.update(commandDTO);
    }

    //删除文章
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return articleService.delete(id);
    }

    //分类列表（下拉框）
    @GetMapping("/category/list")
    public Result<List<KnowledgeCategory>> categoryList() {
        return articleService.listCategories();
    }
}
