




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; If the cursor moves up (cury goes down) these always move to the right or stay put So... we can start scan early

!zone {
recalculate_rowA_up:
        +ix256 .Y,MAPY2,Yk
        jsr get_y_position_in_B
        lda B:bpl+:cmp #H2-128 :bcs+:lda #0:jmp ++:+:clc:adc #128-H2:++:sta .s
        ldx RA,y
        dex
        :-
            inx
.Y=*+1:     lda $FFFF,x
.s=*+1:     cmp #0 
        bcc -
        txa
        sta RA,y
        rts
}


!zone {
recalculate_rowB_up:
        +st8zp isStarted, False 
        +ix256 .Y,MAPY2,Yk
        jsr get_y_position_in_B
        lda B:bmi+:cmp #127-H2 :bcc+:lda #255:jmp ++:+:clc:adc #128+H2:++:sta .b 
        ldy Yk
        lda MB,y
        sta.e
        ldy Yk
        ldx RB,y
        dex
        :-
            inx
.Y=*+1:     lda $FFFF,x
.b=*+1:     cmp #0 
            bcs .f
.e=*+1:     cpx #W1
        bcc-
        inx
.f:
        dex
        txa
        ldy Yk
        sta RB,y
        rts
}

!zone {
recalculate_rowM_up:
        +ix256 .Y,MAPY2,Yk
        jsr get_y_position_in_B
        lda B:bmi+:cmp #128:bcc+:lda #255:jmp ++:+:clc:adc#128:++:sta .m 
        ldy Yk
        lda MB,y
        sta .e
        ldx RM,y
        dex
        :-
            inx
.Y=*+1:     lda $FFFF,x
.m=*+1:     cmp #0 : bcs .f
.e=*+1:     cpx #W1
	    bcc-
.f:
        txa
        ldy Yk
        sta RM,y
        rts
}





; How branching works: 
;    after cmp v:
;    bcc a< v   inverse:    a>=v
;    bcs a>=v   inverse:    a< v
;    bne a!=v   inverse:    a==v
;    beq a==v   inverse:    a!=v
;    bpl sign(a-v) == 0
;    bmi sign(a-v) == 1
;    |a-v|<=128
;        bpl a>=v
;        bmi a<v
;    |a-v|>=128
;        bpl a<v
;        bmi a>=v


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; in this code comparing to "small" triggered "bad" if carry clear, jumping to out of bounds
; carry set presumably was the first good pixel and the stored RA position
; carry clear means register a < compared value (.s)
; so  y <  .s must mean ... invalid
; and y >= .s must mean ... valid
; RA should be he first valid pixel
; descending, this means run til we find an invalid one, then add 1
; MA-1 is last place to check
; but MA could be 0
!zone {
recalculate_rowA_down:
        +ix256 .Y,MAPY2,Yk
        jsr get_y_position_in_B
        lda B:bpl+:cmp #H2-128 :bcs+:lda #0:jmp ++:+:clc:adc #128-H2:++:sta .s
        
        ldy Yk
        lda MA,y
        sta .e
        
        ldx RA,y
        inx
        :-
            dex 
.Y=*+1:     lda $FFFF,x
.s=*+1:     cmp #0
            bcc .o
.e=*+1:     cpx #0
        bne -
        dex
.o:
        inx
        txa
        ldy Yk
        sta RA,y
        rts
}

!zone {
recalculate_rowB_down:
        +ix256 .Y,MAPY2,Yk
        jsr get_y_position_in_B
        lda B:bmi+:cmp #127-H2 :bcc+:lda #255:jmp ++:+:clc:adc #128+H2:++:sta .b 
        ldy Yk
        lda RA,y
        sta .e
        ldx RB,y
        :-
.Y=*+1:     lda $FFFF,x
.b=*+1:     cmp #0
            bcc .f
.e=*+1:     cpx #0
            beq .f
            dex
        jmp -
.f:
        txa
        ldy Yk
        sta RB,y
        rts
}

!zone {
recalculate_rowM_down:
!zone {
recalculate_rowM_down:
        +ix256 .Y,MAPY2,Yk
        jsr get_y_position_in_B
        lda B:bmi+:cmp #128:bcc+:lda #255:jmp ++:+:clc:adc#128:++:sta .m 
        ldy Yk
        lda RA,y
        sta .e
        ldx RM,y
        :-
.Y=*+1:     lda $FFFF,x
.m=*+1:     cmp #0
            bcc .no_mirror
.e=*+1:     cpx #0
            beq .done
            dex
        jmp -
.no_mirror:
        txa
        cmp RM,y
        beq .done
        inx
.done:
        txa
        ldy Yk
        sta RM,y
        rts
}

}








