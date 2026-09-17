package com.example.aispringboot.controller;

import com.example.aispringboot.DTO.response.EmotionDiaryResponseDTO;
import com.example.aispringboot.DTO.response.EmotionStatsResponseDTO;
import com.example.aispringboot.common.PageResult;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.service.EmotionDiaryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 情绪日志接口
@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {

    @Resource
    private EmotionDiaryService emotionDiaryService;

    //分页查询
    @GetMapping("/page")
    public Result<PageResult<EmotionDiaryResponseDTO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String mood) {
        return emotionDiaryService.page(page, pageSize, keyword, mood);
    }

    //情绪统计
    @GetMapping("/stats")
    public Result<EmotionStatsResponseDTO> stats() {
        return emotionDiaryService.stats();
    }
}
