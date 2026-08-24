#!/usr/bin/env ipython3
from pylab import *
from numpy import *

exec(open('shared_constants.asm').read())

def write_table_bytes(f,a,name,cspan=16):
    f.write('!align 255,0\n')
    f.write('%s:\n'%name)
    a = array(a).ravel()
    nrows = int(ceil(a.shape[0]/cspan))
    for row in range(nrows):
        bb = a[row*cspan:(row+1)*cspan]
        s = ','.join(['$%02X'%b for b in bb])
        f.write('\t!byte ' + s + '\n')
    f.write('\n')

flint = lambda q:int32(floor(q))



'''
Calculate z^2 lookup tables
'''

YFACTOR = 2#1

my,mx = H2-.5, W2-.5
SHIFT = 5
px2z  = 2**-SHIFT

highest_mandelbrot_point = 1.12275706363259748
fudge = 1.2 * highest_mandelbrot_point * 16/YBLOCKS
px2z *= fudge

px,py = arange(W)-mx,arange(H)-my

u = (px[None,:] + 1j*py[:,None]/YFACTOR)*px2z

u = (u.real**2-u.imag**2)+1j*(2*u.real*u.imag)
u = u/px2z

_x,_y = flint(u.real+mx+0.5),flint(u.imag*2+my+0.5)

ix = _x.copy()[H2:]
iy = _y.copy()[H2:]

ix %=128
iy  = iy + 128 - H2
bad = (iy<0)|(iy>=256)
bad = (iy<0+14)|(iy>=256-14)
ix[bad]=iy[bad]=0

with open('tabmap.asm', 'w') as f:
    f.write('* = MAPX\n')
    write_table_bytes(f,ix,'__mapx__')
    f.write('* = MAPY\n')
    write_table_bytes(f,iy,'__mapy__')