!if 0 {

ldy Yk
lda RA,y:sta PRA
lda RB,y:sta PRB
lda RM,y:sta PRM
jsr recalculate_row_bounds
ldy Yk
lda RA,y:sta TRA
lda RB,y:sta TRB
lda RM,y:sta TRM

ldy Yk:lda RY,y:sec:sbc #16:sta T
ldy Yk:lda NY,y:sec:sbc #16:sta S
lda T
cmp S
bcs +: jmp .down :+
    +st8 BORDER_COL, BLUE
    ; things go up? 
    
    ldy Yk
    lda PRA:sta RA,y
    lda PRB:sta RB,y
    lda PRM:sta RM,y
    jsr recalculate_rowA_up
    ldy Yk:lda RA,y:cmp TRA:beq+++
        +st8 BORDER_COL, WHITE
        ldy Yk:lda RA,y:                +print8 LOOP_VAL_POS
        lda TRA:                        +print8 LOOP_VAL_POS+2*8
        lda Yk:                         +print8 LOOP_VAL_POS+4*8
        ldy Yk:lda RY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15
        ldy Yk:lda NY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15+8*3
    :+++
    
    ldy Yk
    lda PRA:sta RA,y
    lda PRB:sta RB,y
    lda PRM:sta RM,y
    jsr recalculate_rowB_up
    ldy Yk:lda RB,y:cmp TRB:beq+++
        +st8 BORDER_COL, YELLOW
        ldy Yk:lda RB,y:                +print8 LOOP_VAL_POS
        lda TRB:                        +print8 LOOP_VAL_POS+2*8
        lda Yk:                         +print8 LOOP_VAL_POS+4*8
        ldy Yk:lda RY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15
        ldy Yk:lda NY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15+8*3
    :+++
    
    ldy Yk
    lda PRA:sta RA,y
    lda PRB:sta RB,y
    lda PRM:sta RM,y
    jsr recalculate_rowM_up
    ldy Yk:lda RM,y:cmp TRM:beq+++
        +st8 BORDER_COL, LTRED
        ldy Yk:lda RM,y:                +print8 LOOP_VAL_POS
        lda TRM:                        +print8 LOOP_VAL_POS+2*8
        lda Yk:                         +print8 LOOP_VAL_POS+4*8
        ldy Yk:lda RY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15
        ldy Yk:lda NY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15+8*3
    :+++
    
    jmp ++
:.down
    +st8 BORDER_COL, GREEN
    ; things go down? 
    
    ldy Yk
    lda PRA:sta RA,y
    lda PRB:sta RB,y
    lda PRM:sta RM,y
    jsr recalculate_rowA_down
    ldy Yk:lda RA,y:cmp TRA:beq+++
        +st8 BORDER_COL, WHITE
        ldy Yk:lda RA,y:                +print8 LOOP_VAL_POS
        lda TRA:                        +print8 LOOP_VAL_POS+2*8
        lda Yk:                         +print8 LOOP_VAL_POS+4*8
        ldy Yk:lda RY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15
        ldy Yk:lda NY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15+8*3
    :+++
    
    ldy Yk
    lda PRA:sta RA,y
    lda PRB:sta RB,y
    lda PRM:sta RM,y
    jsr recalculate_rowB_down
    ldy Yk:lda RB,y:cmp TRB:beq+++
        +st8 BORDER_COL, YELLOW
        ldy Yk:lda RB,y:                +print8 LOOP_VAL_POS
        lda TRB:                        +print8 LOOP_VAL_POS+2*8
        lda Yk:                         +print8 LOOP_VAL_POS+4*8
        ldy Yk:lda RY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15
        ldy Yk:lda NY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15+8*3
    :+++
    
    ldy Yk
    lda PRA:sta RA,y
    lda PRB:sta RB,y
    lda PRM:sta RM,y
    jsr recalculate_rowM_down
    ldy Yk:lda RM,y:cmp TRM:beq+++
        +st8 BORDER_COL, LTRED
        ldy Yk:lda RM,y:                +print8 LOOP_VAL_POS
        lda TRM:                        +print8 LOOP_VAL_POS+2*8
        lda Yk:                         +print8 LOOP_VAL_POS+4*8
        ldy Yk:lda RY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15
        ldy Yk:lda NY,y:sec:sbc #16:    +print8 LOOP_VAL_POS+8*15+8*3
    :+++
    
:++

}

