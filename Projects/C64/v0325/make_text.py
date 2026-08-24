#!/usr/bin/env ipython3
from pylab import *
from numpy import *
from matplotlib.image import imread

# color is forced to be 0123 red gray mauve black and we need to compensate
'''
0 red (global)
1 gray 
2 mauve
3 black
'''
permutation = int32([3,2,2,1])

FONT_HEIGHT = 16

def write_table_bytes(f,a,cspan=16):
    a = array(a).ravel()
    nrows = int(ceil(a.shape[0]/cspan))
    for row in range(nrows):
        bb = a[row*cspan:(row+1)*cspan]
        s = ','.join(['$%02X'%b for b in bb])
        f.write('\t!byte ' + s + '\n')
    f.write('\n')
    
def padto4(a):
    w = int32(ceil(a.shape[1]/4))*4
    foo = int32(zeros((FONT_HEIGHT,w)))
    foo[:,:a.shape[1]]=a
    return foo
    
def pack_label(a):
    '''
    Pack label in blitting order.
    
    Labels start as FONT_HEIGHT x W arrays
    Round W up to multiple of 4
    Group columns into 4 
    Pack bytes
    Extract top, bottom blocks
    
    a = im[:,:28]
    a = pack_label(a)
    close('all')
    a = a.reshape(a.shape[0]//8,8)
    #imshow(a);show(block=True)
    # should be 5 butes of 0xff then a bute of # 0001 0101 = 0x15 = 21
    # byte 15 should be
    # 0 0 1 3
    # 3 3 2 1
    # 1111 1001
    # F9 = 249
    # should end in
    # 1 3 3 3
    # 2 1 1 1
    # 1001 0101
    # 0x95 = 149
    print(a.flat[15])
    print(a)
    '''
    a = padto4(a)
    H,W = a.shape
    a = a.reshape( H, W//4, 4)
    a = permutation[a]
    a = (a[...,0]<<6)|(a[...,1]<<4)|(a[...,2]<<2)|(a[...,3]<<0)
    top = a[:8,:].T.flat
    bot = a[8:,:].T.flat
    a = concatenate([top,bot])
    return a

# labels are packed monolithically
def write_label(name,a):
    W0 = a.shape[1]*2
    a = pack_label(a)
    W = prod(a.shape)//2
    f.write('%s_RAW_WIDTH = %d\n'%(name,W0))
    f.write('%s_PAD_WIDTH = %d\n'%(name,W))
    f.write('\t!byte %d\n'%W0)
    f.write('\t!byte %d\n'%W)
    f.write('%s:\n'%name)
    cspan = FONT_HEIGHT
    for bb in a.reshape(prod(a.shape)//cspan,cspan):
        f.write('\t!byte ' + ','.join(['$%02X'%b for b in bb]) + '\n')
    f.write('\n')

#1234567890ABCDEF
def pack_font(a):
    a = padto4(a)
    a = a.reshape( FONT_HEIGHT, a.shape[1]//4, 4)
    a = permutation[a]
    a = (a[...,0]<<6)|(a[...,1]<<4)|(a[...,2]<<2)|(a[...,3]<<0)
    a = a.T
    return a
    
# fonts are packed as separate width 4 characters
def write_font(name,a):
    W0 = a.shape[1]*2
    a = pack_font(a)
    W = prod(a.shape)//2
    f.write('%s_RAW_WIDTH = %d\n'%(name,W0))
    f.write('%s_PAD_WIDTH = %d\n'%(name,W))
    f.write('\t!byte %d\n'%W0)
    f.write('\t!byte %d\n'%W)
    f.write('%s:\n'%name)
    cspan = FONT_HEIGHT
    for bb in a.reshape(prod(a.shape)//cspan,cspan):
        f.write('\t!byte ' + ','.join(['$%02X'%b for b in bb]) + '\n')
    f.write('\n')




im = imread('text.png')[...,:3]

# Hide magenta rules
magenta = all(int32(np.round(im))==[1,0,1],-1)
im[magenta,:] = [0,0,0]

# Average to width 2 pixels
im = sum(im[:,:,:3],axis=2)
im = (im[:,1::2]+im[:,0::2])/2
im = int32(np.round(im+.1))

# Split out the rows
IMW  = im.shape[-1]
NROW = im.shape[0]//FONT_HEIGHT
print('nrows',NROW)
im = im.reshape(NROW,FONT_HEIGHT,IMW)

pwidths = int32([1+(where(any(r,0))[0][-1]|3) for r in im])
bwidths = pwidths//4

rows = [r[:,:w] for r,w in zip(im,pwidths)]

zlabel = rows[0] 
rlabel = rows[1]
ilabel = rows[2]
digits = rows[3]
fpslabel    = rows[4]
interlace   = rows[5]
looplabel   = rows[6]
_           = rows[7]
_           = rows[8]
ZXCV        = rows[9]

Z,X,C,V,I,J,K,L,F = ZXCV.reshape(16,9,8).transpose(1,0,2)

with open('fontscratch.asm', 'w') as f:
    f.write('\n')
    f.write('\n')
    f.write('FONT_HEIGHT = %d\n\n'%FONT_HEIGHT)
    write_label('EQUATION',zlabel  )
    write_label('REALEQ'  ,rlabel  )
    write_label('IMAGEQ'  ,ilabel  )
    write_label('FPSLABEL',fpslabel)
    write_label('LACELABEL',interlace)
    write_label('LOOPLABEL',looplabel)
    
    write_label('ZKEY',Z)
    write_label('XKEY',X)
    write_label('CKEY',C)
    write_label('VKEY',V)
    write_label('IKEY',I)
    write_label('JKEY',J)
    write_label('KKEY',K)
    write_label('LKEY',L)
    write_label('FKEY',F)
    
    f.write('\n')
    f.write('\n')

with open('fontdata.asm', 'w') as f:
    f.write('\n')
    f.write('\n')
    write_font ('DIGITS'  ,digits)
    f.write('\n')
    f.write('\n')
    
    # This is a decimal converstion table for (real,imag) parts of +1
    NDIGITS = 6
    NBITS   = 6
    scale   = 1/64*10**NDIGITS
    N       = 1<<NBITS
    formatter = '%%0%dd'%(NDIGITS+1)
    decimals = int32([[*map(int,formatter%i)] for i in int32(arange(N)*scale+0.5)]).T
    decimals = decimals[1:]
    decimals = (decimals[0::2]<<4)|(decimals[1::2])
    for i in range(NDIGITS//2):
        s = 'DECIMALS_%d:'%i
        f.write(s+'\n')
        cspan = 32
        a = decimals[i]
        for bb in a.reshape(prod(a.shape)//cspan,cspan):
            s = '\t!byte ' + ','.join(['$%02X'%b for b in bb])
            f.write(s + '\n')
    f.write('\n')
    f.write('\n')

    # Decimal conversion table for a byte
    bytes = [int('%02d'%i,16) for i in arange(100)]
    f.write('DECIMAL_INT:\n')
    write_table_bytes(f,bytes)
    f.write('\n')
    f.write('\n')

    




















































