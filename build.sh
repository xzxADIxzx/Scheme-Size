#!/bin/bash
# region constants

r="\033[1;31m"
g="\033[1;32m"
n="\033[0;39m"

function faulty
{
    echo -e "$r!> $1$n"
}

function region
{
    echo -e "$g=> $1$n"
}

# endregion
# region help

if echo $1 | grep -q "h" || [ $# -lt 1 ]
then
    echo "Builds the project"
    echo ""
    echo "Usage: build <operations>"
    echo "Operations:"
    echo "    -h display this text"
    echo "    -d compile for desktop devices"
    echo "    -m compile for mobile devices"
    exit 0
fi

# endregion
# region desktop

if echo $1 | grep -q "d"
then
    region "Compiling the source code..."

    rm -r bin
    mkdir bin

    lib=$(find lib -type f -name *.jar  -print | paste -sd:)
    src=$(find src -type f -name *.java -print | paste -s  )

    javac --release 25 --class-path $lib -d bin $src

    region "Archiving the class files and resources..."

    rm -r build
    mkdir build

    jar --create --file build/Schema.jar -C bin . -C assets .
fi

# endregion
# region mobile

if echo $1 | grep -q "m"
then
    region "Searching for android.jar..."

    pf=$ANDROID_HOME/platforms

    lib=$(find lib -type f -name *.jar       -print | sed -e "s/^/--classpath /" | paste -s )
    cls=$(find bin -type f -name *.class     -print | sort --reverse             | paste -s )
    jar=$(find $pf -type f -name android.jar -print | sort --reverse             | head -n 1)

    region "Found android.jar in $(dirname $jar), compiling..."

    d8 $lib --lib $jar --output bin $cls

    jar --update --file build/Schema.jar -C bin classes.dex
fi

# endregion
