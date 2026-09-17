package com.example.aispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aispringboot.DTO.response.ConsultationRecordResponseDTO;
import com.example.aispringboot.DTO.response.DashboardResponseDTO;
import com.example.aispringboot.DTO.response.EmotionStatsResponseDTO;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.entity.ConsultationSession;
import com.example.aispringboot.entity.EmotionDiary;
import com.example.aispringboot.entity.KnowledgeArticle;
import com.example.aispringboot.entity.User;
import com.example.aispringboot.mapper.CosultationSessionMapper;
import com.example.aispringboot.mapper.EmotionDiaryMapper;
import com.example.aispringboot.mapper.KnowledgeArticleMapper;
import com.example.aispringboot.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 数据看板服务
@Service
public class DashboardService {
    private static final DateTimeFormatter MONTH_DAY_FMT = DateTimeFormatter.ofPattern("MM-dd");

    //情绪中文名 -> 图表颜色
    private static final Map<String, String> MOOD_COLORS = Map.of(
            "愉悦", "#67c23a",
            "平静", "#409eff",
            "平稳", "#00b894",
            "焦虑", "#e6a23c",
            "低落", "#909399",
            "愤怒", "#f56c6c");

    @Resource
    private CosultationSessionMapper sessionMapper;

    @Resource
    private EmotionDiaryMapper diaryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private KnowledgeArticleMapper articleMapper;

    @Resource
    private ConsultationRecordService consultationRecordService;

    @Resource
    private EmotionDiaryService emotionDiaryService;

    //看板总览
    public Result<DashboardResponseDTO> overview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);

        //今日/昨日咨询会话数
        long todaySessions = sessionMapper.selectCount(new LambdaQueryWrapper<ConsultationSession>()
                .ge(ConsultationSession::getStartedAt, todayStart)
                .lt(ConsultationSession::getStartedAt, todayStart.plusDays(1)));
        long yesterdaySessions = sessionMapper.selectCount(new LambdaQueryWrapper<ConsultationSession>()
                .ge(ConsultationSession::getStartedAt, yesterdayStart)
                .lt(ConsultationSession::getStartedAt, todayStart));
        double trend = yesterdaySessions == 0
                ? (todaySessions > 0 ? 100.0 : 0.0)
                : Math.round((todaySessions - yesterdaySessions) * 1000.0 / yesterdaySessions) / 10.0;

        long userCount = userMapper.selectCount(null);
        long articleCount = articleMapper.selectCount(null);

        //会话记录（含风险等级），供统计与最近列表复用
        List<ConsultationSession> allSessions = sessionMapper.selectList(null);
        List<ConsultationRecordResponseDTO> records = consultationRecordService.buildRecords(allSessions);
        long riskCount = records.stream().filter(r -> "高".equals(r.getRisk())).count();

        List<DashboardResponseDTO.StatCard> statCards = List.of(
                DashboardResponseDTO.StatCard.builder()
                        .title("今日AI咨询").value(String.valueOf(todaySessions)).trend(trend)
                        .icon("ChatDotRound").color("#409eff").bg("#ecf5ff").build(),
                DashboardResponseDTO.StatCard.builder()
                        .title("注册用户").value(String.valueOf(userCount)).trend(0.0)
                        .icon("User").color("#67c23a").bg("#f0f9eb").build(),
                DashboardResponseDTO.StatCard.builder()
                        .title("知识文章").value(String.valueOf(articleCount)).trend(0.0)
                        .icon("Document").color("#e6a23c").bg("#fdf6ec").build(),
                DashboardResponseDTO.StatCard.builder()
                        .title("高风险预警").value(String.valueOf(riskCount)).trend(0.0)
                        .icon("Warning").color("#f56c6c").bg("#fef0f0").build());

        //近7天趋势：AI会话数 / 情绪日记数
        List<String> dates = new ArrayList<>();
        List<Long> aiSeries = new ArrayList<>();
        List<Long> diarySeries = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.format(MONTH_DAY_FMT));
            LocalDate target = day;
            aiSeries.add(allSessions.stream()
                    .filter(s -> s.getStartedAt() != null && s.getStartedAt().toLocalDate().equals(target))
                    .count());
            diarySeries.add(diaryMapper.selectCount(
                    new LambdaQueryWrapper<EmotionDiary>().eq(EmotionDiary::getLogDate, target)));
        }
        DashboardResponseDTO.LineChart lineChart = DashboardResponseDTO.LineChart.builder()
                .dates(dates).ai(aiSeries).diary(diarySeries).build();

        //情绪分布饼图
        List<DashboardResponseDTO.PieItem> pieChart = emotionDiaryService.distributionList().stream()
                .map(item -> DashboardResponseDTO.PieItem.builder()
                        .value(item.getCount())
                        .name(item.getName())
                        .color(MOOD_COLORS.getOrDefault(item.getName(), "#909399"))
                        .build())
                .toList();

        //最近5条咨询
        List<ConsultationRecordResponseDTO> recentList = records.stream().limit(5).toList();

        return Result.success(DashboardResponseDTO.builder()
                .statCards(statCards)
                .lineChart(lineChart)
                .pieChart(pieChart)
                .recentList(recentList)
                .build());
    }
}
