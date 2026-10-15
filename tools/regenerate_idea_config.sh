#!/bin/bash
# 快速重新生成 IDEA 配置文件（.iml 和 .idea 目录）
# 跳过 Maven 构建和依赖下载，只重新生成 IDE 配置

set -e

script_dir="$(realpath "$(dirname "$0")")"
# 从 dbeaver/tools 到 dbeaver-ce 需要上两级
generator_dir="$(realpath "$script_dir/../../idea-rcp-launch-config-generator")"
repositories_root_dir="$(realpath "$generator_dir/..")"
target_repo="$(realpath "$script_dir/..")"

echo "=== Regenerate IDEA configuration (skip Maven build & dependency download) ==="
echo "Generator dir: $generator_dir"
echo "Projects root: $repositories_root_dir"
echo "Target repo: $target_repo"

# 直接使用 Maven exec:java 运行已构建的程序，跳过 package 阶段
cd "$generator_dir"
mvn exec:java \
    -q \
    -Dexec.mainClass="org.jkiss.tools.rcplaunchconfig.EntryPoint" \
    -Dexec.args="-eclipse.version \${eclipse-version} -updateWorkspace -config $target_repo/osgi-app.properties -projectsFolder $repositories_root_dir -eclipse $repositories_root_dir/dbeaver-workspace/dependencies -output $repositories_root_dir/dbeaver-workspace/products/"

echo ""
echo "=== IDEA configuration regenerated successfully! ==="
