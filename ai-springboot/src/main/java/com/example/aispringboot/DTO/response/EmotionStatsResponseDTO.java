package com.example.aispringboot.DTO.response;

import lombok.Data;

import java.util.List;

// 情绪统计数据响应
@Data
public class EmotionStatsResponseDTO {
    //日志总数
    private Long total;

    //平均心情指数(1-5)
    private Double avgScore;

    //情绪分布 { name: 中文名, count: 数量 }
    private List<DistributionItem> distribution;

    @Data
    public static class DistributionItem {
        private String name;
        private Long count;

        public DistributionItem() {
        }

        public DistributionItem(String name, Long count) {
            this.name = name;
            this.count = count;
        }
    }
}
