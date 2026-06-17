<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>{{ project.name }}</h2>
        <p class="project-desc">{{ project.description || '暂无描述' }}</p>
      </div>
      <div class="header-actions">
        <el-button @click="$router.push(`/code/${project.id}`)" type="primary">
          <i class="el-icon-edit"></i> 打开编辑器
        </el-button>
        <el-button @click="showSubmitDialog = true" type="success">
          <i class="el-icon-upload2"></i> 提交代码
        </el-button>
      </div>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="文件管理" name="files">
        <div class="file-tree">
          <div v-for="file in files" :key="file.id" class="file-item" :class="{ 'is-directory': file.isDirectory }" @click="openFile(file)">
            <i :class="file.isDirectory ? 'el-icon-folder' : 'el-icon-document'"></i>
            <span>{{ file.fileName }}</span>
            <span class="file-meta">{{ file.fileSize || 0 }} 字节</span>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="提交记录" name="submissions">
        <div class="submission-list">
          <div v-for="sub in submissions" :key="sub.id" class="submission-item" @click="$router.push(`/code/${project.id}`)">
            <div class="submission-info">
              <div class="submission-title">{{ sub.title }}</div>
              <div class="submission-meta">{{ sub.username }} · {{ sub.createdAt }}</div>
            </div>
            <el-tag :type="getStatusType(sub.status)">{{ getStatusName(sub.status) }}</el-tag>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="提交代码" :visible.sync="showSubmitDialog" width="600px">
      <el-form :model="submitForm">
        <el-form-item label="提交标题">
          <el-input v-model="submitForm.title" placeholder="请输入提交标题"></el-input>
        </el-form-item>
        <el-form-item label="提交描述">
          <el-input v-model="submitForm.description" type="textarea" :rows="3" placeholder="请描述您的更改"></el-input>
        </el-form-item>
        <el-form-item label="分支">
          <el-input v-model="submitForm.branch" placeholder="分支名称" value="main"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCode">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProjectDetail, getProjectFiles } from '@/api/project'
import { getProjectSubmissions, createSubmission } from '@/api/submission'

export default {
  name: 'ProjectDetail',
  data() {
    return {
      project: {},
      files: [],
      submissions: [],
      activeTab: 'files',
      showSubmitDialog: false,
      submitForm: { title: '', description: '', branch: 'main', projectId: null, files: [] }
    }
  },
  created() {
    this.fetchProject()
  },
  methods: {
    async fetchProject() {
      try {
        const res = await getProjectDetail(this.$route.params.id)
        this.project = res.data
        this.submitForm.projectId = this.project.id
        const [filesRes, subsRes] = await Promise.all([
          getProjectFiles(this.project.id),
          getProjectSubmissions(this.project.id)
        ])
        this.files = filesRes.data || []
        this.submissions = subsRes.data || []
      } catch (error) {
        this.$router.push('/projects')
      }
    },
    getStatusType(status) {
      const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
      return map[status] || 'info'
    },
    getStatusName(status) {
      const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
      return map[status] || '未知'
    },
    openFile(file) {
      if (!file.isDirectory) {
        this.$router.push(`/code/${this.project.id}?file=${file.id}`)
      }
    },
    async submitCode() {
      if (!this.submitForm.title) {
        this.$message.warning('请输入提交标题')
        return
      }
      try {
        await createSubmission(this.submitForm)
        this.$message.success('代码已提交')
        this.showSubmitDialog = false
        this.fetchProject()
      } catch (error) {
        // Error handled by interceptor
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  h2 {
    color: var(--text-color);
    margin-bottom: 8px;
  }
}

.project-desc {
  color: var(--text-color-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.file-tree {
  background: var(--card-bg);
  border-radius: 8px;
  overflow: hidden;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid var(--border-color);

  &:hover {
    background: var(--bg-color);
  }

  &.is-directory {
    font-weight: 500;
  }

  i {
    color: var(--primary-color);
    font-size: 16px;
  }

  .file-meta {
    margin-left: auto;
    font-size: 12px;
    color: var(--text-color-secondary);
  }
}

.submission-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: var(--card-bg);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateX(4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.submission-title {
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 4px;
}

.submission-meta {
  font-size: 12px;
  color: var(--text-color-secondary);
}
</style>
