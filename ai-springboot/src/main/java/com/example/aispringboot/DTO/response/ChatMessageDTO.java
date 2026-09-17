package com.example.aispringboot.DTO.response;

import lombok.Data;

// 聊天气泡（前端咨询详情弹窗）
@Data
public class ChatMessageDTO {
    //user / ai
    private String from;

    //消息内容
    private String text;

    //时间 HH:mm
    private String time;

    public ChatMessageDTO(String from, String text, String time) {
        this.from = from;
        this.text = text;
        this.time = time;
    }
}
