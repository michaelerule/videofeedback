;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Initialization code that is safe to delete after loading
!warn " -------------------------------  "
!set _p=* : !source "zeropage.asm"   : !warn "| hotsource  ",*-_p," : ",*
!set _p=* : !source "sprites.asm"    : !warn "| sprites    ",*-_p," : ",*
!set _p=* : !source "spritedata.asm" : !warn "| spritedata ",*-_p," : ",*
!set _p=* : !source "fontscratch.asm": !warn "| scratch    ",*-_p," : ",*
!warn "| second scratch region ends  ",*
!warn " -------------------------------  "
!if *>MAP+MSIZE*2 {!error "scratch overflows into fix code memory"}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
COLOR_TL = GRIDX + (GRIDY*40)
COLOR_TR = COLOR_TL + XBLOCKS
COLOR_BL = COLOR_TL + YBLOCKS*40
COLOR_BR = COLOR_BL + XBLOCKS
JC = COLOR_BR - 40*2*7
LC = JC + 2
IC = JC - 40*2 + 1
KC = JC + 40*2 + 1
VC = COLOR_BR - 40*2
CC = VC - 2*40
XC = CC - 2*40
ZC = XC - 2*40
FC = VC + 2 - 40

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
scratch_temp  : !word 0
scratch_screen: !word 0
scratch_bitmap: !word 0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
init:
    jsr start_graphics
    jsr init_sprite
    jsr init_row_bounds
    jsr move_kernel_to_zero_page
    jsr draw_labels
    jsr toggle_looping
    jsr increment_interlace


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
start_graphics:
!zone {
.P=<(X)
clear_bitmap:
    +st16zp .P, BITMAP_A
    ldx #32 ; 31.25, overspill fine
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
clear_default_outside_colors:
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
show_screen_now:
    +orreg   VIC_CTL1,     BITMAP_MODE_ENABLE
    +orreg   VIC_CTL2,     MULTIC_MODE_ENABLE
    +setreg  GLOBALBG_COL, COL_GLOBAL_BG
    +setreg  BORDER_COL,   COL_BORDER
    +orreg   CIA2_DDRA,    VIC_CIA2_VAL
    +andreg  VIC_SELBANK,  VIC_MASK
    +orreg   VIC_SELBANK,  VIC_BANK
    +setreg  VIC_CONFIG,   VIC_CONFG
rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
init_row_bounds:
    ldx #0:-:
        lda #0 :sta RA,x
        lda #W1:sta RB,x
    inx:cpx #H2:bne -
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
draw_labels:
    +st16 scratch_screen, EQ_LABEL
    +st16 scratch_bitmap, EQUATION
    jsr draw_label
    
    +st16 scratch_screen, RE_LABEL
    +st16 scratch_bitmap, REALEQ
    jsr draw_label
    
    +st16 scratch_screen, IM_LABEL
    +st16 scratch_bitmap, IMAGEQ
    jsr draw_label
    
    +st16 scratch_screen, FPS_LABEL
    +st16 scratch_bitmap, FPSLABEL
    jsr draw_label
    
    +st16 scratch_screen, LACE_LABEL
    +st16 scratch_bitmap, LACELABEL
    jsr draw_label
    
    ;+st16 scratch_screen, LOOP_LABEL
    ;+st16 scratch_bitmap, LOOPLABEL
    ;jsr draw_label
    
    Z_LABEL = BOTTOM_ROW - 4*(SCR_W*2) + SCR_W - 8*4
    +st16 scratch_screen, Z_LABEL
    +st16 scratch_bitmap, ZKEY
    jsr draw_label

    X_LABEL = Z_LABEL + SCR_W*2
    +st16 scratch_screen, X_LABEL
    +st16 scratch_bitmap, XKEY
    jsr draw_label

    C_LABEL = X_LABEL + SCR_W*2
    +st16 scratch_screen, C_LABEL
    +st16 scratch_bitmap, CKEY
    jsr draw_label

    V_LABEL = C_LABEL + SCR_W*2
    +st16 scratch_screen, V_LABEL
    +st16 scratch_bitmap, VKEY
    jsr draw_label

    F_LABEL = V_LABEL + 8*2 - SCR_W
    +st16 scratch_screen, F_LABEL
    +st16 scratch_bitmap, FKEY
    jsr draw_label
    
    jsr get_canvas_colors
    jsr update_pallet_key_colors
    
    J_LABEL = Z_LABEL + 0*8 - SCR_W*2*3
    +st16 scratch_screen, J_LABEL
    +st16 scratch_bitmap, JKEY
    jsr draw_label
    
    L_LABEL = J_LABEL + 2*8
    +st16 scratch_screen, L_LABEL
    +st16 scratch_bitmap, LKEY
    jsr draw_label
    
    I_LABEL = J_LABEL + 1*8 - SCR_W*2
    +st16 scratch_screen, I_LABEL
    +st16 scratch_bitmap, IKEY
    jsr draw_label
    
    K_LABEL = I_LABEL + SCR_W*4
    +st16 scratch_screen, K_LABEL
    +st16 scratch_bitmap, KKEY
    jsr draw_label
    
    
    rts
!if SHOW_SIZES {
!warn *-oldpc," draw_labels "
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; scratch_bitmap: ui16 digit bitmap pointer
; scratch_screen: ui16 start of graphics block memory
!zone {
bitmap_width: !byte 0 
draw_label:
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; Bitmap width stored in prior byte
    +t16   scratch_bitmap,.ptr_width
    +dec16 .ptr_width
    .ptr_width = *+1: lda $DEAD
    tax
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    !zone draw_label_top {
        +t16  scratch_bitmap, .bitmap_src
        +t16  scratch_screen, .screen_dst
        stx      .bitmap_width
        ldy #0
        :-
            .bitmap_src=*+1: lda $FFFF
            .screen_dst=*+1: sta $FFFF
            +inc16 .bitmap_src
            +inc16 .screen_dst
        iny
        .bitmap_width=*+1: cpy #00
        bne-
        +t16 .bitmap_src,scratch_temp
    }
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    !zone draw_label_bottom {
        +t16  scratch_temp, .bitmap_src
        +t16  scratch_screen, .screen_dst
        +a16320  .screen_dst
        stx      .bitmap_width
        ldy #0
        :-
            .bitmap_src=*+1: lda $FFFF
            .screen_dst=*+1: sta $FFFF
            +inc16 .bitmap_src
            +inc16 .screen_dst
        iny
        .bitmap_width=*+1: cpy #00
        bne-
    }
rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
.c: !word 0
:move_kernel_to_zero_page:
        +st16 .s, hotstart
        +st16 .d, HOTSTART
        +st16 .c, hotend-hotstart
        ldy #0
        :-
.s=*+1:     lda $FFFF
.d=*+1:     sta $FFFF
            +inc16 .s
            +inc16 .d
            +dec16 .c 
            lda .c
            ora .c+1
        bne -
        rts
        
}








