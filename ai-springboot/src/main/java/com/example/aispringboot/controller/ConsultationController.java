package com.example.aispringboot.controller;

import com.example.aispringboot.DTO.response.ConsultationRecordResponseDTO;
import com.example.aispringboot.common.PageResult;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.service.ConsultationRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// AI 咨询记录管理接口
@RestController
@RequestMapping("/api/consultation")
public class ConsultationController {

    @Resource
    private ConsultationRecordService consultationRecordService;

    //分页查询
    @GetMapping("/page")
    public Result<PageResult<ConsultationRecordResponseDTO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String risk,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return consultationRecordService.page(page, pageSize, keyword, risk, channel, startDate, endDate);
    }

    //会话聊天详情
    @GetMapping("/{id}/messages")
    public Result<List<com.example.aispringboot.DTO.response.ChatMessageDTO>> messages(@PathVariable Long id) {
        return consultationRecordService.messages(id);
    }
}
