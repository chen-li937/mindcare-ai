package com.example.aispringboot.controller;

import com.example.aispringboot.DTO.response.DashboardResponseDTO;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 数据看板接口
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    //看板总览
    @GetMapping
    public Result<DashboardResponseDTO> overview() {
        return dashboardService.overview();
    }
}
