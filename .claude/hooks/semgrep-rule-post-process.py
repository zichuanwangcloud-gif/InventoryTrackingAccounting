#!/usr/bin/env python3
"""
Semgrep Rule Engineer 后处理钩子

功能：
1. 检测 agent 输出是否包含结构化规则内容
2. 自动调用解析脚本创建文件
3. 自动执行测试命令
4. 返回处理结果给主进程

触发条件：Task Tool 返回包含 SEMGREP_RULE_OUTPUT_START 标记
"""

import json
import os
import sys
import subprocess
from pathlib import Path

# 项目根目录
PROJECT_ROOT = Path("/opt/Vul-AI")
PARSER_SCRIPT = PROJECT_ROOT / "scripts" / "parse_semgrep_agent_output.py"

# 输出标记
START_MARKER = "===== SEMGREP_RULE_OUTPUT_START ====="
END_MARKER = "===== SEMGREP_RULE_OUTPUT_END ====="


def detect_semgrep_output(text: str) -> bool:
    """检测是否包含 semgrep-rule-engineer 的结构化输出"""
    return START_MARKER in text and END_MARKER in text


def process_agent_output(agent_output: str, auto_test: bool = True) -> dict:
    """
    处理 agent 输出

    Args:
        agent_output: agent 返回的完整文本
        auto_test: 是否自动执行测试

    Returns:
        处理结果字典
    """
    result = {
        "success": False,
        "files_created": [],
        "test_result": None,
        "errors": []
    }

    # 1. 检测输出格式
    if not detect_semgrep_output(agent_output):
        result["errors"].append("未检测到有效的 SEMGREP_RULE_OUTPUT 格式")
        return result

    # 2. 保存 agent 输出到临时文件
    temp_file = PROJECT_ROOT / "workspace" / "temp_agent_output.txt"
    temp_file.parent.mkdir(parents=True, exist_ok=True)
    temp_file.write_text(agent_output, encoding='utf-8')

    # 3. 调用解析脚本
    try:
        env = os.environ.copy()
        if auto_test:
            env["SEMGREP_AUTO_TEST"] = "true"

        parse_result = subprocess.run(
            [sys.executable, str(PARSER_SCRIPT), "--input", str(temp_file), "--verbose"],
            capture_output=True,
            text=True,
            cwd=str(PROJECT_ROOT),
            env=env,
            timeout=600
        )

        if parse_result.returncode == 0:
            result["success"] = True
            # 解析输出获取创建的文件列表
            for line in parse_result.stdout.split('\n'):
                if line.startswith("✅ 创建文件:"):
                    file_path = line.replace("✅ 创建文件:", "").strip()
                    result["files_created"].append(file_path)

            # 检查测试结果
            if "测试命令执行成功" in parse_result.stdout:
                result["test_result"] = "passed"
            elif "测试命令执行失败" in parse_result.stdout:
                result["test_result"] = "failed"

        else:
            result["errors"].append(f"解析脚本执行失败: {parse_result.stderr}")

    except subprocess.TimeoutExpired:
        result["errors"].append("解析脚本执行超时")
    except Exception as e:
        result["errors"].append(f"执行异常: {str(e)}")

    # 4. 清理临时文件
    if temp_file.exists():
        temp_file.unlink()

    return result


def format_result_for_display(result: dict) -> str:
    """格式化结果用于显示"""
    lines = []

    if result["success"]:
        lines.append("## ✅ Semgrep 规则处理成功\n")

        if result["files_created"]:
            lines.append("### 创建的文件：")
            for f in result["files_created"]:
                lines.append(f"- `{f}`")
            lines.append("")

        if result["test_result"]:
            if result["test_result"] == "passed":
                lines.append("### 测试结果：✅ 通过")
            else:
                lines.append("### 测试结果：❌ 失败")
    else:
        lines.append("## ❌ Semgrep 规则处理失败\n")
        if result["errors"]:
            lines.append("### 错误：")
            for err in result["errors"]:
                lines.append(f"- {err}")

    return "\n".join(lines)


if __name__ == "__main__":
    # 从标准输入读取 agent 输出（用于测试）
    if len(sys.argv) > 1 and sys.argv[1] == "--test":
        test_input = sys.stdin.read()
        result = process_agent_output(test_input, auto_test=True)
        print(format_result_for_display(result))
    else:
        print("用法: echo 'agent输出' | python3 semgrep-rule-post-process.py --test")
