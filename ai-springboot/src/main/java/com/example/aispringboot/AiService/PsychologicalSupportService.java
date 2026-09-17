package com.example.aispringboot.AiService;

import com.example.aispringboot.DTO.command.ConsultationSessionCreateDTO;
import com.example.aispringboot.entity.ConsultationSession;
import com.example.aispringboot.service.ConsultationMessageService;
import com.example.aispringboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class PsychologicalSupportService {
    @Autowired
    private ChatMemory chatMemory;

    @Qualifier("open-ai")
    private final ChatClient chatClient;
    public PsychologicalSupportService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        //创建数据库会话记录
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, createDTO);

        //将初始用户消息保存到Message表
        consultationMessageService.saveUserMessage(consultationSession.getId(), createDTO.getInitialMessage(), null);

        //创建会话信息
        String sessionId = "session_" + consultationSession.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,//24小时
                1,
                "ACTIVE"

        );
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String message) {
        Long dbsessionIdLong = extractSessionId(sessionId);
        if (dbsessionIdLong == null) {
            return Flux.error(new RuntimeException("会话ID格式错误"));
        }

        // 保存用户消息到数据库
        consultationMessageService.saveUserMessage(dbsessionIdLong, message, null);

        String conversationId = "conversation_" + sessionId;

        // 构建 Prompt:系统提示词 + 对话历史由 ChatMemory advisor 自动注入
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(PromptManafe.PSYCHLOGICAL_SUPPORT_SYSTEM_PROMPT),
                new UserMessage(message)
        ));

        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt(prompt)
                .advisors(advisorSpec -> advisorSpec
                        .param("chat_memory_conversation_id", conversationId)
                        .param("chat_memory_retrieve_size", 20))
                .stream()
                .content()
                .doOnNext(fragment -> {
                    fullResponse.append(fragment);
                })
                .doOnComplete(() -> {
                    String completeResponse = fullResponse.toString();
                    // AI 回复保存到数据库
                    consultationMessageService.saveAiMessage(dbsessionIdLong, completeResponse, "openai");
                })
                .doOnError(error -> {
                    // 出错也记录一下(可选)
                    error.printStackTrace();
                });
    }

    //获取参数中的sessionId
    private Long extractSessionId(String sessionId){
        if (sessionId != null && sessionId.startsWith("session_")) {
            String idStr = sessionId.substring("session_".length());
            return Long.parseLong(idStr);
        }
        return null;
    }
}
