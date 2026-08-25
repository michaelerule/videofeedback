;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; The 4 colors in high color mode are half in fixed color ram, half screen ram
; Screen ram pointers can toggle per scan line
SCRAM_LENGTH        = $0400
SCRAM_CHARACTER_END = $03E8
SCRAM_SPRITES_START = $03F8

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Bank selection
; VIC_SELBANK at 0xDD00 is an inverted (active low) register.
; Bits 0..1: The bank number, ^0b11 since logic is inverted
BANK_NUM    = 3                         ; 0..3
BANK_START  = $4000*BANK_NUM            ; RAM address at start of bank
VIC_BANK    = 3-BANK_NUM                ; Low bits for VIC_SELBANK
VIC_MASK    = %11111100                 ; Mask for non-screen lines in VIC bank

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Changing the screen ram pointer requires changing the VIC_CONFG bits
; High nibble multiplied by 0x400 to determine where SCRAM starts. 
SCSEL = 3                               ; 0..7 3-bit screen ram offset selector
SCRAM = BANK_START + SCSEL*$400         ; physical screen ram address

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; In bitmap mode, bit 3 toggles whether bitmap memory starts at +0 or +0x2000
; (other bits do nothing). 
BMPSEL    = 1                           ; (0,1) --> +0 or +0x2000 address offset? 
BITMAP_A  = BANK_START + BMPSEL*$2000   ; Add to start of bank to get memory address
VIC_CONFG = (BMPSEL<<3)|(SCSEL<<4)      ; Flags specifyinge above (--> VIC_CONFIG)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; DDRA bits 1--0 are VIC bank direction (must be set to output)
VIC_CIA2_VAL = 3

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Set in VIC_CTL1 and VIC_CTL2 to use 4 color mode.
BITMAP_MODE_ENABLE = %00100000
MULTIC_MODE_ENABLE = %00010000

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
start_graphics:

    !zone {
    :.P=<(X)
    :clear_bitmap:
        +st16zp .P, BITMAP_A
        ldx #32 ; technically 31.25 but overspill ok           
        ldy #0
        lda #$FF
        :-
            sta (.P),y
            iny
            bne -
            inc <(.P+1)
            dex
        bne -
    }
    
    :clear_default_outside_colors:
        ldx #0
        :.fill_pages
            lda #COLC_OUTSIDE
            sta SCRAM + $000,x
            sta SCRAM + $100,x
            sta SCRAM + $200,x
            lda #COLD_OUTSIDE
            sta CORAM + $000,x
            sta CORAM + $100,x
            sta CORAM + $200,x
            inx
        bne .fill_pages
        ldx #0
        :.fill_residual
            lda #COLC_OUTSIDE : sta SCRAM + $300,x
            lda #COLD_OUTSIDE : sta CORAM  + $300,x
        inx : cpx #$e8 : bne .fill_residual

        lda #COLA_DRAWING
        sta pallet_colors_12
        lda #COLB_DRAWING
        sta pallet_colors_03
        jsr set_canvas_colors
        
    :show_screen_now:
        +orreg   VIC_CTL1,     BITMAP_MODE_ENABLE
        +orreg   VIC_CTL2,     MULTIC_MODE_ENABLE
        +setreg  GLOBALBG_COL, COL_GLOBAL_BG
        +setreg  BORDER_COL,   COL_BORDER
        +orreg   CIA2_DDRA,    VIC_CIA2_VAL
        +andreg  VIC_SELBANK,  VIC_MASK
        +orreg   VIC_SELBANK,  VIC_BANK
        +setreg  VIC_CONFIG,   VIC_CONFG

    ; no return because this is included in a giant init block

























