    
; Formerly X Y
pallet_colors_12: !word 0
pallet_colors_03: !word 0



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
sync_vblank:
    lda $D011
    and #$80        ; Is the raster line counter >= 256?
    bne sync_vblank ; If yes, keep waiting (we are in the lower border)
    lda $D012
    cmp #250        ; Wait for line 250 (safe VBlank/Border arrival)
    bne sync_vblank
    rts
}
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
CANV0 = GRIDX + GRIDY*40
SC0   = SCRAM + CANV0
CO0   = CORAM + CANV0
set_canvas_colors:
    jsr sync_vblank
    
    lda pallet_colors_12
    ldx #XBLOCKS-1 :-
        !for .r,0,YBLOCKS-1 {sta SC0+SCR_BW*.r,x}
    dex:bpl -
    
    lda pallet_colors_03
    ldx #XBLOCKS-1 :-
        !for .r,0,YBLOCKS-1 {sta CO0+SCR_BW*.r,x}
    dex:bpl -
    
    lsr
    lsr
    lsr
    lsr
    sta GLOBALBG_COL
    jmp update_pallet_key_colors
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone rotate_pallet{
rotate_pallet:    
    inc pallet_number
    lda pallet_number
    and #7
    sta pallet_number
    tax
    lda palletsA,x
    sta pallet_colors_12
    lda palletsB,x
    sta pallet_colors_03
    jsr set_canvas_colors
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone get_canvas_colors{
get_canvas_colors:
    lda SCRAM + GRIDX + (GRIDY*40)
    sta pallet_colors_12
    ldy #0
    lda CORAM+GRIDX+(GRIDY*40)
    and #$0F
    sta pallet_colors_03
    lda GLOBALBG_COL
    asl
    asl
    asl
    asl
    ora pallet_colors_03
    sta pallet_colors_03
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro set_key_glyph_color .v {
    and #$F0
    ora #MDGRY
    sta SCRAM+.v+0
    sta SCRAM+.v+1
    sta SCRAM+.v+40
    sta SCRAM+.v+41
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
update_pallet_key_colors:
    ;pallet_colors_12 (SCRAM)
    ;pallet_colors_03 (CORAM)
    lda pallet_colors_03
    +m16
    +set_key_glyph_color VC
    lda pallet_colors_12
    +m16
    +set_key_glyph_color CC
    lda pallet_colors_12
    +set_key_glyph_color XC
    lda pallet_colors_03
    +set_key_glyph_color ZC
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone rotate_color0 {
rotate_color0:
    jsr get_canvas_colors
    ldx pallet_colors_12
    lda pallet_colors_12
    and #$F0
    sta pallet_colors_12
    txa
    and #$0F
    clc
    adc #1
    and #$0F
    ora pallet_colors_12
    sta pallet_colors_12
    jsr set_canvas_colors
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone rotate_color1 {
rotate_color1:
    jsr get_canvas_colors
    ldx pallet_colors_12
    lda pallet_colors_12
    and #$0F
    sta pallet_colors_12
    txa
    and #$F0
    clc
    adc #$10
    and #$F0
    ora pallet_colors_12
    sta pallet_colors_12
    jsr set_canvas_colors
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone rotate_color2 {
rotate_color2:
    jsr get_canvas_colors
    ldx pallet_colors_03
    lda pallet_colors_03
    and #$F0
    sta pallet_colors_03
    txa
    and #$0F
    clc
    adc #1
    and #$0F
    ora pallet_colors_03
    sta pallet_colors_03
    jsr set_canvas_colors
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone rotate_color3 {
rotate_color3:
    jsr get_canvas_colors
    ldx pallet_colors_03
    lda pallet_colors_03
    and #$0F
    sta pallet_colors_03
    txa
    and #$F0
    clc
    adc #$10
    and #$F0
    ora pallet_colors_03
    sta pallet_colors_03
    jsr set_canvas_colors
    rts
}




