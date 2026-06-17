<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>{{ team.name }}</h2>
        <p class="team-desc">{{ team.description || '暂无描述' }}</p>
      </div>
      <div class="header-actions">
        <el-button @click="copyInviteCode" type="primary" plain>
          <i class="el-icon-link"></i> 复制邀请码
        </el-button>
        <el-button @click="leaveTeam" type="danger" plain>
          <i class="el-icon-right"></i> 退出团队
        </el-button>
      </div>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="成员管理" name="members">
        <div class="member-list">
          <div v-for="member in team.members" :key="member.id" class="member-item">
            <el-avatar :size="40">{{ member.username[0] }}</el-avatar>
            <div class="member-info">
              <div class="member-name">{{ member.username }}</div>
              <div class="member-email">{{ member.email }}</div>
            </div>
            <el-tag :type="getRoleTag(member.role)">{{ getRoleName(member.role) }}</el-tag>
            <el-dropdown v-if="isOwner" trigger="click" @command="handleMemberCommand($event, member)">
              <i class="el-icon-more"></i>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="admin">设为管理员</el-dropdown-item>
                <el-dropdown-item command="member">设为成员</el-dropdown-item>
                <el-dropdown-item command="remove" divided>移除成员</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="团队项目" name="projects">
        <div class="project-list">
          <div v-for="project in projects" :key="project.id" class="project-item" @click="$router.push(`/projects/${project.id}`)">
            <div class="project-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="project-info">
              <div class="project-name">{{ project.name }}</div>
              <div class="project-meta">{{ project.language || '未知' }} · {{ project.fileCount || 0 }} 个文件</div>
            </div>
          </div>
          <el-button type="primary" @click="$router.push('/projects')">创建项目</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getTeamDetail, leaveTeam, removeMember, updateMemberRole } from '@/api/team'
import { getProjectPage } from '@/api/project'
import { copyToClipboard } from '@/utils/clipboard'

export default {
  name: 'TeamDetail',
  data() {
    return {
      team: {},
      projects: [],
      activeTab: 'members'
    }
  },
  computed: {
    isOwner() {
      const user = this.$store.state.user.user
      return this.team.creatorId === user?.id
    }
  },
  created() {
    this.fetchTeam()
  },
  methods: {
    async fetchTeam() {
      try {
        const res = await getTeamDetail(this.$route.params.id)
        this.team = res.data
        const projRes = await getProjectPage({ teamId: this.team.id, size: 100 })
        this.projects = projRes.data?.records || []
      } catch (error) {
        this.$router.push('/teams')
      }
    },
    getRoleTag(role) {
      const map = { OWNER: 'danger', ADMIN: 'warning', MEMBER: 'info' }
      return map[role] || 'info'
    },
    getRoleName(role) {
      const map = { OWNER: '拥有者', ADMIN: '管理员', MEMBER: '成员' }
      return map[role] || '成员'
    },
    async copyInviteCode() {
      if (this.team.inviteCode) {
        try {
          await copyToClipboard(this.team.inviteCode)
          this.$message.success('邀请码已复制')
        } catch (error) {
          this.$message.error('复制失败，请手动复制: ' + this.team.inviteCode)
        }
      }
    },
    },
    leaveTeam() {
      this.$confirm('确定退出该团队吗？', '警告', { type: 'warning' }).then(async () => {
        await leaveTeam(this.team.id)
        this.$message.success('已退出团队')
        this.$router.push('/teams')
      }).catch(() => {})
    },
    handleMemberCommand(command, member) {
      if (command === 'remove') {
        this.$confirm('确定移除该成员吗？', '警告', { type: 'warning' }).then(async () => {
          await removeMember(this.team.id, member.userId)
          this.$message.success('成员已移除')
          this.fetchTeam()
        }).catch(() => {})
      } else {
        updateMemberRole(this.team.id, member.userId, command.toUpperCase()).then(() => {
          this.$message.success('角色已更新')
          this.fetchTeam()
        })
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

.team-desc {
  color: var(--text-color-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--card-bg);
  border-radius: 8px;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.member-info {
  flex: 1;
}

.member-name {
  font-weight: 500;
  color: var(--text-color);
}

.member-email {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-item {
  display: flex;
  align-items: center;
  gap: 12px;
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

.project-name {
  font-weight: 500;
  color: var(--text-color);
}

.project-meta {
  font-size: 12px;
  color: var(--text-color-secondary);
}
</style>
