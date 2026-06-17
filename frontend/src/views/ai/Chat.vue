<template>
  <div class="ai-container">
    <div class="ai-sidebar">
      <div class="sidebar-header">
        <h3>对话列表</h3>
        <el-button size="small" circle @click="newConversation">
          <i class="el-icon-plus"></i>
        </el-button>
      </div>
      <div class="conversation-list">
        <div v-for="conv in conversations" :key="conv.id" class="conversation-item" :class="{ active: currentConversation?.id === conv.id }" @click="selectConversation(conv)">
          <i class="el-icon-chat-dot-round"></i>
          <span>{{ conv.title }}</span>
          <i class="el-icon-delete" @click.stop="deleteConversation(conv)"></i>
        </div>
      </div>
    </div>
    <div class="ai-main">
      <div class="chat-header">
        <h3>{{ currentConversation?.title || '新建对话' }}</h3>
      </div>
      <div class="chat-messages" ref="messages">
        <div v-for="msg in messages" :key="msg.id" class="message" :class="msg.role">
          <div class="message-avatar">
            <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-s-platform'"></i>
          </div>
          <div class="message-content">
            <div v-if="msg.role === 'user'" class="message-text">{{ msg.content }}</div>
            <div v-else class="message-text markdown-body" v-html="renderMarkdown(msg.content)"></div>
            <div class="message-time">{{ msg.createdAt }}</div>
          </div>
        </div>
        <div v-if="loading" class="message assistant">
          <div class="message-avatar">
            <i class="el-icon-s-platform"></i>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>
      <div class="chat-input">
        <el-input v-model="inputMessage" placeholder="输入消息..." @keyup.enter.native="sendMessage" :disabled="loading">
          <el-button slot="append" icon="el-icon-s-promotion" @click="sendMessage" :loading="loading"></el-button>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script>
import { chat, getTaskStatus, getConversations, getConversationMessages, deleteConversation } from '@/api/ai'
import { formatDate } from '@/utils/date'
import { renderMarkdown } from '@/utils/markdown'

export default {
  name: 'AiChat',
  data() {
    return {
      conversations: [],
      currentConversation: null,
      messages: [],
      inputMessage: '',
      loading: false,
      pollingTimer: null
    }
  },
  created() {
    this.fetchConversations()
  },
  beforeDestroy() {
    this.stopPolling()
  },
  methods: {
    async fetchConversations() {
      try {
        const res = await getConversations()
        this.conversations = res.data || []
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async selectConversation(conv) {
      this.currentConversation = conv
      try {
        const res = await getConversationMessages(conv.id)
        this.messages = res.data || []
        this.$nextTick(() => this.scrollToBottom())
      } catch (error) {
        // Error handled by interceptor
      }
    },
    newConversation() {
      this.currentConversation = null
      this.messages = []
    },
    async sendMessage() {
      if (!this.inputMessage.trim() || this.loading) return
      const userMessage = this.inputMessage
      this.inputMessage = ''
      this.messages.push({ id: Date.now(), role: 'user', content: userMessage, createdAt: formatDate(new Date()) })
      this.$nextTick(() => this.scrollToBottom())
      this.loading = true
      try {
        const res = await chat({
          conversationId: this.currentConversation?.id,
          message: userMessage
        })
        const { taskId, conversationId } = res.data
        if (!this.currentConversation) {
          this.currentConversation = { id: conversationId, title: userMessage.substring(0, 50) }
          this.fetchConversations()
        }
        this.startPolling(taskId)
      } catch (error) {
        this.messages.push({ id: Date.now() + 1, role: 'assistant', content: '抱歉，发生了错误。请重试。', createdAt: formatDate(new Date()) })
        this.loading = false
      }
    },
    startPolling(taskId) {
      this.stopPolling()
      this.pollingTimer = setInterval(async () => {
        try {
          const res = await getTaskStatus(taskId)
          const { status, message, error } = res.data
          if (status === 'COMPLETED') {
            this.stopPolling()
            this.messages.push({ id: Date.now() + 1, role: 'assistant', content: message, createdAt: formatDate(new Date()) })
            this.loading = false
            this.$nextTick(() => this.scrollToBottom())
          } else if (status === 'FAILED') {
            this.stopPolling()
            this.messages.push({ id: Date.now() + 1, role: 'assistant', content: error || '抱歉，发生了错误。请重试。', createdAt: formatDate(new Date()) })
            this.loading = false
            this.$nextTick(() => this.scrollToBottom())
          }
        } catch (error) {
          this.stopPolling()
          this.messages.push({ id: Date.now() + 1, role: 'assistant', content: '抱歉，发生了错误。请重试。', createdAt: formatDate(new Date()) })
          this.loading = false
        }
      }, 1000)
    },
    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
      }
    },
    async deleteConversation(conv) {
      try {
        await deleteConversation(conv.id)
        this.$message.success('对话已删除')
        if (this.currentConversation?.id === conv.id) {
          this.newConversation()
        }
        this.fetchConversations()
      } catch (error) {
        // Error handled by interceptor
      }
    },
    renderMarkdown(content) {
      return renderMarkdown(content)
    },
    scrollToBottom() {
      if (this.$refs.messages) {
        this.$refs.messages.scrollTop = this.$refs.messages.scrollHeight
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-container {
  display: flex;
  height: calc(100vh - 120px);
  background: var(--card-bg);
  border-radius: 8px;
  overflow: hidden;
}

.ai-sidebar {
  width: 260px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);

  h3 {
    color: var(--text-color);
    font-size: 16px;
  }
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  color: var(--text-color-secondary);

  &:hover {
    background: var(--bg-color);
  }

  &.active {
    background: var(--primary-color);
    color: #fff;
  }

  span {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .el-icon-delete {
    opacity: 0;
    transition: opacity 0.2s ease;

    &:hover {
      color: #f56c6c;
    }
  }

  &:hover .el-icon-delete {
    opacity: 1;
  }
}

.ai-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);

  h3 {
    color: var(--text-color);
    font-size: 16px;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;

  &.user {
    flex-direction: row-reverse;

    .message-content {
      background: var(--primary-color);
      color: #fff;
      border-radius: 12px 12px 0 12px;
    }
  }

  &.assistant .message-content {
    background: var(--bg-color);
    border-radius: 12px 12px 12px 0;
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--bg-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
}

.message-text {
  line-height: 1.6;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--text-color-secondary);
    animation: typing 1.4s infinite ease-in-out;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

.chat-input {
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.markdown-body {
  :deep(pre) {
    background: var(--bg-color);
    border-radius: 8px;
    padding: 12px;
    overflow-x: auto;
    margin: 8px 0;
  }

  :deep(code) {
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
  }

  :deep(p) {
    margin: 4px 0;
  }

  :deep(ul), :deep(ol) {
    padding-left: 20px;
    margin: 8px 0;
  }

  :deep(blockquote) {
    border-left: 3px solid var(--primary-color);
    padding-left: 12px;
    margin: 8px 0;
    opacity: 0.8;
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.6; }
  40% { transform: scale(1); opacity: 1; }
}
</style>
