#!/usr/bin/env ipython3

from pylab import *
from numpy import *

# Drawing area and midpoint
W ,H  = 128  , 96 # must be divisible by 4!
W1 = W-1
H1 = H-1
W2 = W>>1
H2 = H>>1
W2,H2 = W//2 , H//2
my,mx = H2-.5, W2-.5
# Scale conversion (need to be power of two)
SHIFT = 5
px2z = 2**-SHIFT
#print('domain size',2**-SHIFT*min(W2,H2))
# Pixel grid, shifted
px,py = arange(W)-mx,arange(H)-my
flint=lambda q:int32(floor(q))

# Reference Implementation
# Complex grid u=z²
u = ((px[None,:] + 1j*py[:,None])*px2z)**2/px2z
# Converted back to pixel coordinatess
_x,_y = flint(u.real+mx+0.5),flint(u.imag+my+0.5)

ix = _x.copy()
iy = _y.copy()
bad = (iy<0)|(iy>=H)
ix[bad]=iy[bad]=255
ix%=128
f2 = int32(zeros((H,W)))
figure(0,(8,6),120)
for k in range(16):
    for r in range(H2,H):
        for c in range(W):
            x,y = ix[r,c] , iy[r,c]
            if y==255:
                color=0
            else:
                color = f2[y,x]
                color = (color + 1)%16
            f2[r,c] = f2[H1-r,W1-c] = color
    subplot(4,4,k+1)
    imshow(f2)
    axis('off')
subplots_adjust(0,0,1,1,0,0)
show(block=True)


'''
; Verify identity for 
; w1 - (x+r)&w1
; w + ~((x+r)&w1)
; w1 + ~w1 | ~(x+r)
; w1 + ~w1 | (~x-r)
; w1 + ~w1 + ( w1 & (~x + ~r + 1) )
; 0xFF + ( w1 & (~x + C1) )
; (w1 & (~x + C1)) - 1
; w1 + ~((x+r)&w1) = w1 & (~x + ~r) if C1 = ~r + 1
; w1 + ~((x+r)&w1) = w1 & (~x + C1) if C1 = ~r + 1
'''
for r in range(256):
    C1 = ~r+1
    for x in range(W):
        a = W1 - (x+r)&W1
        #b = W - 1 + 1 + (~W1 | (~x+~r+1))
        #b = W - 1 + 1 + ((~W1)&0xFF) + (W1 & (~x+~r+1))
        #b = 256 + (W1 & (~x+~r+1))
        b = W1 & (~x+C1)
        assert a&0xFF==b&0xFF


# version 8, with verification
c0  = int(W2*W2 - H2*H2 - W2 + H2)
Cxy = int(W2*H2*4 - 2*(W2+H2) + 1)
ix = int32(zeros((H2,W)))
iy = int32(zeros((H2,W)))
for i in range(H2,H):
    iH = i*H
    iW = i*W
    for r in range(W):
        rW = r*W
        rH = r*H
        ix[i-H2,r] = ((r*r-i*i + (r-rW-i+iH)   + c0 )>>SHIFT) + W2
        iy[i-H2,r] = ((4*r*i   + 2*(r+i-rH-iW) + Cxy)>>(SHIFT+1)) + H2
        assert _x[i,r]==ix[i-H2,r]
        assert _y[i,r]==iy[i-H2,r]
ix%=128
bad = (iy<0)|(iy>=H)
ix[bad]=iy[bad]=255
hi,lo=iy,ix
assert all(lo>=0)
assert all(hi>=0)
assert all(lo<256)
assert all(hi<256)
with open('tables.asm', 'w') as f:
    f.write('* = MAP_DX\n')
    f.write('DX_CACHE:\n')
    for bb in lo.reshape(H2*4,W//4):
        f.write('\t!byte ' + ','.join(['$%02X'%b for b in bb]) + '\n')
    f.write('\n')
    f.write('* = MAP_DY\n')
    f.write('DY_CACHE:\n')
    for bb in hi.reshape(H2*4,W//4):
        f.write('\t!byte ' + ','.join(['$%02X'%b for b in bb]) + '\n')
    f.write('\n')





