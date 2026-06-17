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
          <textarea v-model="content" class="code-textarea" :placeholder="'编辑 ' + currentFile.fileName"></textarea>
        </div>
        <div v-else class="editor-placeholder">
          <i class="el-icon-edit-outline"></i>
          <p>选择文件开始编辑</p>
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

export default {
  name: 'CodeEditor',
  data() {
    return {
      projectId: null,
      files: [],
      currentFile: null,
      content: '',
      showNewFileDialog: false,
      newFileForm: { fileName: '', filePath: '' }
    }
  },
  created() {
    this.projectId = this.$route.params.projectId
    this.fetchFiles()
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
  width: 220px;
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
</style>
