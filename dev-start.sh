#!/bin/bash
# CoolAI-Java 本地开发环境启动脚本
# 使用方法: ./dev-start.sh [命令]
# 命令:
#   infra    - 仅启动基础设施(PostgreSQL/Redis/RabbitMQ等)
#   auth     - 启动认证服务
#   chat     - 启动聊天服务
#   all      - 启动所有服务
#   stop     - 停止所有服务
#   status   - 查看服务状态
#   build    - 编译项目

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
DETECTED_JAVA_17="$(/usr/libexec/java_home -v 17 2>/dev/null || echo "")"
if [ -n "$DETECTED_JAVA_17" ]; then
    JAVA_HOME="$DETECTED_JAVA_17"
else
    JAVA_HOME="${JAVA_HOME:-}"
fi
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

# 加载项目根目录 .env 配置（如果存在）
if [ -f "$PROJECT_DIR/.env" ]; then
    echo "🔧 加载环境变量: $PROJECT_DIR/.env"
    set -a
    # shellcheck source=/dev/null
    source "$PROJECT_DIR/.env"
    set +a
fi

if [ -z "$JAVA_HOME" ]; then
    echo "❌ 未找到 Java 17，请设置 JAVA_HOME 或安装 JDK 17"
    exit 1
fi

JAVA_MAJOR="$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F[\".] '/version/ {print $2}')"
if [ "$JAVA_MAJOR" != "17" ]; then
    echo "❌ 当前 JAVA_HOME 不是 Java 17: $JAVA_HOME"
    "$JAVA_HOME/bin/java" -version 2>&1 | head -1
    exit 1
fi

echo "☕ Java: $JAVA_HOME"
echo "📁 项目: $PROJECT_DIR"

# ============ 基础设施 ============
start_infra() {
    echo "🚀 启动基础设施..."
    cd "$PROJECT_DIR"
    docker-compose up -d postgres redis rabbitmq minio
    
    echo "⏳ 等待服务就绪..."
    sleep 10
    
    # 检查 PostgreSQL
    if docker exec coolai-postgres pg_isready -U coolai &>/dev/null; then
        echo "✅ PostgreSQL 就绪 (localhost:5432)"
    else
        echo "⏳ PostgreSQL 还在启动，稍等..."
        sleep 5
    fi
    
    # 检查 Redis
    if docker exec coolai-redis redis-cli ping &>/dev/null; then
        echo "✅ Redis 就绪 (localhost:6379)"
    fi
    
    # 检查 RabbitMQ
    if curl -s http://localhost:15672 &>/dev/null; then
        echo "✅ RabbitMQ 就绪 (localhost:15672, 账号: coolai/coolai123)"
    fi
    
    # 检查 MinIO
    if curl -s http://localhost:9000/minio/health/live &>/dev/null; then
        echo "✅ MinIO 就绪 (localhost:9000, 控制台: http://localhost:9001)"
    fi
    
    echo ""
    echo "📊 可选监控服务:"
    echo "   docker-compose up -d prometheus grafana  # Prometheus + Grafana"
    echo "   docker-compose up -d etcd milvus        # Milvus 向量数据库"
    echo "   docker-compose up -d nacos               # Nacos 配置中心"
}

# ============ 编译项目 ============
build_project() {
    echo "🔨 编译项目..."
    cd "$PROJECT_DIR"
    "$JAVA_HOME/bin/java" -version 2>&1 | head -1
    mvn clean package -DskipTests -q
    echo "✅ 编译完成"
}

# ============ 启动微服务 ============
start_service() {
    local service=$1
    local port=$2
    local jar="$PROJECT_DIR/$service/target/$service-2.0.0.jar"
    
    if [ ! -f "$jar" ]; then
        echo "📦 $service 未编译，正在编译..."
        cd "$PROJECT_DIR"
        mvn clean package -pl "$service" -am -DskipTests -q
    fi
    
    echo "🚀 启动 $service (端口: $port)..."
    "$JAVA_HOME/bin/java" -jar "$jar" \
        --spring.profiles.active=dev \
        > "$PROJECT_DIR/logs/$service.log" 2>&1 &
    
    local pid=$!
    echo "   PID: $pid, 日志: logs/$service.log"
    echo "$pid" > "$PROJECT_DIR/logs/$service.pid"
}

start_auth()   { start_service "coolai-auth"      8081; }
start_chat()   { start_service "coolai-chat"      8082; }
start_agent()  { start_service "coolai-agent"      8090; }
start_know()   { start_service "coolai-knowledge"  8083; }
start_knowledge(){ start_know; }
start_tts()    { start_service "coolai-tts"       8084; }
start_voice()  { start_service "coolai-voice"     8085; }
start_singing(){ start_service "coolai-singing"   8086; }
start_video()  { start_service "coolai-video"     8087; }
start_deep()   { start_service "coolai-deepfake"  8088; }
start_gateway(){ start_service "coolai-gateway"   8080; }
start_admin()  { start_service "coolai-admin"     8089; }

start_all() {
    echo "🚀 启动所有微服务..."
    mkdir -p "$PROJECT_DIR/logs"
    
    start_auth
    sleep 3
    start_chat
    start_agent
    start_knowledge
    start_tts
    start_voice
    start_singing
    start_video
    start_deep
    start_admin
    sleep 2
    start_gateway
    
    if [ -d "$PROJECT_DIR/frontend" ] && [ -f "$PROJECT_DIR/frontend/package.json" ]; then
        start_frontend
    fi

    echo ""
    echo "✅ 所有服务已启动"
    show_urls
}

