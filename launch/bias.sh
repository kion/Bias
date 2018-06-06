#!/bin/sh
DIR="$( dirname "$0" )"

LAUNCHER_UPDATE_FILE="$DIR/update_bias.jar"
if [ -f $LAUNCHER_UPDATE_FILE ]; then
   mv "$LAUNCHER_UPDATE_FILE" "$DIR/bias.jar"
   echo "Launcher update has been applied."
fi

JAVA=java

if test -n "$JAVA_HOME"; then
    JAVA="$JAVA_HOME/bin/java"
fi

exec "$JAVA" -cp "$DIR/bias.jar:$DIR/appcore.jar:$DIR/lib/*:$DIR/addons/*" bias.Bias
