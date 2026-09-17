package com.example.aispringboot.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// 数据看板响应（字段对齐前端页面）
@Data
@Builder
public class DashboardResponseDTO {
    //统计卡片
    private List<StatCard> statCards;

    //近7天趋势 { dates: [MM-dd...], ai: [会话数...], diary: [日记数...] }
    private LineChart lineChart;

    //情绪分布 { value, name, color }
    private List<PieItem> pieChart;

    //最近咨询（前5条）
    private List<ConsultationRecordResponseDTO> recentList;

    @Data
    @Builder
    public static class StatCard {
        private String title;
        private String value;
        //较昨日变化百分比
        private Double trend;
        private String icon;
        private String color;
        private String bg;
    }

    @Data
    @Builder
    public static class LineChart {
        private List<String> dates;
        private List<Long> ai;
        private List<Long> diary;
    }

    @Data
    @Builder
    public static class PieItem {
        private Long value;
        private String name;
        private String color;
    }
}
