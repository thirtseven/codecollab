<template>
  <div class="page-container">
    <div class="page-header">
      <h2>项目管理</h2>
      <div class="header-actions">
        <el-input v-model="keyword" placeholder="搜索项目..." prefix-icon="el-icon-search" clearable style="width: 250px; margin-right: 12px;" @clear="fetchProjects" @keyup.enter.native="fetchProjects"></el-input>
        <el-button type="success" @click="showCreateDialog = true">
          <i class="el-icon-plus"></i> 创建项目
        </el-button>
      </div>
    </div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="project in projects" :key="project.id">
        <el-card class="project-card" shadow="hover">
          <div class="project-card-header" @click="$router.push(`/projects/${project.id}`)">
            <div class="project-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="project-actions">
              <el-tag size="small">{{ project.language || '未知' }}</el-tag>
              <el-dropdown trigger="click" @command="handleCommand($event, project)" @click.native.stop>
                <i class="el-icon-more"></i>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="edit">
                    <i class="el-icon-edit"></i> 编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="code">
                    <i class="el-icon-edit-outline"></i> 打开编辑器
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <i class="el-icon-delete"></i> 删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
          <div @click="$router.push(`/projects/${project.id}`)">
            <h3>{{ project.name }}</h3>
            <p>{{ project.description || '暂无描述' }}</p>
            <div class="project-meta">
              <span><i class="el-icon-folder"></i> {{ project.fileCount || 0 }} 个文件</span>
              <span><i class="el-icon-upload2"></i> {{ project.submissionCount || 0 }} 次提交</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination v-if="total > pageSize" :current-page="currentPage" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="handlePageChange" style="margin-top: 20px; text-align: center;"></el-pagination>

    <el-dialog title="创建项目" :visible.sync="showCreateDialog" width="500px">
      <el-form :model="projectForm" :rules="projectRules" ref="projectForm">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        <el-form-item label="所属团队" prop="teamId">
          <el-select v-model="projectForm.teamId" placeholder="请选择团队" style="width: 100%;">
            <el-option v-for="team in myTeams" :key="team.id" :label="team.name" :value="team.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="编程语言">
          <el-select v-model="projectForm.language" placeholder="请选择语言" style="width: 100%;">
            <el-option label="Java" value="Java"></el-option>
            <el-option label="Python" value="Python"></el-option>
            <el-option label="JavaScript" value="JavaScript"></el-option>
            <el-option label="TypeScript" value="TypeScript"></el-option>
            <el-option label="Go" value="Go"></el-option>
            <el-option label="C++" value="C++"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="projectForm.description" type="textarea" :rows="3" placeholder="请输入项目描述"></el-input>
        </el-form-item>
        <el-form-item label="可见性">
          <el-switch v-model="projectForm.isPublic" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="私有"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createProject">创建</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProjectPage, createProject, deleteProject } from '@/api/project'
import { getMyTeams } from '@/api/team'

export default {
  name: 'ProjectList',
  data() {
    return {
      projects: [],
      myTeams: [],
      keyword: '',
      currentPage: 1,
      pageSize: 12,
      total: 0,
      showCreateDialog: false,
      creating: false,
      projectForm: { name: '', teamId: null, language: '', description: '', isPublic: 0 },
      projectRules: {
        name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        teamId: [{ required: true, message: '请选择团队', trigger: 'change' }]
      }
    }
  },
  created() {
    this.fetchProjects()
    this.fetchMyTeams()
  },
  methods: {
    async fetchProjects() {
      try {
        const res = await getProjectPage({ page: this.currentPage, size: this.pageSize, keyword: this.keyword })
        this.projects = res.data?.records || []
        this.total = res.data?.total || 0
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async fetchMyTeams() {
      try {
        const res = await getMyTeams()
        this.myTeams = res.data || []
      } catch (error) {
        // Error handled by interceptor
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchProjects()
    },
    createProject() {
      this.$refs.projectForm.validate(async valid => {
        if (!valid) return
        this.creating = true
        try {
          await createProject(this.projectForm)
          this.$message.success('项目创建成功')
          this.showCreateDialog = false
          this.fetchProjects()
        } catch (error) {
          // Error handled by interceptor
        } finally {
          this.creating = false
        }
      })
    },
    handleCommand(command, project) {
      if (command === 'edit') {
        this.$router.push(`/projects/${project.id}`)
      } else if (command === 'code') {
        this.$router.push(`/code/${project.id}`)
      } else if (command === 'delete') {
        this.$confirm(`确定删除项目 "${project.name}" 吗？删除后将无法恢复！`, '警告', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: 'el-button--danger'
        }).then(async () => {
          try {
            await deleteProject(project.id)
            this.$message.success('项目已删除')
            this.fetchProjects()
          } catch (error) {
            // Error handled by interceptor
          }
        }).catch(() => {})
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h2 {
    color: var(--text-color);
  }
}

.header-actions {
  display: flex;
  align-items: center;
}

.project-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  .project-card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
  }

  .project-actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon-more {
      cursor: pointer;
      padding: 4px;
      border-radius: 4px;
      transition: background-color 0.2s ease;

      &:hover {
        background: var(--bg-color);
      }
    }
  }

  .project-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }

  h3 {
    color: var(--text-color);
    margin-bottom: 8px;
    font-size: 18px;
  }

  p {
    color: var(--text-color-secondary);
    font-size: 14px;
    margin-bottom: 12px;
    min-height: 40px;
  }

  .project-meta {
    display: flex;
    gap: 16px;
    color: var(--text-color-secondary);
    font-size: 12px;

    i {
      margin-right: 4px;
    }
  }
}
</style>
