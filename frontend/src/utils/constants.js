export const APP_NAME = 'CodeCollab'
export const APP_DESC = '多人代码协作平台'

export const NAV = {
  DASHBOARD: '工作台',
  TEAMS: '团队管理',
  PROJECTS: '项目管理',
  AI_ASSISTANT: 'AI 助手',
  ADMIN: '系统管理',
  PROFILE: '个人中心',
  LOGOUT: '退出登录'
}

export const AUTH = {
  LOGIN: '登录',
  REGISTER: '注册',
  USERNAME: '用户名',
  PASSWORD: '密码',
  EMAIL: '邮箱',
  VERIFICATION_CODE: '验证码',
  NICKNAME: '昵称',
  SEND_CODE: '发送验证码',
  LOGIN_SUCCESS: '登录成功',
  REGISTER_SUCCESS: '注册成功',
  LOGOUT_SUCCESS: '已退出登录',
  NO_ACCOUNT: '还没有账号？',
  HAS_ACCOUNT: '已有账号？',
  FORGOT_PASSWORD: '忘记密码？',
  CREATE_ACCOUNT: '创建账号',
  LOGIN_SUBTITLE: '多人代码协作平台',
  REGISTER_SUBTITLE: '创建您的账号'
}

export const TEAM = {
  TITLE: '团队管理',
  CREATE: '创建团队',
  JOIN: '加入团队',
  LEAVE: '退出团队',
  NAME: '团队名称',
  DESCRIPTION: '团队描述',
  MEMBERS: '成员管理',
  MAX_MEMBERS: '最大成员数',
  INVITE_CODE: '邀请码',
  INVITE_CODE_PLACEHOLDER: '请输入邀请码',
  MEMBER_COUNT: '成员数',
  OWNER: '创建者',
  COPY_CODE: '复制邀请码',
  CODE_COPIED: '邀请码已复制',
  DELETE_CONFIRM: '确定删除该团队吗？',
  LEAVE_CONFIRM: '确定退出该团队吗？',
  REMOVE_MEMBER: '移除成员',
  REMOVE_CONFIRM: '确定移除该成员吗？',
  SET_ADMIN: '设为管理员',
  SET_MEMBER: '设为普通成员',
  SEARCH_PLACEHOLDER: '搜索团队...',
  NO_TEAMS: '暂无团队',
  NO_DESCRIPTION: '暂无描述'
}

export const PROJECT = {
  TITLE: '项目管理',
  CREATE: '创建项目',
  NAME: '项目名称',
  DESCRIPTION: '项目描述',
  LANGUAGE: '编程语言',
  SELECT_TEAM: '选择团队',
  VISIBILITY: '可见性',
  PUBLIC: '公开',
  PRIVATE: '私有',
  FILES: '文件管理',
  SUBMISSIONS: '提交记录',
  FILE_COUNT: '文件数',
  SUBMISSION_COUNT: '提交数',
  DELETE_CONFIRM: '确定删除该项目吗？',
  SEARCH_PLACEHOLDER: '搜索项目...',
  NO_PROJECTS: '暂无项目',
  OPEN_EDITOR: '打开编辑器',
  SUBMIT_CODE: '提交代码'
}

export const CODE = {
  TITLE: '代码编辑器',
  SAVE: '保存',
  NEW_FILE: '新建文件',
  FILE_NAME: '文件名',
  FILE_PATH: '文件路径',
  SELECT_FILE: '选择文件编辑',
  SAVE_SUCCESS: '文件已保存',
  CREATE_SUCCESS: '文件已创建',
  SUBMIT: '提交',
  REVIEW: '审核',
  APPROVE: '通过',
  REJECT: '驳回',
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  BRANCH: '分支',
  COMMIT_TITLE: '提交标题',
  COMMIT_DESC: '提交描述'
}

export const AI = {
  TITLE: 'AI 助手',
  NEW_CHAT: '新建对话',
  SEND: '发送',
  TYPE_MESSAGE: '输入消息...',
  CONVERSATIONS: '对话列表',
  DELETE_CONFIRM: '确定删除该对话吗？',
  THINKING: '思考中...',
  HELP_INTRO: '你好！我是 CodeCollab AI 助手，可以帮你解答代码问题。',
  HELP_ITEMS: '我可以帮你：\n- 代码审查和建议\n- Bug 修复\n- 编写文档\n- 解释代码概念\n- 生成代码片段'
}

