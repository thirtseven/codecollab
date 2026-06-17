<template>
  <div class="code-editor-container">
    <div class="editor-header">
      <div class="file-info">
        <i class="el-icon-back" @click="$router.back()"></i>
        <span>{{ currentFile?.fileName || '请选择文件' }}</span>
      </div>
      <div class="editor-actions">
        <el-button size="small" @click="saveFile" type="primary" :disabled="!currentFile">
          <i class="el-icon-check"></i> 保存
        </el-button>
        <el-button size="small" @click="showNewFileDialog = true">
          <i class="el-icon-plus"></i> 新建文件
        </el-button>
        <el-button size="small" @click="toggleAiPanel" :type="showAiPanel ? 'success' : 'default'">
          <i class="el-icon-chat-dot-round"></i> AI 助手
        </el-button>
      </div>
    </div>
    <div class="editor-body">
      <div class="file-sidebar">
        <div class="sidebar-header">文件列表</div>
        <div v-for="file in files" :key="file.id" class="file-item" :class="{ active: currentFile?.id === file.id }" @click="openFile(file)">
          <i :class="file.isDirectory ? 'el-icon-folder' : 'el-icon-document'"></i>
          <span>{{ file.fileName }}</span>
        </div>
      </div>
      <div class="editor-main">
        <div v-if="currentFile" class="editor-content">
          <textarea v-model="content" class="code-textarea" ref="codeEditor"
            :placeholder="'编辑 ' + currentFile.fileName"
            @keydown.tab.prevent="insertTab"></textarea>
        </div>
        <div v-else class="editor-placeholder">
          <i class="el-icon-edit-outline"></i>
          <p>选择文件开始编辑</p>
        </div>
      </div>
      <div v-if="showAiPanel" class="ai-panel">
        <div class="ai-header">
          <div class="ai-title">
            <i class="el-icon-chat-dot-round"></i>
            <span>AI 助手</span>
          </div>
          <div class="ai-actions">
            <el-tooltip content="发送当前代码给AI分析" placement="bottom">
              <el-button size="mini" circle @click="sendCodeToAi">
                <i class="el-icon-document-checked"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="关闭面板" placement="bottom">
              <el-button size="mini" circle @click="showAiPanel = false">
                <i class="el-icon-close"></i>
              </el-button>
            </el-tooltip>
          </div>
        </div>
        <div class="ai-messages" ref="aiMessages">
          <div v-for="(msg, index) in aiMessages" :key="index" class="ai-message" :class="msg.role">
            <div class="message-avatar">
              <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-s-platform'"></i>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div v-if="msg.hasCode" class="message-actions">
                <el-button size="mini" type="primary" @click="insertCode(msg.code)">
                  <i class="el-icon-document-copy"></i> 插入代码
                </el-button>
                <el-button size="mini" @click="copyCode(msg.code)">
                  <i class="el-icon-copy-document"></i> 复制
                </el-button>
              </div>
            </div>
          </div>
          <div v-if="aiLoading" class="ai-message assistant">
            <div class="message-avatar">
              <i class="el-icon-s-platform"></i>
            </div>
            <div class="message-content">
              <div class="thinking-indicator">
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span class="thinking-text">AI 正在思考...</span>
              </div>
            </div>
          </div>
        </div>
        <div class="ai-quick-actions">
          <el-button size="mini" @click="quickAction('explain')">解释代码</el-button>
          <el-button size="mini" @click="quickAction('optimize')">优化建议</el-button>
          <el-button size="mini" @click="quickAction('fix')">查找错误</el-button>
          <el-button size="mini" @click="quickAction('comment')">添加注释</el-button>
        </div>
        <div class="ai-input">
          <el-input v-model="aiInput" type="textarea" :rows="2"
            placeholder="输入指令，例如：写一个登录验证函数..."
            @keydown.ctrl.enter.native="sendAiMessage"></el-input>
          <div class="ai-input-actions">
            <span class="ai-tip">Ctrl+Enter 发送</span>
            <el-button type="primary" size="small" @click="sendAiMessage" :loading="aiLoading">
              <i class="el-icon-s-promotion"></i> 发送
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="新建文件" :visible.sync="showNewFileDialog" width="400px">
      <el-form :model="newFileForm">
        <el-form-item label="文件名">
          <el-input v-model="newFileForm.fileName" placeholder="例如：index.js"></el-input>
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="newFileForm.filePath" placeholder="例如：/src/index.js"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showNewFileDialog = false">取消</el-button>
        <el-button type="primary" @click="createNewFile">创建</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProjectFiles, getFileContent, saveFile } from '@/api/project'
