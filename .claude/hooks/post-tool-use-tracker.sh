#!/bin/bash
set -e

# Post-tool-use hook that tracks edited files and their repos
# This runs after Edit, MultiEdit, or Write tools complete successfully


# Read tool information from stdin
tool_info=$(cat)


# Extract relevant data
tool_name=$(echo "$tool_info" | jq -r '.tool_name // empty')
file_path=$(echo "$tool_info" | jq -r '.tool_input.file_path // empty')
session_id=$(echo "$tool_info" | jq -r '.session_id // empty')


# Skip if not an edit tool or no file path
if [[ ! "$tool_name" =~ ^(Edit|MultiEdit|Write)$ ]] || [[ -z "$file_path" ]]; then
    exit 0  # Exit 0 for skip conditions
fi

# Skip markdown files
if [[ "$file_path" =~ \.(md|markdown)$ ]]; then
    exit 0  # Exit 0 for skip conditions
fi

# Create cache directory in project (renamed from tsc-cache to error-cache for multi-language support)
project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"
cache_dir="$project_dir/.claude/error-cache/${session_id:-default}"
mkdir -p "$cache_dir"

# Function to detect repo from file path
detect_repo() {
    local file="$1"
    local project_root="${CLAUDE_PROJECT_DIR:-$(pwd)}"

    # Remove project root from path
    local relative_path="${file#$project_root/}"

    # Extract first directory component
    local repo=$(echo "$relative_path" | cut -d'/' -f1)

    # Common project directory patterns
    case "$repo" in
        # Frontend variations
        frontend|client|web|app|ui)
            echo "$repo"
            ;;
        # Backend variations
        backend|server|api|src|services)
            echo "$repo"
            ;;
        # Database
        database|prisma|migrations)
            echo "$repo"
            ;;
        # Package/monorepo structure
        packages)
            # For monorepos, get the package name
            local package=$(echo "$relative_path" | cut -d'/' -f2)
            if [[ -n "$package" ]]; then
                echo "packages/$package"
            else
                echo "$repo"
            fi
            ;;
        # Examples directory
        examples)
            local example=$(echo "$relative_path" | cut -d'/' -f2)
            if [[ -n "$example" ]]; then
                echo "examples/$example"
            else
                echo "$repo"
            fi
            ;;
        *)
            # Check if it's a source file in root
            if [[ ! "$relative_path" =~ / ]]; then
                echo "root"
            else
                echo "unknown"
            fi
            ;;
    esac
}

# Function to get build command for repo
get_build_command() {
    local repo="$1"
    local project_root="${CLAUDE_PROJECT_DIR:-$(pwd)}"
    local repo_path="$project_root/$repo"

    # Check if package.json exists and has a build script
    if [[ -f "$repo_path/package.json" ]]; then
        if grep -q '"build"' "$repo_path/package.json" 2>/dev/null; then
            # Detect package manager (prefer pnpm, then npm, then yarn)
            if [[ -f "$repo_path/pnpm-lock.yaml" ]]; then
                echo "cd $repo_path && pnpm build"
            elif [[ -f "$repo_path/package-lock.json" ]]; then
                echo "cd $repo_path && npm run build"
            elif [[ -f "$repo_path/yarn.lock" ]]; then
                echo "cd $repo_path && yarn build"
            else
                echo "cd $repo_path && npm run build"
            fi
            return
        fi
    fi

    # Special case for database with Prisma
    if [[ "$repo" == "database" ]] || [[ "$repo" =~ prisma ]]; then
        if [[ -f "$repo_path/schema.prisma" ]] || [[ -f "$repo_path/prisma/schema.prisma" ]]; then
            echo "cd $repo_path && npx prisma generate"
            return
        fi
    fi

    # No build command found
    echo ""
}

