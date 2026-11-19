#!/bin/sh
# Render (and similar hosts) tell the app which port to use through $PORT.
# The connector also gets a larger swallow size so a rejected photo upload
# still returns a friendly message instead of a dropped connection.
set -e
PORT="${PORT:-8080}"
sed -i -E "s#<Connector port=\"[0-9]+\" protocol=\"HTTP/1.1\"( maxSwallowSize=\"[0-9]+\")?#<Connector port=\"${PORT}\" protocol=\"HTTP/1.1\" maxSwallowSize=\"10485760\"#" /usr/local/tomcat/conf/server.xml
exec catalina.sh run
