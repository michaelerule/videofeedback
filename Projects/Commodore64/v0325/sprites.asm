;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Sprites as cursor

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; General constants
TPAD        = 50
LPAD        = 24
PXSTART     = GRIDX*8-1
PYSTART     = GRIDY*8
PGSIZE      = 1024
MAXSPRITES  = 8
NSPRITES    = 8

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Cursor specific setup
C0    = 10
CRX0  = (SCR_W>>1)+LPAD-C0
CRY0  = (SCR_H>>1)+TPAD-C0-1-4*(1-(YBLOCKS&1))
CXMIN = (320-W*2)/2 + LPAD - C0 - 1
CXMAX = CXMIN + 255
CYMIN = CRY0 - H2 + 1
CYMAX = CRY0 + H2 + 1
CYMAX_PATCH = CYMAX - 2

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Other functions need these definitions
CRM = %00000001
DTM = %00000010
D1M = %00000100
D2M = %00001000
D3M = %00010000
D4M = %00100000
D5M = %01000000
D6M = %10000000
CRX = SPXS + 2*0
DTX = SPXS + 2*1
D1X = SPXS + 2*2
D2X = SPXS + 2*3
D3X = SPXS + 2*4
D4X = SPXS + 2*5
D5X = SPXS + 2*6
D6X = SPXS + 2*7
CRY = SPYS + 2*0
DTY = SPYS + 2*1
D1Y = SPYS + 2*2
D2Y = SPYS + 2*3
D3Y = SPYS + 2*4
D4Y = SPYS + 2*5
D5Y = SPYS + 2*6
D6Y = SPYS + 2*7

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; A previous attempt to use the bottom 8 lines of bitmap to store sprites. This 
; failed (the row hiding code interacted with bounds check?). With this layout 
; there IS room below the screen graphics. 0xD000 - 1024 = 0xCC00. For cleaner 
; sprites we can use the 512 bytes below this:

SPLEN = 64
SPBMP = SCRAM - NSPRITES*SPLEN
CRAM  = SPBMP + SPLEN*0
DTRAM = SPBMP + SPLEN*1
E1RAM = SPBMP + SPLEN*2
E2RAM = SPBMP + SPLEN*3
E3RAM = SPBMP + SPLEN*4
E4RAM = SPBMP + SPLEN*5
E5RAM = SPBMP + SPLEN*6
E6RAM = SPBMP + SPLEN*7

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
init_sprite

    ldx #0:-:
        lda sprite_data_cur   ,x : sta CRAM ,x
        lda sprite_data_target,x : sta DTRAM,x
        lda sprite_data_eye1  ,x : sta E1RAM,x
        lda sprite_data_eye2  ,x : sta E2RAM,x
        lda sprite_data_eye3  ,x : sta E3RAM,x
        lda sprite_data_eye4  ,x : sta E4RAM,x
        lda sprite_data_eye5  ,x : sta E5RAM,x
        lda sprite_data_eye6  ,x : sta E6RAM,x
    inx:cpx #SPLEN:bne -
    
    SPPT = SCRAM + PGSIZE - MAXSPRITES
    +st8 SPPT+0, (CRAM  & $3FFF)>>6 
    +st8 SPPT+1, (DTRAM & $3FFF)>>6 
    +st8 SPPT+2, (E1RAM & $3FFF)>>6
    +st8 SPPT+3, (E2RAM & $3FFF)>>6
    +st8 SPPT+4, (E3RAM & $3FFF)>>6
    +st8 SPPT+5, (E4RAM & $3FFF)>>6
    +st8 SPPT+6, (E5RAM & $3FFF)>>6
    +st8 SPPT+7, (E6RAM & $3FFF)>>6
    
    +st8 SPCS+0, WHITE
    +st8 SPCS+1, BLACK
    +st8 SPCS+2, DKRED
    +st8 SPCS+3, MAUVE
    +st8 SPCS+4, LTBLU
    +st8 SPCS+5, LTGRY
    +st8 SPCS+6, TEAL
    +st8 SPCS+7, LTGRN
    
    lda #CRY0:ldx #0:-:sta SPYS,x:inx:inx:cpx#16:bne -
    lda #CRX0:ldx #0:-:sta SPXS,x:inx:inx:cpx#16:bne -
    +st8 SPMSB,0
    +st8 SPON,$FF
    
    ; Initialize timer
    lda #$ff
    sta CIA1_TA
    sta CIA1_TA+1
    +st8 CIA1_CRA, %00010001 
    
    rts
    
    


