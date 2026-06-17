# CodeCollab - Multi-person Code Submission Platform

## Project Summary

A full-stack web application for collaborative code development, built with SpringBoot + MyBatis (backend) and Vue2 + ElementUI (frontend).

## Architecture

```
Frontend (Vue2 + ElementUI) → Nginx (Port 80) → Backend (SpringBoot, Port 8080) → MariaDB
```

## Tech Stack

- **Backend**: SpringBoot 2.7.18, MyBatis Plus 3.5.5, JWT, BCrypt
- **Frontend**: Vue2, ElementUI, Vuex, Vue Router
- **Database**: MariaDB 10.6 (MySQL compatible)
- **Server**: Nginx reverse proxy
- **Network**: WSL2 → Windows (custom domain)

## Features (5 Modules)

### 1. User Center (用户中心)
- Registration with email verification (163 SMTP)
- Login with JWT authentication
- Profile management
- Role-based access control (ADMIN, TEACHER, STUDENT)

### 2. Team Management (团队管理)
- Create/join teams with invite codes
- Member management (add/remove/role change)
- Permission assignment
- Team detail view with member list

### 3. Project Management (项目管理)
- Create projects within teams
- File management (create/edit/delete)
- Directory structure support
- Project collaboration

### 4. Code Submission (代码提交)
- Submit code with files
- Version history tracking
- Submission review (approve/reject)
- Batch file operations

### 5. AI Assistant (AI助手)
- Chat with AI (custom API integration)
- Conversation history
- Context-aware responses
- Project-specific assistance

### 6. Admin Panel (管理员面板)
- User management (CRUD)
- System configuration
- Dashboard statistics
- Data export (Excel/CSV)

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/send-code` - Send verification code
- `GET /api/auth/user-info` - Get current user info
- `PUT /api/auth/profile` - Update profile

### Teams
- `GET /api/teams` - List teams
- `POST /api/teams` - Create team
- `GET /api/teams/{id}` - Get team detail
- `POST /api/teams/join` - Join team with invite code
- `GET /api/teams/{id}/members` - Get team members

### Projects
- `GET /api/projects` - List projects
- `POST /api/projects` - Create project
- `GET /api/projects/{id}` - Get project detail
- `GET /api/projects/{id}/files` - Get project files
- `POST /api/projects/{id}/files` - Save file

### Submissions
- `GET /api/submissions` - List submissions
- `POST /api/submissions` - Create submission
- `GET /api/submissions/{id}` - Get submission detail
- `POST /api/submissions/{id}/review` - Review submission

### AI
- `POST /api/ai/chat` - Chat with AI
- `GET /api/ai/conversations` - List conversations
- `GET /api/ai/conversations/{id}/messages` - Get messages

### Admin
- `GET /api/admin/dashboard` - Dashboard stats
- `GET /api/admin/users` - List users
- `PUT /api/admin/users/{id}/status` - Update user status
- `GET /api/admin/configs` - System configs

## Database Schema

- `users` - User accounts with roles
- `email_verifications` - Email verification codes
- `teams` - Team information
- `team_members` - Team membership
- `projects` - Project information
- `project_files` - Project files
- `code_submissions` - Code submissions
- `submission_files` - Submission files
- `ai_conversations` - AI chat conversations
- `ai_messages` - AI chat messages
- `permissions` - Role permissions
- `system_configs` - System configuration

## Running the Application

### Prerequisites
- Java 17
- MariaDB/MySQL
- Node.js 18+
- Nginx

### Start Commands

```bash
# Start everything
./start.sh

# Or manually:
# 1. Start MariaDB
sudo service mariadb start

# 2. Start Backend
cd backend
java -jar target/codecollab-backend-1.0.0.jar

# 3. Start Frontend (development)
cd frontend
npm run serve

# 4. Nginx (production)
sudo nginx
```

### Access URLs

- **Frontend**: http://localhost (via nginx)
- **Backend API**: http://localhost:8080/api
- **Custom Domain**: http://codecollab.local (add to Windows hosts)

### Default Admin Account

- **Username**: admin
- **Password**: admin123

## Test Suite

Run the test suite to verify all functionality:

```bash
./test.sh
```

Expected output: 27/27 tests passed

## Project Structure

```
codecollab/
├── backend/
│   ├── src/main/java/com/codecollab/
│   │   ├── CodeCollabApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── interceptor/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── util/
│   │   └── vo/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/schema.sql
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── router/
│   │   ├── store/
│   │   ├── utils/
│   │   └── views/
│   ├── package.json
│   └── vue.config.js
├── nginx.conf
├── start.sh
└── test.sh
```

## SMTP Configuration

- **Host**: smtp.163.com
- **Port**: 465 (SSL)
- **Username**: codecollab@163.com
- **Password**: SRiHc33XFwP2HikR

## Environment Notes

- **WSL Proxy**: The environment uses proxychains. Always `unset LD_PRELOAD PROXYCHAINS_CONF_FILE` before running Java/MySQL commands.
- **MySQL Auth**: Use `mysql_native_password` authentication plugin.
- **Network**: Backend binds to 0.0.0.0:8080, nginx proxies from port 80.
