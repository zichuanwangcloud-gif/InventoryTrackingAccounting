#!/bin/bash
# Pre-Commit Checker Hook
# 在编辑文件时自动检查代码质量（语法、类型、格式等）

set -e

# 跳过条件
if [[ -n "$SKIP_PRE_COMMIT_CHECK" ]]; then
    exit 0
fi

# 读取工具信息
tool_info=$(cat)

# 提取文件路径
file_path=$(echo "$tool_info" | jq -r '.tool_input.file_path // empty')
tool_name=$(echo "$tool_info" | jq -r '.tool_name // empty')

# 只检查 Edit/Write/MultiEdit
if [[ ! "$tool_name" =~ ^(Edit|Write|MultiEdit)$ ]] || [[ -z "$file_path" ]]; then
    exit 0
fi

# 跳过非代码文件
if [[ "$file_path" =~ \.(md|markdown|json|yaml|yml|txt|log)$ ]]; then
    exit 0
fi

project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"
full_path="$project_dir/$file_path"

# 检查文件是否存在
if [[ ! -f "$full_path" ]]; then
    exit 0
fi

# 收集检查结果
issues=()
warnings=()
checks_passed=0
total_checks=0

# 1. 语法检查
check_syntax() {
    local file="$1"
    total_checks=$((total_checks + 1))
    
    case "$file" in
        *.ts|*.tsx)
            if command -v tsc >/dev/null 2>&1; then
                local tsconfig_dir=$(dirname "$file")
                # 查找最近的 tsconfig.json
                while [[ "$tsconfig_dir" != "/" ]] && [[ "$tsconfig_dir" != "$project_dir" ]]; do
                    if [[ -f "$tsconfig_dir/tsconfig.json" ]]; then
                        if ! tsc --project "$tsconfig_dir/tsconfig.json" --noEmit "$file" 2>/dev/null; then
                            issues+=("TypeScript syntax/type errors found")
                            return 1
                        fi
                        checks_passed=$((checks_passed + 1))
                        return 0
                    fi
                    tsconfig_dir=$(dirname "$tsconfig_dir")
                done
                # 如果没有 tsconfig，只做基本检查
                if tsc --noEmit "$file" 2>&1 | grep -q "error"; then
                    issues+=("TypeScript syntax errors found")
                    return 1
                fi
            fi
            checks_passed=$((checks_passed + 1))
            ;;
        *.js|*.jsx)
            if command -v node >/dev/null 2>&1; then
                if ! node --check "$file" >/dev/null 2>&1; then
                    issues+=("JavaScript syntax errors found")
                    return 1
                fi
            fi
            checks_passed=$((checks_passed + 1))
            ;;
        *.py)
            if command -v python3 >/dev/null 2>&1; then
                if ! python3 -m py_compile "$file" 2>/dev/null; then
                    issues+=("Python syntax errors found")
                    return 1
                fi
                # 检查类型（如果有 mypy）
                if command -v mypy >/dev/null 2>&1; then
                    local mypy_result=$(mypy "$file" 2>&1 | grep -E "error|Error" | head -3 || true)
                    if [[ -n "$mypy_result" ]]; then
                        warnings+=("Type checking issues (mypy)")
                    fi
                fi
            fi
            checks_passed=$((checks_passed + 1))
            ;;
        *.sh)
            if ! bash -n "$file" 2>/dev/null; then
                issues+=("Bash syntax errors found")
                return 1
            fi
            checks_passed=$((checks_passed + 1))
            ;;
    esac
    return 0
}

# 1.5. 格式检查
check_format() {
    local file="$1"
    
    case "$file" in
        *.ts|*.tsx|*.js|*.jsx)
            # 检查是否有 prettier 配置
            local dir=$(dirname "$file")
            if [[ -f "$dir/.prettierrc" ]] || [[ -f "$dir/.prettierrc.json" ]] || \
               [[ -f "$project_dir/.prettierrc" ]] || [[ -f "$project_dir/.prettierrc.json" ]]; then
                if command -v prettier >/dev/null 2>&1; then
                    if ! prettier --check "$file" >/dev/null 2>&1; then
                        warnings+=("Code formatting issues (prettier)")
                    fi
                fi
            fi
            ;;
        *.py)
            # 检查是否有 black 配置
            if command -v black >/dev/null 2>&1; then
                if ! black --check --quiet "$file" 2>/dev/null; then
                    warnings+=("Code formatting issues (black)")
                fi
            fi
            ;;
    esac
}

# 2. 检查 TODO/FIXME
check_todos() {
    local file="$1"
    local todos
    
    todos=$(grep -n "TODO\|FIXME\|XXX\|HACK" "$file" 2>/dev/null | head -5 || true)
    
    if [[ -n "$todos" ]]; then
        local count=$(echo "$todos" | wc -l | tr -d ' ')
        warnings+=("Found $count TODO/FIXME comment(s)")
    fi
}

# 3. 检查长行
check_long_lines() {
    local file="$1"
    local long_lines
    
    long_lines=$(awk 'length > 120 {print NR": "length" chars"}' "$file" 2>/dev/null | head -3 || true)
    
    if [[ -n "$long_lines" ]]; then
        warnings+=("Long lines (>120 chars) found")
    fi
}

# 4. 检查未使用的导入（简单检查）
check_unused_imports() {
    local file="$1"
    
    if [[ "$file" =~ \.(ts|tsx|js|jsx)$ ]]; then
        # 简单检查：导入但未使用的变量（需要更复杂的 AST 分析）
        warnings+=("Consider checking for unused imports")
    fi
}

# 执行检查
check_syntax "$full_path"
check_format "$full_path"
check_todos "$full_path"
check_long_lines "$full_path"
check_unused_imports "$full_path"

# 如果没有问题，静默退出
if [[ ${#issues[@]} -eq 0 ]] && [[ ${#warnings[@]} -eq 0 ]]; then
    exit 0
fi

# 输出结果
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 PRE-COMMIT CHECK"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📁 File: $file_path"
echo ""

# 显示问题
if [[ ${#issues[@]} -gt 0 ]]; then
    echo "🔴 Issues Found:"
    for issue in "${issues[@]}"; do
        echo "   ❌ $issue"
    done
    echo ""
fi

# 显示警告
if [[ ${#warnings[@]} -gt 0 ]]; then
    echo "⚠️  Warnings:"
    for warning in "${warnings[@]}"; do
        echo "   ⚠️  $warning"
    done
    echo ""
fi

# 显示统计
echo "📊 Checks: $checks_passed/$total_checks passed"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💡 TIP: Set SKIP_PRE_COMMIT_CHECK=1 to disable"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

exit 0

