
SCR_W   = 320
SCR_H   = 200
SCR_BW  = 40
SCR_BH  = 25

XSHIFT  = 5                             ;# Can't be larger than 5
XBLOCKS = (1<<XSHIFT)                   ;# Number 8x8 blocks used for x
YBLOCKS = 18                           ;# Number 8x8 blocks used for y
XHALF   = XBLOCKS>>1
YHALF   = YBLOCKS>>1
W       = XBLOCKS*4
H       = YBLOCKS*8
W1      = W-1
H1      = H-1
W2      = W>>1
H2      = H>>1
GRIDX   = (SCR_BW - XBLOCKS)>>1              ;# blocks before
GRIDY   = (SCR_BH - YBLOCKS)>>1              ;# blocks above

XBLOCK_START = (SCR_BW - XBLOCKS)>>1 
XBLOCK_END   = XBLOCK_START + XBLOCKS
