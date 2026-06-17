package com.codecollab.service;

import com.codecollab.dto.AiChatDTO;
import com.codecollab.vo.AiConversationVO;
import java.util.List;
import java.util.Map;

public interface AiService {
    Map<String, Object> chat(Long userId, AiChatDTO dto);
    List<AiConversationVO> getUserConversations(Long userId);
    List<Map<String, Object>> getConversationMessages(Long conversationId, Long userId);
    void deleteConversation(Long conversationId, Long userId);
}
