package com.example.aispringboot.DTO.response;

import lombok.Data;

// AI 咨询会话记录响应（字段对齐前端页面）
@Data
public class ConsultationRecordResponseDTO {
    //会话ID
    private Long id;

    //用户昵称
    private String user;

    //咨询方式（当前均为 AI对话）
    private String channel;

    //会话标题（摘要）
    private String summary;

    //风险等级 低/中/高（依据风险消息数）
    private String risk;

    //持续时长 如 12分钟
    private String duration;

    //消息条数
    private Integer messages;

    //风险消息条数
    private Integer riskMessages;

    //开始时间 yyyy-MM-dd HH:mm
    private String startTime;

    //已结束 / 进行中
    private String status;
}