print_env() {
    echo "# To use Java 17 for project commands, run this in your shell:"
    echo "#   eval \"$(dirname \"$0\")/dev-start.sh setup\""
    echo "export JAVA_HOME=\"$JAVA_HOME\""
    echo "export PATH=\"$JAVA_HOME/bin:$PATH\""
}

start_frontend() {
    local frontend_dir="$PROJECT_DIR/frontend"
    if [ ! -d "$frontend_dir" ] || [ ! -f "$frontend_dir/package.json" ]; then
        echo "⚠️  找不到前端项目目录，跳过前端启动"
        return
    fi

    if ! command -v npm >/dev/null 2>&1; then
        echo "⚠️  未找到 npm，请先安装 Node.js 和 npm，再启动前端"
        return
    fi

    echo "🚀 启动前端开发服务器..."
    cd "$frontend_dir"
    if [ ! -d node_modules ]; then
        echo "📦 前端依赖未安装，正在安装..."
        npm install
    fi

    npm run dev -- --host 0.0.0.0 > "$PROJECT_DIR/logs/frontend.log" 2>&1 &
    local pid=$!
    echo "   PID: $pid, 日志: logs/frontend.log"
    echo "$pid" > "$PROJECT_DIR/logs/frontend.pid"
}

stop_frontend() {
    local pidfile="$PROJECT_DIR/logs/frontend.pid"
    if [ -f "$pidfile" ]; then
        local pid
        pid=$(cat "$pidfile")
        kill "$pid" 2>/dev/null && echo "   已停止前端 PID: $pid"
        rm -f "$pidfile"
    fi
}

# ============ 停止服务 ============
stop_all() {
    echo "🛑 停止所有微服务..."
    if [ -d "$PROJECT_DIR/logs" ]; then
        for pidfile in "$PROJECT_DIR/logs/"*.pid; do
            if [ -f "$pidfile" ]; then
                local pid=$(cat "$pidfile")
                kill "$pid" 2>/dev/null && echo "   已停止 PID: $pid"
                rm "$pidfile"
            fi
        done
    fi
    stop_frontend
    echo "✅ 微服务已停止"
}

stop_infra() {
    echo "🛑 停止基础设施..."
    cd "$PROJECT_DIR"
    docker-compose down
    echo "✅ 基础设施已停止"
}

# ============ 状态检查 ============
show_status() {
    echo "📊 服务状态:"
    echo ""
    
    # 基础设施
    echo "基础设施:"
    for svc in coolai-postgres coolai-redis coolai-rabbitmq coolai-minio; do
        if docker ps --format "{{.Names}}" 2>/dev/null | grep -q "$svc"; then
            echo "  ✅ $svc - 运行中"
        else
            echo "  ❌ $svc - 未运行"
        fi
    done
    
    echo ""
    echo "微服务:"
    for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090; do
        if curl -s "http://localhost:$port/actuator/health" &>/dev/null; then
            echo "  ✅ 端口 $port - 运行中"
        else
            echo "  ❌ 端口 $port - 未运行"
        fi
    done
}

show_urls() {
    echo ""
    echo "🔗 服务地址:"
    echo "  网关:     http://localhost:8080"
    echo "  认证:     http://localhost:8081"
    echo "  聊天:     http://localhost:8082"
    echo "  Agent:    http://localhost:8090"
    echo "  知识库:   http://localhost:8083"
    echo "  TTS:      http://localhost:8084"
    echo "  声音克隆: http://localhost:8085"
    echo "  AI唱歌:   http://localhost:8086"
    echo "  视频生成: http://localhost:8087"
    echo "  人物替换: http://localhost:8088"
    echo "  系统管理: http://localhost:8089"
    echo "  前端:     http://localhost:5173"
    echo ""
    echo "  Swagger:  http://localhost:8081/swagger-ui.html"
    echo "  RabbitMQ: http://localhost:15672 (coolai/coolai123)"
    echo "  MinIO:    http://localhost:9001 (coolai/coolai12345)"
}

# ============ 主入口 ============
mkdir -p "$PROJECT_DIR/logs"

case "${1:-help}" in
    infra)
        start_infra
        ;;
    build)
        build_project
        ;;
    auth)
        mkdir -p "$PROJECT_DIR/logs"
        start_auth
        ;;
    chat)
        mkdir -p "$PROJECT_DIR/logs"
        start_chat
        ;;
    frontend)
        mkdir -p "$PROJECT_DIR/logs"
        start_frontend
        ;;
    all)
        start_infra
        echo ""
        build_project
        echo ""
        start_all
        ;;
    gateway)
        mkdir -p "$PROJECT_DIR/logs"
        start_gateway
        ;;
    admin)
        mkdir -p "$PROJECT_DIR/logs"
        start_admin
        ;;
    setup)
        print_env
        ;;
    stop)
        stop_all
        ;;
    down)
        stop_all
        stop_infra
        ;;
    status)
        show_status
        ;;
    urls)
        show_urls
        ;;
    help|*)
        echo "CoolAI-Java 本地开发脚本"
        echo ""
        echo "用法: $0 <命令>"
        echo ""
        echo "命令:"
        echo "  infra    启动基础设施 (PG/Redis/RabbitMQ/MinIO)"
        echo "  build    编译项目"
        echo "  auth     启动认证服务"
        echo "  chat     启动聊天服务"
        echo "  gateway  启动网关服务"
        echo "  admin    启动管理后台"
        echo "  frontend 启动前端开发服务器"
        echo "  all      启动基础设施 + 编译 + 启动所有服务"
        echo "  setup    输出 Java 17 环境配置，供 shell sourcing 使用"
        echo "  stop     停止微服务"
        echo "  down     停止微服务 + 基础设施"
        echo "  status   查看服务状态"
        echo "  urls     显示服务地址"
        ;;
esac
