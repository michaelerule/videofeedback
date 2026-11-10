;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; Colors

BLACK = $0
WHITE = $1
DKRED  = $2
TEAL  = $3
MAUVE = $4
GREEN = $5
BLUE  = $6
YELLOW = $7
ORANGE = $8
BROWN = $9
LTRED = $a
DKGRY = $b
MDGRY = $c
LTGRN = $d
LTBLU = $e
LTGRY = $f

; Color order is 
; hi nibble COLB (global bg)
; hi nibble COLA
; lo nibble COLA
; lo nibble COLB
; Upper nibble always background in GLOBALBG_COL

!if 0{
COL0_DRAWING = DKGRY ; This cannot be changed
COL1_DRAWING = MDGRY
COL2_DRAWING = LTGRY
COL3_DRAWING = WHITE
}

COL0_DRAWING = BLUE ; This is global bg
COL1_DRAWING = LTBLU
COL2_DRAWING = TEAL
COL3_DRAWING = LTGRN

; See also these registers
; BORDER_COL   = $d020 ; Border 
; GLOBALBG_COL = $d021 ; Background

COL_BORDER    = BLACK
COL_GLOBAL_BG = COL0_DRAWING ; This cannot be changed

COLA_DRAWING = (COL1_DRAWING<<4)+COL2_DRAWING
COLB_DRAWING = (COL0_DRAWING<<4)+COL3_DRAWING

COL0_OUTSIDE = COL0_DRAWING  ; This cannot be changed
COL1_OUTSIDE = LTGRN
COL2_OUTSIDE = LTBLU
COL3_OUTSIDE = BLACK

COLC_OUTSIDE = (COL1_OUTSIDE<<4)+COL2_OUTSIDE
COLD_OUTSIDE = (COL0_OUTSIDE<<4)+COL3_OUTSIDE

FILLCOL   = 0*%01010101
CLEARCOL  = 0*%01010101

NDITHERS = 8

; There is some bug when using this table lookup
; I needed to extend it by 16 to avoid bad pixels
; Most likely trying to render an out of bounds location
NCOLS = 8*2-2










