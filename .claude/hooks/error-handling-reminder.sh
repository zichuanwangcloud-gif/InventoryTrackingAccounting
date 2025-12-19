#!/bin/bash
set -e

# Error Handling Reminder Hook (Pure Shell Version)
# 在会话结束前检查编辑的文件，提醒错误处理最佳实践
# 支持：Python, Java, TypeScript/JavaScript, Go, Bash

# 跳过条件
if [[ -n "$SKIP_ERROR_REMINDER" ]]; then
    exit 0
fi

# 读取 stdin 输入
input=$(cat)

# 提取 session_id
session_id=$(echo "$input" | jq -r '.session_id // empty')
project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"

# 检查缓存目录
cache_dir="$project_dir/.claude/error-cache/${session_id:-default}"
tracking_file="$cache_dir/edited-files.log"

if [[ ! -f "$tracking_file" ]]; then
    exit 0
fi

# 读取编辑的文件
edited_files=()
while IFS=: read -r timestamp filepath repo; do
    [[ -n "$filepath" ]] && edited_files+=("$filepath")
done < "$tracking_file"

if [[ ${#edited_files[@]} -eq 0 ]]; then
    exit 0
fi

# 文件分类
backend_files=()
frontend_files=()
database_files=()
python_files=()
java_files=()
go_files=()
bash_files=()

# 检测函数
should_check() {
    local file="$1"
    # 跳过测试文件、配置文件、类型定义
    [[ "$file" =~ \.(test|spec)\.(ts|tsx|py|java)$ ]] && return 1
    [[ "$file" =~ \.(config|d)\.(ts|tsx)$ ]] && return 1
    [[ "$file" =~ types/ ]] && return 1
    [[ "$file" =~ \.styles\.ts$ ]] && return 1
    [[ "$file" =~ \.(md|markdown|json|yaml|yml|toml)$ ]] && return 1
    return 0
}

get_category() {
    local file="$1"

    # Python
    [[ "$file" =~ \.py$ ]] && echo "python" && return

    # Java
    [[ "$file" =~ \.java$ ]] && echo "java" && return

    # Go
    [[ "$file" =~ \.go$ ]] && echo "go" && return

    # Bash
    [[ "$file" =~ \.(sh|bash)$ ]] && echo "bash" && return

    # Frontend (React/TSX)
    if [[ "$file" =~ \.(tsx|jsx)$ ]] || \
       [[ "$file" =~ frontend/ ]] || \
       [[ "$file" =~ client/ ]] || \
       [[ "$file" =~ /components/ ]] || \
       [[ "$file" =~ /features/ ]]; then
        echo "frontend"
        return
    fi

    # Backend (Node.js/TS)
    if [[ "$file" =~ /controllers/ ]] || \
       [[ "$file" =~ /services/ ]] || \
       [[ "$file" =~ /routes/ ]] || \
       [[ "$file" =~ /api/ ]] || \
       [[ "$file" =~ /server/ ]]; then
        echo "backend"
        return
    fi

    # Database
    if [[ "$file" =~ /database/ ]] || \
       [[ "$file" =~ /prisma/ ]] || \
       [[ "$file" =~ /migrations/ ]]; then
        echo "database"
        return
    fi

    # TypeScript/JavaScript 默认归为后端
    [[ "$file" =~ \.(ts|js)$ ]] && echo "backend" && return

    echo "other"
}

# 分析文件内容
analyze_file() {
    local file="$1"
    local result=""

    [[ ! -f "$file" ]] && return

    local content
    content=$(cat "$file" 2>/dev/null || echo "")

    # 通用检测
    [[ "$content" =~ try[[:space:]]*\{ ]] && result+="try-catch,"
    [[ "$content" =~ async[[:space:]]+ ]] && result+="async,"

    # Python 特定
    if [[ "$file" =~ \.py$ ]]; then
        [[ "$content" =~ except[[:space:]]*: ]] && result+="bare-except,"
        [[ "$content" =~ logging\. ]] && result+="logging,"
        [[ "$content" =~ raise[[:space:]] ]] && result+="raise,"
    fi

    # Java 特定
    if [[ "$file" =~ \.java$ ]]; then
        [[ "$content" =~ throws[[:space:]] ]] && result+="throws,"
        [[ "$content" =~ @ExceptionHandler ]] && result+="exception-handler,"
    fi

    # Node.js/TS 特定
    if [[ "$file" =~ \.(ts|js|tsx|jsx)$ ]]; then
        [[ "$content" =~ prisma\. ]] && result+="prisma,"
        [[ "$content" =~ Sentry\. ]] && result+="sentry,"
        [[ "$content" =~ fetch\( ]] && result+="fetch,"
        [[ "$content" =~ axios\. ]] && result+="axios,"
        [[ "$content" =~ Controller ]] && result+="controller,"
    fi

    # Go 特定
    if [[ "$file" =~ \.go$ ]]; then
        [[ "$content" =~ if[[:space:]]+err[[:space:]]*!=[[:space:]]*nil ]] && result+="error-check,"
        [[ "$content" =~ panic\( ]] && result+="panic,"
    fi

    echo "${result%,}"
}

# 分类文件
for file in "${edited_files[@]}"; do
    should_check "$file" || continue

    category=$(get_category "$file")
    case "$category" in
        python) python_files+=("$file") ;;
        java) java_files+=("$file") ;;
        go) go_files+=("$file") ;;
        bash) bash_files+=("$file") ;;
        frontend) frontend_files+=("$file") ;;
        backend) backend_files+=("$file") ;;
        database) database_files+=("$file") ;;
    esac
