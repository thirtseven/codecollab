<template>
  <div class="page-container">
    <h2 class="page-title">{{ NAV.DASHBOARD }}</h2>
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ background: stat.gradient }">
          <div class="stat-icon">
            <i :class="stat.icon"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card">
          <h3><i class="el-icon-s-cooperation"></i> 我的团队</h3>
          <div v-if="teams.length" class="team-list">
            <div v-for="team in teams" :key="team.id" class="team-item" @click="$router.push(`/teams/${team.id}`)">
              <div class="team-avatar">{{ team.name[0] }}</div>
              <div class="team-info">
                <div class="team-name">{{ team.name }}</div>
                <div class="team-desc">{{ team.description || '暂无描述' }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无团队">
            <el-button type="primary" @click="$router.push('/teams')">创建团队</el-button>
          </el-empty>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <h3><i class="el-icon-folder-opened"></i> 最近项目</h3>
          <div v-if="projects.length" class="project-list">
            <div v-for="project in projects" :key="project.id" class="project-item" @click="$router.push(`/projects/${project.id}`)">
              <div class="project-icon">
                <i class="el-icon-document"></i>
              </div>
              <div class="project-info">
                <div class="project-name">{{ project.name }}</div>
                <div class="project-meta">
                  <span>{{ project.language || '未知' }}</span>
                  <span>{{ project.fileCount || 0 }} 个文件</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无项目">
            <el-button type="primary" @click="$router.push('/projects')">创建项目</el-button>
          </el-empty>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getMyTeams } from '@/api/team'
import { getProjectPage } from '@/api/project'
import { NAV } from '@/utils/constants'

export default {
  name: 'Dashboard',
  data() {
    return {
      NAV,
      teams: [],
      projects: [],
      stats: [
        { label: '我的团队', value: 0, icon: 'el-icon-s-cooperation', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
        { label: '我的项目', value: 0, icon: 'el-icon-folder-opened', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
        { label: '代码提交', value: 0, icon: 'el-icon-upload2', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
        { label: 'AI 对话', value: 0, icon: 'el-icon-chat-dot-round', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
      ]
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      try {
        const [teamsRes, projectsRes] = await Promise.all([
          getMyTeams(),
          getProjectPage({ page: 1, size: 5 })
        ])
        this.teams = teamsRes.data || []
        this.projects = projectsRes.data?.records || []
        this.stats[0].value = this.teams.length
        this.stats[1].value = this.projects.length
      } catch (error) {
        // Error handled by interceptor
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.page-title {
  margin-bottom: 24px;
  color: var(--text-color);
  font-size: 24px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  padding: 24px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  }
}

.stat-icon {
  font-size: 36px;
  opacity: 0.8;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

h3 {
  color: var(--text-color);
  margin-bottom: 16px;
  font-size: 18px;

  i {
    margin-right: 8px;
    color: var(--primary-color);
  }
}

.team-list, .project-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-item, .project-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--bg-color);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: var(--primary-color);
    color: #fff;
    transform: translateX(4px);
  }
}

.team-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--primary-color);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.team-name, .project-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.team-desc, .project-meta {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.project-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #e6a23c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.project-meta {
  display: flex;
  gap: 12px;
}
</style>
