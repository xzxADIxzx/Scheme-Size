#!/bin/bash

b="\033[1;32m"
n="\033[0;39m"

jitpack="https://jitpack.io"



[ "$1" != "dependencies-only" ] && [ "$1" != "sources-too" ] && echo -e "Use either$b dependencies-only$n or$b sources-too$n as an argument" && exit 1

echo -e "=>$b Downloading dependencies...$n"



rm -r lib
mkdir lib

function download
{
    echo "Downloading $1/$2"
    wget --tries=3 --timeout=3 --quiet -P lib $jitpack/com/github/$1/$2/$3/$2-$3$end
}

end=".jar"
download Anuken/Arc arc-core v146
download Anuken/Arc arcnet v146
download Anuken/Mindustry core v146
download Anuken rhino 73a812444ac388ac2d94013b5cadc8f70b7ea027



[ "$1" != "sources-too" ] && exit 0

echo -e "=>$b Downloading sources...$n"



end="-sources.jar"
download Anuken/Arc arc-core v146
download Anuken/Arc arcnet v146
download Anuken/Mindustry core v146
