#!/usr/bin/env sh
set -e

if ! command -v inotifywait >/dev/null 2>&1; then
    echo "Installing inotify-tools..."
    apt-get update -y >/dev/null
    apt-get install -y inotify-tools >/dev/null
fi

watch_compile() {
    while inotifywait -e modify,create,delete,move -r /app/src/main/java /app/src/main/resources >/dev/null 2>&1; do
        echo "Detected change. Recompiling..."
        mvn -DskipTests -q compile
        touch /app/.reloadtrigger
    done
}

echo "Initial compile..."
mvn -DskipTests -q compile
touch /app/.reloadtrigger

watch_compile &

exec mvn -DskipTests spring-boot:run
