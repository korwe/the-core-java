#!/bin/bash

MAIN_CLASS=com.korwe.thecore.service.CoreServices

if [[ -z $JAVA_HOME ]]; then
    JAVACMD=`which java`
else 
    JAVACMD="$JAVA_HOME/bin/java"
fi

CP="."
for jar in lib/*.jar ; do
    CP=$CP:$jar
done

$JAVACMD -cp $CP $MAIN_CLASS > console.log 2>&1 &

echo $! > coreservices.pid



