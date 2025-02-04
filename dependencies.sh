#!/bin/bash

b="\033[1;32m"
n="\033[0;39m"

jitpack="https://jitpack.io"



echo -e "=>$b Downloading dependencies...$n"

rm -r lib
mkdir lib

function download
{ 
    echo "Downloading $1/$2"
    wget --tries=3 --timeout=3 --quiet -P lib $jitpack/com/github/$1/$2/$3/$2-$3.jar
}

download Anuken/Arc arc-core v146
download Anuken/Arc arcnet v146
download Anuken/Mindustry core v146
download Anuken rhino 73a812444ac388ac2d94013b5cadc8f70b7ea027
