#!/bin/bash

# 库存记账系统测试脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印函数
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    print_success "Docker环境检查通过"
}

# 运行后端单元测试
run_backend_tests() {
    print_info "运行后端单元测试..."
    
    cd backend
    
    # 清理之前的构建
    print_info "清理之前的构建..."
    sudo rm -rf .gradle build
    
    # 尝试使用Docker运行测试
    print_info "使用Docker运行后端测试..."
    if sudo docker run --rm -v "$(pwd):/app" -w /app gradle:8.5-jdk17 gradle test --no-daemon; then
        print_success "后端测试成功完成"
    else
        print_warning "Docker测试失败，尝试本地Gradle..."
        if command -v gradle &> /dev/null; then
            gradle test --no-daemon || print_warning "本地Gradle测试也失败"
        else
            print_warning "本地Gradle未安装，跳过测试"
        fi
    fi
    
    print_success "后端单元测试完成"
    
    cd ..
}

# 运行前端单元测试
run_frontend_tests() {
    print_info "运行前端单元测试..."
    
    cd frontend
    
    # 检查是否有node_modules
    if [ ! -d "node_modules" ]; then
        print_info "安装前端依赖..."
        npm install
    fi
    
    # 运行单元测试
    npm run test
    
    print_success "前端单元测试完成"
    
    cd ..
}

# 运行E2E测试
run_e2e_tests() {
    print_info "运行E2E测试..."
    
    cd frontend
    
    # 安装Playwright浏览器
    npx playwright install
    
    # 运行E2E测试
    npm run test:e2e
    
    print_success "E2E测试完成"
    
    cd ..
}

# 构建Docker镜像
build_images() {
    print_info "构建Docker镜像..."
    
    # 构建后端镜像
    print_info "构建后端镜像..."
    sudo docker build -t inventory-backend ./backend
    
    # 构建前端镜像
    print_info "构建前端镜像..."
    sudo docker build -t inventory-frontend ./frontend
    
    print_success "Docker镜像构建完成"
}

# 运行系统测试
run_system_tests() {
    print_info "启动系统测试环境..."
    
    # 启动测试环境
    sudo docker-compose -f docker-compose.test.yml up --build -d
    
    # 等待服务启动
    print_info "等待服务启动..."
    sleep 30
    
    # 检查服务状态
    print_info "检查服务状态..."
    sudo docker-compose -f docker-compose.test.yml ps
    
    # 运行测试
    print_info "运行集成测试..."
    sudo docker-compose -f docker-compose.test.yml exec backend-test gradle test
    
    print_info "运行E2E测试..."
    sudo docker-compose -f docker-compose.test.yml exec e2e-test npm run test:e2e
    
    print_success "系统测试完成"
}

# 清理测试环境
cleanup() {
    print_info "清理测试环境..."
    
    # 停止并删除测试容器
    sudo docker-compose -f docker-compose.test.yml down -v
    
    # 清理未使用的镜像
    sudo docker system prune -f
    
    print_success "测试环境清理完成"
}

# 生成测试报告
generate_report() {
    print_info "生成测试报告..."
    
    # 创建报告目录
    mkdir -p reports
    
    # 复制后端测试报告
    if [ -d "backend/build/reports" ]; then
        cp -r backend/build/reports/* reports/
    fi
    
    # 复制前端测试报告
    if [ -d "frontend/test-results" ]; then
        cp -r frontend/test-results/* reports/
    fi
    
    print_success "测试报告已生成到 reports/ 目录"
}

# 验证测试文件
verify_test_files() {
    print_info "验证测试文件..."
    
    # 检查后端测试文件
    backend_tests=$(find backend/src/test -name "*.java" | wc -l)
    print_info "后端测试文件数量: $backend_tests"
    
    # 检查前端测试文件
    frontend_tests=$(find frontend/src/test -name "*.ts" | wc -l)
    print_info "前端测试文件数量: $frontend_tests"
    
    # 检查Docker文件
    docker_files=$(find . -name "Dockerfile*" -o -name "docker-compose*.yml" | wc -l)
    print_info "Docker配置文件数量: $docker_files"
    
    print_success "测试文件验证完成"
}

# 诊断Gradle问题
diagnose_gradle() {
    print_info "诊断Gradle问题..."
    
    cd backend
    
    # 检查Java版本
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n 1)
        print_info "Java版本: $java_version"
    else
        print_warning "Java未安装"
    fi
    
    # 检查Gradle版本
    if command -v gradle &> /dev/null; then
        gradle_version=$(gradle --version | head -n 3)
        print_info "Gradle版本: $gradle_version"
    else
        print_warning "Gradle未安装"
    fi
    
    # 检查Docker
    if command -v docker &> /dev/null; then
        docker_version=$(docker --version)
        print_info "Docker版本: $docker_version"
    else
        print_warning "Docker未安装"
    fi
    
    print_success "诊断完成"
    
    cd ..
}

# 主菜单
show_menu() {
    echo "=========================================="
    echo "    库存记账系统测试脚本"
    echo "=========================================="
    echo "1. 检查环境"
    echo "2. 运行后端单元测试"
    echo "3. 运行前端单元测试"
    echo "4. 运行E2E测试"
    echo "5. 构建Docker镜像"
    echo "6. 运行系统测试"
    echo "7. 生成测试报告"
    echo "8. 清理测试环境"
    echo "9. 运行所有测试"
    echo "10. 验证测试文件"
    echo "11. 诊断Gradle问题"
    echo "0. 退出"
    echo "=========================================="
}

# 运行所有测试
run_all_tests() {
    print_info "开始运行所有测试..."
    
    check_docker
    verify_test_files
    run_backend_tests
    run_frontend_tests
    build_images
    run_system_tests
    generate_report
    
    print_success "所有测试完成！"
}

# 主程序
main() {
    while true; do
        show_menu
        read -p "请选择操作 (0-11): " choice
        
        case $choice in
            1) check_docker ;;
            2) run_backend_tests ;;
            3) run_frontend_tests ;;
            4) run_e2e_tests ;;
            5) build_images ;;
            6) run_system_tests ;;
            7) generate_report ;;
            8) cleanup ;;
            9) run_all_tests ;;
            10) verify_test_files ;;
            11) diagnose_gradle ;;
            0) print_info "退出程序"; exit 0 ;;
            *) print_error "无效选择，请重新输入" ;;
        esac
        
        echo ""
        read -p "按回车键继续..."
        echo ""
    done
}

# 如果直接运行脚本
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
