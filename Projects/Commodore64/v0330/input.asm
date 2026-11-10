;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
NINTERLACES=14
interlace_values: 
    !byte 1,2,3,4,6,8,10,12,14,16,18,20,22,24

increment_interlace:
    inc IINTERLACE
    lda IINTERLACE
    cmp #NINTERLACES
    bcc +
        lda #0
    :+
    sta IINTERLACE
    tay
    lda interlace_values,y
    sta ninterlace
    ; Update UI to show interlace value
    tay
    lda   DECIMAL_INT,y
    sta   text_byte_value
    +st16 text_screen_ptr, LACE_VAL_POS
    jsr print_uint8
    rts
}


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!if 0 {
      |B0 |B1 |B2 |B3 |B4 |B5 |B6 |B7 | Joy2
    --|---|---|---|---|---|---|---|---|-----
    A0| Dl| Rt|CSh| F7| F1| F3| F5|CSv| Up
    A1| 3 | W | A | 4 | Z | S | E |SHl| Down
    A2| 5 | R | D | 6 | C | F | T | X | Left
    A3| 7 | Y | G | 8 | B | H | U | V | Right
    A4| 9 | I | J | 0 | M | K | O | N | Fire
    A5| + | P | L | - | . | : | @ | , |
    A6| £ | * | ; |Hom|SHr| = | ↑ | / |
    A7| 1 | ← |Ctl| 2 |Spc|c64| Q | Run/Stop
}
ON_COLOR  = ((WHITE<<4)|MDGRY)
OFF_COLOR = ((COL1_OUTSIDE<<4)|COL2_OUTSIDE)
poll_keys:
    +st8 CIA1_DDRB,  INPUT
    
    ; ZXCV
    jsr Kpress_A1 : beq ++
        lda K : and #KB_Z : beq+ : jsr rotate_color3 :+
    :++
    
    jsr Kpress_A2 : beq ++
        lda K : and #KB_X : beq+ : jsr rotate_color1 :+
        lda K : and #KB_C : beq+ : jsr rotate_color0 :+
        lda K : and #KB_F : beq+ : jsr toggle_pallet_is_rotating :+
    :++
    
    jsr Kpress_A3 : beq ++
        lda K : and #KB_V : beq+ : jsr rotate_color2 :+
    :++
    
    ; The IC, FC etc locations are in ***scratch.asm***
    jsr Kpress_A4 
    
    ldy #OFF_COLOR : lda K_ : and #KB_I : beq+: ldy #ON_COLOR :+ tya : +set_key_glyph_color IC
    ldy #OFF_COLOR : lda K_ : and #KB_J : beq+: ldy #ON_COLOR :+ tya : +set_key_glyph_color JC
    ldy #OFF_COLOR : lda K_ : and #KB_K : beq+: ldy #ON_COLOR :+ tya : +set_key_glyph_color KC
    lda K_ : and #KB_I : beq+: jsr go_north :+
    lda K_ : and #KB_J : beq+: jsr go_west  :+
    lda K_ : and #KB_K : beq+: jsr go_south :+
    
    jsr Kpress_A5 
    
    ldy #OFF_COLOR : lda K_ : and #KB_L : beq+: ldy #ON_COLOR :+ tya : +set_key_glyph_color LC
    lda K_ : and #KB_L : beq+: jsr go_east :+
    
    lda K : beq ++
        ;lda K : and #KB_P     : beq+ : jsr toggle_looping      :+
        lda K : and #KB_AT     : beq+ : jsr rotate_color0       :+
        lda K : and #KB_HYPHEN : beq+ : jsr rotate_color1       :+
        lda K : and #KB_COMMA  : beq+ : jsr rotate_color2       :+
        lda K : and #KB_PLUS   : beq+ : jsr rotate_color3       :+
    :++
    
    
    
    jsr Kpress_A7 
    
    QC = COLOR_BL 
    ldy #OFF_COLOR : lda K_ : and #KB_Q : beq+: ldy #ON_COLOR :+ tya : +set_key_glyph_color QC
    
    lda K : beq ++
        lda K : and #KB_Q      : beq+ : jsr increment_interlace :+
    :++
    
    +st8 CIA1_DDRA, INPUT
    +st8 CIA1_PORTA,IDLE 
    rts
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
pallet_is_rotating:!byte 0
toggle_pallet_is_rotating:
    lda #1
    sec
    sbc pallet_is_rotating
    sta pallet_is_rotating
    bne+
        lda #OFF_COLOR 
        +set_key_glyph_color FC
        rts
    :+
        lda #ON_COLOR 
        +set_key_glyph_color FC
        rts


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;NextColor[NPALLET-1]=0
;NextColor[NPALLET-1]=NPALLET-1
!zone {
is_looping: !byte 1
toggle_looping:

    rts
    
    !if 0 {

    lda #1
    eor is_looping
    sta is_looping
    bne+
        ; Set update to adc #1
        lda #$69
        sta <(increment_point)    
        lda #$01
        sta <(increment_point+1)
        lda #CHTRUE
        jmp ++
    :+
        ; Set update to asl noop 
        ; One cycle to be saved here for the adventerous. TODO
        lda #$0A
        sta <(increment_point)    
        lda #$EA
        sta <(increment_point+1)
        lda #CHFALSE
    :++
    sta   text_digit_value
    +st16 text_screen_ptr, LOOP_VAL_POS
    jsr draw_digit
    
    }
rts
}





















    
