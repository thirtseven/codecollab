# CodeCollab API 接口文档

## 基础信息

- **Base URL**: `http://172.31.183.54:8080/api`
- **认证方式**: JWT Bearer Token
- **内容类型**: `application/json`

## 认证模块

### 登录
```
POST /auth/login
请求体: { "username": "string", "password": "string" }
响应: { "code": 200, "data": { "token": "string", "refreshToken": "string", "user": {...} } }
```

### 注册
```
POST /auth/register
请求体: { "username": "string", "email": "string", "password": "string", "code": "string", "nickname": "string" }
响应: { "code": 200, "data": { "id": 1, "username": "string", ... } }
```

### 发送验证码
```
POST /auth/send-code?email={email}&type={type}
参数: email - 邮箱地址, type - 类型(REGISTER/RESET_PASSWORD)
响应: { "code": 200, "message": "success" }
```

### 获取用户信息
```
GET /auth/user-info
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "id": 1, "username": "string", ... } }
```

### 更新个人资料
```
PUT /auth/profile
请求头: Authorization: Bearer {token}
请求体: { "nickname": "string", "phone": "string", "bio": "string", "githubUrl": "string", "website": "string" }
响应: { "code": 200, "data": {...} }
```

### 修改密码
```
PUT /auth/password?oldPassword={old}&newPassword={new}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

## 团队模块

### 获取团队列表
```
GET /teams?page={page}&size={size}&keyword={keyword}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "total": 10, "records": [...] } }
```

### 创建团队
```
POST /teams
请求头: Authorization: Bearer {token}
请求体: { "name": "string", "description": "string", "maxMembers": 50 }
响应: { "code": 200, "data": { "id": 1, "name": "string", ... } }
```

### 获取团队详情
```
GET /teams/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "id": 1, "name": "string", "members": [...] } }
```

### 更新团队
```
PUT /teams/{id}
请求头: Authorization: Bearer {token}
请求体: { "name": "string", "description": "string", "maxMembers": 50 }
响应: { "code": 200, "data": {...} }
```

### 删除团队
```
DELETE /teams/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 获取我的团队
```
GET /teams/my
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

### 加入团队
```
POST /teams/join?inviteCode={code}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 退出团队
```
POST /teams/{id}/leave
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 获取团队成员
```
GET /teams/{id}/members
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

### 移除成员
```
DELETE /teams/{teamId}/members/{userId}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 更新成员角色
```
PUT /teams/{teamId}/members/{userId}/role?role={role}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

## 项目模块

### 获取项目列表
```
GET /projects?page={page}&size={size}&teamId={teamId}&keyword={keyword}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "total": 10, "records": [...] } }
```

### 创建项目
```
POST /projects
请求头: Authorization: Bearer {token}
请求体: { "name": "string", "description": "string", "teamId": 1, "language": "Java", "isPublic": 0 }
响应: { "code": 200, "data": { "id": 1, "name": "string", ... } }
```

### 获取项目详情
```
GET /projects/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "id": 1, "name": "string", ... } }
```

### 更新项目
```
PUT /projects/{id}
请求头: Authorization: Bearer {token}
请求体: { "name": "string", "description": "string", "language": "Java", "isPublic": 0 }
响应: { "code": 200, "data": {...} }
```

### 删除项目
```
DELETE /projects/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 获取项目文件
```
GET /projects/{id}/files
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

### 获取文件内容
```
GET /projects/files/{fileId}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "id": 1, "content": "string", ... } }
```

### 保存文件
```
POST /projects/{id}/files
请求头: Authorization: Bearer {token}
请求体: { "filePath": "/src/Main.java", "fileName": "Main.java", "content": "..." }
响应: { "code": 200, "data": { "id": 1, ... } }
```

### 删除文件
```
DELETE /projects/files/{fileId}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 创建目录
```
POST /projects/{id}/directories?dirPath={path}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

## 代码提交模块

### 获取提交列表
```
GET /submissions?page={page}&size={size}&projectId={projectId}&status={status}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "total": 10, "records": [...] } }
```

### 创建提交
```
POST /submissions
请求头: Authorization: Bearer {token}
请求体: {
  "projectId": 1,
  "title": "string",
  "description": "string",
  "branch": "main",
  "files": [
    { "filePath": "/src/Main.java", "fileName": "Main.java", "content": "...", "changeType": "ADD" }
  ]
}
响应: { "code": 200, "data": { "id": 1, ... } }
```

### 获取提交详情
```
GET /submissions/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "id": 1, ... } }
```

### 获取提交文件
```
GET /submissions/{id}/files
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

### 审核提交
```
POST /submissions/{id}/review?status={status}&comment={comment}
请求头: Authorization: Bearer {token}
参数: status - APPROVED/REJECTED, comment - 审核意见
响应: { "code": 200, "message": "success" }
```

### 删除提交
```
DELETE /submissions/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 获取项目提交
```
GET /submissions/project/{projectId}?limit={limit}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

## AI 助手模块

### 发送消息
```
POST /ai/chat
请求头: Authorization: Bearer {token}
请求体: { "conversationId": 1, "message": "string", "projectId": 1 }
响应: { "code": 200, "data": { "conversationId": 1, "message": "string" } }
```

### 获取对话列表
```
GET /ai/conversations
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

### 获取对话消息
```
GET /ai/conversations/{id}/messages
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

### 删除对话
```
DELETE /ai/conversations/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

## 管理模块

### 获取仪表盘统计
```
GET /admin/dashboard
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "totalUsers": 10, "totalTeams": 5, ... } }
```

### 获取用户列表
```
GET /admin/users?page={page}&size={size}&keyword={keyword}&role={role}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": { "total": 10, "records": [...] } }
```

### 更新用户状态
```
PUT /admin/users/{id}/status?status={status}
请求头: Authorization: Bearer {token}
参数: status - 1(正常)/0(禁用)
响应: { "code": 200, "message": "success" }
```

### 更新用户角色
```
PUT /admin/users/{id}/role?role={role}
请求头: Authorization: Bearer {token}
参数: role - ADMIN/TEACHER/STUDENT
响应: { "code": 200, "message": "success" }
```

### 删除用户
```
DELETE /admin/users/{id}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

### 获取系统配置
```
GET /admin/configs
请求头: Authorization: Bearer {token}
响应: { "code": 200, "data": [...] }
```

### 更新系统配置
```
PUT /admin/configs/{key}?value={value}
请求头: Authorization: Bearer {token}
响应: { "code": 200, "message": "success" }
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
