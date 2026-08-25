
False = 0
True  = 1

SK = 160
SH = 200
BK = (SK>>3)-8
BH = (SH>>3)-8

!source "shared_constants.asm"

BSTART = 8*(40*GRIDY+GRIDX)
BHALF  = BSTART + 40*(H2 & %11111000) + (H2 & %00000111)
BHALFR = BHALF  - 40*8 + (XBLOCKS-1)*8
BEND   = 8*(40*GRIDY-GRIDX+40*YBLOCKS)-1

c0  = W2*W2 - H2*H2 - W2 + H2
Cxy = W2*H2*4 - 2*(W2+H2) + 1

F0 = BITMAP_A + BHALF
R0 = BITMAP_A + BHALFR - 40*8 + 8*9


