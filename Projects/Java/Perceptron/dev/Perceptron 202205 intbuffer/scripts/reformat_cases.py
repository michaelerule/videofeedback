#!/usr/bin/env python3

import os,sys
from pylab import *

'''
Reformats java switch-case statements into table-like format
'''


to_reformat = '''
                case VK_LEFT:P.text.left();                 break; 
                case VK_RIGHT:P.text.right();               break; 
                case VK_UP: P.text.up();                    break; 
                case VK_DOWN:P.text.down();                 break; 
                case VK_BACK_SPACE:P.text.backspace();      break; 
                case VK_ENTER:P.text.toMap();              break; 
                case VK_PAGE_UP:P.text.scrollUp();         break; 
                case VK_PAGE_DOWN:P.text.scrollDown();     break; 
                case VK_ESCAPE:System.exit(0);                     break; 
                case VK_CAPS_LOCK:P.save();                        break;
                case VK_TAB:P.text.toggle_cursor();                break; 
                case VK_ALT:break;                                       
                case VK_SHIFT:break;                                     
                case VK_CONTROL:entry_mode = P.text.cursor_on = false;break;
                '''

to_reformat = ''.join([s.strip() for s in to_reformat.split('\n')])
cases = to_reformat.split('case')

from collections import defaultdict
widths = defaultdict(lambda:0)
for case in cases: 
    statements = case.replace(':',';').split(';')
    statements = [s.strip() for s in statements]
    statements = [s for s in statements if len(s)]
    for i,s in enumerate(statements):
        l = len(s) + 2
        #if i==0: 
        #    l = min(l,7)
        if l>widths[i]:
            widths[i] = l
print(widths)


while 4 + len('case ') + sum(list(widths.values())) > 80:
    keys, values = zip(*widths.items())
    worst = argmax(values)
    worst_key = keys[worst]
    worst_value = values[worst]
    print('worst key',worst_key)
    print('worst value',worst_value)
    print(4 + len('case ') + sum(list(widths.values())))
    
    new_widths = defaultdict(lambda:0)
    for case in cases: 
        statements = case.replace(':',';').split(';')
        statements = [s.strip() for s in statements]
        statements = [s for s in statements if len(s)]
        for i,s in enumerate(statements):
            l = len(s) + 2
            #if i==0: l = min(l,7)
            if i == worst_key and l >= worst_value:
                continue
            if l>new_widths[i]:
                new_widths[i] = l
                
    print(new_widths)
    widths = new_widths

keys = sorted(list(widths.keys()))
widths = [widths[k] for k in keys]
sumwidths = cumsum(widths)
print(sumwidths)

for case in cases: 
    text = '    case '
    statements = case.replace(':',';').split(';')
    statements = [s.strip() for s in statements]
    statements = [s for s in statements if len(s)]
    for i,s in enumerate(statements):
        if i==0:
            text += (s+':').ljust(widths[0])
            continue
        if s=='break':
            text = text.ljust(80-6)
            text += 'break;'
            continue
        text += (s+';')
        text = text.ljust(9 + sumwidths[i])
    print(text)


























