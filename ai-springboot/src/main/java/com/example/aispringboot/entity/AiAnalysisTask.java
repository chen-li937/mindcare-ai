package com.example.aispringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// AI分析任务实体
@Data
@TableName("ai_analysis_task")
@Builder
public class AiAnalysisTask {
    //任务ID
    @TableId(type = IdType.AUTO)
    private Long id;

    //用户ID
    @TableField("user_id")
    private Long userId;

    //业务类型 emotion_diary/consultation_message 等
    @TableField("biz_type")
    private String bizType;

    //关联业务ID
    @TableField("biz_id")
    private Long bizId;

    //PENDING/RUNNING/SUCCESS/FAILED
    private String status;

    //分析结果(JSON字符串)
    private String result;

    //错误信息
    @TableField("error_msg")
    private String errorMsg;

    //创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;

    //更新时间
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
