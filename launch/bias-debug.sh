#!/bin/sh
DIR="$( dirname "$0" )"
JAVA=java
if test -n "$JAVA_HOME"; then
    JAVA="$JAVA_HOME/bin/java"
fi
exec "$JAVA" -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y -cp "$DIR/bias.jar:$DIR/appcore.jar:$DIR/lib/*:$DIR/addons/*" bias.Bias