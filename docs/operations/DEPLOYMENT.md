# 部署文档

## 1. 环境要求

### 1.1 开发环境
- JDK 17+
- Node.js 18+
- PostgreSQL 16+
- Docker & Docker Compose（可选）

### 1.2 生产环境
- JDK 17+
- Node.js 18+
- PostgreSQL 16+（主从配置）
- Nginx（反向代理）
- Redis（缓存，可选）

## 2. 本地开发部署

### 2.1 数据库启动
```bash
# 使用Docker启动PostgreSQL
docker run --name inventory-pg \
  -e POSTGRES_DB=inventory \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### 2.2 后端启动
```bash
cd backend

# 安装依赖
./gradlew build -x test

# 启动应用
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2.3 前端启动
```bash
cd frontend

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev
```

### 2.4 访问地址
- 前端：http://localhost:5173
- 后端：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

## 3. Docker部署

### 3.1 构建镜像
```bash
# 构建后端镜像
cd backend
docker build -t inventory-backend:latest .

# 构建前端镜像
cd frontend
docker build -t inventory-frontend:latest .
```

### 3.2 Docker Compose配置
```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: inventory
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/inventory
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: your-secret-key
    ports:
      - "8080:8080"
    depends_on:
      - postgres

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

### 3.3 启动服务
```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 4. 生产环境部署

### 4.1 服务器配置
```bash
# 系统要求
- CPU: 2核心以上
- 内存: 4GB以上
- 磁盘: 50GB以上
- 操作系统: Ubuntu 20.04+ / CentOS 8+
```

### 4.2 数据库配置
```bash
# PostgreSQL配置
sudo apt-get install postgresql-16

# 创建数据库
sudo -u postgres createdb inventory

# 创建用户
sudo -u postgres createuser inventory_user

# 设置密码
sudo -u postgres psql -c "ALTER USER inventory_user PASSWORD 'your_password';"

# 授权
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE inventory TO inventory_user;"
```

### 4.3 应用部署
```bash
# 创建应用目录
sudo mkdir -p /opt/inventory
sudo chown $USER:$USER /opt/inventory

# 上传应用文件
scp inventory-backend.jar /opt/inventory/
scp -r frontend/dist/* /opt/inventory/frontend/

# 创建systemd服务
sudo tee /etc/systemd/system/inventory-backend.service > /dev/null <<EOF
[Unit]
Description=Inventory Backend Service
After=network.target

[Service]
Type=simple
User=inventory
WorkingDirectory=/opt/inventory
ExecStart=/usr/bin/java -jar inventory-backend.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable inventory-backend
sudo systemctl start inventory-backend
```

### 4.4 Nginx配置
```nginx
# /etc/nginx/sites-available/inventory
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /opt/inventory/frontend;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 5. 环境变量配置

### 5.1 后端环境变量
```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/inventory
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT配置
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400

# 文件存储配置
FILE_UPLOAD_PATH=/opt/inventory/uploads
FILE_MAX_SIZE=5242880

# 日志配置
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG
```

### 5.2 前端环境变量
```bash
# API基础URL
VITE_API_BASE=http://localhost:8080/api

# 应用配置
VITE_APP_TITLE=Inventory Tracking
VITE_APP_VERSION=1.0.0
```

## 6. 数据库迁移

### 6.1 初始化数据库
```sql
-- 创建数据库
CREATE DATABASE inventory;

-- 创建用户
CREATE USER inventory_user WITH PASSWORD 'your_password';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE inventory TO inventory_user;
```

### 6.2 运行迁移脚本
```bash
# 使用Flyway进行数据库迁移
cd backend
./gradlew flywayMigrate
```

## 7. 监控配置

### 7.1 应用监控
```bash
# 安装监控工具
sudo apt-get install htop iotop nethogs

# 配置日志轮转
sudo tee /etc/logrotate.d/inventory > /dev/null <<EOF
/opt/inventory/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 inventory inventory
}
EOF
```

### 7.2 健康检查
```bash
# 后端健康检查
curl http://localhost:8080/actuator/health

# 数据库连接检查
psql -h localhost -U inventory_user -d inventory -c "SELECT 1;"
```

## 8. 备份策略

### 8.1 数据库备份
```bash
# 创建备份脚本
#!/bin/bash
BACKUP_DIR="/opt/backups/inventory"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U inventory_user inventory > $BACKUP_DIR/inventory_$DATE.sql

# 清理旧备份（保留30天）
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

### 8.2 文件备份
```bash
# 备份上传文件
rsync -av /opt/inventory/uploads/ /opt/backups/uploads/
```

## 9. 安全配置

### 9.1 防火墙配置
```bash
# 开放必要端口
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw enable
```

### 9.2 SSL证书配置
```bash
# 使用Let's Encrypt
sudo apt-get install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

## 10. 故障排查

### 10.1 常见问题
- **数据库连接失败**：检查数据库服务状态和连接参数
- **应用启动失败**：检查Java版本和端口占用
- **文件上传失败**：检查文件权限和磁盘空间
- **前端无法访问**：检查Nginx配置和静态文件路径

### 10.2 日志查看
```bash
# 应用日志
sudo journalctl -u inventory-backend -f

# Nginx日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# 数据库日志
sudo tail -f /var/log/postgresql/postgresql-16-main.log
```
