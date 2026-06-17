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
          <el-empty v-if="files.length === 0" description="暂无文件"></el-empty>
        </div>
      </el-tab-pane>
      <el-tab-pane label="提交记录" name="submissions">
        <div class="submission-list">
          <div v-for="sub in submissions" :key="sub.id" class="submission-item">
            <div class="submission-info" @click="showSubmissionDetail(sub)">
              <div class="submission-title">
                <i class="el-icon-document-checked"></i>
                {{ sub.title }}
              </div>
              <div class="submission-meta">
                <span><i class="el-icon-user"></i> {{ sub.username }}</span>
                <span><i class="el-icon-time"></i> {{ sub.createdAt }}</span>
                <span><i class="el-icon-folder"></i> {{ sub.branch || 'main' }}</span>
              </div>
              <div v-if="sub.description" class="submission-desc">{{ sub.description }}</div>
            </div>
            <div class="submission-actions">
              <el-tag :type="getStatusType(sub.status)" size="medium">
                {{ getStatusName(sub.status) }}
              </el-tag>
              <div v-if="sub.status === 'PENDING' && isTeamOwner" class="review-buttons">
                <el-button type="success" size="small" @click.stop="openReviewDialog(sub, 'APPROVED')">
                  <i class="el-icon-check"></i> 通过
                </el-button>
                <el-button type="danger" size="small" @click.stop="openReviewDialog(sub, 'REJECTED')">
                  <i class="el-icon-close"></i> 驳回
                </el-button>
              </div>
              <div v-if="sub.status !== 'PENDING' && sub.reviewComment" class="review-comment">
                <i class="el-icon-chat-line-square"></i> {{ sub.reviewComment }}
              </div>
            </div>
          </div>
          <el-empty v-if="submissions.length === 0" description="暂无提交记录"></el-empty>
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
        <el-form-item label="目标分支">
          <el-input v-model="submitForm.branch" placeholder="分支名称">
            <template slot="prepend">
              <i class="el-icon-collection-tag"></i>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCode">提交审核</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="reviewDialogTitle" :visible.sync="showReviewDialog" width="500px">
      <el-form :model="reviewForm">
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="4"
            :placeholder="reviewForm.status === 'REJECTED' ? '请填写驳回原因（必填）' : '请填写审核意见（可选）'"></el-input>
        </el-form-item>
        <el-form-item v-if="reviewForm.status === 'APPROVED'" label="合并分支">
          <el-input v-model="reviewForm.targetBranch" placeholder="目标分支名称">
            <template slot="prepend">
              <i class="el-icon-collection-tag"></i>
            </template>
          </el-input>
          <div class="form-tip">代码将被合并到此分支</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button :type="reviewForm.status === 'APPROVED' ? 'success' : 'danger'"
          :loading="reviewing" @click="submitReview">
          {{ reviewForm.status === 'APPROVED' ? '通过并合并' : '确认驳回' }}
        </el-button>
      </div>
    </el-dialog>

    <el-dialog title="提交详情" :visible.sync="showDetailDialog" width="700px">
      <div v-if="currentSubmission" class="submission-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="提交标题">{{ currentSubmission.title }}</el-descriptions-item>
          <el-descriptions-item label="提交状态">
            <el-tag :type="getStatusType(currentSubmission.status)">
              {{ getStatusName(currentSubmission.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交人">{{ currentSubmission.username }}</el-descriptions-item>
          <el-descriptions-item label="目标分支">{{ currentSubmission.branch || 'main' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentSubmission.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="文件数量">{{ currentSubmission.fileCount || 0 }} 个</el-descriptions-item>
          <el-descriptions-item label="提交描述" :span="2">
            {{ currentSubmission.description || '无描述' }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="submissionFiles.length > 0" class="file-changes">
          <h4>文件变更</h4>
          <div v-for="file in submissionFiles" :key="file.id" class="file-change-item">
            <div class="file-change-header">
              <el-tag :type="getChangeType(file.changeType)" size="small">{{ file.changeType }}</el-tag>
              <span class="file-path">{{ file.filePath }}</span>
            </div>
            <pre v-if="file.content" class="file-content"><code>{{ file.content }}</code></pre>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProjectDetail, getProjectFiles } from '@/api/project'
import { getProjectSubmissions, createSubmission, reviewSubmission, getSubmissionFiles } from '@/api/submission'
import { getTeamDetail } from '@/api/team'

export default {
  name: 'ProjectDetail',
  data() {
    return {
      project: {},
      files: [],
      submissions: [],
      activeTab: 'files',
      showSubmitDialog: false,
      showReviewDialog: false,
      showDetailDialog: false,
      reviewing: false,
      isTeamOwner: false,
      currentSubmission: null,
      submissionFiles: [],
      submitForm: { title: '', description: '', branch: 'main', projectId: null, files: [] },
      reviewForm: { submissionId: null, status: '', comment: '', targetBranch: 'main' }
    }
  },
  computed: {
    reviewDialogTitle() {
      return this.reviewForm.status === 'APPROVED' ? '通过审核 - 合并代码' : '驳回提交'
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
        await this.checkTeamOwnership()
      } catch (error) {
        this.$router.push('/projects')
      }
    },
    async checkTeamOwnership() {
      if (this.project.teamId) {
        try {
          const res = await getTeamDetail(this.project.teamId)
          const userId = this.$store.state.user.user?.id
          this.isTeamOwner = res.data.members?.some(m =>
            m.userId === userId && (m.role === 'OWNER' || m.role === 'ADMIN')
          ) || false
        } catch (error) {
          this.isTeamOwner = false
        }
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
    getChangeType(type) {
      const map = { ADD: 'success', MODIFY: 'warning', DELETE: 'danger' }
      return map[type] || 'info'
    },
    openFile(file) {
      if (!file.isDirectory) {
        this.$router.push(`/code/${this.project.id}?file=${file.id}`)
      }
    },
    async showSubmissionDetail(sub) {
      this.currentSubmission = sub
      try {
        const res = await getSubmissionFiles(sub.id)
        this.submissionFiles = res.data || []
      } catch (error) {
        this.submissionFiles = []
      }
      this.showDetailDialog = true
    },
    openReviewDialog(submission, status) {
      this.reviewForm = {
        submissionId: submission.id,
        status: status,
        comment: '',
        targetBranch: submission.branch || 'main'
      }
      this.showReviewDialog = true
    },
    async submitReview() {
      if (this.reviewForm.status === 'REJECTED' && !this.reviewForm.comment.trim()) {
        this.$message.warning('驳回时必须填写审核意见')
        return
      }
      this.reviewing = true
      try {
        await reviewSubmission(
          this.reviewForm.submissionId,
          this.reviewForm.status,
          this.reviewForm.comment
        )
        this.$message.success(this.reviewForm.status === 'APPROVED' ? '代码已通过审核并合并' : '提交已驳回')
        this.showReviewDialog = false
        this.fetchProject()
      } catch (error) {
        // Error handled by interceptor
      } finally {
        this.reviewing = false
      }
    },
    async submitCode() {
      if (!this.submitForm.title) {
        this.$message.warning('请输入提交标题')
        return
      }
      try {
        await createSubmission(this.submitForm)
        this.$message.success('代码已提交，等待审核')
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
  gap: 16px;
}

.submission-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: var(--card-bg);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.submission-info {
  flex: 1;
  cursor: pointer;
}

.submission-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 8px;

  i {
    color: var(--primary-color);
    margin-right: 4px;
  }
}

.submission-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-bottom: 8px;

  i {
    margin-right: 4px;
  }
}

.submission-desc {
  font-size: 13px;
  color: var(--text-color-secondary);
  padding: 8px 12px;
  background: var(--bg-color);
  border-radius: 4px;
  margin-top: 8px;
}

.submission-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  margin-left: 20px;
}

.review-buttons {
  display: flex;
  gap: 8px;
}

.review-comment {
  font-size: 12px;
  color: var(--text-color-secondary);
  padding: 8px 12px;
  background: var(--bg-color);
  border-radius: 4px;
  max-width: 200px;

  i {
    margin-right: 4px;
  }
}

.submission-detail {
  .el-descriptions {
    margin-bottom: 20px;
  }
}

.file-changes {
  h4 {
    color: var(--text-color);
    margin-bottom: 12px;
  }
}

.file-change-item {
  margin-bottom: 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.file-change-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--bg-color);
  border-bottom: 1px solid var(--border-color);
}

.file-path {
  font-family: monospace;
  font-size: 13px;
  color: var(--text-color);
}

.file-content {
  padding: 16px;
  background: var(--card-bg);
  font-family: monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow-x: auto;
  margin: 0;

  code {
    color: var(--text-color);
  }
}

.form-tip {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-top: 4px;
}
</style>