export const ADMIN = {
  TITLE: '系统管理',
  DASHBOARD: '仪表盘',
  USERS: '用户管理',
  CONFIGS: '系统配置',
  TOTAL_USERS: '总用户数',
  TOTAL_TEAMS: '总团队数',
  TOTAL_PROJECTS: '总项目数',
  TOTAL_SUBMISSIONS: '总提交数',
  ACTIVE_USERS: '活跃用户',
  PENDING_SUBMISSIONS: '待审核提交',
  SEARCH_USERS: '搜索用户...',
  FILTER_ROLE: '筛选角色',
  ENABLE: '启用',
  DISABLE: '禁用',
  DELETE: '删除',
  SAVE: '保存',
  STATUS_UPDATED: '状态已更新',
  ROLE_UPDATED: '角色已更新',
  USER_DELETED: '用户已删除',
  CONFIG_UPDATED: '配置已更新',
  DELETE_USER_CONFIRM: '确定删除用户 {username} 吗？'
}

export const PROFILE = {
  TITLE: '个人中心',
  EDIT_PROFILE: '编辑资料',
  CHANGE_PASSWORD: '修改密码',
  OLD_PASSWORD: '原密码',
  NEW_PASSWORD: '新密码',
  CONFIRM_PASSWORD: '确认密码',
  PHONE: '手机号',
  BIO: '个人简介',
  GITHUB: 'GitHub',
  WEBSITE: '个人网站',
  SAVE_CHANGES: '保存修改',
  PASSWORD_MISMATCH: '两次密码不一致',
  PROFILE_UPDATED: '资料已更新',
  PASSWORD_CHANGED: '密码已修改',
  JOINED: '加入时间'
}

export const COMMON = {
  CONFIRM: '确定',
  CANCEL: '取消',
  SAVE: '保存',
  DELETE: '删除',
  EDIT: '编辑',
  CREATE: '创建',
  SEARCH: '搜索',
  LOADING: '加载中...',
  NO_DATA: '暂无数据',
  SUCCESS: '操作成功',
  ERROR: '操作失败',
  WARNING: '警告',
  CONFIRM_ACTION: '确定执行此操作吗？',
  YES: '是',
  NO: '否',
  TOTAL: '共',
  ITEMS: '条',
  PAGE: '页',
  PREV: '上一页',
  NEXT: '下一页'
}

export const VALIDATION = {
  REQUIRED: '此项为必填项',
  USERNAME_REQUIRED: '请输入用户名',
  USERNAME_LENGTH: '用户名长度为 3-20 个字符',
  PASSWORD_REQUIRED: '请输入密码',
  PASSWORD_LENGTH: '密码长度为 6-20 个字符',
  EMAIL_REQUIRED: '请输入邮箱',
  EMAIL_INVALID: '邮箱格式不正确',
  CODE_REQUIRED: '请输入验证码',
  TEAM_NAME_REQUIRED: '请输入团队名称',
  TEAM_NAME_LENGTH: '团队名称长度为 2-50 个字符',
  PROJECT_NAME_REQUIRED: '请输入项目名称',
  TEAM_REQUIRED: '请选择团队',
  FILE_PATH_REQUIRED: '请输入文件路径',
  MESSAGE_REQUIRED: '请输入消息',
  PASSWORD_CONFIRM_REQUIRED: '请确认密码'
}

export const ROLES = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
  OWNER: '拥有者',
  MEMBER: '成员'
}

export const LANGUAGES = {
  JAVA: 'Java',
  PYTHON: 'Python',
  JAVASCRIPT: 'JavaScript',
  TYPESCRIPT: 'TypeScript',
  GO: 'Go',
  CPP: 'C++',
  C: 'C',
  RUST: 'Rust',
  SWIFT: 'Swift',
  KOTLIN: 'Kotlin'
}

export const STATUS = {
  ACTIVE: '正常',
  INACTIVE: '禁用',
  BANNED: '封禁',
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}
