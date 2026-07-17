

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; create 128x128 surface acting as 64x128
SK = 160
SH = 200
BK = SK/8-8
BH = SH/8-8

XSHIFT  = 5                             ; Can't be larger than 5
XBLOCKS = (1<<XSHIFT)                   ; Number 8x8 blocks used for x
YBLOCKS = 24                            ; Number 8x8 blocks used for y

XHALF  = XBLOCKS>>1
YHALF  = YBLOCKS>>1

GRIDX  = (40 - XBLOCKS)>>1              ; blocks before
GRIDY  = (25 - YBLOCKS)>>1              ; blocks above

BSTART = 8*(40*GRIDY+GRIDX)
BHALF  = BSTART + 40*8*(YBLOCKS>>1)
BHALFR = BHALF - 40*8 + (XBLOCKS-1)*8
BEND   = 8*(40*GRIDY-GRIDX+40*YBLOCKS)-1

; Virtual canvas size with 2x2 pixels
XPx2   = XBLOCKS*4
YPx2   = YBLOCKS*4
Xmax   = XPx2-1
Ymax   = YPx2-1

; From make_table.py
W  = XPx2
H  = YPx2
W1 = W-1
H1 = H-1
W2 = W>>1
H2 = H>>1
SHIFT = 5
Z2PX  = 1<<SHIFT

X0  = ((W2*W2 - H2*H2 - W2 + H2)>>1) + (W<<(SHIFT-2)) 
YQ  = (H-1)*(1-W)
Y0  = W*H2 - W2 - H2 + (H<<(SHIFT-1)) + YQ
NY0 = -Y0
NYQ = -YQ

IXF = W2*(W2-1)>>1
IXD = X0-IXF
IX0 = W2*(W2-1)>>1
IY0 = H1*W1

c0  = W2*W2 - H2*H2 - W2 + H2
Cxy = W2*H2*4 - 2*(W2+H2) + 1

d0  = c0 + (W2<<SHIFT)
Dxy = Cxy + (H2<<SHIFT+1)
Dxy2 = Dxy>>1
Dxyb = Dxy&1

Yoff = H1*W1-Dxy2
YofS = 1+(Yoff>>SHIFT)
Yof2 = YofS<<SHIFT

Wm = W<<(SHIFT-2)
Hm = H<<(SHIFT-1)
Wn = W<<(SHIFT-1)
Hn = H<<(SHIFT)

Ymax2  = Ymax + YofS

YofS_raw  = YofS * 64
Ymax2_raw = Ymax2 * 64



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; Zero page allocations
;;;;;;;; uint16: ensure even address

CR1 = $0C
CR2 = $0D
C1 = $14
C2 = $15


DX = $0A
Yr = $0E
ip = $10
dm = $12
ix = $16
iy = $18
DY = $1A
r  = $1C
i  = $1E

_A = $20
_B = $22
_C = $24
_D = $26
_E = $28
_F = $2A
_G = $2C
_H = $2E

_I = $30
_J = $32
_K = $34
_L = $36
_M = $38
_N = $3A
_O = $3C
_P = $3E

_Q = $40
_R = $42
_S = $44
_T = $46
_U = $48
_V = $4A
_W = $4C
_X = $4E

_Y = $50
_Z = $52

ix0  = $54
iy0  = $56
ix00 = $58
iy00 = $5A
qx   = $5C
qy   = $5E

;;;;;;;; uint8
Ai = $60
Xi = $61
Yi = $62
Aj = $63
Xj = $64
Yj = $65
Ak = $66
Xk = $67
Yk = $68
Al = $69
Xl = $6A
Yl = $6B
Ao = $6C
Xo = $6D
Yf = $6E

Ab = $70
Xb = $71
Yb = $72
Ac = $73
Xc = $74
Yc = $75

a1 = $80
x1 = $81
y1 = $82
a2 = $83
x2 = $84
y2 = $85

iH = $90
iW = $92
rH = $94
rW = $96

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Used by the multiply routines
ytmp  = $f4                             ; 1-byte internal copy of multiplier
_lhs  = $f5                             ; 2-byte internal copy of multiplicand
_rhs  = $f7                             ; 2-byte internal copy of multiplier
xtmp  = $f9                             ; temporary 16-bit space to shift x safely
XY    = $fb
ctr   = $fd

SCANLINE_BIT = $f0
poll_delay = $f2









