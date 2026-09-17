package com.example.aispringboot.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aispringboot.DTO.response.EmotionDiaryResponseDTO;
import com.example.aispringboot.DTO.response.EmotionStatsResponseDTO;
import com.example.aispringboot.common.PageResult;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.entity.AiAnalysisTask;
import com.example.aispringboot.entity.EmotionDiary;
import com.example.aispringboot.entity.User;
import com.example.aispringboot.exception.BusinessException;
import com.example.aispringboot.mapper.AiAnalysisTaskMapper;
import com.example.aispringboot.mapper.EmotionDiaryMapper;
import com.example.aispringboot.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// 心情日记服务
@Service
public class EmotionDiaryService {
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    //数据库情绪类型 <-> 中文名（固定展示顺序）
    private static final List<String[]> MOODS = List.of(
            new String[]{"happy", "愉悦"},
            new String[]{"calm", "平静"},
            new String[]{"neutral", "平稳"},
            new String[]{"anxious", "焦虑"},
            new String[]{"sad", "低落"},
            new String[]{"angry", "愤怒"});

    @Resource
    private EmotionDiaryMapper diaryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AiAnalysisTaskMapper taskMapper;

    //分页查询
    public Result<PageResult<EmotionDiaryResponseDTO>> page(long page, long pageSize, String keyword, String mood) {
        LambdaQueryWrapper<EmotionDiary> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            qw.like(EmotionDiary::getContent, keyword);
        }
        if (StrUtil.isNotBlank(mood)) {
            qw.eq(EmotionDiary::getMoodType, toMoodCode(mood));
        }
        qw.orderByDesc(EmotionDiary::getLogDate).orderByDesc(EmotionDiary::getId);

        Page<EmotionDiary> result = diaryMapper.selectPage(new Page<>(page, pageSize), qw);
        List<EmotionDiaryResponseDTO> rows = toDTOs(result.getRecords());
        return Result.success(PageResult.of(rows, result.getTotal()));
    }

    //情绪统计
    public Result<EmotionStatsResponseDTO> stats() {
        long total = diaryMapper.selectCount(null);

        //平均心情指数
        List<Object> avgObj = diaryMapper.selectObjs(
                new QueryWrapper<EmotionDiary>().select("IFNULL(AVG(mood_score), 0)"));
        double avgScore = 0.0;
        if (!avgObj.isEmpty() && avgObj.get(0) instanceof Number num) {
            avgScore = BigDecimal.valueOf(num.doubleValue()).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        EmotionStatsResponseDTO dto = new EmotionStatsResponseDTO();
        dto.setTotal(total);
        dto.setAvgScore(avgScore);
        dto.setDistribution(distributionList());
        return Result.success(dto);
    }

    //情绪分布（固定顺序，供看板复用）
    public List<EmotionStatsResponseDTO.DistributionItem> distributionList() {
        QueryWrapper<EmotionDiary> qw = new QueryWrapper<>();
        qw.select("mood_type", "COUNT(*) AS cnt").groupBy("mood_type");
        Map<String, Long> countMap = diaryMapper.selectMaps(qw).stream()
                .collect(Collectors.toMap(
                        m -> String.valueOf(m.get("mood_type")),
                        m -> ((Number) m.get("cnt")).longValue()));

        List<EmotionStatsResponseDTO.DistributionItem> list = new ArrayList<>();
        for (String[] mood : MOODS) {
            list.add(new EmotionStatsResponseDTO.DistributionItem(mood[1], countMap.getOrDefault(mood[0], 0L)));
        }
        return list;
    }

    /* ---------------- 私有方法 ---------------- */

    private List<EmotionDiaryResponseDTO> toDTOs(List<EmotionDiary> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        //用户昵称
        Set<Long> userIds = records.stream().map(EmotionDiary::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        //AI 建议（取每个日记最新一条成功分析）
        Set<Long> diaryIds = records.stream().map(EmotionDiary::getId).collect(Collectors.toSet());
        List<AiAnalysisTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<AiAnalysisTask>()
                .eq(AiAnalysisTask::getBizType, "emotion_diary")
                .eq(AiAnalysisTask::getStatus, "SUCCESS")
                .in(AiAnalysisTask::getBizId, diaryIds)
                .orderByDesc(AiAnalysisTask::getId));
        Map<Long, String> suggestionMap = new HashMap<>();
        for (AiAnalysisTask task : tasks) {
            suggestionMap.putIfAbsent(task.getBizId(), extractSuggestion(task.getResult()));
        }

        return records.stream().map(diary -> {
            EmotionDiaryResponseDTO dto = new EmotionDiaryResponseDTO();
            dto.setId(diary.getId());
            User user = userMap.get(diary.getUserId());
            dto.setUser(user != null ? user.getDisplayName() : "未知用户");
            dto.setMood(toMoodName(diary.getMoodType()));
            dto.setScore(diary.getMoodScore());
            dto.setEmotionTags(parseTags(diary.getEmotionTags()));
            dto.setContent(diary.getContent());
            dto.setAiReply(suggestionMap.get(diary.getId()));
            dto.setWeather(diary.getWeather());
            dto.setSleepHours(diary.getSleepHours());
            dto.setLogDate(diary.getLogDate());
            dto.setCreatedAt(diary.getCreatedAt() != null ? diary.getCreatedAt().format(DATETIME_FMT) : "");
            return dto;
        }).collect(Collectors.toList());
    }

    private String extractSuggestion(String resultJson) {
        if (StrUtil.isBlank(resultJson)) {
            return null;
        }
        try {
            return JSONUtil.parseObj(resultJson).getStr("suggestion");
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseTags(String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return List.of();
        }
        try {
            return JSONUtil.toList(tagsJson, String.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toMoodName(String code) {
        return MOODS.stream()
                .filter(m -> m[0].equals(code))
                .findFirst()
                .map(m -> m[1])
                .orElse(code);
    }

    private String toMoodCode(String name) {
        return MOODS.stream()
                .filter(m -> m[1].equals(name))
                .findFirst()
                .map(m -> m[0])
                .orElseThrow(() -> new BusinessException("无效的情绪类型：" + name));
    }
}
