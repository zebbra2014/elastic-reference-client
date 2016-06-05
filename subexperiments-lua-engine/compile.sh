#!/bin/sh
CP=lib/*
SP=.

/bin/rm -f nxt.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -sourcepath ${SP} -classpath ${CP} -d classes/  nxt/*/*.java org/*/*/*.java org/*/*/*/*.java || exit 1

echo "test compiled successfully"
