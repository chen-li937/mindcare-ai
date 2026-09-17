package com.example.aispringboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aispringboot.DTO.command.ArticleCommandDTO;
import com.example.aispringboot.DTO.response.ArticleResponseDTO;
import com.example.aispringboot.common.PageResult;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.entity.KnowledgeArticle;
import com.example.aispringboot.entity.KnowledgeCategory;
import com.example.aispringboot.exception.BusinessException;
import com.example.aispringboot.mapper.KnowledgeArticleMapper;
import com.example.aispringboot.mapper.KnowledgeCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 知识文章管理服务
@Service
public class ArticleService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //文章状态：已发布/草稿/已下架 <-> 数据库 1/0/2
    private static final Map<String, Integer> STATUS_CODE = Map.of("已发布", 1, "草稿", 0, "已下架", 2);
    private static final Map<Integer, String> CODE_STATUS = Map.of(1, "已发布", 0, "草稿", 2, "已下架");

    @Resource
    private KnowledgeArticleMapper articleMapper;

    @Resource
    private KnowledgeCategoryMapper categoryMapper;

    //分页查询
    public Result<PageResult<ArticleResponseDTO>> page(long page, long pageSize, String keyword, String category, String status) {
        LambdaQueryWrapper<KnowledgeArticle> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            qw.like(KnowledgeArticle::getTitle, keyword);
        }
        if (StrUtil.isNotBlank(category)) {
            qw.eq(KnowledgeArticle::getCategoryId, resolveCategoryId(category));
        }
        if (StrUtil.isNotBlank(status)) {
            Integer statusCode = STATUS_CODE.get(status);
            if (statusCode == null) {
                throw new BusinessException("无效的文章状态：" + status);
            }
            qw.eq(KnowledgeArticle::getStatus, statusCode);
        }
        qw.orderByDesc(KnowledgeArticle::getIsTop)
                .orderByDesc(KnowledgeArticle::getPublishedAt)
                .orderByDesc(KnowledgeArticle::getId);

        Page<KnowledgeArticle> result = articleMapper.selectPage(new Page<>(page, pageSize), qw);
        Map<Long, String> catMap = categoryNameMap();
        List<ArticleResponseDTO> rows = result.getRecords().stream()
                .map(e -> toDTO(e, catMap))
                .collect(Collectors.toList());
        return Result.success(PageResult.of(rows, result.getTotal()));
    }

    //新增文章
    public Result<ArticleResponseDTO> save(ArticleCommandDTO commandDTO) {
        Long categoryId = resolveCategoryId(commandDTO.getCategory());
        KnowledgeArticle entity = KnowledgeArticle.builder()
                .title(commandDTO.getTitle())
                .summary(commandDTO.getSummary())
                .cover(commandDTO.getCover())
                .content(commandDTO.getContent())
                .tags(joinTags(commandDTO.getTags()))
                .author(StrUtil.blankToDefault(commandDTO.getAuthor(), "编辑部"))
                .readMinutes(commandDTO.getReadMinutes() != null ? commandDTO.getReadMinutes() : 5)
                .categoryId(categoryId)
                .isTop(Boolean.TRUE.equals(commandDTO.getIsTop()) ? 1 : 0)
                .status(mapStatusToCode(commandDTO.getStatus()))
                .publishedAt("已发布".equals(commandDTO.getStatus()) ? LocalDateTime.now() : null)
                .viewCount(0)
                .likeCount(0)
                .favoriteCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        articleMapper.insert(entity);
        return Result.success(toDTO(entity, categoryNameMap()));
    }

    //更新文章
    public Result<ArticleResponseDTO> update(ArticleCommandDTO commandDTO) {
        if (commandDTO.getId() == null) {
            throw new BusinessException("缺少文章ID");
        }
        KnowledgeArticle existing = articleMapper.selectById(commandDTO.getId());
        if (existing == null) {
            throw new BusinessException("文章不存在");
        }
        KnowledgeArticle entity = KnowledgeArticle.builder()
                .id(commandDTO.getId())
                .title(commandDTO.getTitle())
                .summary(commandDTO.getSummary())
                .cover(commandDTO.getCover())
                .content(commandDTO.getContent())
                .tags(joinTags(commandDTO.getTags()))
                .author(StrUtil.blankToDefault(commandDTO.getAuthor(), "编辑部"))
                .readMinutes(commandDTO.getReadMinutes() != null ? commandDTO.getReadMinutes() : 5)
                .categoryId(resolveCategoryId(commandDTO.getCategory()))
                .isTop(Boolean.TRUE.equals(commandDTO.getIsTop()) ? 1 : 0)
                .status(mapStatusToCode(commandDTO.getStatus()))
                .updatedAt(LocalDateTime.now())
                .build();
        //首次发布时补充发布时间
        if (entity.getStatus() == 1 && existing.getPublishedAt() == null) {
            entity.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(entity);
        return Result.success(toDTO(articleMapper.selectById(commandDTO.getId()), categoryNameMap()));
    }

    //删除文章
    public Result<Void> delete(Long id) {
        if (articleMapper.selectById(id) == null) {
            throw new BusinessException("文章不存在");
        }
        articleMapper.deleteById(id);
        return Result.success();
    }

    //分类列表（下拉框用）
    public Result<List<KnowledgeCategory>> listCategories() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>().orderByAsc(KnowledgeCategory::getSort)));
    }

    /* ---------------- 私有方法 ---------------- */

    private ArticleResponseDTO toDTO(KnowledgeArticle entity, Map<Long, String> catMap) {
        ArticleResponseDTO dto = new ArticleResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setCategory(catMap.getOrDefault(entity.getCategoryId(), "未分类"));
        dto.setCategoryId(entity.getCategoryId());
        dto.setTags(splitTags(entity.getTags()));
        dto.setSummary(entity.getSummary());
        dto.setContent(entity.getContent());
        dto.setAuthor(entity.getAuthor());
        dto.setReadMinutes(entity.getReadMinutes());
        dto.setViews(entity.getViewCount());
        dto.setLikes(entity.getLikeCount());
        dto.setFavorites(entity.getFavoriteCount());
        dto.setIsTop(entity.getIsTop() != null && entity.getIsTop() == 1);
        dto.setStatus(CODE_STATUS.getOrDefault(entity.getStatus(), "草稿"));
        dto.setPublishTime(entity.getPublishedAt() != null ? entity.getPublishedAt().format(DATE_FMT) : "—");
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private Long resolveCategoryId(String categoryName) {
        if (StrUtil.isBlank(categoryName)) {
            throw new BusinessException("文章分类不能为空");
        }
        KnowledgeCategory category = categoryMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeCategory>().eq(KnowledgeCategory::getName, categoryName));
        if (category == null) {
            throw new BusinessException("文章分类不存在：" + categoryName);
        }
        return category.getId();
    }

    private Map<Long, String> categoryNameMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(KnowledgeCategory::getId, KnowledgeCategory::getName));
    }

    private Integer mapStatusToCode(String status) {
        Integer code = STATUS_CODE.get(StrUtil.blankToDefault(status, "草稿"));
        if (code == null) {
            throw new BusinessException("无效的文章状态：" + status);
        }
        return code;
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }

    private List<String> splitTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return new ArrayList<>();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
