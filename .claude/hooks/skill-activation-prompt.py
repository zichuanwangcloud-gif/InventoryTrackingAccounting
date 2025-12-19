#!/usr/bin/env python3
"""
Skill Activation Prompt Hook (Python Version)
根据用户输入的 prompt 匹配 skill-rules.json 中的关键词，提示激活对应技能
"""

import sys
import json
import os
import re
from collections import defaultdict

# 读取 stdin 输入
try:
    input_data = json.load(sys.stdin)
    prompt = input_data.get('prompt', '').lower().strip()
except (json.JSONDecodeError, KeyError):
    prompt = ''

if not prompt:
    sys.exit(0)

# 获取 skill-rules.json 路径
project_dir = os.environ.get('CLAUDE_PROJECT_DIR', os.getcwd())
rules_file = os.path.join(project_dir, '.claude', 'skills', 'skill-rules.json')

if not os.path.isfile(rules_file):
    sys.exit(0)

# 存储匹配的技能 {skill_name: priority}
matched_skills = {}

# 从 skill-rules.json 动态加载技能配置
try:
    with open(rules_file, 'r', encoding='utf-8') as f:
        skill_rules = json.load(f)
        skills_config = skill_rules.get('skills', {})
except (json.JSONDecodeError, IOError):
    # 如果读取失败，静默退出
    sys.exit(0)


def should_skip_skill(skill_name, skill_config, prompt):
    """检查是否应该跳过这个技能的激活"""
    # 检查 excludePatterns
    prompt_triggers = skill_config.get('promptTriggers', {})
    exclude_patterns = prompt_triggers.get('excludePatterns', [])
    for pattern in exclude_patterns:
        if re.search(pattern, prompt, re.IGNORECASE):
            return True

    # 检查 skipConditions.commands
    skip_conditions = skill_config.get('skipConditions', {})
    skip_commands = skip_conditions.get('commands', [])
    for command in skip_commands:
        # 检查 prompt 是否以这个命令开头
        if prompt.startswith(command.lower()):
            return True

    return False


def match_keywords(skill_name, keywords, priority):
    """匹配关键词：检查所有词是否都在 prompt 中"""
    if skill_name in matched_skills:
        return True

    for keyword in keywords:
        # 将关键词按空格分割，检查所有词是否都在 prompt 中
        words = keyword.split()
        if all(word in prompt for word in words):
            matched_skills[skill_name] = priority
            return True
    return False


def match_intent(skill_name, patterns, priority):
    """意图模式匹配（正则表达式）"""
    if skill_name in matched_skills:
        return True

    for pattern in patterns:
        if re.search(pattern, prompt, re.IGNORECASE):
            matched_skills[skill_name] = priority
            return True
    return False


# 动态处理所有技能
for skill_name, skill_config in skills_config.items():
    # 检查是否应该跳过这个技能
    if should_skip_skill(skill_name, skill_config, prompt):
        continue

    priority = skill_config.get('priority', 'medium')
    prompt_triggers = skill_config.get('promptTriggers', {})

    # 获取关键词
    keywords = prompt_triggers.get('keywords', [])
    if keywords:
        match_keywords(skill_name, keywords, priority)

    # 获取意图模式
    intent_patterns = prompt_triggers.get('intentPatterns', [])
    if intent_patterns and skill_name not in matched_skills:
        match_intent(skill_name, intent_patterns, priority)

# 如果没有匹配，直接退出
if not matched_skills:
    sys.exit(0)

# 按优先级分组
skills_by_priority = defaultdict(list)
for skill_name, priority in matched_skills.items():
    skills_by_priority[priority].append(skill_name)

# 生成输出
print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
print("🎯 SKILL ACTIVATION CHECK")
print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
print("")

# 按优先级输出（critical > high > medium > low）
priority_order = ['critical', 'high', 'medium', 'low']
priority_labels = {
    'critical': '⚠️ CRITICAL SKILLS (REQUIRED):',
    'high': '📚 RECOMMENDED SKILLS:',
    'medium': '💡 SUGGESTED SKILLS:',
    'low': '📌 OPTIONAL SKILLS:'
}

for priority in priority_order:
    if priority in skills_by_priority:
        print(priority_labels[priority])
        for skill in skills_by_priority[priority]:
            print(f"  → {skill}")
        print("")

print("ACTION: Use Skill tool BEFORE responding")
print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

sys.exit(0)

