#!/data/data/com.termux/files/usr/bin/env bash
rm -rf ./build
mkdir ./build
javac -sourcepath ./src -d ./build ./src/perceptron/Main.java

