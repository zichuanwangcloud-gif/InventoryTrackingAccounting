#!/bin/bash
set -e

# Skill Activation Prompt Hook (Pure Shell Version)
# 根据用户输入的 prompt 匹配 skill-rules.json 中的关键词，提示激活对应技能

# 读取 stdin 输入
input=$(cat)

# 提取 prompt 并转小写
prompt=$(echo "$input" | jq -r '.prompt // empty' | tr '[:upper:]' '[:lower:]')

if [[ -z "$prompt" ]]; then
    exit 0
fi

# 获取 skill-rules.json 路径
project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"
rules_file="$project_dir/.claude/skills/skill-rules.json"

if [[ ! -f "$rules_file" ]]; then
    exit 0
fi

# 存储匹配的技能（使用普通数组，兼容 bash 3.2）
# 格式：matched_skills 存储 "skill_name:priority" 格式的字符串
matched_skills=()

# 定义技能关键词（从 skill-rules.json 提取的核心关键词）
# skill-developer
skill_developer_keywords="skill system|create skill|add skill|skill triggers|skill rules|hook system|skill development|skill-rules.json"

# backend-dev-guidelines
backend_keywords="backend|microservice|controller|service|repository|route|routing|express|endpoint|middleware|validation|zod|prisma|database access|basecontroller|dependency injection|unifiedconfig"

# frontend-dev-guidelines
frontend_keywords="component|react component|ui|interface|page|modal|dialog|form|mui|material-ui|grid|styling|frontend|react"

# error-tracking
error_tracking_keywords="error handling|exception|sentry|error tracking|captureexception|monitoring|performance tracking"

# 检查技能是否已匹配（兼容 bash 3.2）
skill_matched() {
    local skill_name="$1"
    local i
    for i in "${matched_skills[@]}"; do
        if [[ "$i" == "$skill_name:"* ]]; then
            return 0
        fi
    done
    return 1
}

# 添加匹配的技能
add_matched_skill() {
    local skill_name="$1"
    local priority="$2"
    matched_skills+=("$skill_name:$priority")
}

# 匹配函数（兼容 bash 3.2）
match_keywords() {
    local skill_name="$1"
    local keywords="$2"
    local priority="$3"
    local kw
    local rest="$keywords"

    # 如果已经匹配过，直接返回
    skill_matched "$skill_name" && return 0 || true

    # 手动分割关键词字符串（兼容 bash 3.2，避免使用数组）
    while [[ -n "$rest" ]]; do
        # 提取第一个关键词（到 | 或字符串末尾）
        if [[ "$rest" == *"|"* ]]; then
            kw="${rest%%|*}"
            rest="${rest#*|}"
        else
            kw="$rest"
            rest=""
        fi
        
        # 检查关键词是否在 prompt 中（兼容 bash 3.2）
        # 使用 expr 来检查子字符串（兼容所有 bash 版本）
        if [[ -n "$kw" ]]; then
            # 将关键词按空格分割，检查所有词是否都在 prompt 中
            local kw_words="$kw"
            local all_found=1
            while [[ -n "$kw_words" ]]; do
                local word
                if [[ "$kw_words" == *" "* ]]; then
                    word="${kw_words%% *}"
                    kw_words="${kw_words#* }"
                else
                    word="$kw_words"
                    kw_words=""
                fi
                if [[ -n "$word" ]]; then
                    # 使用 expr 检查子字符串（兼容 bash 3.2）
                    if expr "$prompt" : ".*$word" > /dev/null 2>&1; then
                        : # 词找到了，继续
                    else
                        all_found=0
                        break
                    fi
                fi
            done
            if [[ $all_found -eq 1 ]]; then
                add_matched_skill "$skill_name" "$priority"
                return 0
            fi
        fi
    done
    
    return 1
}

# 意图模式匹配（简化版正则）
match_intent() {
    local skill_name="$1"
    local priority="$2"
    shift 2
    local patterns=("$@")

    # 如果已经匹配过，直接返回
    skill_matched "$skill_name" && return 0 || true

    for pattern in "${patterns[@]}"; do
        if echo "$prompt" | grep -qiE "$pattern"; then
            add_matched_skill "$skill_name" "$priority"
            return 0
        fi
    done
    return 1
}

# 执行关键词匹配
match_keywords "skill-developer" "$skill_developer_keywords" "high"
match_keywords "backend-dev-guidelines" "$backend_keywords" "high"
match_keywords "frontend-dev-guidelines" "$frontend_keywords" "high"
match_keywords "error-tracking" "$error_tracking_keywords" "high"

# 意图模式匹配（补充）
if ! skill_matched "skill-developer" 2>/dev/null; then
    match_intent "skill-developer" "high" \
        "(how do|how does|explain).*skill" \
        "(create|add|modify|build).*skill" \
        "skill.*(work|trigger|activate|system)"
fi

if ! skill_matched "backend-dev-guidelines" 2>/dev/null; then
    match_intent "backend-dev-guidelines" "high" \
        "(create|add|implement|build).*(route|endpoint|api|controller|service|repository)" \
        "(fix|handle|debug).*(error|exception|backend)" \
        "(add|implement).*(middleware|validation)" \
        "(how to|best practice).*(backend|route|controller|service)"
fi

if ! skill_matched "frontend-dev-guidelines" 2>/dev/null; then
    match_intent "frontend-dev-guidelines" "high" \
        "(create|add|make|build|update|modify|edit).*(component|ui|page|modal|dialog|form)" \
        "(how to|best practice).*(component|react|mui)" \
        "(style|design|layout).*(component|ui)"
fi

if ! skill_matched "error-tracking" 2>/dev/null; then
    match_intent "error-tracking" "high" \
        "(add|create|implement|setup).*(error handling|sentry|error tracking)" \
        "(fix|handle|catch).*(error|exception)" \
        "(how to|best practice).*(error|sentry|monitoring)"
fi

# 如果没有匹配，直接退出
if [[ ${#matched_skills[@]} -eq 0 ]]; then
    exit 0
fi

# 生成输出
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎯 SKILL ACTIVATION CHECK"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 按优先级分组输出
critical_skills=()
high_skills=()
medium_skills=()
low_skills=()

for skill_entry in "${matched_skills[@]}"; do
    # 解析 "skill_name:priority" 格式
    skill_name="${skill_entry%%:*}"
    priority="${skill_entry#*:}"
    case "$priority" in
        critical) critical_skills+=("$skill_name") ;;
        high) high_skills+=("$skill_name") ;;
        medium) medium_skills+=("$skill_name") ;;
        low) low_skills+=("$skill_name") ;;
    esac
done

if [[ ${#critical_skills[@]} -gt 0 ]]; then
    echo "⚠️ CRITICAL SKILLS (REQUIRED):"
    for s in "${critical_skills[@]}"; do
        echo "  → $s"
    done
    echo ""
fi

if [[ ${#high_skills[@]} -gt 0 ]]; then
    echo "📚 RECOMMENDED SKILLS:"
    for s in "${high_skills[@]}"; do
        echo "  → $s"
    done
    echo ""
fi

if [[ ${#medium_skills[@]} -gt 0 ]]; then
    echo "💡 SUGGESTED SKILLS:"
    for s in "${medium_skills[@]}"; do
        echo "  → $s"
    done
    echo ""
fi

if [[ ${#low_skills[@]} -gt 0 ]]; then
    echo "📌 OPTIONAL SKILLS:"
    for s in "${low_skills[@]}"; do
        echo "  → $s"
    done
    echo ""
fi

echo "ACTION: Use Skill tool BEFORE responding"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

exit 0
