#!/bin/sh
set -eu
workshop_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
workshop_java_home=${FORGE_JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
workshop_jar="$workshop_root/.dev/dist/forge-workshop.jar"
if [ ! -f "$workshop_jar" ]; then
    printf '%s\n' 'Run python3 scripts/build-workshop.py first.' >&2
    exit 1
fi
python3 "$workshop_root/scripts/workshop-profile.py"
cd "$workshop_root/forge-gui"
exec "$workshop_java_home/bin/java" -Xmx4096m \
    -Dforge.assetsDir="$workshop_root/forge-gui/" \
    -Dfile.encoding=UTF-8 -Dio.netty.tryReflectionSetAccessible=true \
    -jar "$workshop_jar" "$@"
