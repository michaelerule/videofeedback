;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Testing code for debug, dev.

; cur/pre A/B start/end idx (inclusive)
TA = * : !fill H2, 0
TB = * : !fill H2, 0
TM = * : !fill H2, 0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; MA: first good pixel (static)
; MB: last  good pixel (static)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Temp state variables
isStarted = <X
didMirror = <M

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Sx Bx Mx zero page bytes
!zone {
;---------------------------------------
; params: Yk uint8 row index
; sets .s,.b,.m values
set_position_markers:
    jsr get_y_position_in_B
    lda B:bpl+:cmp #H2-128 :bcs+:lda #0  :jmp ++:+:clc:adc #128-H2:++:sta .s
    lda B:bmi+:cmp #127-H2 :bcc+:lda #255:jmp ++:+:clc:adc #128+H2:++:sta .b 
    lda B:bmi+:cmp #128    :bcc+:lda #255:jmp ++:+:clc:adc #128   :++:sta .m 
    rts
;---------------------------------------
; params: a: loaded shifted mapy data
; return: y: bool   true if in bounds.
is_in_bounds: 
.s=*+1: cmp #0      : bcc .out
.b=*+1: cmp #0      : bcs .out
        ldy #True   : rts
.out:   ldy #False  : rts
;---------------------------------------
; params: a: loaded shifted mapy data
; return: y: bool   true if mirrored
is_mirrored:
.m=*+1: cmp #0          : bcc .e
        ldy #True       : rts
.e:     ldy #False      : rts
}
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
!zone {
experimental_row_bounds_test:
    jsr set_position_markers
    +ix256 Y,MAPY2,Yk

    ldy Yk:lda RY,y:sec:sbc #16:sta T
    ldy Yk:lda NY,y:sec:sbc #16:sta S
    lda T
    cmp S
    bcs +: jmp .down :+ 
    ;.......................................
    .up: ; things go up?         new >= prev 
        +st8 BORDER_COL, BLUE
        
        jsr .upA
        jsr .upB
        jsr .upM
        
        ldy Yk:lda TA,y:cmp RA,y:beq+:+setreg BORDER_COL, GREEN:jmp.f:+
        ldy Yk:lda TB,y:cmp RB,y:beq+:+setreg BORDER_COL, BLUE :jmp.f:+
        ldy Yk:lda TM,y:cmp RM,y:beq+:+setreg BORDER_COL, DKRED:jmp.f:+
        jmp .e
    ;.......................................
    .down: ; things go down?     prev >= new
        +st8 BORDER_COL, DKRED
        
        jsr .downA
        jsr .downB
        jsr .downM
        ;failsafe
        
        ldy Yk 
        lda TM,y
        cmp #W
        bcc+
            sec
            sbc #1
        :+
        sta TM,y
        ldy Yk:lda TA,y:cmp RA,y:beq+:+setreg BORDER_COL, LTGRN:jmp.f:+
        ldy Yk:lda TB,y:cmp RB,y:beq+:+setreg BORDER_COL, LTBLU:jmp.f:+
        ldy Yk:lda TM,y:cmp RM,y:beq+:+setreg BORDER_COL, LTRED:jmp.f:+
    ;.......................................
.e:
    rts
.f:
    .chstride = 3*8
    lda Yk:                         +print8 LACE_VAL_POS
    ldy Yk:lda RY,y:sec:sbc #16:    +print8 LACE_VAL_POS + .chstride
    ldy Yk:lda NY,y:sec:sbc #16:    +print8 LACE_VAL_POS + .chstride*2

    ldy Yk:lda PA,y:                +print8 LOOP_VAL_POS
    ldy Yk:lda PB,y:                +print8 LOOP_VAL_POS + .chstride
    ldy Yk:lda PM,y:                +print8 LOOP_VAL_POS + .chstride*2


    ldy Yk:lda RA,y:                +print8 LACE_VAL_POS + .chstride*3
    ldy Yk:lda RB,y:                +print8 LACE_VAL_POS + .chstride*4
    ldy Yk:lda RM,y:                +print8 LACE_VAL_POS + .chstride*5
    
    ldy Yk:lda TA,y:                +print8 LOOP_VAL_POS + .chstride*3
    ldy Yk:lda TB,y:                +print8 LOOP_VAL_POS + .chstride*4
    ldy Yk:lda TM,y:                +print8 LOOP_VAL_POS + .chstride*5
    rts
    
    
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
experimental_row_bounds_deploy:
    jsr set_position_markers
    +ix256 Y,MAPY2,Yk
    ldy Yk:lda RY,y:sec:sbc #16:sta T
    ldy Yk:lda NY,y:sec:sbc #16:sta S
    lda T
    cmp S
    bcs +: jmp .downd :+ 
    .upd: ; things go up?         new >= prev 
        jsr .upA
        jsr .upB
        jsr .upM
        jmp .ed
    .downd: ; things go down?     prev >= new
        jsr .downA
        jsr .downB
        jsr .downM
        ldy Yk 
        lda TM,y
        cmp #W
        bcc+
            sec
            sbc #1
        :+
        sta TM,y
.ed:
    ldy Yk
    lda TA,y:sta RA,y
    lda TM,y:sta RM,y
    lda TB,y:sta RB,y
    rts
;---------------------------------------
.upA: !zone {
        +t16 Y,.y
        ldy Yk
        ldx PA,y
        :-
.y=*+1:     lda $FFFF,x
            jsr is_in_bounds : cpy #True : bne +
                txa:ldy Yk:sta TA,y
                rts
            :+
            inx
        bcc-
    .f: +setreg  BORDER_COL, YELLOW : jmp .f
}
;---------------------------------------
.upM: !zone {
        +t16 Y,.y
        ldy Yk
        lda MB,y
        sta TM,y
        ldx PM,y
        :-
.y=*+1:     lda $FFFF,x
            jsr is_mirrored : cpy #False : beq +
                txa:ldy Yk:sta TM,y
                rts
            :+
            inx
            cpx #W
        bcc- 
        rts
}
;---------------------------------------
.upB: !zone {
        +t16 Y,.y
        ldy Yk
        lda MB,y
        sta TB,y
        ldx PB,y
        :-
.y=*+1:     lda $FFFF,x
            jsr is_in_bounds : cpy #True : beq+
                dex:txa:ldy Yk:sta TB,y
                rts
            :+
            inx
            cpx #W
	    bcc-
        rts
}
;---------------------------------------
.downA: !zone {
        +t16 Y,.y
        ldy Yk
        lda MA,y
        sta TA,y
        ldx PA,y
        :-
.y=*+1:     lda $FFFF,x
            jsr is_in_bounds : cpy #True : beq+
                inx:txa:ldy Yk:sta TA,y
                rts
            :+
            dex
            cpx #0-1
	    bne-
        rts
}
!if 0 {
;---------------------------------------
.downM: !zone {
        +t16 Y,.y
        ldy Yk
        lda MA,y
        sta TM,y
        ldx PM,y
        :-
.y=*+1:     lda $FFFF,x
            jsr is_mirrored : cpy #True : beq +
                txa:clc:adc #1:ldy Yk:sta TM,y
                rts
            :+
            dex
            cpx #0
	    bne-
        rts
}
} else {
;---------------------------------------
.downM: !zone {
        +t16 Y,.y
        ldy Yk
        lda MA,y
        sta TM,y
        sec
        sbc #1
        sta .e
        ldx PM,y
        :-
.y=*+1:     lda $FFFF,x
            jsr is_mirrored : cpy #True : beq +
                txa:clc:adc #1:ldy Yk:sta TM,y
                rts
            :+
            dex
.e=*+1:     cpx #0
	    bne-
        rts
}
}
;---------------------------------------
.downB: !zone {
        +t16 Y,.y
        ldy Yk
        ldx PB,y
        :-
.y=*+1:     lda $FFFF,x
            jsr is_in_bounds : cpy #True : bne+
                txa:ldy Yk:sta TB,y
                rts
            :+
            dex
	    bne-
    .f: +setreg  BORDER_COL, LTRED : jmp .f
}
;---------------------------------------
.failsafe: !zone {
        +ix256 .y,MAPY2,Yk
        +st8zp isStarted, False 
        +st8zp didMirror, False 
        
        ldy Yk
        lda MB,y
        sta TM,y
        
        sta TB,y
        clc:adc #1
        sta .e1
        sta .e2
        
        ldx MA,y
        :-
.y=*+1:     lda $FFFF,x                  : beq .out
            jsr is_in_bounds : cpy #True : bne .out
            jsr is_mirrored  : cpy #True : bne +
            lda didMirror    : bne +
                txa:ldy Yk:sta TM,y
                +st8 didMirror, True
            :+
            lda <isStarted : bne+
                txa:ldy Yk:sta TA,y
                +st8 <isStarted, True
            :+
            inx
.e1=*+1:    cpx #W
	    bcc-
        rts
.out:
            lda isStarted : beq+
                dex:txa:ldy Yk:sta TB,y
                rts
            :+
            inx
.e2=*+1:    cpx #W
        bcc-
        rts
}
}






















