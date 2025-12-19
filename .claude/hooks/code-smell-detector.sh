#!/bin/bash
# Code Smell Detector Hook
# 检测代码异味：长函数、重复代码、魔法数字、深嵌套等

set -e

# 跳过条件
if [[ -n "$SKIP_CODE_SMELL_CHECK" ]]; then
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
if [[ "$file_path" =~ \.(md|markdown|json|yaml|yml|txt|log|config)$ ]]; then
    exit 0
fi

project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"
full_path="$project_dir/$file_path"

# 检查文件是否存在
if [[ ! -f "$full_path" ]]; then
    exit 0
fi

# 收集代码异味
smells=()
warnings=()

# 1. 检测长函数（> 50 行）
check_long_functions() {
    local file="$1"
    local ext="${file##*.}"
    
    case "$ext" in
        ts|tsx|js|jsx|py|java)
            # 简单检测：计算函数开始到结束的行数
            local in_function=0
            local function_start=0
            local function_name=""
            local line_num=0
            local brace_count=0
            
            while IFS= read -r line; do
                line_num=$((line_num + 1))
                
                # 检测函数开始
                if [[ "$line" =~ (function[[:space:]]+([a-zA-Z_$][a-zA-Z0-9_$]*)|([a-zA-Z_$][a-zA-Z0-9_$]*)[[:space:]]*[=:][[:space:]]*(async[[:space:]]+)?\(|def[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*)|public[[:space:]]+[a-zA-Z_][a-zA-Z0-9_<>[[:space:]]*[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*)\([^)]*\{) ]]; then
                    if [[ $in_function -eq 0 ]]; then
                        in_function=1
                        function_start=$line_num
                        brace_count=0
                        # 提取函数名
                        if [[ "$line" =~ function[[:space:]]+([a-zA-Z_$][a-zA-Z0-9_$]*) ]]; then
                            function_name="${BASH_REMATCH[1]}"
                        elif [[ "$line" =~ def[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*) ]]; then
                            function_name="${BASH_REMATCH[1]}"
                        elif [[ "$line" =~ public[[:space:]]+[a-zA-Z_][a-zA-Z0-9_<>[[:space:]]*[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*)\( ]]; then
                            function_name="${BASH_REMATCH[1]}"
                        fi
                    fi
                fi
                
                # 计算大括号/缩进
                if [[ $in_function -eq 1 ]]; then
                    local open_braces=$(echo "$line" | grep -o '{' | wc -l | tr -d ' ')
                    local close_braces=$(echo "$line" | grep -o '}' | wc -l | tr -d ' ')
                    brace_count=$((brace_count + open_braces - close_braces))
                    
                    # Python: 检测缩进减少
                    if [[ "$ext" == "py" ]]; then
                        local indent=$(echo "$line" | sed 's/[^ ].*//' | wc -c)
                        if [[ $indent -le 1 ]] && [[ $line_num -gt $function_start ]]; then
                            local func_length=$((line_num - function_start))
                            if [[ $func_length -gt 50 ]]; then
                                smells+=("Long function: $function_name() ($func_length lines, line $function_start)")
                            fi
                            in_function=0
                        fi
                    elif [[ $brace_count -le 0 ]] && [[ $line_num -gt $function_start ]]; then
                        local func_length=$((line_num - function_start))
                        if [[ $func_length -gt 50 ]]; then
                            smells+=("Long function: $function_name() ($func_length lines, line $function_start)")
                        fi
                        in_function=0
                    fi
                fi
            done < "$file"
            ;;
    esac
}

# 2. 检测深嵌套（> 4 层）
check_deep_nesting() {
    local file="$1"
    local max_nesting=0
    local nesting_line=0
    local current_nesting=0
    local line_num=0
    
    while IFS= read -r line; do
        line_num=$((line_num + 1))
        
        # 检测控制结构（if, for, while, switch, try）
        if [[ "$line" =~ (if|for|while|switch|try|catch)[[:space:]]*\( ]]; then
            current_nesting=$((current_nesting + 1))
            if [[ $current_nesting -gt $max_nesting ]]; then
                max_nesting=$current_nesting
                nesting_line=$line_num
            fi
        fi
        
        # 检测结束（简化：大括号或关键字）
        if [[ "$line" =~ ^[[:space:]]*\}[[:space:]]*$ ]] || \
           [[ "$line" =~ ^[[:space:]]*(else|elif|catch|finally)[[:space:]] ]]; then
            if [[ $current_nesting -gt 0 ]]; then
                current_nesting=$((current_nesting - 1))
            fi
        fi
    done < "$file"
    
    if [[ $max_nesting -gt 4 ]]; then
        warnings+=("Deep nesting: $max_nesting levels (line $nesting_line)")
    fi
}

# 3. 检测魔法数字
check_magic_numbers() {
    local file="$1"
    local magic_numbers
    
    # 查找可能的魔法数字（不在变量赋值或常量定义中）
    magic_numbers=$(grep -n -E "[^a-zA-Z_$]([0-9]{2,}|[0-9]+\.[0-9]+)[^a-zA-Z0-9_]" "$file" 2>/dev/null | \
        grep -vE "(const|let|var|=\s*[0-9]|version|port|id|index)" | head -5 || true)
    
    if [[ -n "$magic_numbers" ]]; then
        local count=$(echo "$magic_numbers" | wc -l | tr -d ' ')
        warnings+=("Potential magic numbers found ($count locations)")
    fi
}

# 4. 检测重复代码（简单检测：重复的代码块）
check_duplicate_code() {
    local file="$1"
    # 这是一个简化的检测，实际需要更复杂的 AST 分析
    # 这里只检测明显的重复模式
    local duplicate_patterns
    
    duplicate_patterns=$(awk 'length > 20 {print}' "$file" 2>/dev/null | \
        sort | uniq -d | head -3 || true)
    
    if [[ -n "$duplicate_patterns" ]]; then
        warnings+=("Potential duplicate code patterns detected")
    fi
}

# 5. 检测过长文件（> 500 行）
check_long_file() {
    local file="$1"
    local line_count=$(wc -l < "$file" 2>/dev/null | tr -d ' ')
    
    if [[ $line_count -gt 500 ]]; then
        warnings+=("Long file: $line_count lines (consider splitting)")
    fi
}

# 6. 检测未使用的变量（简单检测）
check_unused_variables() {
    local file="$1"
    local ext="${file##*.}"
    
    if [[ "$ext" == "py" ]]; then
        # Python: 检测可能的未使用变量（以 _ 开头的变量通常表示未使用）
        local underscore_vars=$(grep -n "[^_]_[a-zA-Z][a-zA-Z0-9_]*[[:space:]]*=" "$file" 2>/dev/null | head -3 || true)
        if [[ -n "$underscore_vars" ]]; then
            warnings+=("Potential unused variables (consider removing or using)")
        fi
    fi
}

# 执行检测
check_long_functions "$full_path"
check_deep_nesting "$full_path"
check_magic_numbers "$full_path"
check_duplicate_code "$full_path"
check_long_file "$full_path"
check_unused_variables "$full_path"

# 如果没有问题，静默退出
if [[ ${#smells[@]} -eq 0 ]] && [[ ${#warnings[@]} -eq 0 ]]; then
    exit 0
fi

# 输出结果
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "👃 CODE SMELL DETECTION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📁 File: $file_path"
echo ""

# 显示代码异味
if [[ ${#smells[@]} -gt 0 ]]; then
    echo "🔴 Code Smells:"
    for smell in "${smells[@]}"; do
        echo "   ❌ $smell"
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

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💡 TIP: Set SKIP_CODE_SMELL_CHECK=1 to disable"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

exit 0

