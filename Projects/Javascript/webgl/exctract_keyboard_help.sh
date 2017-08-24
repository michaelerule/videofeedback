#!/usr/bin/env bash

cat ./perceptron_keyboard.js | grep case | grep -Eo '/\*.*?\*/' | sed "s/^\/\*//g" | sed "s/\*\/$//g" | pr -tw134 -3 | python3 -c "import sys; print('var perceptron_help_string=\`\n'+''.join(sys.stdin.readlines())+'\`;')" > "help_string.js"

cat ./help_string.js
