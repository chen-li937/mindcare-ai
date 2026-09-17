package com.example.aispringboot.DTO.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 情绪日志响应（字段对齐前端页面）
@Data
public class EmotionDiaryResponseDTO {
    private Long id;

    //用户昵称（脱敏前）
    private String user;

    //情绪中文名：愉悦/平静/平稳/焦虑/低落/愤怒
    private String mood;

    //心情指数 1-5
    private Integer score;

    //情绪标签数组
    private List<String> emotionTags;

    //日记正文
    private String content;

    //AI 建议（来自 ai_analysis_task 的分析结果）
    private String aiReply;

    //天气 sunny/cloudy/rainy/snowy
    private String weather;

    //睡眠时长(小时)
    private BigDecimal sleepHours;

    //记录日期
    private LocalDate logDate;

    //记录时间 yyyy-MM-dd HH:mm
    private String createdAt;
}
