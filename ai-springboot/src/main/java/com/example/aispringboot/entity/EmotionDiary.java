package com.example.aispringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// 心情日记实体
@Data
@TableName("emotion_diary")
@Builder
public class EmotionDiary {
    //日记ID
    @TableId(type = IdType.AUTO)
    private Long id;

    //用户ID
    @TableField("user_id")
    private Long userId;

    //情绪类型 happy/calm/neutral/anxious/sad/angry
    @TableField("mood_type")
    private String moodType;

    //心情指数 1-5
    @TableField("mood_score")
    private Integer moodScore;

    //情绪标签(JSON数组字符串)
    @TableField("emotion_tags")
    private String emotionTags;

    //日记正文
    private String content;

    //天气 sunny/cloudy/rainy/snowy
    private String weather;

    //睡眠时长(小时)
    @TableField("sleep_hours")
    private BigDecimal sleepHours;

    //1私密 0公开
    @TableField("is_private")
    private Integer isPrivate;

    //记录日期
    @TableField("log_date")
    private LocalDate logDate;

    //创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;

    //更新时间
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