# Function to get check command for repo (supports Python, Java, TypeScript, Bash)
get_check_command() {
    local repo="$1"
    local project_root="${CLAUDE_PROJECT_DIR:-$(pwd)}"
    local repo_path="$project_root/$repo"

    # Python projects
    if [[ -f "$repo_path/pyproject.toml" ]] || [[ -f "$repo_path/setup.py" ]] || [[ -f "$repo_path/requirements.txt" ]]; then
        # Check for mypy
        if command -v mypy >/dev/null 2>&1 && [[ -f "$repo_path/mypy.ini" ]] || [[ -f "$repo_path/pyproject.toml" ]] && grep -q "\[tool.mypy\]" "$repo_path/pyproject.toml" 2>/dev/null; then
            echo "cd $repo_path && python -m mypy ."
        # Check for pylint
        elif command -v pylint >/dev/null 2>&1 && [[ -f "$repo_path/.pylintrc" ]] || [[ -f "$repo_path/pylintrc" ]]; then
            echo "cd $repo_path && python -m pylint \$(find . -name '*.py' -not -path '*/\.*' -not -path '*/venv/*' -not -path '*/env/*' | head -10)"
        # Basic syntax check
        else
            echo "cd $repo_path && python -m py_compile \$(find . -name '*.py' -not -path '*/\.*' -not -path '*/venv/*' -not -path '*/env/*' | head -10) 2>&1 || true"
        fi
        return
    fi

    # Java projects (Maven)
    if [[ -f "$repo_path/pom.xml" ]]; then
        echo "cd $repo_path && mvn compile -q 2>&1 || mvn compile"
        return
    fi

    # Java projects (Gradle)
    if [[ -f "$repo_path/build.gradle" ]] || [[ -f "$repo_path/build.gradle.kts" ]]; then
        echo "cd $repo_path && ./gradlew compileJava --quiet 2>&1 || ./gradlew compileJava"
        return
    fi

    # TypeScript/JavaScript projects
    if [[ -f "$repo_path/tsconfig.json" ]]; then
        # Check for Vite/React-specific tsconfig
        if [[ -f "$repo_path/tsconfig.app.json" ]]; then
            echo "cd $repo_path && npx tsc --project tsconfig.app.json --noEmit"
        else
            echo "cd $repo_path && npx tsc --noEmit"
        fi
        return
    fi

    # Frontend projects (package.json with TypeScript)
    if [[ -f "$repo_path/package.json" ]] && grep -q "typescript" "$repo_path/package.json" 2>/dev/null; then
        if [[ -f "$repo_path/tsconfig.json" ]]; then
            echo "cd $repo_path && npx tsc --noEmit"
        else
            echo "cd $repo_path && npm run build 2>&1 || npm run lint 2>&1 || true"
        fi
        return
    fi

    # Bash scripts
    if [[ "$file_path" =~ \.(sh|bash)$ ]]; then
        echo "bash -n \"$file_path\" 2>&1 || shellcheck \"$file_path\" 2>&1 || true"
        return
    fi

    # No check command found
    echo ""
}

# Detect repo
repo=$(detect_repo "$file_path")

# Skip if unknown repo
if [[ "$repo" == "unknown" ]] || [[ -z "$repo" ]]; then
    exit 0  # Exit 0 for skip conditions
fi

# Log edited file
echo "$(date +%s):$file_path:$repo" >> "$cache_dir/edited-files.log"

# Update affected repos list
if ! grep -q "^$repo$" "$cache_dir/affected-repos.txt" 2>/dev/null; then
    echo "$repo" >> "$cache_dir/affected-repos.txt"
fi

# Store build and check commands
build_cmd=$(get_build_command "$repo")
check_cmd=$(get_check_command "$repo")

if [[ -n "$build_cmd" ]]; then
    echo "$repo:build:$build_cmd" >> "$cache_dir/build-commands.txt.tmp"
fi

if [[ -n "$check_cmd" ]]; then
    echo "$repo:check:$check_cmd" >> "$cache_dir/check-commands.txt.tmp"
fi

# Remove duplicates from commands
if [[ -f "$cache_dir/build-commands.txt.tmp" ]]; then
    sort -u "$cache_dir/build-commands.txt.tmp" > "$cache_dir/build-commands.txt"
    rm -f "$cache_dir/build-commands.txt.tmp"
fi

if [[ -f "$cache_dir/check-commands.txt.tmp" ]]; then
    sort -u "$cache_dir/check-commands.txt.tmp" > "$cache_dir/check-commands.txt"
    rm -f "$cache_dir/check-commands.txt.tmp"
fi

# Exit cleanly
exit 0