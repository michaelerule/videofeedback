#!/usr/bin/env bash

# the following command is terrible, I know
# 

cat ./perceptron_keyboard.js | grep case | grep -Eo '/\*.*?\*/' | grep -Ev 'not bound'  | sed "s/^\/\*//g" | sed "s/\*\/$//g" | pr -tw100 -2 | python3 -c "import sys; print('var perceptron_help_string=\`\n'+''.join(sys.stdin.readlines())+'\`;')" > "help_string.js"

cat ./help_string.js
