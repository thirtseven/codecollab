package com.codecollab.controller;

import com.codecollab.common.Result;
import com.codecollab.dto.AiChatDTO;
import com.codecollab.service.AiService;
import com.codecollab.vo.AiConversationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(HttpServletRequest request, @Valid @RequestBody AiChatDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiService.chat(userId, dto));
    }

    @GetMapping("/tasks/{taskId}")
    public Result<Map<String, Object>> getTaskStatus(HttpServletRequest request, @PathVariable Long taskId) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiService.getTaskStatus(taskId, userId));
    }

    @GetMapping("/conversations")
    public Result<List<AiConversationVO>> getConversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiService.getUserConversations(userId));
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<Map<String, Object>>> getConversationMessages(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiService.getConversationMessages(id, userId));
    }

    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        aiService.deleteConversation(id, userId);
        return Result.success();
    }
}
