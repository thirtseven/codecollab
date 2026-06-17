<template>
  <div class="page-container">
    <div class="page-header">
      <h2>团队管理</h2>
      <div class="header-actions">
        <el-input v-model="keyword" placeholder="搜索团队..." prefix-icon="el-icon-search" clearable style="width: 250px; margin-right: 12px;" @clear="fetchTeams" @keyup.enter.native="fetchTeams"></el-input>
        <el-button type="primary" @click="showJoinDialog = true">
          <i class="el-icon-right"></i> 加入团队
        </el-button>
        <el-button type="success" @click="showCreateDialog = true">
          <i class="el-icon-plus"></i> 创建团队
        </el-button>
      </div>
    </div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="team in teams" :key="team.id">
        <el-card class="team-card" shadow="hover" @click.native="$router.push(`/teams/${team.id}`)">
          <div class="team-card-header">
            <div class="team-avatar">{{ team.name[0] }}</div>
            <el-dropdown trigger="click" @command="handleCommand($event, team)" @click.native.stop>
              <i class="el-icon-more"></i>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <h3>{{ team.name }}</h3>
          <p>{{ team.description || '暂无描述' }}</p>
          <div class="team-meta">
            <span><i class="el-icon-user"></i> {{ team.memberCount || 0 }} 成员</span>
            <span>{{ team.creatorName }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination v-if="total > pageSize" :current-page="currentPage" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="handlePageChange" style="margin-top: 20px; text-align: center;"></el-pagination>

    <el-dialog title="创建团队" :visible.sync="showCreateDialog" width="500px">
      <el-form :model="teamForm" :rules="teamRules" ref="teamForm">
        <el-form-item label="团队名称" prop="name">
          <el-input v-model="teamForm.name" placeholder="请输入团队名称"></el-input>
        </el-form-item>
        <el-form-item label="团队描述">
          <el-input v-model="teamForm.description" type="textarea" :rows="3" placeholder="请输入团队描述"></el-input>
        </el-form-item>
        <el-form-item label="最大成员数">
          <el-input-number v-model="teamForm.maxMembers" :min="2" :max="100"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createTeam">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="加入团队" :visible.sync="showJoinDialog" width="400px">
      <el-form>
        <el-form-item label="邀请码">
          <el-input v-model="inviteCode" placeholder="请输入邀请码"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" @click="joinTeam">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTeamPage, createTeam, deleteTeam, joinTeam } from '@/api/team'

export default {
  name: 'TeamList',
  data() {
    return {
      teams: [],
      keyword: '',
      currentPage: 1,
      pageSize: 12,
      total: 0,
      showCreateDialog: false,
      showJoinDialog: false,
      inviteCode: '',
      creating: false,
      teamForm: { name: '', description: '', maxMembers: 50 },
      teamRules: {
        name: [{ required: true, message: '请输入团队名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchTeams()
  },
  methods: {
    async fetchTeams() {
      try {
        const res = await getTeamPage({ page: this.currentPage, size: this.pageSize, keyword: this.keyword })
        this.teams = res.data?.records || []
        this.total = res.data?.total || 0
      } catch (error) {
        // Error handled by interceptor
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchTeams()
    },
    createTeam() {
      this.$refs.teamForm.validate(async valid => {
        if (!valid) return
        this.creating = true
        try {
          await createTeam(this.teamForm)
          this.$message.success('团队创建成功')
          this.showCreateDialog = false
          this.fetchTeams()
        } catch (error) {
          // Error handled by interceptor
        } finally {
          this.creating = false
        }
      })
    },
    async joinTeam() {
      if (!this.inviteCode) {
        this.$message.warning('请输入邀请码')
        return
      }
      try {
        await joinTeam(this.inviteCode)
        this.$message.success('已加入团队')
        this.showJoinDialog = false
        this.fetchTeams()
      } catch (error) {
        // Error handled by interceptor
      }
    },
    handleCommand(command, team) {
      if (command === 'delete') {
        this.$confirm('确定删除该团队吗？', '警告', { type: 'warning' }).then(async () => {
          await deleteTeam(team.id)
          this.$message.success('团队已删除')
          this.fetchTeams()
        }).catch(() => {})
      } else if (command === 'edit') {
        this.$router.push(`/teams/${team.id}`)
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

.team-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  .team-card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
  }

  .team-avatar {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: bold;
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

  .team-meta {
    display: flex;
    justify-content: space-between;
    color: var(--text-color-secondary);
    font-size: 12px;

    i {
      margin-right: 4px;
    }
  }
}
</style>
