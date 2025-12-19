#!/bin/bash
# Security Checker Hook
# 检查常见安全问题：硬编码密钥、SQL注入、XSS等

set -e

# 跳过条件
if [[ -n "$SKIP_SECURITY_CHECK" ]]; then
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
if [[ "$file_path" =~ \.(md|markdown|json|yaml|yml|txt|log|test|spec)$ ]]; then
    exit 0
fi

project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"
full_path="$project_dir/$file_path"

# 检查文件是否存在
if [[ ! -f "$full_path" ]]; then
    exit 0
fi

# 收集安全问题
security_issues=()
warnings=()

# 1. 检查硬编码密钥/密码
check_hardcoded_secrets() {
    local file="$1"
    local secrets
    
    # 检查常见的密钥模式
    secrets=$(grep -n -iE "(password|secret|api[_-]?key|token|auth[_-]?key)\s*[=:]\s*['\"][^'\"]{8,}" "$file" 2>/dev/null | head -5 || true)
    
    if [[ -n "$secrets" ]]; then
        local count=$(echo "$secrets" | wc -l | tr -d ' ')
        security_issues+=("Potential hardcoded secret found ($count location(s))")
    fi
}

# 2. 检查 SQL 注入风险
check_sql_injection() {
    local file="$1"
    
    if [[ "$file" =~ \.(ts|js|py|java)$ ]]; then
        # 检查字符串拼接的 SQL 查询
        local sql_concatenation=$(grep -n -E "SELECT|INSERT|UPDATE|DELETE.*\+.*\$" "$file" 2>/dev/null | head -3 || true)
        
        if [[ -n "$sql_concatenation" ]]; then
            warnings+=("Potential SQL injection risk (string concatenation)")
        fi
    fi
}

# 3. 检查 XSS 风险（前端）
check_xss() {
    local file="$1"
    
    if [[ "$file" =~ \.(tsx|jsx|ts|js)$ ]]; then
        # 检查危险的 innerHTML 使用
        local innerhtml=$(grep -n "innerHTML\s*=" "$file" 2>/dev/null | head -3 || true)
        
        if [[ -n "$innerhtml" ]]; then
            warnings+=("Potential XSS risk (innerHTML usage)")
        fi
        
        # 检查危险的 eval
        local eval_usage=$(grep -n "eval\s*(" "$file" 2>/dev/null | head -3 || true)
        
        if [[ -n "$eval_usage" ]]; then
            security_issues+=("Dangerous eval() usage found")
        fi
    fi
}

# 4. 检查不安全的随机数生成
check_weak_random() {
    local file="$1"
    
    if [[ "$file" =~ \.(ts|js|py)$ ]]; then
        # JavaScript: Math.random() 用于安全目的
        if [[ "$file" =~ \.(ts|js)$ ]]; then
            local math_random=$(grep -n "Math\.random()" "$file" 2>/dev/null | head -3 || true)
            if [[ -n "$math_random" ]]; then
                warnings+=("Math.random() is not cryptographically secure")
            fi
        fi
    fi
}

# 5. 检查敏感信息泄露
check_sensitive_info() {
    local file="$1"
    
    # 检查调试信息中的敏感数据
    local debug_sensitive=$(grep -n -iE "(console\.(log|debug|warn).*password|console\.(log|debug|warn).*token|console\.(log|debug|warn).*secret)" "$file" 2>/dev/null | head -3 || true)
    
    if [[ -n "$debug_sensitive" ]]; then
        security_issues+=("Sensitive data in console logs")
    fi
}

# 执行检查
check_hardcoded_secrets "$full_path"
check_sql_injection "$full_path"
check_xss "$full_path"
check_weak_random "$full_path"
check_sensitive_info "$full_path"

# 如果没有问题，静默退出
if [[ ${#security_issues[@]} -eq 0 ]] && [[ ${#warnings[@]} -eq 0 ]]; then
    exit 0
fi

# 输出结果
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔒 SECURITY CHECK"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📁 File: $file_path"
echo ""

# 显示安全问题
if [[ ${#security_issues[@]} -gt 0 ]]; then
    echo "🔴 Security Issues:"
    for issue in "${security_issues[@]}"; do
        echo "   ❌ $issue"
    done
    echo ""
fi

# 显示警告
if [[ ${#warnings[@]} -gt 0 ]]; then
    echo "⚠️  Security Warnings:"
    for warning in "${warnings[@]}"; do
        echo "   ⚠️  $warning"
    done
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💡 TIP: Set SKIP_SECURITY_CHECK=1 to disable"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

exit 0