import { chat, getTaskStatus, getConversations, getConversationMessages } from '@/api/ai'

export default {
  name: 'CodeEditor',
  data() {
    return {
      projectId: null,
      files: [],
      currentFile: null,
      content: '',
      showNewFileDialog: false,
      newFileForm: { fileName: '', filePath: '' },
      showAiPanel: false,
      aiMessages: [],
      aiInput: '',
      aiLoading: false,
      aiConversationId: null,
      aiPollingTimer: null
    }
  },
  created() {
    this.projectId = this.$route.params.projectId
    this.fetchFiles()
  },
  beforeDestroy() {
    this.stopAiPolling()
  },
  methods: {
    async fetchFiles() {
      try {
        const res = await getProjectFiles(this.projectId)
        this.files = (res.data || []).filter(f => !f.isDirectory)
        const fileId = this.$route.query.file
        if (fileId) {
          const file = this.files.find(f => f.id == fileId)
          if (file) this.openFile(file)
        }
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async openFile(file) {
      try {
        const res = await getFileContent(file.id)
        this.currentFile = res.data
        this.content = res.data.content || ''
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async saveFile() {
      if (!this.currentFile) return
      try {
        await saveFile(this.projectId, {
          filePath: this.currentFile.filePath,
          fileName: this.currentFile.fileName,
          content: this.content
        })
        this.$message.success('文件已保存')
      } catch (error) {
        // Error handled by interceptor
      }
    },
    createNewFile() {
      if (!this.newFileForm.fileName || !this.newFileForm.filePath) {
        this.$message.warning('请填写完整信息')
        return
      }
      saveFile(this.projectId, {
        ...this.newFileForm,
        content: ''
      }).then(() => {
        this.$message.success('文件已创建')
        this.showNewFileDialog = false
        this.fetchFiles()
      })
    },
    insertTab(e) {
      const textarea = e.target
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      this.content = this.content.substring(0, start) + '  ' + this.content.substring(end)
      this.$nextTick(() => {
        textarea.selectionStart = textarea.selectionEnd = start + 2
      })
    },
    toggleAiPanel() {
      this.showAiPanel = !this.showAiPanel
      if (this.showAiPanel && this.aiMessages.length === 0) {
        this.aiMessages.push({
          role: 'assistant',
          content: '你好！我是 CodeCollab AI 助手。我可以帮你：\n\n• 解释代码逻辑\n• 提供优化建议\n• 查找潜在错误\n• 生成代码模块\n\n请选择文件后点击"发送当前代码"或直接输入指令。',
          hasCode: false
        })
      }
    },
    async sendCodeToAi() {
      if (!this.content) {
        this.$message.warning('请先打开文件')
        return
      }
      const fileName = this.currentFile?.fileName || '未知文件'
      const message = `请分析以下 ${fileName} 的代码：\n\n\`\`\`\n${this.content}\n\`\`\``
      this.aiMessages.push({ role: 'user', content: `发送代码: ${fileName}`, hasCode: false })
      await this.sendToAi(message)
    },
    async sendAiMessage() {
      if (!this.aiInput.trim() || this.aiLoading) return
      const message = this.aiInput
      this.aiInput = ''
      this.aiMessages.push({ role: 'user', content: message, hasCode: false })
      await this.sendToAi(message)
    },
    async quickAction(action) {
      if (!this.content) {
        this.$message.warning('请先打开文件')
        return
      }
      const actions = {
        explain: '请详细解释这段代码的功能和逻辑',
        optimize: '请分析这段代码并提供优化建议',
        fix: '请检查这段代码是否有潜在的错误或bug',
        comment: '请为这段代码添加详细的中文注释'
      }
      const message = `${actions[action]}：\n\n\`\`\`\n${this.content}\n\`\`\``
      this.aiMessages.push({ role: 'user', content: actions[action], hasCode: false })
      await this.sendToAi(message)
    },
    async sendToAi(message) {
      this.aiLoading = true
      this.$nextTick(() => this.scrollToBottom())
      try {
        const res = await chat({
          conversationId: this.aiConversationId,
          message: message,
          projectId: this.projectId
        })
        const { taskId, conversationId } = res.data
        this.aiConversationId = conversationId
        this.startAiPolling(taskId)
      } catch (error) {
        this.aiMessages.push({
          role: 'assistant',
          content: '抱歉，发生了错误。请重试。',
          hasCode: false
        })
        this.aiLoading = false
      }
    },
    startAiPolling(taskId) {
      this.stopAiPolling()
      this.aiPollingTimer = setInterval(async () => {
        try {
          const res = await getTaskStatus(taskId)
          const { status, message, error } = res.data
          if (status === 'COMPLETED') {
            this.stopAiPolling()
            const codeMatch = message.match(/```[\s\S]*?\n([\s\S]*?)```/)
            this.aiMessages.push({
              role: 'assistant',
              content: message,
              hasCode: !!codeMatch,
              code: codeMatch ? codeMatch[1] : ''
            })
            this.aiLoading = false
            this.$nextTick(() => this.scrollToBottom())
          } else if (status === 'FAILED') {
            this.stopAiPolling()
            this.aiMessages.push({
              role: 'assistant',
              content: error || '抱歉，发生了错误。请重试。',
              hasCode: false
            })
            this.aiLoading = false
            this.$nextTick(() => this.scrollToBottom())
          }
        } catch (error) {
          this.stopAiPolling()
          this.aiMessages.push({
            role: 'assistant',
            content: '抱歉，发生了错误。请重试。',
            hasCode: false
          })
          this.aiLoading = false
        }
      }, 1000)
    },
    stopAiPolling() {
      if (this.aiPollingTimer) {
        clearInterval(this.aiPollingTimer)
        this.aiPollingTimer = null
      }
    },
    insertCode(code) {
      if (!this.currentFile) {
        this.$message.warning('请先打开文件')
        return
      }
      const textarea = this.$refs.codeEditor
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      this.content = this.content.substring(0, start) + '\n' + code + '\n' + this.content.substring(end)
      this.$message.success('代码已插入')
    },
    copyCode(code) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(code).then(() => {
          this.$message.success('代码已复制')
        })
      } else {
        const textarea = document.createElement('textarea')
        textarea.value = code
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        this.$message.success('代码已复制')
      }
    },
    formatMessage(content) {
      return content
        .replace(/```[\s\S]*?```/g, '<pre class="code-block">$&</pre>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
    },
    scrollToBottom() {
      if (this.$refs.aiMessages) {
        this.$refs.aiMessages.scrollTop = this.$refs.aiMessages.scrollHeight
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.code-editor-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
  border-radius: 8px;
  overflow: hidden;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-color);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color);
  font-weight: 500;

  i {
    cursor: pointer;
    font-size: 18px;
    color: var(--primary-color);
  }
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.file-sidebar {
  width: 200px;
  border-right: 1px solid var(--border-color);
  overflow-y: auto;
  background: var(--bg-color);
}

.sidebar-header {
  padding: 12px 16px;
  font-weight: 500;
  color: var(--text-color);
  border-bottom: 1px solid var(--border-color);
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  color: var(--text-color-secondary);

  &:hover {
    background: var(--card-bg);
  }

  &.active {
    background: var(--primary-color);
    color: #fff;
  }

  i {
    font-size: 14px;
  }
}

.editor-main {
  flex: 1;
  overflow: hidden;
}

.editor-content {
  height: 100%;
}

.code-textarea {
  width: 100%;
  height: 100%;
  padding: 16px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: var(--card-bg);
  color: var(--text-color);
  tab-size: 2;
}

.editor-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-color-secondary);

  i {
    font-size: 48px;
    margin-bottom: 16px;
  }
}

.ai-panel {
  width: 380px;
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-color);
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: var(--text-color);

  i {
    color: var(--primary-color);
  }
}

.ai-actions {
  display: flex;
  gap: 4px;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.ai-message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

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
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  flex-shrink: 0;
}

.message-content {
  max-width: 85%;
  padding: 10px 14px;
}

.message-text {
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.message-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.ai-quick-actions {
  display: flex;
  gap: 8px;
  padding: 8px 16px;
  border-top: 1px solid var(--border-color);
  flex-wrap: wrap;
}

.ai-input {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
}

.ai-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.ai-tip {
  font-size: 11px;
  color: var(--text-color-secondary);
}

.thinking-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
}

.thinking-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color);
  animation: thinking 1.4s infinite ease-in-out;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

.thinking-text {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-left: 4px;
}

@keyframes thinking {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}
</style>