!macro t8y .s,.d {lda .s,y:sta .d,y}







!zone {

experimental_row_bounds_deploy2:
    ;;;; patch ;;;;;;;;;;;;;;;;;;;;;;;;;
    jsr set_position_markers2
    ;;;; patch ;;;;;;;;;;;;;;;;;;;;;;;;;

    jsr get_y_position_in_B
    lda B:bpl+:cmp #H2-128 :bcs+:lda #0  :jmp ++:+:clc:adc #128-H2:++:sta S
    lda B:bmi+:cmp #128    :bcc+:lda #255:jmp ++:+:clc:adc #128   :++:sta M
    lda B:bmi+:cmp #127-H2 :bcc+:lda #255:jmp ++:+:clc:adc #128+H2:++:sta B 
    
    +ix256 Y,MAPY2,Yk
    ldy Yk
    lda NY,y:sec:sbc #16:sta Z
    lda RY,y:sec:sbc #16:sta T
    lda T
    cmp Z
    bcs +: jmp .DOWND2 :+ 
.UPD2:
    .upA:   !zone {+t16 Y,.y:+t8 S,.s:ldy Yk:ldx PA,y::-
    .y=*+1:     lda $FFFF,x
    .s=*+1:     cmp #0 : bcs .fin
            inx:bcc-:jmp CRASHOUT:.fin:txa:sta RA,y}
    .upM:   !zone {+t16 Y,.y:+t8 M,.m:ldy Yk:lda MB,y:sta RM,y:ldx PM,y::-
    .y=*+1:     lda $FFFF,x
    .m=*+1:     cmp #0 : bcc +:txa:sta RM,y:jmp.fin:+
            inx:!if W=128 {bpl-} else {cpx #W:bcc-}:.fin}
    .upB:   !zone {+t16 Y,.y:+t8 B,.b:ldy Yk:lda MB,y:sta RB,y:ldx PB,y:-
    .y=*+1:     lda $FFFF,x
    .b=*+1:     cmp #0:bcc+:dex:txa:sta RB,y:jmp.fin:+
            inx:!if W=128 {bpl-} else {cpx #W:bcc-}:.fin}
    rts
.DOWND2: 
    .downA: !zone {+t16 Y,.y:+t8 S,.s:ldy Yk:lda MA,y:sta RA,y:ldx PA,y:-
    .y=*+1:     lda $FFFF,x
    .s=*+1:     cmp #0 : bcs+:inx:txa:sta RA,y:jmp.fin:+
            dex:cpx #-1:bne-:.fin}
    .downB: !zone {+t16 Y,.y:+t8 B,.b:ldy Yk:ldx PB,y:-
    .y=*+1:     lda $FFFF,x
    .b=*+1:     cmp #0 : bcc .fin
            dex:bne-:jmp CRASHOUT:.fin:txa:ldy Yk:sta RB,y}
    ;;;; patch ;;;;;;;;;;;;;;;;;;;;;;;;;
    .downM: !zone {
            +ix256 Y,MAPY2,Yk
            +t16 Y,.y
            ldy Yk
            lda MA,y
            sta RM,y
            sec
            sbc #1
            sta .e
            ldx PM,y
            :-
    .y=*+1:     lda $FFFF,x
                jsr is_mirrored2 : cpy #True : beq +
                    txa:clc:adc #1:ldy Yk:sta RM,y
                    jmp .fin
                :+
                dex
    .e=*+1:     cpx #0
	        bne-
	        :.fin
    }
    ldy Yk 
    lda RM,y
    cmp #W
    bcc+
        sec
        sbc #1
    :+
    sta RM,y
    ;;;; patch ;;;;;;;;;;;;;;;;;;;;;;;;;
    rts
}


;;;; patch ;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
;---------------------------------------
; params: Yk uint8 row index
; sets .s,.b,.m values
set_position_markers2:
    jsr get_y_position_in_B
    lda B:bpl+:cmp #H2-128 :bcs+:lda #0  :jmp ++:+:clc:adc #128-H2:++:sta .s
    lda B:bmi+:cmp #127-H2 :bcc+:lda #255:jmp ++:+:clc:adc #128+H2:++:sta .b 
    lda B:bmi+:cmp #128    :bcc+:lda #255:jmp ++:+:clc:adc #128   :++:sta .m 
    rts
;---------------------------------------
; params: a: loaded shifted mapy data
; return: y: bool   true if in bounds.
is_in_bounds2: 
.s=*+1: cmp #0      : bcc .out
.b=*+1: cmp #0      : bcs .out
        ldy #True   : rts
.out:   ldy #False  : rts
;---------------------------------------
; params: a: loaded shifted mapy data
; return: y: bool   true if mirrored
is_mirrored2:
.m=*+1: cmp #0          : bcc .e
        ldy #True       : rts
.e:     ldy #False      : rts
}
;;;; patch ;;;;;;;;;;;;;;;;;;;;;;;;;
























