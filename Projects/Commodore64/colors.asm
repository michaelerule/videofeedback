
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; Colors

BLACK = $0
WHITE = $1
RED   = $2
TEAL  = $3
MAUVE = $4
GREEN = $5
BLUE  = $6
LEMON = $7
EARTH = $8
BROWN = $9
LTRED = $a
DKGRY = $b
MDGRY = $c
LTGRN = $d
LTBLU = $e
LTGRY = $f

; Color order is 
; hi nibble COLORB (global bg)
; hi nibble COLORA
; lo nibble COLORA
; lo nibble COLORB
; Upper nibble always background in REG_GLOBALBG_COLOR

COLOR0_DRAWING = MAUVE ; This cannot be changed
COLOR1_DRAWING = LTRED
COLOR2_DRAWING = LEMON
COLOR3_DRAWING = TEAL

; See also these registers
; REG_BORDER_COLOR   = $d020 ; Border 
; REG_GLOBALBG_COLOR = $d021 ; Background

COLOR_BORDER    = BLACK
COLOR_GLOBAL_BG = COLOR0_DRAWING ; This cannot be changed

COLORA = (COLOR1_DRAWING<<4)+COLOR2_DRAWING
COLORB = (COLOR0_DRAWING<<4)+COLOR3_DRAWING

COLOR0_OUTSIDE = COLOR0_DRAWING  ; This cannot be changed
COLOR1_OUTSIDE = BLACK
COLOR2_OUTSIDE = BLACK
COLOR3_OUTSIDE = BLACK

COLORC = (COLOR1_OUTSIDE<<4)+COLOR2_OUTSIDE
COLORD = (COLOR0_OUTSIDE<<4)+COLOR3_OUTSIDE






!if 0{
NCOLORS = 21
CM1: !byte 0, 1,0, 1, 2,1, 2, 2,3, 3, 3,0, 0, 0,1, 1, 1,2, 2, 2,3, 3, 3,0, 0
CM2: !byte 0, 0,1, 1, 1,2, 2, 3,2, 3, 0,3, 0, 1,0, 1, 2,1, 2, 3,2, 3, 0,3, 0
}

!if 1{
NCOLORS = 32
CM1: !byte 0,0,1,1,2,2,3,3,0,0,1,1,2,2,3,3,0,0,1,1,2,2,3,3,0,0,1,1,2,2,3,3,0
CM2: !byte 0,1,1,2,2,3,3,0,0,1,1,2,2,3,3,0,0,0,1,1,2,2,3,3,0,0,1,1,2,2,3,3,0
}




































