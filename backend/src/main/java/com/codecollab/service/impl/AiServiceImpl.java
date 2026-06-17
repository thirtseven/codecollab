package com.codecollab.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codecollab.dto.AiChatDTO;
import com.codecollab.entity.AiConversation;
import com.codecollab.entity.AiMessage;
import com.codecollab.entity.AiTask;
import com.codecollab.exception.BusinessException;
import com.codecollab.mapper.AiConversationMapper;
import com.codecollab.mapper.AiMessageMapper;
import com.codecollab.mapper.AiTaskMapper;
import com.codecollab.mapper.SystemConfigMapper;
import com.codecollab.service.AiService;
import com.codecollab.vo.AiConversationVO;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private AiConversationMapper conversationMapper;

    @Autowired
    private AiMessageMapper messageMapper;

    @Autowired
    private AiTaskMapper taskMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Value("${ai.api-url:https://api.openai.com/v1/chat/completions}")
    private String defaultApiUrl;

    @Value("${ai.api-key:}")
    private String defaultApiKey;

    @Value("${ai.model:gpt-3.5-turbo}")
    private String defaultModel;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();

    @Override
    @Transactional
    public Map<String, Object> chat(Long userId, AiChatDTO dto) {
        Long conversationId = dto.getConversationId();
        if (conversationId == null) {
            AiConversation conversation = new AiConversation();
            conversation.setUserId(userId);
            String title = dto.getMessage() != null && !dto.getMessage().isEmpty()
                ? dto.getMessage().substring(0, Math.min(dto.getMessage().length(), 50))
                : "新建对话";
            conversation.setTitle(title);
            conversation.setProjectId(dto.getProjectId());
            conversationMapper.insert(conversation);
            conversationId = conversation.getId();
        }

        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(dto.getMessage());
        messageMapper.insert(userMessage);

        AiTask task = new AiTask();
        task.setUserId(userId);
        task.setConversationId(conversationId);
        task.setMessage(dto.getMessage());
        task.setStatus("PENDING");
        taskMapper.insert(task);

        processAiTaskAsync(task.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId());
        result.put("conversationId", conversationId);
        result.put("status", "PENDING");
        return result;
    }

    @Override
    public Map<String, Object> getTaskStatus(Long taskId, Long userId) {
        AiTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId());
        result.put("status", task.getStatus());
        result.put("conversationId", task.getConversationId());
        if ("COMPLETED".equals(task.getStatus())) {
            result.put("message", task.getResponse());
        } else if ("FAILED".equals(task.getStatus())) {
            result.put("error", task.getErrorMessage());
        }
        return result;
    }

    @Async("aiExecutor")
    public void processAiTaskAsync(Long taskId) {
        AiTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        try {
            task.setStatus("PROCESSING");
            taskMapper.updateById(task);

            List<AiMessage> history = getConversationHistory(task.getConversationId());
            String aiResponse = callAiApi(history);

            AiMessage assistantMessage = new AiMessage();
            assistantMessage.setConversationId(task.getConversationId());
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(aiResponse);
            messageMapper.insert(assistantMessage);

            task.setStatus("COMPLETED");
            task.setResponse(aiResponse);
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    @Override
    public List<AiConversationVO> getUserConversations(Long userId) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId);
        wrapper.orderByDesc(AiConversation::getUpdatedAt);
        List<AiConversation> conversations = conversationMapper.selectList(wrapper);
        List<AiConversationVO> vos = new ArrayList<>();
        for (AiConversation conv : conversations) {
            AiConversationVO vo = new AiConversationVO();
            vo.setId(conv.getId());
            vo.setTitle(conv.getTitle());
            vo.setProjectId(conv.getProjectId());
            vo.setCreatedAt(conv.getCreatedAt());
            LambdaQueryWrapper<AiMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(AiMessage::getConversationId, conv.getId());
            vo.setMessageCount(Math.toIntExact(messageMapper.selectCount(msgWrapper)));
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public List<Map<String, Object>> getConversationMessages(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId);
        wrapper.orderByAsc(AiMessage::getCreatedAt);
        List<AiMessage> messages = messageMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiMessage msg : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("role", msg.getRole());
            map.put("content", msg.getContent());
            map.put("createdAt", msg.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        conversationMapper.deleteById(conversationId);
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId);
        messageMapper.delete(wrapper);
    }

    private List<AiMessage> getConversationHistory(Long conversationId) {
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId);
        wrapper.orderByAsc(AiMessage::getCreatedAt);
        return messageMapper.selectList(wrapper);
    }

    private String callAiApi(List<AiMessage> history) {
        String apiUrl = getConfigValue("ai_api_url", defaultApiUrl);
        String apiKey = getConfigValue("ai_api_key", defaultApiKey);
        String model = getConfigValue("ai_model", defaultModel);

        if (apiKey == null || apiKey.isEmpty()) {
            return generateLocalResponse(history);
        }

        JSONArray messagesArray = new JSONArray();
        for (AiMessage msg : history) {
            JSONObject msgObj = new JSONObject();
            msgObj.put("role", msg.getRole());
            msgObj.put("content", msg.getContent());
            messagesArray.add(msgObj);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messagesArray);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);

        JSONObject extraBody = new JSONObject();
        extraBody.put("thinking_mode", "high");
        requestBody.put("extra_body", extraBody);

        Request request = new Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                ResponseBody errorBody = response.body();
                String errorMsg = errorBody != null ? errorBody.string() : "Unknown error";
                throw new BusinessException(500, "AI API调用失败: " + response.code() + " - " + errorMsg);
            }
            ResponseBody body = response.body();
            if (body == null) {
                return "AI未返回响应";
            }
            String responseBody = body.string();
            JSONObject json = JSON.parseObject(responseBody);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                String content = message.getString("content");
                if (content != null && !content.isEmpty()) {
                    return content;
                }
                String reasoning = message.getString("reasoning_content");
                if (reasoning != null && !reasoning.isEmpty()) {
                    return reasoning;
                }
            }
            return "AI未返回响应";
        } catch (IOException e) {
            throw new BusinessException(500, "AI API调用失败: " + e.getMessage());
        }
    }

    private String getConfigValue(String key, String defaultValue) {
        String value = systemConfigMapper.selectValueByKey(key);
        return value != null ? value : defaultValue;
    }

    private String generateLocalResponse(List<AiMessage> history) {
        if (history.isEmpty()) {
            return "你好！我是 CodeCollab AI 助手，可以帮你解答代码问题。请问有什么可以帮您的？";
        }
        String lastMessage = history.get(history.size() - 1).getContent().toLowerCase();
        if (lastMessage.contains("hello") || lastMessage.contains("hi") || lastMessage.contains("你好")) {
            return "你好！我可以帮你解答代码问题，请问有什么可以帮您的？";
        } else if (lastMessage.contains("help") || lastMessage.contains("帮助")) {
            return "我可以帮您：\n- 代码审查和建议\n- Bug 修复\n- 编写文档\n- 解释代码概念\n- 生成代码片段\n\n请问您需要什么帮助？";
        } else if (lastMessage.contains("bug") || lastMessage.contains("error") || lastMessage.contains("错误")) {
            return "我很乐意帮您调试代码。请分享错误信息和相关代码，我会帮您找出并修复问题。";
        } else if (lastMessage.contains("review") || lastMessage.contains("审查")) {
            return "我可以帮您审查代码的最佳实践、潜在 bug 和改进点。请分享您想让我审查的代码。";
        } else {
            return "我理解您的问题。为了提供更好的帮助，请提供更多关于您代码的上下文或分享相关文件。我随时为您提供帮助！";
        }
    }
}
