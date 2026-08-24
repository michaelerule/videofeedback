!source "colors.asm" 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; The 4 colors in high color mode are half in fixed color ram, half screen ram
; Screen ram pointers can toggle per scan line
; We have 4 colors per scan line and 6 in total
; 
SCREEN_W   = 320
SCREEN_H   = 200
SCREEN_RAM_LENGTH        = $0400
SCREEN_RAM_CHARACTER_END = $03E8
SCREEN_RAM_SPRITES_START = $03F8

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Bank selection
; 
; REG_VIC_SELBANK at 0xDD00 is an inverted (active low) register.
;
; Bits 0..1: The bank number, ^0b11 since logic is inverted
; Bits 2..7 connect to the serial for disks, printers, so don't touch
; 
BANK_NUMBER = 0                         ; We will use bank 0
BANK_START  = $4000*BANK_NUMBER         ; RAM address at start of bank
VIC_BANK    = 3-BANK_NUMBER             ; Low bits for REG_VIC_SELBANK
VIC_MASK    = %11111100                 ; Mask for non-screen lines in VIC bank

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Bitmap and screen selection
; 
; Changing the screen ram pointer requires changing the VIC_CONFIGURATION bits
; that are sent to REG_VIC_CONFIG. 
; 
; The high nibble of this register is multiplied by 0x400 to determine the
; location where SCREEN_RAM starts. 
;
; If using bank 0
;   Do not use 0: $0000 conflicts with zero page
;   Do not use 2: $0801 is your program entry point
;   Using anyting >=3 requires blanking length 0x400 in your code to avoid it.
; 
SCREEN_SEL  = 1                         ; 0..7 3-bit screen ram offset selector
SCRN_OFFSET = SCREEN_SEL*$400           ; screen ram offset (bytes)
SCREEN_RAM  = BANK_START+SCRN_OFFSET    ; physical screen ram address
; 
; In bitmap mode, bit 3 toggles whether bitmap memory starts at +0 or +0x2000
; (the other bits do nothing). 
; 
; In character mode, bits 3--1 form a 3-bit number which, if multiplied by
; 0x800, points to the offset of the character ram in the active bank. 
; 
; Bit 0 is... allegedly unused (:
BITMAP_SEL  = 1                         ; (0,1) --> +0 or +0x2000 address offset? 
BMP_OFFSET  = BITMAP_SEL*$2000          ; (see above) 
BITMAP_A    = BANK_START + BMP_OFFSET   ; Add to start of bank to get memory address
;
; Register flags to specify the above (save to REG_VIC_CONFIG)
;
VIC_CONFIGURATION  = (BITMAP_SEL<<3)|(SCREEN_SEL<<4) 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Data direction register, port A
; Port A 
;   bits 7--3 are for serial
;   bit  2    is.. an attention line?
;   bits 1--0 are VIC bank direction? (must be set to output
VIC_REG_CIA2_VAL   = 3

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; These get or's into REG_VIC_CTL1 and REG_VIC_CTL2, respectively, to use 
; 4 color bitmap mode.
BITMAP_MODE_ENABLE = %00100000
MULTIC_MODE_ENABLE = %00010000

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
start_graphics
    sei
    lda #$35            ; Bank out BASIC and KERNAL ROM, leave I/O visible
    sta $01
    lda #<BITMAP_A : sta dm
    lda #>BITMAP_A : sta dm+1
    ldx #32             ; clear 32 pages ($20 * 256 = 8192 bytes) 
    ldy #0
    lda #$ff
page_LOOP_multi
    sta (dm),y
    iny
    bne page_LOOP_multi
    inc dm+1
    dex
    bne page_LOOP_multi
    ldx #0
.fill_pages
    
    lda #COLORC
    sta SCREEN_RAM + $000,x
    sta SCREEN_RAM + $100,x
    sta SCREEN_RAM + $200,x
    
    lda #COLORD
    sta REG_COLOR_RAM + $000,x
    sta REG_COLOR_RAM + $100,x
    sta REG_COLOR_RAM + $200,x
    
    inx
    bne .fill_pages

    ldx #0
.fill_tail
    
    lda #COLORC
    sta SCREEN_RAM    + $300,x
    
    lda #COLORD
    sta REG_COLOR_RAM + $300,x
    
    inx
    cpx #$e8            
    bne .fill_tail
        
    +orreg   REG_VIC_CTL1,       BITMAP_MODE_ENABLE
    +orreg   REG_VIC_CTL2,       MULTIC_MODE_ENABLE
    +setreg  REG_GLOBALBG_COLOR, COLOR_GLOBAL_BG
    +setreg  REG_BORDER_COLOR,   COLOR_BORDER
    +orreg   REG_CIA2_DDRA,      VIC_REG_CIA2_VAL
    +andreg  REG_VIC_SELBANK,    VIC_MASK
    +orreg   REG_VIC_SELBANK,    VIC_BANK
    +setreg  REG_VIC_CONFIG,     VIC_CONFIGURATION
	;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	; PREP CANVAS
    +set16 _E,SCREEN_RAM    + GRIDX + (GRIDY*40)
    +set16 _F,REG_COLOR_RAM + GRIDX + (GRIDY*40)
    lda #YBLOCKS
    sta Yb
-   ldy #0
--  lda #COLORA
    sta (_E),y
    lda #COLORB
    sta (_F),y
    iny
    cpy #XBLOCKS
    bne --
    lda _E : clc : adc #40 : sta _E
    bcc + : inc _E+1
+   lda _F : clc : adc #40 : sta _F
    bcc + : inc _F+1
+   
    dec Yb
    bne -
    rts



