#!/bin/bash
# CoolAI-Java 服务器部署脚本
# 使用方法: ./deploy.sh [命令]
# 命令:
#   init     - 首次部署（构建镜像 + 启动）
#   start    - 启动所有服务
#   stop     - 停止所有服务
#   restart  - 重启服务
#   status   - 查看服务状态
#   logs     - 查看日志
#   update   - 更新代码并重新部署

set -e

COMPOSE_FILE="docker-compose.prod.yml"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 检查 .env 文件
if [ ! -f "$PROJECT_DIR/.env" ]; then
    echo "⚠️  未找到 .env 文件，从模板创建..."
    cp "$PROJECT_DIR/.env.example" "$PROJECT_DIR/.env"
    echo "📝 请编辑 .env 文件配置你的密钥:"
    echo "   vi $PROJECT_DIR/.env"
    echo ""
    echo "必要配置项:"
    echo "  - JWT_SECRET           (JWT密钥)"
    echo "  - OPENAI_API_KEY       (OpenAI API密钥)"
    echo "  - MIMO_API_KEY         (小米 MiMo API 密钥，兼容 OpenAI API)"
    echo "  - ANTHROPIC_API_KEY    (Claude API密钥, 可选)"
    echo ""
    read -p "是否继续使用默认配置? (y/n) " -n 1 -r
    echo
    [[ ! $REPLY =~ ^[Yy]$ ]] && exit 1
fi

# 加载环境变量
set -a
source "$PROJECT_DIR/.env"
set +a

case "${1:-help}" in
    init)
        echo "🚀 首次部署 CoolAI-Java..."
        
        # 1. 创建必要目录
        mkdir -p "$PROJECT_DIR/deploy/ssl"
        mkdir -p "$PROJECT_DIR/logs"
        
        # 2. 检查 SSL 证书
        if [ ! -f "$PROJECT_DIR/deploy/ssl/cert.pem" ]; then
            echo "⚠️  未找到 SSL 证书，生成自签名证书..."
            openssl req -x509 -nodes -days 365 \
                -newkey rsa:2048 \
                -keyout "$PROJECT_DIR/deploy/ssl/key.pem" \
                -out "$PROJECT_DIR/deploy/ssl/cert.pem" \
                -subj "/CN=localhost"
            echo "✅ 自签名证书已生成 (生产环境请替换为正式证书)"
        fi
        
        # 3. 构建镜像
        echo "🔨 构建 Docker 镜像..."
        cd "$PROJECT_DIR"
        docker-compose -f "$COMPOSE_FILE" build
        
        # 4. 启动基础设施
        echo "🚀 启动基础设施..."
        docker-compose -f "$COMPOSE_FILE" up -d postgres redis rabbitmq minio etcd milvus
        
        echo "⏳ 等待数据库就绪..."
        sleep 15
        
        # 5. 启动微服务
        echo "🚀 启动微服务..."
        docker-compose -f "$COMPOSE_FILE" up -d
        
        # 6. 如果有 GPU
        echo ""
        echo "💡 如有 NVIDIA GPU，启动 GPU Worker:"
        echo "   docker-compose -f $COMPOSE_FILE --profile gpu up -d gpu-worker"
        
        echo ""
        echo "✅ 部署完成!"
        echo "   API: https://your-server"
        echo "   MinIO 控制台: http://your-server:9001"
        echo "   RabbitMQ 管理: http://your-server:15672"
        ;;
    
    start)
        docker-compose -f "$COMPOSE_FILE" up -d
        ;;
    
    stop)
        docker-compose -f "$COMPOSE_FILE" down
        ;;
    
    restart)
        docker-compose -f "$COMPOSE_FILE" restart
        ;;
    
    status)
        docker-compose -f "$COMPOSE_FILE" ps
        ;;
    
    logs)
        local svc="${2:-}"
        if [ -n "$svc" ]; then
            docker-compose -f "$COMPOSE_FILE" logs -f "$svc"
        else
            docker-compose -f "$COMPOSE_FILE" logs -f --tail=100
        fi
        ;;
    
    update)
        echo "🔄 更新部署..."
        cd "$PROJECT_DIR"
        git pull
        docker-compose -f "$COMPOSE_FILE" build
        docker-compose -f "$COMPOSE_FILE" up -d
        echo "✅ 更新完成"
        ;;
    
    help|*)
        echo "CoolAI-Java 服务器部署脚本"
        echo ""
        echo "用法: $0 <命令>"
        echo ""
        echo "命令:"
        echo "  init     首次部署 (构建+启动)"
        echo "  start    启动所有服务"
        echo "  stop     停止所有服务"
        echo "  restart  重启服务"
        echo "  status   查看服务状态"
        echo "  logs [服务名]  查看日志"
        echo "  update   更新代码并重新部署"
        ;;
esac
