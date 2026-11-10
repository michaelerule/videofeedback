#!/usr/bin/env ipython3
from pylab import *
from numpy import *
    
exec(open('shared_constants.asm').read())

def write_table_bytes(f,a,name=None,cspan=16):
    if not name is None:
        f.write('%s:\n'%name)
    a = array(a).ravel()
    nrows = int(ceil(a.shape[0]/cspan))
    for row in range(nrows):
        bb = a[row*cspan:(row+1)*cspan]
        s = ','.join(['$%02X'%b for b in bb])
        f.write('\t!byte ' + s + '\n')
    f.write('\n')

BLACK  = 0x0
WHITE  = 0x1
DKRED  = 0x2
TEAL   = 0x3
MAUVE  = 0x4
GREEN  = 0x5
BLUE   = 0x6
YELLOW = 0x7
ORANGE = 0x8
BROWN  = 0x9
LTRED  = 0xa
DKGRY  = 0xb
MDGRY  = 0xc
LTGRN  = 0xd
LTBLUE = 0xe
LTGRY  = 0xf

pallet0 = [0,0,1,1,2,2,3,3,0,0,1,1,2,2,3]
pallet1 = [0,1,1,2,2,3,3,0,0,1,1,2,2,3,3]
pallet0 = [0,0,1,1,2,2,3,2,2,1,1,0,0,0,1,1,2,2,3,2,2,1,1,0,0,0,1,1,2,2,3,2,2,1,1,0,0,0,1,1,2,2,3,2,2,1,1,0,0,0,1,1,2,2,3]*24
pallet1 = [0,1,1,2,2,3,3,3,2,2,1,1,0,1,1,2,2,3,3,3,2,2,1,1,0,1,1,2,2,3,3,3,2,2,1,1,0,1,1,2,2,3,3,3,2,2,1,1,0,1,1,2,2,3,3]*24




pallet0   = [0,0,1,1,2,2,3,2,2,1,1,0,0,0,1,1,2,2,3,2,2,1,1,0,0,0,1,1,2,2,3,2,2,1,1,0,0,0,1,1,2,2,3,2,2,1,1,0]*24
pallet1   = [0,1,1,2,2,3,3,3,2,2,1,1,0,1,1,2,2,3,3,3,2,2,1,1,0,1,1,2,2,3,3,3,2,2,1,1,0,1,1,2,2,3,3,3,2,2,1,1]*24
npallet   = 256
pallet0   = pallet0[:npallet]
pallet1   = pallet1[:npallet]
pallets   = int32([pallet0,pallet1])
nextcolor = arange(npallet)+1
nextcolor[-1] = nextcolor[-2]




########################################
"""
This file will be !source included in the zero page code
"""
with open("zpallet.asm", 'w') as f:
    f.write('\n')
    write_table_bytes(f,nextcolor,'NextColor')


########################################
with open("tabpallet.asm", 'w') as f:
    '''
    These are pre-stored 4-color pallets
    '''
    rotation = [
        BROWN,
        DKRED,
        MAUVE,
        LTBLUE,
        TEAL, 
        LTGRN,
        YELLOW,
        LTRED,
    ]
    rotation = rotation + rotation[:3]
    NPALLETS = len(rotation)-3
    print(NPALLETS)
    A,B = [],[]
    for i in range(NPALLETS): 
        c = rotation[i:i+4]
        A.append((c[1]<<4)+c[2])
        B.append((c[0]<<4)+c[3])
    f.write('\n')
    f.write('NPALLET=%d\n\n'%npallet)
    f.write('NPALLETS=%d\n'%NPALLETS)
    write_table_bytes(f,A,'palletsA')
    write_table_bytes(f,B,'palletsB')
        
    '''
    These are dither patterns
    Iterate over even/odd rows and four pixel locations
    Could be nice if 0, 1 could use same high byte
    
    Eh, screw it make these page align
    '''
    for i in arange(2):
        for j in arange(4):
            pallet = pallets[ 1 if (i%2)==(j%2) else 0 ]
            bytes = int32(pallet) << ((3-j)*2)
            bytes = concatenate([bytes,bytes])
            name  = 'pallet%d%d'%(j,i)
            f.write('!align 255,0\n')
            f.write('!warn "  %s at ",*\n'%name)
            write_table_bytes(f,bytes,name)
