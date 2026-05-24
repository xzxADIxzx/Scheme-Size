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
    echo "Downloads dependencies of the project"
    echo ""
    echo "Usage: build <operations>"
    echo "Operations:"
    echo "    -h display this text"
    echo "    -c clear the library"
    echo "    -d download dependencies"
    echo "    -s download sources"
    exit 0
fi

# endregion
# region download

function download
{
    if echo $5 | grep -q "sources"
    then
        file="-$4-sources.jar"
    else
        file="-$4.jar"
    fi
    if echo $3 | grep -q "null"
    then
        link="https://jitpack.io/com/github/$1/$2/$4/$2$file"
    else
        link="https://jitpack.io/com/github/$1/$2/$3/$4/$3$file"
    fi

    echo -e "$g < Downloading $1/$2:$3$n"
    curl --fail --show-error --silent --output-dir lib --remote-name $link
}

# endregion
# region clear

if echo $1 | grep -q "c"
then
    region "Clearing the library..."

    rm -r lib
    mkdir lib
fi

# endregion
# region dependencies

if echo $1 | grep -q "d"
then
    region "Downloading dependencies..."

    download Anuken Arc       arc-core v158       library
    download Anuken Arc       arcnet   v158       library
    download Anuken Mindustry core     v158       library
    download Anuken rhino     null     54b75cbd12 library
fi

# endregion
# region sources

if echo $1 | grep -q "s"
then
    region "Downloading sources..."

    download Anuken Arc       arc-core v158       sources
    download Anuken Arc       arcnet   v158       sources
    download Anuken Mindustry core     v158       sources
fi

# endregion
