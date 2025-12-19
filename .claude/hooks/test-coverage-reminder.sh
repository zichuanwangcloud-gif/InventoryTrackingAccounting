#!/bin/bash
# Test Coverage Reminder Hook
# 提醒为新添加的函数/类添加测试

set -e

# 跳过条件
if [[ -n "$SKIP_TEST_REMINDER" ]]; then
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

# 跳过测试文件本身
if [[ "$file_path" =~ \.(test|spec)\.(ts|tsx|js|jsx|py|java)$ ]] || \
   [[ "$file_path" =~ /test[s]?/ ]] || \
   [[ "$file_path" =~ /spec[s]?/ ]]; then
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

# 检测新添加的函数/类
new_functions=()
new_classes=()

# 检测函数/类（根据文件类型）
detect_new_code() {
    local file="$1"
    local ext="${file##*.}"
    
    case "$ext" in
        ts|tsx|js|jsx)
            # TypeScript/JavaScript: 检测 export function/class
            while IFS= read -r line; do
                if [[ "$line" =~ ^[[:space:]]*(export[[:space:]]+)?(async[[:space:]]+)?function[[:space:]]+([a-zA-Z_$][a-zA-Z0-9_$]*) ]]; then
                    new_functions+=("${BASH_REMATCH[3]}")
                elif [[ "$line" =~ ^[[:space:]]*(export[[:space:]]+)?class[[:space:]]+([a-zA-Z_$][a-zA-Z0-9_$]*) ]]; then
                    new_classes+=("${BASH_REMATCH[2]}")
                elif [[ "$line" =~ ^[[:space:]]*export[[:space:]]+(const|let|var)[[:space:]]+([a-zA-Z_$][a-zA-Z0-9_$]*)[[:space:]]*=[[:space:]]*(async[[:space:]]+)?\( ]]; then
                    new_functions+=("${BASH_REMATCH[2]}")
                fi
            done < "$file"
            ;;
        py)
            # Python: 检测 def 和 class
            while IFS= read -r line; do
                if [[ "$line" =~ ^[[:space:]]*def[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*) ]]; then
                    # 跳过私有方法（以 _ 开头）
                    if [[ ! "${BASH_REMATCH[1]}" =~ ^_ ]]; then
                        new_functions+=("${BASH_REMATCH[1]}")
                    fi
                elif [[ "$line" =~ ^[[:space:]]*class[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*) ]]; then
                    new_classes+=("${BASH_REMATCH[1]}")
                fi
            done < "$file"
            ;;
        java)
            # Java: 检测 public class 和 public method
            while IFS= read -r line; do
                if [[ "$line" =~ ^[[:space:]]*public[[:space:]]+class[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*) ]]; then
                    new_classes+=("${BASH_REMATCH[1]}")
                elif [[ "$line" =~ ^[[:space:]]*public[[:space:]]+[a-zA-Z_][a-zA-Z0-9_]*[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*)\( ]]; then
                    new_functions+=("${BASH_REMATCH[1]}")
                fi
            done < "$file"
            ;;
    esac
}

# 检查测试文件是否存在
check_test_file() {
    local file="$1"
    local base_name="${file%.*}"
    local ext="${file##*.}"
    local dir=$(dirname "$file")
    local filename=$(basename "$base_name")
    
    # 根据语言和项目结构查找测试文件
    case "$ext" in
        ts|tsx)
            # TypeScript: 查找 *.test.ts, *.spec.ts, __tests__/*.ts
            if [[ -f "$base_name.test.$ext" ]] || \
               [[ -f "$base_name.spec.$ext" ]] || \
               [[ -f "$dir/__tests__/$filename.$ext" ]] || \
               [[ -f "$dir/__tests__/$filename.test.$ext" ]]; then
                return 0
            fi
            ;;
        js|jsx)
            # JavaScript: 同 TypeScript
            if [[ -f "$base_name.test.$ext" ]] || \
               [[ -f "$base_name.spec.$ext" ]] || \
               [[ -f "$dir/__tests__/$filename.$ext" ]] || \
               [[ -f "$dir/__tests__/$filename.test.$ext" ]]; then
                return 0
            fi
            ;;
        py)
            # Python: 查找 test_*.py 或 *_test.py
            local test_file1="$dir/test_$filename.py"
            local test_file2="$dir/${filename}_test.py"
            if [[ -f "$test_file1" ]] || [[ -f "$test_file2" ]]; then
                return 0
            fi
            # 检查 tests/ 目录
            local tests_dir="$project_dir/tests"
            if [[ -d "$tests_dir" ]]; then
                local rel_path="${file#$project_dir/}"
                local test_path="$tests_dir/${rel_path%.py}/test_${filename}.py"
                if [[ -f "$test_path" ]]; then
                    return 0
                fi
            fi
            ;;
        java)
            # Java: 查找 *Test.java
            local test_file="$dir/${filename}Test.java"
            if [[ -f "$test_file" ]]; then
                return 0
            fi
            ;;
    esac
    return 1
}

# 执行检测
detect_new_code "$full_path"

# 如果没有新代码，退出
if [[ ${#new_functions[@]} -eq 0 ]] && [[ ${#new_classes[@]} -eq 0 ]]; then
    exit 0
fi

# 检查测试文件
test_file_exists=$(check_test_file "$full_path" && echo "yes" || echo "no")

# 如果没有测试文件，输出提醒
if [[ "$test_file_exists" == "no" ]]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🧪 TEST COVERAGE REMINDER"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📁 File: $file_path"
    echo ""
    
    if [[ ${#new_classes[@]} -gt 0 ]]; then
        echo "📝 New classes found: ${#new_classes[@]}"
        for cls in "${new_classes[@]}"; do
            echo "   → $cls"
        done
        echo ""
    fi
    
    if [[ ${#new_functions[@]} -gt 0 ]]; then
        echo "📝 New functions found: ${#new_functions[@]}"
        for func in "${new_functions[@]}"; do
            echo "   → $func"
        done
        echo ""
    fi
    
    echo "❓ Test file: Not found"
    echo ""
    echo "💡 Consider adding tests for:"
    if [[ ${#new_classes[@]} -gt 0 ]]; then
        for cls in "${new_classes[@]}"; do
            echo "   - $cls"
        done
    fi
    if [[ ${#new_functions[@]} -gt 0 ]]; then
        for func in "${new_functions[@]}"; do
            echo "   - $func()"
        done
    fi
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "💡 TIP: Set SKIP_TEST_REMINDER=1 to disable"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
fi

exit 0

