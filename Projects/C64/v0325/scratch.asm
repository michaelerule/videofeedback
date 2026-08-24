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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Initialization code that is safe to delete after loading

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!set oldpc=* :
init_row_bounds:
    ldx #0:-:
        lda #0 :sta RA,x
        lda #W1:sta RB,x
    inx:cpx #H2:bne -
    rts
!if SHOW_SIZES {!warn *-oldpc," init_row_bounds "}

scratch_temp  : !word 0
scratch_screen: !word 0
scratch_bitmap: !word 0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!set oldpc=* :
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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; scratch_bitmap: uint16 digit bitmap pointer
; scratch_screen: uint16 start of graphics block memory
!set oldpc=*
!zone draw_label{
bitmap_width: !byte 0 
draw_label:
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; Bitmap width stored in previous byte
    +t16   scratch_bitmap,.ptr_width
    +dec16 .ptr_width
    .ptr_width = *+1: lda $DEAD
    tax
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
!if SHOW_SIZES {!warn *-oldpc," draw_label "}

!set oldpc=*
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
!if SHOW_SIZES {!warn *-oldpc," move_kernel_to_zero_page "}


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!if 0 {
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Erases low memory and scratch code
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; This copies the 128 stride MAPY lookup
; Into the interleave +128 bytes of 256 
; stride MAP scratch
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Nestle this in the map data, not Y 
; table copy location.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!align 255,0
* = *+128
!zone {
init_pack_map_tables:
    +st16 .s, MAPY
    +st16 .d, MAPY2    
    ldx #0 
    :--
        ldy #0 
        :-
.s=*+1:     lda $FFFF
.d=*+1:     sta $FFFF
            +inc16 .s
            +inc16 .d
        iny
        cpy #W
        bne -
        +a16l .d,128
    inx
    txa
    cmp #H2
    bne--
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Arguably this can't go in scratch
; since it erases itself. 
!zone {
fill_map_with_first_pallet_index:
    +st16 .d, MAP    
    ldx #0 
    :--
        ldy #0 
        :-
            lda #0
.d=*+1:     sta $FFFF
            +inc16 .d
        iny
        cpy #W
        bne -
        +a16l .d,128
    inx
    txa
    cmp #H2
    bne--
    rts
}
}
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;









































































