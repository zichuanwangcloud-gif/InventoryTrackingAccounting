#!/usr/bin/env python3
"""
Semgrep Execution Guard Hook (PreToolUse)

拦截 Bash 工具调用，验证 Semgrep Docker 命令是否符合规范。

Hook Type: PreToolUse
Enforcement: BLOCK (exit code 2)
Trigger: Bash 工具 + 命令包含 "docker run" + "semgrep"
"""

import sys
import json
import os
import re
import subprocess
from pathlib import Path
from typing import Dict, Optional, Tuple

# 脚本路径
# Hook 在 .claude/hooks/ ，scripts 在 .claude/skills/semgrep-execution/scripts/
CLAUDE_DIR = Path(__file__).parent.parent  # /opt/Vul-AI/.claude
SCRIPT_DIR = CLAUDE_DIR / "skills/semgrep-execution/scripts"
VALIDATE_SCRIPT = SCRIPT_DIR / "validate_command.py"

# 跳过条件
SKIP_ENV_VAR = "SKIP_SEMGREP_VALIDATION"


def load_tool_info() -> Optional[Dict]:
    """从 stdin 读取工具调用信息"""
    try:
        tool_info = json.load(sys.stdin)
        return tool_info
    except Exception as e:
        print(f"Error reading tool info: {e}", file=sys.stderr)
        return None


def should_skip() -> bool:
    """检查是否应该跳过验证"""
    # 环境变量跳过
    if os.environ.get(SKIP_ENV_VAR):
        return True
    return False


def is_semgrep_command(command: str) -> bool:
    """检测命令是否是 Semgrep Docker 命令"""
    # 必须同时包含 "docker run" 和 "semgrep"
    has_docker_run = bool(re.search(r'docker\s+run', command, re.IGNORECASE))
    has_semgrep = bool(re.search(r'semgrep', command, re.IGNORECASE))

    return has_docker_run and has_semgrep


def validate_command(command: str, config_path: Optional[str] = None) -> Tuple[bool, str]:
    """
    调用验证脚本验证命令

    Returns:
        (是否通过, 错误信息)
    """
    if not VALIDATE_SCRIPT.exists():
        return True, ""  # 脚本不存在，允许通过

    # 构建验证命令
    validate_cmd = [
        "python3",
        str(VALIDATE_SCRIPT),
        "--command", command,
        "--strict"
    ]

    if config_path and os.path.exists(config_path):
        validate_cmd.extend(["--config-path", config_path])

    try:
        # 执行验证
        result = subprocess.run(
            validate_cmd,
            capture_output=True,
            text=True,
            timeout=10
        )

        if result.returncode == 0:
            return True, ""
        else:
            return False, result.stderr

    except subprocess.TimeoutExpired:
        return False, "❌ 验证超时（>10秒）"
    except Exception as e:
        return False, f"❌ 验证过程出错: {e}"


def find_config_path() -> Optional[str]:
    """查找 config.json 路径"""
    # 尝试从命令中推断 workspace 路径
    cwd = os.getcwd()

    # 查找 workspace 目录
    workspace_candidates = [
        Path(cwd) / "workspace",
        Path(cwd).parent / "workspace",
        Path("/opt/Vul-AI/workspace")
    ]

    for workspace in workspace_candidates:
        if workspace.exists():
            # 查找最近的 config.json
            for config_file in workspace.rglob("config.json"):
                if config_file.exists():
                    return str(config_file)

    return None


def format_block_message(error_message: str) -> str:
    """格式化阻止消息"""
    lines = [
        "",
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        "🚫 SEMGREP EXECUTION BLOCKED",
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        "",
        "你的 Semgrep Docker 命令不符合执行规范，已被阻止。",
        "",
        error_message,
        "",
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        "💡 推荐使用标准化脚本生成命令",
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        "",
        "使用以下脚本自动生成符合规范的命令：",
        "",
        "  # 步骤 1: 解析路径",
        "  RULES_PATH=$(python scripts/resolve_paths.py --type rules)",
        "  PROJECT_PATH=$(python scripts/resolve_paths.py --type project)",
        "",
        "  # 步骤 2: 生成标准命令",
        "  DOCKER_CMD=$(python scripts/generate_command.py \\",
        "    --project-path \"$PROJECT_PATH\" \\",
        "    --rules-path \"$RULES_PATH\" \\",
        "    --severity WARNING)",
        "",
        "  # 步骤 3: 执行（自动通过验证）",
        "  eval \"$DOCKER_CMD\"",
        "",
        "详细文档：.claude/skills/semgrep-execution/SKILL.md",
        "",
        f"如需临时跳过验证：export {SKIP_ENV_VAR}=1",
        "",
    ]

    return "\n".join(lines)


def main():
    """主函数"""
    # 检查跳过条件
    if should_skip():
        sys.exit(0)

    # 读取工具信息
    tool_info = load_tool_info()
    if not tool_info:
        sys.exit(0)

    # 只处理 Bash 工具
    tool_name = tool_info.get("tool_name", "")
    if tool_name != "Bash":
        sys.exit(0)

    # 提取命令
    tool_input = tool_info.get("tool_input", {})
    command = tool_input.get("command", "")

    if not command:
        sys.exit(0)

    # 检查是否是 Semgrep 命令
    if not is_semgrep_command(command):
        sys.exit(0)

    # 查找配置文件
    config_path = find_config_path()

    # 验证命令
    passed, error_message = validate_command(command, config_path)

    if passed:
        # 验证通过，静默允许
        sys.exit(0)
    else:
        # 验证失败，阻止执行
        block_message = format_block_message(error_message)
        print(block_message, file=sys.stderr)
        sys.exit(2)  # Exit code 2 = BLOCK


if __name__ == "__main__":
    main()