'''
Screen ram packed as:   y//8    x//8    y%8     x%8
hot_f0 = F + 320
hot_r0 = R - 320
if YBLOCKS & 1:
    if ((Yk + 4) & 2):
        hot_f0 += 312      # 320 - 8
    else:
        hot_r0 += 312      # 320 - 8
XSHIFT  = 5                             ;# Can't be larger than 5
XBLOCKS = (1<<XSHIFT)                   ;# Number 8x8 blocks used for x
YBLOCKS = 21                            ;# Number 8x8 blocks used for y
XHALF   = XBLOCKS>>1
YHALF   = YBLOCKS>>1
W       = XBLOCKS*4
H       = YBLOCKS*8
W1      = W-1
H1      = H-1
W2      = W>>1
H2      = H>>1
GRIDX   = (40 - XBLOCKS)>>1              ;# blocks before
GRIDY   = (25 - YBLOCKS)>>1              ;# blocks above
BSTART = 8*(40*GRIDY+GRIDX)
BHALF  = BSTART + 40*(H2 & %11111000) + (H2 & %00000111)
BHALFR = BHALF  - 40*8 + (XBLOCKS-1)*8
BEND   = 8*(40*GRIDY-GRIDX+40*YBLOCKS)-1
F0 = BITMAP_A + BHALF
R0 = BITMAP_A + BHALFR - 40*8 + 8*9
Let dy (python) be Yk (asm)
lda Yk:+d8:tax
+st16 F,F0
+a16x320 F 
+smp F,.f
.f = F0 + 320*(dy//8)
!if (YBLOCKS & 1) {
    lda Yk:+d4:+md2
    beq+:+a16320 .f+1 : +s8l .f+1,8:jmp ++
    :+:+a16320 .r+1 : +s8l .r+1,8:++
}
if YBLOCKS & 1:
    a = (dy//4)%2
    if (even):
        lo(f) += 320-8
    else:
        lo(r) += 320-8
lda Yk:+d2:+md4:sta C
bcs+:+ix128smc YF0,C,.tf:+ix128smc YR1,C,.tr:jmp ++
:+  :+ix128smc YF1,C,.tf:+ix128smc YR0,C,.tr:++
(tf/tr are fine so skip this)
'''
with open("tabfr.asm", 'w') as f:
    f.write('!align 255,0\n')
    write_table_bytes(f,arange(128)*2        ,'YFEVE')
    write_table_bytes(f,arange(128)[::-1]*2  ,'YREVE')
    write_table_bytes(f,arange(128)*2+1      ,'YFODD')
    write_table_bytes(f,arange(128)[::-1]*2+1,'YRODD')
    f.write('\n')
    f.write('YF0 = YFEVE-3\n')
    f.write('YR0 = YREVE-3\n')
    f.write('YF1 = YFODD-3\n')
    f.write('YR1 = YRODD-3\n')
    f.write('\n')
    
    # Something messy and confusing
    los = []
    his = []
    for dy in range(H2):
        C = (dy//2)%4
        if dy%2==0:
            los.append( '\t!byte <(YF0 + %d + 3)\n'%C )
            his.append( '\t!byte >(YF0 + %d + 3)\n'%C )
        else:
            los.append( '\t!byte <(YF1 + %d + 3)\n'%C )
            his.append( '\t!byte >(YF1 + %d + 3)\n'%C )
    f.write('!align 255,0\n')
    f.write('tfset_lo:\n')
    for l in los:
        f.write(l)
    f.write('\n')
    f.write('!align 255,0\n')
    f.write('tfset_hi:\n')
    for l in his:
        f.write(l)
    f.write('\n')

    los = []
    his = []
    for dy in range(H2):
        C = (dy//2)%4
        if dy%2==0:
            los.append( '\t!byte <(YR1 + %d + 3)\n'%C )
            his.append( '\t!byte >(YR1 + %d + 3)\n'%C )
        else:
            los.append( '\t!byte <(YR0 + %d + 3)\n'%C )
            his.append( '\t!byte >(YR0 + %d + 3)\n'%C )
    f.write('!align 255,0\n')
    f.write('trset_lo:\n')
    for l in los:
        f.write(l)
    for l in los:
        f.write(l)
    f.write('\n')
    f.write('!align 255,0\n')
    f.write('trset_hi:\n')
    for l in his:
        f.write(l)
    for l in his:
        f.write(l)
    f.write('\n')
    
    BANK_NUMBER = 3
    BANK_START  = 0x4000*BANK_NUMBER
    BITMAP_SEL  = 1
    BMP_OFFSET  = BITMAP_SEL*0x2000
    BITMAP_A    = BANK_START + BMP_OFFSET
    BITMAP_A    = int(BITMAP_A)
    BSTART      = 8*(40*GRIDY+GRIDX)
    BHALF       = BSTART + 40*(H2 & 0xF8) + (H2 & 7)
    BHALFR      = BHALF  - 40*8 + (XBLOCKS-1)*8
    BEND        = 8*(40*GRIDY-GRIDX+40*YBLOCKS)-1
    F0          = BITMAP_A + BHALF
    R0          = BITMAP_A + BHALFR - 40*8 + 8*9
    
    bhi,blo = [],[]
    for dy in range(H2):
        hot_f0 = F0 + 320*(dy//8)
        if YBLOCKS & 1:
            if ((dy // 4) % 2):
                hot_f0 += 320-8
        i = hot_f0
        bhi.append(i//256)
        blo.append(i %256)
    write_table_bytes(f,blo,'rftab_lo')
    write_table_bytes(f,bhi,'rftab_hi')
    
    bhi,blo = [],[]
    for dy in range(H2):
        hot_r0 = R0 - 320*(dy//8)
        if YBLOCKS & 1:
            if not ((dy // 4) % 2):
                hot_r0 += 320-8
        i = hot_r0
        bhi.append(i//256)
        blo.append(i %256)
    write_table_bytes(f,blo,'rrtab_lo')
    write_table_bytes(f,bhi,'rrtab_hi')

    """ A "next x" table that just contains x+4 """
    write_table_bytes(f,(arange(256)+4)&0xff,'NEXT4')

    """ A "prev x" table that just contains x-4 """
    write_table_bytes(f,(arange(256)-4)&0xff,'PREV4')

    """ A "next x" table that just contains x+8 """
    write_table_bytes(f,(arange(256)+4)&0xff,'NEXT8')

    """
    Limits of each row
    MA: 0 index of first in-bounds pixel
    MB: 0 index of last  in-bounds pixel    
    """
    write_table_bytes(f,int32([where(~b)[0][ 0] for b in bad]),'MA')
    write_table_bytes(f,int32([where(~b)[0][-1] for b in bad]),'MB')










