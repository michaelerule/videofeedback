HERE=$(pwd)
MAIN='Main-Class: perceptron.Main\n'

mkdir ./build_temp
rm -r ./build_temp/*
 
javac -d build_temp -g:none $(find src -name *.java -type f)

echo -e $MAIN > manifest
jar cf0m Perceptron.jar manifest -C build_temp .
rm manifest

echo -e 'java -jar Perceptron.jar\n' > run
chmod 777 run

echo -e '#include "stdlib.h"\nint main(int c,char **v){return system("java -jar Perceptron.jar");}' > a.c
gcc -o Perceptron a.c
rm a.c

rm -r build_temp

cd $HERE


