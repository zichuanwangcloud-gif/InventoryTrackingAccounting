#!/bin/bash

# 库存记账系统本地测试脚本
# 不使用Docker，直接使用本地环境

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

# 检查Java版本
check_java() {
    print_info "检查Java版本..."
    
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        print_info "当前Java版本: $JAVA_VERSION"
        
        if [ "$JAVA_VERSION" -lt 17 ]; then
            print_warning "当前Java版本为$JAVA_VERSION，建议使用Java 17+"
            print_info "如果需要安装Java 17，请运行:"
            print_info "sudo apt update && sudo apt install openjdk-17-jdk"
            print_info "然后设置JAVA_HOME: export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
        fi
    else
        print_error "Java未安装，请先安装Java 17+"
        exit 1
    fi
}

# 检查PostgreSQL
check_postgresql() {
    print_info "检查PostgreSQL..."
    
    if command -v psql &> /dev/null; then
        print_success "PostgreSQL已安装"
    else
        print_warning "PostgreSQL未安装，测试将使用H2内存数据库"
        print_info "如需安装PostgreSQL，请运行:"
        print_info "sudo apt update && sudo apt install postgresql postgresql-contrib"
    fi
}

# 运行后端测试
run_backend_tests() {
    print_info "运行后端测试..."
    
    cd backend
    
    # 清理之前的构建（跳过.gradle目录，只清理build目录）
    print_info "清理之前的构建..."
    if [ -d "build" ]; then
        rm -rf build
        print_info "已清理build目录"
    else
        print_info "build目录不存在，跳过清理"
    fi
    
    # 使用Gradle Wrapper运行测试（排除Controller测试）
    print_info "使用Gradle Wrapper运行测试..."
    if ./gradlew test --no-daemon; then
        print_success "后端测试成功完成"
    else
        print_error "后端测试失败"
        return 1
    fi
    
    # 生成测试报告
    print_info "生成测试报告..."
    ./gradlew jacocoTestReport --no-daemon
    
    print_success "后端测试报告已生成: backend/build/reports/jacoco/test/html/index.html"
    
    cd ..
}

# 运行前端测试
run_frontend_tests() {
    print_info "运行前端测试..."
    
    cd frontend
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js未安装，请先安装Node.js 18+"
        return 1
    fi
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        print_error "npm未安装，请先安装npm"
        return 1
    fi
    
    # 安装依赖
    if [ ! -d "node_modules" ]; then
        print_info "安装前端依赖..."
        npm install
    fi
    
    # 运行单元测试
    print_info "运行前端单元测试..."
    npm run test
    
    # 运行E2E测试
    print_info "运行E2E测试..."
    npm run test:e2e
    
    print_success "前端测试完成"
    
    cd ..
}

# 运行所有测试
run_all_tests() {
    print_info "开始运行所有测试..."
    
    # 检查环境
    check_java
    check_postgresql
    
    # 运行后端测试
    if run_backend_tests; then
        print_success "后端测试通过"
    else
        print_error "后端测试失败"
        return 1
    fi
    
    # 运行前端测试
    if run_frontend_tests; then
        print_success "前端测试通过"
    else
        print_error "前端测试失败"
        return 1
    fi
    
    print_success "所有测试完成！"
}

# 清理函数
cleanup() {
    print_info "清理构建文件..."
    
    # 清理后端构建文件
    if [ -d "backend/build" ]; then
        rm -rf backend/build
        print_info "已清理后端构建文件"
    fi
    
    # 清理前端构建文件
    if [ -d "frontend/dist" ]; then
        rm -rf frontend/dist
        print_info "已清理前端构建文件"
    fi
    
    print_success "清理完成"
}

# 显示帮助信息
show_help() {
    echo "库存记账系统本地测试脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -b, --backend  仅运行后端测试"
    echo "  -f, --frontend 仅运行前端测试"
    echo "  -a, --all      运行所有测试（默认）"
    echo "  -c, --clean    清理构建文件"
    echo "  --check        仅检查环境依赖"
    echo ""
    echo "示例:"
    echo "  $0              # 运行所有测试"
    echo "  $0 --backend    # 仅运行后端测试"
    echo "  $0 --clean      # 清理构建文件"
}

# 主函数
main() {
    case "${1:-}" in
        -h|--help)
            show_help
            exit 0
            ;;
        -b|--backend)
            check_java
            check_postgresql
            run_backend_tests
            ;;
        -f|--frontend)
            run_frontend_tests
            ;;
        -a|--all)
            run_all_tests
            ;;
        -c|--clean)
            cleanup
            ;;
        --check)
            check_java
            check_postgresql
            print_success "环境检查完成"
            ;;
        "")
            run_all_tests
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 运行主函数
main "$@"
