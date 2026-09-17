package com.example.aispringboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aispringboot.DTO.response.ChatMessageDTO;
import com.example.aispringboot.DTO.response.ConsultationRecordResponseDTO;
import com.example.aispringboot.common.PageResult;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.entity.ConsultationMessage;
import com.example.aispringboot.entity.ConsultationSession;
import com.example.aispringboot.entity.User;
import com.example.aispringboot.mapper.ConsultationMessageMapper;
import com.example.aispringboot.mapper.CosultationSessionMapper;
import com.example.aispringboot.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// AI 咨询会话记录服务（管理端）
@Service
public class ConsultationRecordService {
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private CosultationSessionMapper sessionMapper;

    @Resource
    private ConsultationMessageMapper messageMapper;

    @Resource
    private UserMapper userMapper;

    //分页查询（聚合消息数/风险数/时长后内存过滤分页）
    public Result<PageResult<ConsultationRecordResponseDTO>> page(long page, long pageSize, String keyword,
                                                                  String risk, String channel,
                                                                  String startDate, String endDate) {
        List<ConsultationSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<ConsultationSession>()
                        .orderByDesc(ConsultationSession::getStartedAt)
                        .orderByDesc(ConsultationSession::getId));
        List<ConsultationRecordResponseDTO> rows = buildRecords(sessions);

        //条件过滤
        if (StrUtil.isNotBlank(keyword)) {
            rows = rows.stream()
                    .filter(r -> (r.getUser() != null && r.getUser().contains(keyword))
                            || (r.getSummary() != null && r.getSummary().contains(keyword)))
                    .collect(Collectors.toList());
        }
        if (StrUtil.isNotBlank(risk)) {
            rows = rows.stream().filter(r -> r.getRisk().equals(risk)).collect(Collectors.toList());
        }
        if (StrUtil.isNotBlank(channel)) {
            rows = rows.stream().filter(r -> r.getChannel().equals(channel)).collect(Collectors.toList());
        }
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        if (start != null || end != null) {
            rows = rows.stream().filter(r -> {
                if (r.getStartTime() == null || r.getStartTime().isEmpty()) {
                    return false;
                }
                LocalDate day = LocalDate.parse(r.getStartTime().substring(0, 10));
                return (start == null || !day.isBefore(start)) && (end == null || !day.isAfter(end));
            }).collect(Collectors.toList());
        }

        //内存分页
        long total = rows.size();
        int from = (int) Math.min(total, Math.max(0, (page - 1) * pageSize));
        int to = (int) Math.min(total, from + pageSize);
        List<ConsultationRecordResponseDTO> pageList = from >= to ? List.of() : rows.subList(from, to);
        return Result.success(PageResult.of(pageList, total));
    }

    //会话聊天详情（气泡数据）
    public Result<List<ChatMessageDTO>> messages(Long sessionId) {
        List<ConsultationMessage> msgs = messageMapper.selectList(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId)
                        .orderByAsc(ConsultationMessage::getCreatedAt)
                        .orderByAsc(ConsultationMessage::getId));
        List<ChatMessageDTO> chat = msgs.stream()
                .map(m -> new ChatMessageDTO(
                        "user".equals(m.getSenderType()) ? "user" : "ai",
                        m.getContent() == null ? "" : m.getContent(),
                        m.getCreatedAt() == null ? "" : m.getCreatedAt().format(TIME_FMT)))
                .collect(Collectors.toList());
        return Result.success(chat);
    }

    //构建会话记录DTO（供本服务与看板复用）
    public List<ConsultationRecordResponseDTO> buildRecords(List<ConsultationSession> sessions) {
        if (sessions.isEmpty()) {
            return List.of();
        }
        //用户昵称
        Set<Long> userIds = sessions.stream().map(ConsultationSession::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        //消息数 / 风险消息数 / 最后一条消息时间
        Map<Long, Long> msgCount = groupCount(false);
        Map<Long, Long> riskCount = groupCount(true);
        Map<Long, LocalDateTime> lastTimeMap = groupLastTime();
        LocalDateTime now = LocalDateTime.now();

        List<ConsultationRecordResponseDTO> list = new ArrayList<>();
        for (ConsultationSession session : sessions) {
            ConsultationRecordResponseDTO dto = new ConsultationRecordResponseDTO();
            dto.setId(session.getId());
            User user = userMap.get(session.getUserId());
            dto.setUser(user != null ? user.getDisplayName() : "未知用户");
            dto.setChannel("AI对话");
            dto.setSummary(session.getSessionTitle());
            long risks = riskCount.getOrDefault(session.getId(), 0L);
            dto.setRisk(risks == 0 ? "低" : risks == 1 ? "中" : "高");
            dto.setRiskMessages((int) risks);
            long count = msgCount.getOrDefault(session.getId(), 0L);
            dto.setMessages((int) count);
            LocalDateTime lastTime = lastTimeMap.get(session.getId());
            if (lastTime != null) {
                long minutes = Math.max(1, Duration.between(session.getStartedAt(), lastTime).toMinutes());
                dto.setDuration(minutes + "分钟");
            } else {
                dto.setDuration("—");
            }
            dto.setStartTime(session.getStartedAt() != null ? session.getStartedAt().format(DATETIME_FMT) : "");
            //最后一小时内有新消息视为进行中
            dto.setStatus(lastTime != null && Duration.between(lastTime, now).toHours() < 1 ? "进行中" : "已结束");
            list.add(dto);
        }
        return list;
    }

    /* ---------------- 私有方法 ---------------- */

    private Map<Long, Long> groupCount(boolean riskOnly) {
        QueryWrapper<ConsultationMessage> qw = new QueryWrapper<>();
        qw.select("session_id", "COUNT(*) AS cnt");
        if (riskOnly) {
            qw.eq("is_risk", 1);
        }
        qw.groupBy("session_id");
        return messageMapper.selectMaps(qw).stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("session_id")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()));
    }

    private Map<Long, LocalDateTime> groupLastTime() {
        QueryWrapper<ConsultationMessage> qw = new QueryWrapper<>();
        qw.select("session_id", "MAX(created_at) AS last_time").groupBy("session_id");
        return messageMapper.selectMaps(qw).stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("session_id")).longValue(),
                        m -> toLocalDateTime(m.get("last_time"))));
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return null;
    }

    private LocalDate parseDate(String date) {
        if (StrUtil.isBlank(date)) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
