#!/usr/bin/env ipython3 --pylab


import os
th = './thumbnails'
if not os.path.exists(th):
    os.makedirs(th)

import subprocess
output = subprocess.getoutput('find ./presets* -iname *.png')

thumbs = []
for f in output.split('\n'):
    thumbname = th+os.sep+f[:-4].replace('.','_').replace('/','_').replace('__','_')+'.png'
    cmd = 'convert "%s" -trim -resize \'x128>\' "%s"'%(f,thumbname)
    print(f, thumbname, cmd)
    if not os.path.exists(thumbname):
        subprocess.getoutput(cmd)
    thumbs.append('<a href="%s"><img src="%s"/></a>'%(f,thumbname))
    
import random
random.shuffle(thumbs)

template = '''
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8" />
</head>
%s
</body>
'''

html = template%('\n'.join(thumbs))

with open('thumbnails.html','w') as outfile:
    outfile.write(html)
    outfile.flush()
    outfile.close()
