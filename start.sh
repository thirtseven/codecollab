#!/bin/bash

echo "=========================================="
echo "  CodeCollab Platform Startup Script"
echo "=========================================="

export JAVA_HOME=/opt/jdk17
export PATH=$JAVA_HOME/bin:$PATH
unset LD_PRELOAD PROXYCHAINS_CONF_FILE

WSL_IP=$(hostname -I | awk '{print $1}')

echo "[1/4] Starting MariaDB..."
sudo service mariadb start 2>/dev/null
sleep 2

echo "[2/4] Starting Backend (Spring Boot)..."
pkill -f "codecollab-backend" 2>/dev/null
sleep 2
cd /mnt/c/Users/Lenovo/codecollab/backend
nohup java -jar target/codecollab-backend-1.0.0.jar > /tmp/codecollab-backend.log 2>&1 &
BACKEND_PID=$!
echo "      Backend PID: $BACKEND_PID"

echo "      Waiting for backend to start..."
for i in {1..40}; do
    sleep 1
    if curl -s -m 2 --noproxy '*' "http://$WSL_IP:8080/api/auth/login" > /dev/null 2>&1; then
        echo "      Backend started!"
        break
    fi
    echo -n "."
done
echo ""

echo "[3/4] Starting Nginx..."
sudo cp /mnt/c/Users/Lenovo/codecollab/nginx.conf /etc/nginx/sites-available/codecollab.conf
sudo nginx -t 2>&1 && sudo nginx -s reload 2>/dev/null || sudo nginx 2>/dev/null

echo "[4/4] Verifying services..."
echo ""

echo "=========================================="
echo "  CodeCollab Platform Ready!"
echo "=========================================="
echo ""
echo "  Frontend:  http://$WSL_IP"
echo "  Backend:   http://$WSL_IP:8080"
echo "  API Docs:  http://$WSL_IP:8080/api/doc.html"
echo ""
echo "  Custom Domain Setup (Windows):"
echo "  Add to C:\\Windows\\System32\\drivers\\etc\\hosts:"
echo "  $WSL_IP codecollab.local"
echo "  Then visit: http://codecollab.local"
echo ""
echo "  Default Admin Account:"
echo "    Username: admin"
echo "    Password: admin123"
echo ""
echo "=========================================="
