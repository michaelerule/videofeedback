;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
main:
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  
; Copy 128-stride Y lookup table into
; 256(+128) stride table interleaved 
; with map color data
init_pack_map_tables:
    +st16 .s, MAPY
    +st16 .d, MAPY2    
    ldx #0 :--
        ldy #0 :-
.s=*+1:     lda $FFFF
.d=*+1:     sta $FFFF
            +inc16 .s
            +inc16 .d
        iny:cpy#W:bne-
        +a16l .d,128
    inx:txa:cmp#H2:bne--
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  
init_rows:
    jsr get_cursor_position
    +st8zp Yk,0
    :-
        ldy Yk
        lda DX : sta RX,y : sta NX,y
        lda DY : sta RY,y : sta NY,y
        lda #0 : sta PA,y : sta RA,y
        lda #W1: sta PB,y : sta RB,y
        
        jsr recalculate_row_bounds
        jsr clear_start
        jsr clear_end
        
        jsr prep_x
        jsr prep_y
        
        jsr render
        ldy Yk
        lda RB,y:sta PB,y
        lda RA,y:sta PA,y
    inc Yk
    lda Yk
    cmp #H2
    bne-

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
mainloop:
    +t88 ninterlace,_ilace_
    +t88 _ilace_,iinterlace
    :--
        dec iinterlace
        +t8 iinterlace, Yk
        :-
            jsr poll_cursor
            
            jsr do_line
            ldy Yk
            lda RB,y:sta PB,y
            lda RA,y:sta PA,y
            
            +a88 Yk,_ilace_:cmp #H2:bcc-
        jsr update_frame_duration
        lda iinterlace
    bne --
    jsr update_dx_label
    jsr update_dy_label
jmp mainloop

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
do_line:
    jsr get_cursor_position
    ldy Yk
    lda DX   : sta NX,y
    lda DY   : sta NY,y
    lda RY,y
    cmp NY,y : beq+:jsr rebuild_y_row:+
    lda RX,y
    cmp NX,y : beq+:jsr rebuild_x_row:+
    jmp render

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; post/prep_x depends on RM mirror point
; set by recalculate_row_bounds
rebuild_y_row:
    
    ; Load Yk's old c=RX+iRY, undo pack
    ldy Yk
    lda RX,y:sta DX  
    lda RY,y:sta DY
    jsr post_y

    ; Current limits become previous    
    ldy Yk
    lda RA,y:sta PA,y
    lda RB,y:sta PB,y
    lda RM,y:sta PM,y
    
    ; Recalculate row Yk's limits
    jsr recalculate_bounds
    
    ; Zero stale map data at start
    jsr clear_start
    jsr clear_end
    
    ; Save new iRY, load c=RX+iRY
    ldy Yk
    lda RX,y:sta DX
    lda NY,y
    sta RY,y:sta DY
    
    ; Re-do packing
    jsr prep_y
    
    ; Update real-part (x) mirroring
    jsr xor_x_row
    
rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
rebuild_x_row:
    
    lda RX,y:sta DX
    lda RY,y:sta DY
    
    jsr post_x
    
    lda NX,y:sta RX,y:sta DX
    jsr prep_x
    
rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
clear_start:    
    ; return if clear
    ldy Yk:lda PA,y:cmp RA,y:bcc+:rts:+
        
    ; Map data start for row Yk
    +ix256 .z,MAP,Yk
    
    ; Fill [PA,RA) start index 
    ldy Yk
    
    ; Map stride 256: cannot overflow
    ldx PA,y
    txa
    clc
    adc .z
    sta .z
    
    lda RA,y
    sec
    sbc PA,y
    tax

    ; Zero map
    lda #0
    :- 
.z=*+1:     sta $FFFF,x
            dex
    bpl -
rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
clear_end:    
    ; return if clear
    ldy Yk:lda RB,y:cmp PB,y:bcc+:rts:+
        
    ; Map data start for row Yk
    +ix256 .z,MAP,Yk
    
    ; Fill (RB,PB] end index
    ldy Yk
    
    ; Map stride 256: cannot overflow
    ldx RB,y
    txa
    clc
    adc .z
    sta .z
    
    lda PB,y
    sec
    sbc RB,y
    tax

    ; Zero map
    lda #0
    :- 
.z=*+1:     sta $FFFF,x
            dex
    bne -
rts
}


















