/*
 Navicat Premium Dump SQL

 Source Server         : sql
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : mind_assistant

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 17/09/2026 13:20:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_analysis_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_analysis_task`;
CREATE TABLE `ai_analysis_task`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型 emotion_diary/consultation_message 等',
  `biz_id` bigint UNSIGNED NOT NULL COMMENT '关联业务ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
  `result` json NULL COMMENT '分析结果(JSON)',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_task_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE,
  INDEX `idx_task_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_task_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI分析任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_analysis_task
-- ----------------------------
INSERT INTO `ai_analysis_task` VALUES (1, 2, 'emotion_diary', 1, 'SUCCESS', '{\"score\": 0.8, \"emotion\": \"焦虑\", \"suggestion\": \"建议进行4-7-8呼吸法\"}', NULL, '2026-08-18 22:45:00', '2026-08-18 22:45:00');
INSERT INTO `ai_analysis_task` VALUES (2, 2, 'consultation_message', 1, 'SUCCESS', '{\"risk\": false, \"score\": 0.75, \"emotion\": \"焦虑\"}', NULL, '2026-09-03 23:31:00', '2026-09-03 23:31:00');
INSERT INTO `ai_analysis_task` VALUES (3, 3, 'emotion_diary', 8, 'SUCCESS', '{\"score\": 0.85, \"emotion\": \"压力\", \"suggestion\": \"建议梳理可控/不可控清单\"}', NULL, '2026-08-05 23:05:00', '2026-08-05 23:05:00');
INSERT INTO `ai_analysis_task` VALUES (4, 4, 'consultation_message', 15, 'SUCCESS', '{\"risk\": true, \"score\": 0.9, \"emotion\": \"焦虑\", \"suggestion\": \"触发危机干预，建议联系热线\"}', NULL, '2026-09-03 01:35:00', '2026-09-03 01:35:00');

-- ----------------------------
-- Table structure for consultation_message
-- ----------------------------
DROP TABLE IF EXISTS `consultation_message`;
CREATE TABLE `consultation_message`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint UNSIGNED NOT NULL COMMENT '会话ID',
  `sender_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送者类型 user/assistant/system',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT '消息类型 text/image/voice/file/system 等',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `emotion_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '情绪标签',
  `ai_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI模型标识',
  `tokens` int NOT NULL DEFAULT 0 COMMENT '消耗token数',
  `is_risk` tinyint NOT NULL DEFAULT 0 COMMENT '1检测到风险内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_msg_session`(`session_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_msg_session` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of consultation_message
-- ----------------------------
INSERT INTO `consultation_message` VALUES (1, 1, 'user', 'text', '最近总是睡不着，躺下就开始想各种事，越想越清醒。', '焦虑', NULL, 28, 0, '2026-09-03 23:30:00');
INSERT INTO `consultation_message` VALUES (2, 1, 'assistant', 'text', '听起来这段时间你的大脑在夜里很难「下班」。我想先了解一点：你通常躺下后大概多久能睡着？白天的精神状态怎么样？', NULL, 'mindcare-v1', 52, 0, '2026-09-03 23:31:00');
INSERT INTO `consultation_message` VALUES (3, 1, 'user', 'text', '可能一两个小时吧，有时候三点多还醒着。白天特别困，但晚上又很精神。', '焦虑', NULL, 34, 0, '2026-09-03 23:34:00');
INSERT INTO `consultation_message` VALUES (4, 1, 'assistant', 'text', '这种「白天困、晚上清醒」的节律颠倒很常见，它往往和睡前的刺激积累有关。\n\n可以试试三件小事：\n1. 睡前一小时把灯光调暗，停止刷短视频；\n2. 躺下 20 分钟没睡着就离开床，到昏暗处做点无聊的事；\n3. 不管几点睡，第二天固定时间起床。\n\n如果这种情况持续超过一个月，建议到睡眠门诊或找咨询师做一次系统评估。', NULL, 'mindcare-v1', 118, 0, '2026-09-03 23:36:00');
INSERT INTO `consultation_message` VALUES (5, 1, 'user', 'text', '好的，我今晚试试。谢谢你。', '平静', NULL, 14, 0, '2026-09-03 23:48:00');
INSERT INTO `consultation_message` VALUES (6, 1, 'assistant', 'text', '不客气，能为自己做点什么，本身就是一种力量。如果夜里情绪很难受，随时可以再来找我聊聊。🌙', NULL, 'mindcare-v1', 36, 0, '2026-09-03 23:50:00');
INSERT INTO `consultation_message` VALUES (7, 2, 'user', 'text', '室友说了一句让我很不舒服的话，我当时什么都没说，现在越想越生气。', '愤怒', NULL, 32, 0, '2026-08-30 21:50:00');
INSERT INTO `consultation_message` VALUES (8, 2, 'assistant', 'text', '当时没说出来，可能是你更在意关系的和平，这本身没有错。\n\n要不要一起试试用「观察—感受—需要—请求」的结构，把那句话重新组织一遍？你先说说，她具体说了什么？', NULL, 'mindcare-v1', 76, 0, '2026-08-30 21:52:00');
INSERT INTO `consultation_message` VALUES (9, 2, 'user', 'text', '她说「你怎么又在玩手机，怪不得效率这么低」。', '委屈', NULL, 26, 0, '2026-08-30 21:58:00');
INSERT INTO `consultation_message` VALUES (10, 2, 'assistant', 'text', '我们可以这样表达：\n\n「昨天你说我『又在玩手机』的时候（观察），我有点委屈（感受），因为我其实刚写完一版方案，需要休息一下（需要）。下次如果你担心进度，可以直接问我一句进度怎么样（请求）。」\n\n注意：请求要具体、可执行，对方才更容易接住。', NULL, 'mindcare-v1', 104, 0, '2026-08-30 22:10:00');
INSERT INTO `consultation_message` VALUES (11, 3, 'user', 'text', '项目 deadline 提前了，我这两天心跳一直很快，晚上也睡不好。', '压力', NULL, 30, 0, '2026-08-05 23:40:00');
INSERT INTO `consultation_message` VALUES (12, 3, 'assistant', 'text', '身体已经在用「心跳加快、睡不好」提醒你：负荷偏高了。\n\n先做一个 30 秒的小练习——吸气 4 秒、屏息 7 秒、呼气 8 秒，做 4 轮。做完我们再一起把事情拆成「可控」和「不可控」两列，好吗？', NULL, 'mindcare-v1', 92, 0, '2026-08-05 23:42:00');
INSERT INTO `consultation_message` VALUES (13, 3, 'user', 'text', '做完确实好一点了。可控的……好像只有「今晚先睡」和「明天上午先做最难的部分」。', '平静', NULL, 40, 0, '2026-08-06 00:10:00');
INSERT INTO `consultation_message` VALUES (14, 3, 'assistant', 'text', '你已经找到最重要的两件事了。今晚的目标不是完成，而是恢复体力。\n\n去洗漱、调暗灯光，把明天的第一步写在纸上，然后允许自己休息。明天上午 9 点，从最难的那部分开始 25 分钟。', NULL, 'mindcare-v1', 86, 0, '2026-08-06 00:15:00');
INSERT INTO `consultation_message` VALUES (15, 4, 'user', 'text', '明天答辩，我现在特别慌，感觉什么都记不住。', '焦虑', NULL, 22, 0, '2026-09-03 01:30:00');
INSERT INTO `consultation_message` VALUES (16, 4, 'assistant', 'text', '紧张说明你很在乎，这是正常的。\n\n现在这个时间点，最有帮助的不是继续背稿，而是让身体先降下来：做 4 轮 4-7-8 呼吸，然后把「最坏的结果」写下来，再写一句「如果真发生了，我会怎么应对」。\n\n如果此刻你感到强烈的绝望或有伤害自己的念头，请立刻联系身边信任的人，或拨打心理援助热线。你不需要一个人扛。', NULL, 'mindcare-v1', 112, 1, '2026-09-03 01:40:00');

-- ----------------------------
-- Table structure for consultation_session
-- ----------------------------
DROP TABLE IF EXISTS `consultation_session`;
CREATE TABLE `consultation_session`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `session_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '新的对话' COMMENT '会话标题',
  `started_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `last_emotion_analysis` json NULL COMMENT '最近一次情绪分析结果(JSON)',
  `last_emotion_updated_at` datetime NULL DEFAULT NULL COMMENT '最近情绪分析更新时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1正常 0已删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_user`(`user_id` ASC, `started_at` ASC) USING BTREE,
  CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of consultation_session
-- ----------------------------
INSERT INTO `consultation_session` VALUES (1, 2, '最近总是睡不着', '2026-09-03 23:30:00', '{\"score\": 0.75, \"emotion\": \"焦虑\", \"keywords\": [\"失眠\", \"焦虑\"]}', '2026-09-03 23:50:00', 1, '2026-09-03 23:30:00', '2026-09-03 23:50:00');
INSERT INTO `consultation_session` VALUES (2, 2, '怎么和室友表达我的感受', '2026-08-30 21:50:00', '{\"score\": 0.6, \"emotion\": \"愤怒\", \"keywords\": [\"室友\", \"沟通\"]}', '2026-08-30 22:10:00', 1, '2026-08-30 21:50:00', '2026-08-30 22:10:00');
INSERT INTO `consultation_session` VALUES (3, 3, '工作压力好大', '2026-08-05 23:40:00', '{\"score\": 0.8, \"emotion\": \"压力\", \"keywords\": [\"deadline\", \"心跳\"]}', '2026-08-06 00:15:00', 1, '2026-08-05 23:40:00', '2026-08-06 00:15:00');
INSERT INTO `consultation_session` VALUES (4, 4, '答辩前很焦虑', '2026-09-03 01:30:00', '{\"score\": 0.9, \"emotion\": \"焦虑\", \"keywords\": [\"答辩\", \"恐慌\"]}', '2026-09-03 01:40:00', 1, '2026-09-03 01:30:00', '2026-09-03 01:40:00');
INSERT INTO `consultation_session` VALUES (5, 1, 'AI心理健康助手 - 2026/6/20 14:14:27', '2026-09-16 23:40:26', NULL, NULL, 1, '2026-09-16 23:40:26', '2026-09-16 23:40:26');
INSERT INTO `consultation_session` VALUES (6, 1, 'AI心理健康助手 - 2026/6/20 14:14:27', '2026-09-16 23:47:30', NULL, NULL, 1, '2026-09-16 23:47:30', '2026-09-16 23:47:30');

-- ----------------------------
-- Table structure for emotion_diary
-- ----------------------------
DROP TABLE IF EXISTS `emotion_diary`;
CREATE TABLE `emotion_diary`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日记ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `mood_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '情绪类型 happy/calm/neutral/anxious/sad/angry',
  `mood_score` tinyint NOT NULL DEFAULT 3 COMMENT '心情指数 1-5',
  `emotion_tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '情绪标签(JSON数组)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '日记正文',
  `weather` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '天气 sunny/cloudy/rainy/snowy',
  `sleep_hours` decimal(3, 1) NULL DEFAULT NULL COMMENT '睡眠时长(小时)',
  `is_private` tinyint NOT NULL DEFAULT 1 COMMENT '1私密 0公开',
  `log_date` date NOT NULL COMMENT '记录日期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_emotion_user_date`(`user_id` ASC, `log_date` ASC) USING BTREE,
  INDEX `idx_emotion_user_time`(`user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_emotion_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '心情日记表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of emotion_diary
-- ----------------------------
INSERT INTO `emotion_diary` VALUES (1, 2, 'anxious', 2, '[\"焦虑\",\"疲惫\"]', '今天开会被临时点名汇报，脑子一片空白。晚上回来还是有点心慌，做了 4 轮深呼吸稍微好一点。', 'cloudy', 5.5, 1, '2026-08-18', '2026-08-18 22:40:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (2, 2, 'neutral', 3, '[\"平静\"]', '把待办清单写下来了，发现真正紧急的只有两件。心里踏实了一点。', 'sunny', 6.5, 1, '2026-08-20', '2026-08-20 23:05:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (3, 2, 'calm', 4, '[\"放松\",\"满足\"]', '晚上去江边走了 40 分钟，风吹着很舒服，很久没有这么安静地和自己待着了。', 'sunny', 7.2, 1, '2026-08-24', '2026-08-24 22:15:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (4, 2, 'happy', 5, '[\"开心\",\"被理解\"]', '和室友聊了很久，她说「你已经做得很好了」，我居然哭了。原来我一直在等这句话。', 'sunny', 7.8, 0, '2026-08-30', '2026-08-30 23:30:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (5, 2, 'sad', 2, '[\"低落\",\"自我怀疑\"]', '又失眠了，凌晨三点还在想上周那句话是不是说错了。', 'rainy', 4.0, 1, '2026-09-03', '2026-09-04 03:10:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (6, 2, 'neutral', 3, '[\"平稳\"]', '开始用「担忧时间」的方法，把担心的事集中到晚上八点想。白天确实清爽了不少。', 'cloudy', 6.8, 1, '2026-09-07', '2026-09-07 22:50:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (7, 2, 'calm', 4, '[\"平静\",\"有希望\"]', '复测了 PHQ-9，从 11 分降到 7 分。虽然还是轻度，但我知道方向是对的。', 'sunny', 7.0, 1, '2026-09-08', '2026-09-08 22:20:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (8, 3, 'anxious', 2, '[\"压力\",\"紧绷\"]', '项目 deadline 提前了，一整天都在赶进度，晚上心跳还是很快。', 'cloudy', 5.0, 1, '2026-08-05', '2026-08-05 23:00:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (9, 3, 'happy', 5, '[\"轻松\",\"成就感\"]', '终于交稿了！奖励自己看了场电影，笑到肚子疼。', 'sunny', 8.0, 0, '2026-08-12', '2026-08-12 21:40:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (10, 3, 'calm', 4, '[\"平静\"]', '做了正念冥想的引导练习，专注呼吸 15 分钟，久违的安静。', 'sunny', 7.5, 1, '2026-09-05', '2026-09-05 21:30:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (11, 4, 'anxious', 2, '[\"焦虑\",\"失眠\"]', '一想到明天的答辩就睡不着，手心一直在出汗。', 'rainy', 4.5, 1, '2026-09-02', '2026-09-03 01:20:00', '2026-09-10 21:46:59');
INSERT INTO `emotion_diary` VALUES (12, 4, 'angry', 2, '[\"烦躁\",\"委屈\"]', '组里有人一直不干活，最后却是我被说进度慢。真的很憋屈。', 'cloudy', 6.0, 1, '2026-09-06', '2026-09-06 23:15:00', '2026-09-10 21:46:59');

-- ----------------------------
-- Table structure for knowledge_article
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_article`;
CREATE TABLE `knowledge_article`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `category_id` bigint UNSIGNED NOT NULL COMMENT '分类ID',
  `title` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `summary` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正文(Markdown)',
  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签(逗号分隔)',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '编辑部' COMMENT '作者',
  `read_minutes` int NOT NULL DEFAULT 5 COMMENT '预计阅读时长(分钟)',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` int NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '1置顶',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1已发布 0草稿 2下架',
  `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_cate`(`category_id` ASC) USING BTREE,
  INDEX `idx_article_status`(`status` ASC, `published_at` ASC) USING BTREE,
  FULLTEXT INDEX `ft_article_title`(`title`, `summary`),
  CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `knowledge_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_article
-- ----------------------------
INSERT INTO `knowledge_article` VALUES (1, 2, '焦虑不是敌人：与焦虑共处的 5 个练习', '焦虑本身不是问题，真正消耗我们的是「对焦虑的焦虑」。本文提供 5 个可立即上手的练习。', '/img/article/a1.jpg', '## 一、先承认：焦虑是身体的保护机制\n\n当你心跳加快、手心出汗时，身体并不是在「背叛」你，它在说：**我检测到风险了**。\n\n> 焦虑 = 对未来的担忧 + 身体的唤醒反应\n\n## 二、5 个练习\n\n### 1. 4-7-8 呼吸法\n吸气 4 秒，屏息 7 秒，呼气 8 秒，重复 4 轮。延长呼气可以激活副交感神经。\n\n### 2. 焦虑日记：把「模糊」变「具体」\n写下三栏：我担心的事 / 发生的概率 / 如果真的发生我会怎么做。\n\n### 3. 5-4-3-2-1 着陆技术\n说出 5 样你看到的、4 样能摸到的、3 种听到的、2 种闻到的、1 种尝到的。\n\n### 4. 设定「担忧时间」\n每天固定 15 分钟专门用来担心，其他时间出现担忧时告诉自己：留到那个时段。\n\n### 5. 降低咖啡因摄入\n咖啡因的半衰期约 5 小时，下午 3 点后的一杯咖啡，可能正在为深夜的焦虑「续费」。\n\n## 三、什么时候需要求助\n\n如果焦虑已经持续 **两周以上**，并影响到睡眠、食欲、工作或人际关系，请考虑寻求专业帮助。', '焦虑,CBT,自助练习', '李明', 8, 15230, 864, 412, 1, 1, '2026-07-12 10:00:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `knowledge_article` VALUES (2, 1, '抑郁情绪 vs 抑郁症：你需要知道的区别', '难过了一周就是抑郁吗？这篇文章帮你理清「情绪」与「障碍」的边界。', '/img/article/a2.jpg', '## 一、核心区别\n\n| 维度 | 抑郁情绪 | 抑郁症 |\n|---|---|---|\n| 持续时间 | 数小时~数天 | 持续 ≥ 2 周 |\n| 诱因 | 通常有明确事件 | 可有可无 |\n| 影响 | 基本可维持生活 | 明显损害社会功能 |\n| 波动 | 会因好事好转 | 愉悦感普遍缺失 |\n\n## 二、抑郁的三大核心症状\n\n1. **心境低落**：几乎每天大部分时间都感到悲伤、空虚\n2. **兴趣减退**：以前喜欢的事也不想做了（快感缺失）\n3. **精力下降**：休息了也依然疲惫\n\n## 三、常见的伴随症状\n\n- 睡眠紊乱（入睡困难 / 早醒 / 嗜睡）\n- 食欲与体重明显变化\n- 注意力难以集中、自我评价过低\n- 反复出现死亡或自伤念头 ⚠️\n\n## 四、重要提醒\n\n**出现自伤、自杀念头时，请立即联系专业人员或拨打心理援助热线。**\n\n> 抑郁症是一种可以被治疗的疾病，不是「想不开」，更不是「意志力差」。', '抑郁,科普,识别', '编辑部', 6, 21045, 1320, 687, 1, 1, '2026-06-28 09:30:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `knowledge_article` VALUES (3, 3, '睡前一小时，决定你的睡眠质量', '睡不着往往不是「躺下之后」的问题，而是「躺下之前」就已经注定了。', '/img/article/a3.jpg', '## 一、睡眠不是开关，是滑梯\n\n从清醒到入睡需要一个**降速过程**，睡前一小时就是这段滑梯。\n\n## 二、睡前 60 分钟清单\n\n- ✅ 调暗灯光，把主灯换成暖色小灯\n- ✅ 用纸质书代替短视频（蓝光 + 信息刺激是双杀）\n- ✅ 写下明天的待办清单，让大脑「卸载」\n- ✅ 温水泡脚或淋浴，体温下降会促进入睡\n- ❌ 睡前饮酒（酒精让人入睡快，但会破坏深睡）\n- ❌ 在床上处理工作和争吵\n\n## 三、躺下 20 分钟还睡不着怎么办\n\n**离开床**。到昏暗的客厅做点无聊的事，有困意再回到床上。\n\n这是刺激控制疗法的核心：让大脑重新建立「床 = 睡觉」的联结。\n\n## 四、CBT-I 小贴士\n\n固定起床时间比固定入睡时间更重要。哪怕昨晚只睡了 4 小时，也请在同一时间起床。', '睡眠,CBT-I,失眠', '孙琪', 7, 9840, 522, 301, 0, 1, '2026-08-02 20:15:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `knowledge_article` VALUES (4, 4, '如何停止精神内耗：给大脑装一个「关闭标签页」', '反刍思维就像浏览器开了 47 个标签页，而你还在不停地点开新的。', '/img/article/a4.jpg', '## 一、什么是反刍思维\n\n反刍（Rumination）是指反复咀嚼同一件事的**原因和后果**，却不产生任何行动。\n\n- 解决问题：我想怎么做 → 有出口\n- 反刍思维：我为什么这么差劲 → 原地打转\n\n## 二、识别信号\n\n- 「如果当时我……」出现 3 次以上\n- 身体开始紧绷、肩膀发酸\n- 刷手机但没有在看内容\n\n## 三、三个干预动作\n\n**1. 命名它**\n对自己说：「我现在正在反刍，这不是思考，是循环。」\n\n**2. 30 秒行动法则**\n问自己：这件事 30 秒内我能做点什么？如果答案是不能，就写进待办清单。\n\n**3. 第三人称视角**\n用「小满，你现在遇到的问题是……」代替「我完蛋了」。研究显示，自我距离化能显著降低情绪强度。\n\n## 四、允许自己「不够好」\n\n内耗的底层往往是**严苛的自我评价标准**。试着把对朋友说的那句话，原封不动地说给自己听。', '内耗,反刍,自我关怀', '张雯', 6, 18760, 1094, 533, 1, 1, '2026-08-18 14:20:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `knowledge_article` VALUES (5, 4, '高敏感人群（HSP）的自我照顾指南', '如果你总是「想太多、感受太强、容易被影响」，这可能不是缺点，而是一种神经特质。', '/img/article/a5.jpg', '## 一、高敏感的四个特征（DOES）\n\n- **D**epth of processing：信息加工更深\n- **O**verstimulation：容易过度刺激\n- **E**motional reactivity：情绪反应更强\n- **S**ensitivity to subtleties：察觉细微刺激\n\n## 二、日常自我照顾清单\n\n| 场景 | 建议 |\n|---|---|\n| 社交后 | 预留 30 分钟独处「回血」 |\n| 工作 | 用降噪耳机划分专注区块 |\n| 冲突 | 允许自己说「我需要想一想再回复」 |\n| 睡前 | 减少刺激性内容输入 |\n\n## 三、重新定义敏感\n\n敏感不是玻璃心，而是**高分辨率地活着**。同一份感受力，让你更容易受伤，也让你更容易被一首歌打动。', '高敏感,HSP,自我成长', '张雯', 6, 7620, 438, 266, 0, 1, '2026-08-25 11:00:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `knowledge_article` VALUES (6, 2, '职场压力管理：把「必须」换成「可以」', '压垮我们的往往不是工作量，而是脑海里那些绝对化的句式。', '/img/article/a6.jpg', '## 一、三种「语言陷阱」\n\n| 陷阱句式 | 背后的信念 | 替换说法 |\n|---|---|---|\n| 我必须做到完美 | 完美主义 | 我可以先完成 80% |\n| 我不能拒绝 | 讨好型 | 我可以先评估自己的余量 |\n| 我应该更努力 | 内疚驱动 | 我可以在合理的范围内努力 |\n\n## 二、压力的「可控 / 不可控」清单\n\n把所有压力事件分成两列：\n\n- **可控**：沟通节奏、任务优先级、休息安排\n- **不可控**：公司决策、同事性格、行业周期\n\n把精力 100% 投在第一列。\n\n## 三、职业倦怠的三个维度\n\n1. 情绪耗竭\n2. 去人格化（对工作冷漠、对同事疏离）\n3. 个人成就感降低\n\n若三项都明显，请优先安排休假与专业支持。', '职场,倦怠,压力管理', '刘洋', 7, 11350, 617, 289, 0, 1, '2026-09-01 09:00:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `knowledge_article` VALUES (7, 5, '吵架也能增进感情：非暴力沟通四步法', '大多数争吵的升级，不是因为观点不同，而是因为表达方式伤了人。', '/img/article/a7.jpg', '## 四步法：观察 → 感受 → 需要 → 请求\n\n**❌ 反例**\n「你怎么老是不回我消息，你根本不在乎我！」\n\n**✅ 正例**\n- 观察：昨天我发的三条消息，到今天早上都没有收到回复\n- 感受：我有点不安，也有点失落\n- 需要：因为我很看重我们之间的连接感\n- 请求：以后忙的时候，能不能先给我一个「稍后回复」？\n\n## 两个提醒\n\n1. **区分感受和评判**：「我觉得被忽视」是评判，「我感到失落」是感受。\n2. **请求要具体可执行**：说「多关心我」不如说「每天睡前聊 10 分钟」。', '沟通,非暴力沟通,人际关系', '陈静', 6, 14380, 902, 471, 0, 1, '2026-09-03 16:40:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `knowledge_article` VALUES (8, 6, '恋爱中的「情绪账户」：如何存钱而不是透支', '每一段亲密关系都有一本看不见的账，日常的小互动就是存取款。', '/img/article/a8.jpg', '## 一、什么是情绪账户\n\n心理学家 Gottman 提出：关系中的每一次互动都是一次**存款或取款**。\n\n- 存款：回应对方的小分享、说谢谢、记得对方提过的小事\n- 取款：冷嘲热讽、翻旧账、忽视对方\n\n## 二、稳定的黄金比例\n\n研究表明，健康关系中正向互动与负向互动的比例约为 **5 : 1**。\n\n## 三、三个高性价比的「存款」动作\n\n1. 对方说话时放下手机，看着对方\n2. 每天一句具体的欣赏（「你今天处理那件事很有耐心」）\n3. 吵架时先修复情绪，再讨论问题\n\n## 四、当关系持续消耗你\n\n如果一段关系中长期感到被贬低、被控制、被威胁，请务必寻求专业支持，必要时联系相关援助机构。', '亲密关系,情绪账户,婚恋', '陈静', 7, 8910, 486, 312, 0, 1, '2026-09-05 15:10:00', '2026-09-10 21:46:58', '2026-09-10 21:46:58');

-- ----------------------------
-- Table structure for knowledge_category
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_category`;
CREATE TABLE `knowledge_category`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主题色',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识文章分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_category
-- ----------------------------
INSERT INTO `knowledge_category` VALUES (1, '情绪管理', 'Sunny', '#F6A623', 1, 1, '2026-09-10 21:46:58');
INSERT INTO `knowledge_category` VALUES (2, '压力与焦虑', 'Lightning', '#E6683C', 2, 1, '2026-09-10 21:46:58');
INSERT INTO `knowledge_category` VALUES (3, '睡眠健康', 'Moon', '#6C5CE7', 3, 1, '2026-09-10 21:46:58');
INSERT INTO `knowledge_category` VALUES (4, '自我成长', 'Star', '#00B894', 4, 1, '2026-09-10 21:46:58');
INSERT INTO `knowledge_category` VALUES (5, '人际交往', 'ChatDotRound', '#0984E3', 5, 1, '2026-09-10 21:46:58');
INSERT INTO `knowledge_category` VALUES (6, '婚恋情感', 'Heart', '#E84393', 6, 1, '2026-09-10 21:46:58');

-- ----------------------------
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_info`;
CREATE TABLE `sys_file_info`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '上传用户ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储路径',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访问URL',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型 avatar/article_cover等',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_file_biz`(`biz_type` ASC) USING BTREE,
  CONSTRAINT `fk_file_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_info
-- ----------------------------
INSERT INTO `sys_file_info` VALUES (1, 1, 'admin.png', '/img/avatar/admin.png', '/img/avatar/admin.png', 'image/png', 10240, 'avatar', '2026-09-10 21:46:58');
INSERT INTO `sys_file_info` VALUES (2, 2, 'u2.png', '/img/avatar/u2.png', '/img/avatar/u2.png', 'image/png', 10240, 'avatar', '2026-09-10 21:46:58');
INSERT INTO `sys_file_info` VALUES (3, 3, 'u3.png', '/img/avatar/u3.png', '/img/avatar/u3.png', 'image/png', 10240, 'avatar', '2026-09-10 21:46:58');
INSERT INTO `sys_file_info` VALUES (4, 1, 'a1.jpg', '/img/article/a1.jpg', '/img/article/a1.jpg', 'image/jpeg', 20480, 'article_cover', '2026-09-10 21:46:58');
INSERT INTO `sys_file_info` VALUES (5, 1, 'a2.jpg', '/img/article/a2.jpg', '/img/article/a2.jpg', 'image/jpeg', 20480, 'article_cover', '2026-09-10 21:46:58');
INSERT INTO `sys_file_info` VALUES (6, 1, 'a3.jpg', '/img/article/a3.jpg', '/img/article/a3.jpg', 'image/jpeg', 20480, 'article_cover', '2026-09-10 21:46:58');
INSERT INTO `sys_file_info` VALUES (7, 1, 'a4.jpg', '/img/article/a4.jpg', '/img/article/a4.jpg', 'image/jpeg', 20480, 'article_cover', '2026-09-10 21:46:58');
INSERT INTO `sys_file_info` VALUES (8, 1, 'a5.jpg', '/img/article/a5.jpg', '/img/article/a5.jpg', 'image/jpeg', 20480, 'article_cover', '2026-09-10 21:46:58');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(BCrypt 加密)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像 URL',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `usertype` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色 USER/COUNSELOR/ADMIN',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_user_role`(`usertype` ASC) USING BTREE,
  INDEX `idx_user_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$H45lkYV/.sXN1ekL6GmL.OvBBTGVP91KEieHv2D25L2vXE2E7Q9G.', '系统管理员', '/img/avatar/admin.png', 1, '1990-01-01', '13800000000', 'admin@mindcare.cn', 'ADMIN', 1, '2026-09-10 21:46:58', '2026-09-15 23:21:02');
INSERT INTO `user` VALUES (2, 'demo', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '小满', '/img/avatar/u2.png', 2, '2002-05-18', '13800000002', 'demo@mindcare.cn', 'USER', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (3, 'xiaoyu', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '雨停', '/img/avatar/u3.png', 2, '2000-11-02', '13800000003', 'xiaoyu@mindcare.cn', 'USER', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (4, 'lilei', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '李雷', '/img/avatar/u4.png', 1, '1998-03-25', '13800000004', 'lilei@mindcare.cn', 'USER', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (5, 'lin_li', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '李明', '/img/avatar/c5.png', 1, '1982-06-12', '13900000005', 'liming@mindcare.cn', 'COUNSELOR', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (6, 'zhang_wen', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '张雯', '/img/avatar/c6.png', 2, '1988-09-30', '13900000006', 'zhangwen@mindcare.cn', 'COUNSELOR', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (7, 'wang_hao', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '王浩', '/img/avatar/c7.png', 1, '1985-02-14', '13900000007', 'wanghao@mindcare.cn', 'COUNSELOR', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (8, 'chen_jing', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '陈静', '/img/avatar/c8.png', 2, '1991-12-08', '13900000008', 'chenjing@mindcare.cn', 'COUNSELOR', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (9, 'liu_yang', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '刘洋', '/img/avatar/c9.png', 1, '1987-07-21', '13900000009', 'liuyang@mindcare.cn', 'COUNSELOR', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (10, 'sun_qi', '$2a$10$zXYiYvPLTfxyUx8NxWNP/uNOAYWT5MW1GgFbQx4663MPL2sflnBMK', '孙琪', '/img/avatar/c10.png', 2, '1993-04-16', '13900000010', 'sunqi@mindcare.cn', 'COUNSELOR', 1, '2026-09-10 21:46:58', '2026-09-10 21:46:58');
INSERT INTO `user` VALUES (11, 'test', '$2a$10$mK7UkFy1WC38hydfSHpkAe/TXMkuxp3ki43Rz5oY5jwZdEKN1w7om', 'aifei', NULL, 1, NULL, '17894563547', '3128167168@qq.com', 'USER', 1, '2026-09-16 14:09:37', '2026-09-16 14:09:37');

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `target_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收藏类型 article/counselor 等',
  `target_id` bigint UNSIGNED NOT NULL COMMENT '收藏目标ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fav_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_fav_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------
INSERT INTO `user_favorite` VALUES (1, 2, 'article', 1, '2026-08-20 22:20:00');
INSERT INTO `user_favorite` VALUES (2, 2, 'article', 4, '2026-08-19 08:20:00');
INSERT INTO `user_favorite` VALUES (3, 2, 'article', 8, '2026-09-06 21:10:00');
INSERT INTO `user_favorite` VALUES (4, 3, 'article', 3, '2026-08-10 23:45:00');
INSERT INTO `user_favorite` VALUES (5, 3, 'article', 7, '2026-09-04 20:35:00');
INSERT INTO `user_favorite` VALUES (6, 4, 'article', 1, '2026-08-22 19:40:00');
INSERT INTO `user_favorite` VALUES (7, 4, 'article', 6, '2026-09-02 10:12:00');

SET FOREIGN_KEY_CHECKS = 1;