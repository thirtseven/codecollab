#!/bin/bash

CODECOLLAB_WSL_IP=$(hostname -I | awk '{print $1}')
BASE_URL="http://$CODECOLLAB_WSL_IP:8080/api"
unset LD_PRELOAD PROXYCHAINS_CONF_FILE http_proxy https_proxy HTTP_PROXY HTTPS_PROXY

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS=0
FAIL=0
TOKEN=""

log_pass() { echo -e "${GREEN}[PASS]${NC} $1"; ((PASS++)); }
log_fail() { echo -e "${RED}[FAIL]${NC} $1"; ((FAIL++)); }
log_info() { echo -e "${YELLOW}[INFO]${NC} $1"; }

api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local auth=$4

    local curl_args="-s -m 10 --noproxy '*' -H 'Content-Type: application/json'"
    if [ "$auth" = "true" ] && [ -n "$TOKEN" ]; then
        curl_args="$curl_args -H 'Authorization: Bearer $TOKEN'"
    fi

    if [ "$method" = "GET" ]; then
        eval curl $curl_args "$BASE_URL$endpoint"
    elif [ "$method" = "POST" ]; then
        eval curl $curl_args -X POST -d "'$data'" "$BASE_URL$endpoint"
    elif [ "$method" = "PUT" ]; then
        eval curl $curl_args -X PUT -d "'$data'" "$BASE_URL$endpoint"
    elif [ "$method" = "DELETE" ]; then
        eval curl $curl_args -X DELETE "$BASE_URL$endpoint"
    fi
}

check_code() {
    local response=$1
    local expected=$2
    local description=$3

    local code=$(echo "$response" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2)
    if [ "$code" = "$expected" ]; then
        log_pass "$description"
        return 0
    else
        log_fail "$description (Expected code $expected, got $code)"
        echo "  Response: $(echo "$response" | head -c 200)"
        return 1
    fi
}

echo "=========================================="
echo "  CodeCollab Test Suite"
echo "=========================================="
echo ""

log_info "Testing Authentication Module..."
echo "---"

RESPONSE=$(api_call "POST" "/auth/login" '{"username":"admin","password":"admin123"}')
check_code "$RESPONSE" "200" "Admin Login"
TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TOKEN" ]; then
    log_pass "Token extracted successfully"
else
    log_fail "Failed to extract token"
fi

RESPONSE=$(api_call "POST" "/auth/login" '{"username":"admin","password":"wrong"}')
check_code "$RESPONSE" "400" "Login with wrong password"

RESPONSE=$(api_call "POST" "/auth/login" '{"username":"nonexistent","password":"test"}')
check_code "$RESPONSE" "400" "Login with nonexistent user"

RESPONSE=$(api_call "GET" "/auth/user-info" "" "true")
check_code "$RESPONSE" "200" "Get user info"

echo ""
log_info "Testing Team Module..."
echo "---"

RESPONSE=$(api_call "POST" "/teams" '{"name":"Test Team","description":"A test team","maxMembers":10}' "true")
check_code "$RESPONSE" "200" "Create team"
TEAM_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

RESPONSE=$(api_call "GET" "/teams" "" "true")
check_code "$RESPONSE" "200" "Get team list"

RESPONSE=$(api_call "GET" "/teams/$TEAM_ID" "" "true")
check_code "$RESPONSE" "200" "Get team detail"

RESPONSE=$(api_call "GET" "/teams/my" "" "true")
check_code "$RESPONSE" "200" "Get my teams"

RESPONSE=$(api_call "GET" "/teams/$TEAM_ID/members" "" "true")
check_code "$RESPONSE" "200" "Get team members"

echo ""
log_info "Testing Project Module..."
echo "---"

RESPONSE=$(api_call "POST" "/projects" "{\"name\":\"Test Project\",\"description\":\"A test project\",\"teamId\":$TEAM_ID,\"language\":\"Java\"}" "true")
check_code "$RESPONSE" "200" "Create project"
PROJECT_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

RESPONSE=$(api_call "GET" "/projects" "" "true")
check_code "$RESPONSE" "200" "Get project list"

RESPONSE=$(api_call "GET" "/projects/$PROJECT_ID" "" "true")
check_code "$RESPONSE" "200" "Get project detail"

RESPONSE=$(api_call "GET" "/projects/$PROJECT_ID/files" "" "true")
check_code "$RESPONSE" "200" "Get project files"

echo ""
log_info "Testing File Operations..."
echo "---"

RESPONSE=$(api_call "POST" "/projects/$PROJECT_ID/files" '{"filePath":"/src/App.java","fileName":"App.java","content":"public class App { public static void main(String[] args) {} }"}' "true")
check_code "$RESPONSE" "200" "Save file"
FILE_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

RESPONSE=$(api_call "GET" "/projects/files/$FILE_ID" "" "true")
check_code "$RESPONSE" "200" "Get file content"

echo ""
log_info "Testing Code Submission Module..."
echo "---"

RESPONSE=$(api_call "POST" "/submissions" "{\"projectId\":$PROJECT_ID,\"title\":\"Test Submission\",\"description\":\"Initial commit\",\"branch\":\"main\",\"files\":[{\"filePath\":\"/src/Main.java\",\"fileName\":\"Main.java\",\"content\":\"public class Main {}\",\"changeType\":\"ADD\"}]}" "true")
check_code "$RESPONSE" "200" "Create submission"
SUBMISSION_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

RESPONSE=$(api_call "GET" "/submissions" "" "true")
check_code "$RESPONSE" "200" "Get submission list"

RESPONSE=$(api_call "GET" "/submissions/$SUBMISSION_ID" "" "true")
check_code "$RESPONSE" "200" "Get submission detail"

RESPONSE=$(api_call "GET" "/submissions/$SUBMISSION_ID/files" "" "true")
check_code "$RESPONSE" "200" "Get submission files"

echo ""
log_info "Testing Submission Review..."
echo "---"

RESPONSE=$(api_call "POST" "/submissions/$SUBMISSION_ID/review?status=APPROVED&comment=Good+work" "" "true")
check_code "$RESPONSE" "200" "Review submission"

echo ""
log_info "Testing AI Module..."
echo "---"

RESPONSE=$(api_call "POST" "/ai/chat" '{"message":"Hello, can you help me?"}' "true")
check_code "$RESPONSE" "200" "AI chat"
CONVERSATION_ID=$(echo "$RESPONSE" | grep -o '"conversationId":[0-9]*' | cut -d: -f2)

RESPONSE=$(api_call "GET" "/ai/conversations" "" "true")
check_code "$RESPONSE" "200" "Get conversations"

RESPONSE=$(api_call "GET" "/ai/conversations/$CONVERSATION_ID/messages" "" "true")
check_code "$RESPONSE" "200" "Get conversation messages"

echo ""
log_info "Testing Admin Module..."
echo "---"

RESPONSE=$(api_call "GET" "/admin/dashboard" "" "true")
check_code "$RESPONSE" "200" "Get dashboard stats"

RESPONSE=$(api_call "GET" "/admin/users" "" "true")
check_code "$RESPONSE" "200" "Get user list"

RESPONSE=$(api_call "GET" "/admin/configs" "" "true")
check_code "$RESPONSE" "200" "Get system configs"

echo ""
echo "=========================================="
echo "  Test Results"
echo "=========================================="
echo -e "  ${GREEN}Passed: $PASS${NC}"
echo -e "  ${RED}Failed: $FAIL${NC}"
echo "  Total: $((PASS + FAIL))"
echo "=========================================="

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
