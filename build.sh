#!/bin/bash

b="\033[1;32m"
n="\033[0;39m"

project="Schema"



[ "$1" != "desktop" ] && [ "$1" != "mobile" ] && echo -e "Use either$b desktop$n or$b mobile$n as an argument" && exit 1

echo -e "=>$b Building desktop jar...$n"



echo "Compiling the source code"

rm -r bin
mkdir bin

lib=$(find lib -type f -name *.jar  -print | paste -sd:)
src=$(find src -type f -name *.java -print | paste -s)

javac --release 16 --class-path $lib -d bin $src



echo "Archiving the class files and resources"

rm -r build
mkdir build

jar --create --file build/$project.jar -C bin .
jar --update --file build/$project.jar -C src/resources .
jar --update --file build/$project.jar mod.hjson



[ "$1" != "mobile" ] && exit 0

echo -e "=>$b Building mobile jar...$n"



pf=$ANDROID_HOME/platforms

lib=$(find lib -type f -name *.jar       -print | sed -e "s/^/--classpath /" | paste -s)
cls=$(find bin -type f -name *.class     -print |                              paste -s)
jar=$(find $pf -type f -name android.jar -print | sort --reverse             | head --lines=1)

echo "Found android.jar in $(dirname $jar)"

d8 $lib --lib $jar --output bin $cls

jar --update --file build/$project.jar -C bin classes.dex