done

# 检查是否有需要提醒的文件
total_files=$((${#backend_files[@]} + ${#frontend_files[@]} + ${#database_files[@]} + ${#python_files[@]} + ${#java_files[@]} + ${#go_files[@]} + ${#bash_files[@]}))

if [[ $total_files -eq 0 ]]; then
    exit 0
fi

# 生成提醒
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 ERROR HANDLING SELF-CHECK"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Python 提醒
if [[ ${#python_files[@]} -gt 0 ]]; then
    echo "🐍 Python Changes (${#python_files[@]} files)"
    echo "   ❓ 是否避免了裸 except: 语句？"
    echo "   ❓ 是否使用 logging 记录错误？"
    echo "   ❓ 自定义异常是否有清晰的错误信息？"
    echo ""
fi

# Java 提醒
if [[ ${#java_files[@]} -gt 0 ]]; then
    echo "☕ Java Changes (${#java_files[@]} files)"
    echo "   ❓ 是否正确声明了 throws？"
    echo "   ❓ 是否使用了 @ExceptionHandler？"
    echo "   ❓ 是否记录了异常堆栈？"
    echo ""
fi

# Go 提醒
if [[ ${#go_files[@]} -gt 0 ]]; then
    echo "🔷 Go Changes (${#go_files[@]} files)"
    echo "   ❓ 是否检查了所有 error 返回值？"
    echo "   ❓ 是否使用 errors.Wrap 添加上下文？"
    echo "   ❓ 是否避免了不必要的 panic？"
    echo ""
fi

# Backend (Node.js) 提醒
if [[ ${#backend_files[@]} -gt 0 ]]; then
    echo "⚙️  Backend/Node.js Changes (${#backend_files[@]} files)"
    echo "   ❓ catch 块中是否添加了 Sentry.captureException()？"
    echo "   ❓ Prisma 操作是否有错误处理？"
    echo "   ❓ Controller 是否使用 BaseController.handleError()？"
    echo ""
fi

# Frontend 提醒
if [[ ${#frontend_files[@]} -gt 0 ]]; then
    echo "💻 Frontend Changes (${#frontend_files[@]} files)"
    echo "   ❓ API 调用是否显示了用户友好的错误消息？"
    echo "   ❓ 是否使用了 Error Boundary？"
    echo "   ❓ 加载状态和错误状态是否都处理了？"
    echo ""
fi

# Database 提醒
if [[ ${#database_files[@]} -gt 0 ]]; then
    echo "🗄️  Database Changes (${#database_files[@]} files)"
    echo "   ❓ 字段名是否与 schema 一致？"
    echo "   ❓ 迁移是否已测试？"
    echo ""
fi

# Bash 提醒
if [[ ${#bash_files[@]} -gt 0 ]]; then
    echo "🐚 Bash Changes (${#bash_files[@]} files)"
    echo "   ❓ 是否使用了 set -e 或错误检查？"
    echo "   ❓ 关键命令是否有 || 错误处理？"
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💡 TIP: 设置 SKIP_ERROR_REMINDER=1 可禁用此提醒"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

exit 0
