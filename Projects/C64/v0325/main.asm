;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
main:
    ;jsr init_pack_map_tables
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  
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
    
    ; don't run it, it destroys itself since on scratch
    ;jsr fill_map_with_first_pallet_index

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  
init_rows:
    +t88 DTY,CRY_OLD
    jsr get_new_cursor_position
    +st8zp Yk,0
    :-
        ldy Yk
        lda DX : sta RX,y : sta NX,y
        lda DY : sta RY,y : sta NY,y
        lda #0 : sta PA,y : sta RA,y
        lda #W1: sta PB,y : sta RB,y
        
        jsr recalculate_row_bounds
        jsr clear_start
        
        ldy Yk
        lda RA,y
        sta PA,y
        
        jsr prep_x
        jsr prep_y
        jsr render
        
        ldy Yk
        lda RB,y
        sta PB,y
    inc Yk
    lda Yk
    cmp #H2
    bne-

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
.forever
    +t88 ninterlace,_ilace_
    +t88 _ilace_,iinterlace
    :--
        dec iinterlace
        +t8 iinterlace, Yk
        :-
            jsr poll_cursor
            jsr do_line
            ldy Yk:lda RB,y:sta PB,y
            +a88 Yk,_ilace_ : cmp #H2 : bcc -
        jsr update_frame_duration
        lda iinterlace
    bne --
    jsr update_dx_label
    jsr update_dy_label
jmp .forever

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
do_line:
    jsr get_new_cursor_position
    ldy Yk
    lda DX   : sta NX,y
    lda DY   : sta NY,y
    lda RY,y : cmp NY,y : beq+ : jsr rebuild_y_row :+
    lda RX,y : cmp NX,y : beq+ : jsr rebuild_x_row :+
    jmp render

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; post/prep_x depends on RM mirror point
; set by recalculate_row_bounds
rebuild_y_row:
    ldy Yk :: lda RX,y:sta DX   :: lda RY,y:sta DY
    jsr post_y
    ldy Yk :: lda RA,y:sta PA,y :: lda RB,y:sta PB,y :: lda RM,y:sta PM,y
    jsr experimental_row_bounds_deploy2
    jsr clear_start
    ldy Yk :: lda RA,y:sta PA,y :: lda RX,y:sta DX   :: lda NY,y:sta RY,y:sta DY
    jsr prep_y
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




















