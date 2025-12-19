#!/bin/bash
# Documentation Reminder Hook
# 提醒更新文档：API 端点、函数/类文档、README 等

set -e

# 跳过条件
if [[ -n "$SKIP_DOC_REMINDER" ]]; then
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

# 收集文档提醒
doc_reminders=()
warnings=()

# 1. 检测新的 API 端点
check_new_api_endpoints() {
    local file="$1"
    local ext="${file##*.}"
    local new_endpoints=()
    
    case "$ext" in
        ts|tsx|js|jsx)
            # Express/Node.js: 检测 router.get/post/put/delete
            while IFS= read -r line; do
                if [[ "$line" =~ (router|app)\.(get|post|put|delete|patch|put)\([[:space:]]*['\"]([^'\"]+) ]]; then
                    new_endpoints+=("${BASH_REMATCH[2]} ${BASH_REMATCH[3]}")
                fi
            done < "$file"
            ;;
        py)
            # Flask/FastAPI: 检测 @app.route 或 @router
            while IFS= read -r line; do
                if [[ "$line" =~ @(app|router)\.(get|post|put|delete|patch)\([[:space:]]*['\"]([^'\"]+) ]]; then
                    new_endpoints+=("${BASH_REMATCH[2]} ${BASH_REMATCH[3]}")
                elif [[ "$line" =~ @app\.route\([[:space:]]*['\"]([^'\"]+) ]]; then
                    new_endpoints+=("${BASH_REMATCH[1]}")
                fi
            done < "$file"
            ;;
        java)
            # Spring Boot: 检测 @GetMapping, @PostMapping 等
            while IFS= read -r line; do
                if [[ "$line" =~ @(Get|Post|Put|Delete|Patch)Mapping\([[:space:]]*value[[:space:]]*=[[:space:]]*['\"]([^'\"]+) ]]; then
                    new_endpoints+=("${BASH_REMATCH[1]} ${BASH_REMATCH[2]}")
                fi
            done < "$file"
            ;;
    esac
    
    if [[ ${#new_endpoints[@]} -gt 0 ]]; then
        doc_reminders+=("New API endpoints: ${#new_endpoints[@]}")
        for endpoint in "${new_endpoints[@]}"; do
            warnings+=("  → $endpoint")
        done
    fi
}

# 2. 检测缺少文档字符串的函数/类
check_missing_docs() {
    local file="$1"
    local ext="${file##*.}"
    local missing_docs=()
    
    case "$ext" in
        ts|tsx|js|jsx)
            # TypeScript/JavaScript: 检测 export function/class 是否有 JSDoc
            local in_export=0
            local function_name=""
            while IFS= read -r line; do
                if [[ "$line" =~ ^[[:space:]]*export[[:space:]]+(function|class|const|let|var)[[:space:]]+([a-zA-Z_$][a-zA-Z0-9_$]*) ]]; then
                    function_name="${BASH_REMATCH[2]}"
                    # 检查前几行是否有 JSDoc
                    local has_doc=0
                    local line_num=$(grep -n "$line" "$file" | cut -d: -f1)
                    if [[ $line_num -gt 1 ]]; then
                        local prev_line=$(sed -n "$((line_num - 1))p" "$file")
                        if [[ "$prev_line" =~ ^[[:space:]]*/\*\* ]] || [[ "$prev_line" =~ ^[[:space:]]*//.*@ ]]; then
                            has_doc=1
                        fi
                    fi
                    if [[ $has_doc -eq 0 ]]; then
                        missing_docs+=("$function_name")
                    fi
                fi
            done < "$file"
            ;;
        py)
            # Python: 检测 def/class 是否有 docstring
            while IFS= read -r line; do
                if [[ "$line" =~ ^[[:space:]]*(def|class)[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*) ]]; then
                    local name="${BASH_REMATCH[2]}"
                    local line_num=$(grep -n "$line" "$file" | cut -d: -f1)
                    # 检查下一行是否有 docstring
                    local next_line=$(sed -n "$((line_num + 1))p" "$file")
                    if [[ ! "$next_line" =~ \"\"\" ]] && [[ ! "$next_line" =~ ''' ]]; then
                        missing_docs+=("$name")
                    fi
                fi
            done < "$file"
            ;;
        java)
            # Java: 检测 public class/method 是否有 JavaDoc
            while IFS= read -r line; do
                if [[ "$line" =~ ^[[:space:]]*public[[:space:]]+(class|interface)[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*) ]] || \
                   [[ "$line" =~ ^[[:space:]]*public[[:space:]]+[a-zA-Z_][a-zA-Z0-9_<>[[:space:]]*[[:space:]]+([a-zA-Z_][a-zA-Z0-9_]*)\( ]]; then
                    local name="${BASH_REMATCH[2]}${BASH_REMATCH[1]}"
                    local line_num=$(grep -n "$line" "$file" | cut -d: -f1)
                    local has_doc=0
                    if [[ $line_num -gt 1 ]]; then
                        local prev_line=$(sed -n "$((line_num - 1))p" "$file")
                        if [[ "$prev_line" =~ ^[[:space:]]*/\*\* ]]; then
                            has_doc=1
                        fi
                    fi
                    if [[ $has_doc -eq 0 ]]; then
                        missing_docs+=("$name")
                    fi
                fi
            done < "$file"
            ;;
    esac
    
    if [[ ${#missing_docs[@]} -gt 0 ]] && [[ ${#missing_docs[@]} -le 5 ]]; then
        warnings+=("Functions/classes missing documentation: ${#missing_docs[@]}")
        for doc in "${missing_docs[@]}"; do
            warnings+=("  → $doc")
        done
    fi
}

# 3. 检测 README 是否需要更新
check_readme_update() {
    local file="$1"
    local dir=$(dirname "$file")
    local readme_file="$dir/README.md"
    
    # 如果文件在根目录或主要目录，检查 README
    if [[ "$dir" == "$project_dir" ]] || \
       [[ "$dir" == "$project_dir/src" ]] || \
       [[ "$dir" == "$project_dir/backend" ]] || \
       [[ "$dir" == "$project_dir/frontend" ]]; then
        if [[ ! -f "$readme_file" ]]; then
            warnings+=("README.md not found in $dir")
        fi
    fi
}

# 4. 检测 API 文档文件
check_api_docs() {
    local file="$1"
    local dir=$(dirname "$file")
    
    # 如果检测到 API 端点，检查是否有 API 文档
    if [[ "$file" =~ (route|controller|api|endpoint) ]]; then
        local api_doc="$dir/API.md"
        local api_doc2="$project_dir/docs/API.md"
        
        if [[ ! -f "$api_doc" ]] && [[ ! -f "$api_doc2" ]]; then
            warnings+=("API documentation file not found")
        fi
    fi
}

# 执行检测
check_new_api_endpoints "$full_path"
check_missing_docs "$full_path"
check_readme_update "$full_path"
check_api_docs "$full_path"

# 如果没有问题，静默退出
if [[ ${#doc_reminders[@]} -eq 0 ]] && [[ ${#warnings[@]} -eq 0 ]]; then
    exit 0
fi

# 输出结果
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📚 DOCUMENTATION REMINDER"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📁 File: $file_path"
echo ""

# 显示提醒
if [[ ${#doc_reminders[@]} -gt 0 ]]; then
    echo "📝 Documentation Needed:"
    for reminder in "${doc_reminders[@]}"; do
        echo "   ⚠️  $reminder"
    done
    echo ""
fi

# 显示警告
if [[ ${#warnings[@]} -gt 0 ]]; then
    echo "⚠️  Suggestions:"
    for warning in "${warnings[@]}"; do
        echo "   $warning"
    done
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💡 TIP: Set SKIP_DOC_REMINDER=1 to disable"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

exit 0

