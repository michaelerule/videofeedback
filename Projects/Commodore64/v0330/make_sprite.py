#!/usr/bin/env ipython3
from pylab import *
from numpy import *
from matplotlib.image import imread


'''
PAL
0.937:1
1.0672358591248665

24*0.937 = 22.488
15*0.937 = 15


NTSC
0.75:1
1.3333333333333333
'''

def binary_to_packed(f,im,name):
    rows = ['%'+''.join(map(str,i)) for i in im.reshape(21*3,8)]
    rows = array(rows).reshape(21,3)
    f.write(';;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n')
    f.write('; Sprite bitmap (24x21 Pixels), aspect 1.28\n')
    f.write('%s\n'%name)
    for row in rows:
        f.write('\t!byte '+','.join(row)+' ;\n')
    f.write('\t!byte %00000000,%00000000,%00000000 ; hidden row?\n')
    f.write('\n')
    

with open('spritedata.asm', 'w') as f:
    f.write('\n')
        
    im   = int32(sum(imread('dot.png')[:,:,:3],axis=2)>=1)
    binary_to_packed(f,im,'sprite_data_dot')
        
    im   = int32(sum(imread('cross.png')[:,:,:3],axis=2)>=1)
    binary_to_packed(f,im,'sprite_data_cur')
    
    im   = int32(sum(imread('target.png')[:,:,:3],axis=2)>=1)
    binary_to_packed(f,im,'sprite_data_target')

    im      = imread('dot_color.png')[:,:,:3]
    midline = [*map(tuple,im[10])]
    colors  = list(set(midline))
    order   = argsort([midline.index(c) for c in colors])
    colors  = [*map(tuple,array(colors)[order])]
    ncolor  = len(colors)
    w,h = s = im.shape[:2]
    im      = int32([colors.index(tuple(p)) for p in im.reshape(w*h,3)]).reshape(w,h)
    print(ncolor)
    neye = 6
    for n in range(neye):
        binary_to_packed(f,int32( (im>n)&(im<=min(neye,n+2)) ),'sprite_data_eye%d'%(neye-n))














