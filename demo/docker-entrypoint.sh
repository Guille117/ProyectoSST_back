#!/bin/sh
set -eu
mkdir -p /app/uploads
chown -R app:app /app/uploads
exec gosu app java $JAVA_OPTS -jar /app/app.jar
