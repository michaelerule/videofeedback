
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; cur/pre A/B start/end idx (inclusive)
; Mirror index 
RA = * : !fill H2, 0
RB = * : !fill H2, 0
RM = * : !fill H2, 0
PA = * : !fill H2, 0
PB = * : !fill H2, 0
PM = * : !fill H2, 0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; MA: first good pixel (static)
; MB: last  good pixel (static)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Current and next X/Y shift (+c) values
RX = * : !fill H2, 0
RY = * : !fill H2, 0
NX = * : !fill H2, 0
NY = * : !fill H2, 0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Temp state variables
isStarted = <X
didMirror = <M

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; uint8 B = Y0 - clipped(CY)
get_y_position_in_B:
    lda DTY:cmp #CYMAX_PATCH:bcc+:lda #CYMAX_PATCH::+
    sta T:lda #CRY0:sec:sbc T:sta B
    rts

!zone {
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
recalculate_row_bounds:

        +st8zp isStarted, False 
        +st8zp didMirror, False 
        +ix256 .Y,MAPY2,Yk
        jsr get_y_position_in_B
        lda B:bpl+:cmp #H2-128 :bcs+:lda #0  :jmp ++:+:clc:adc #128-H2:++:sta .s
        lda B:bmi+:cmp #127-H2 :bcc+:lda #255:jmp ++:+:clc:adc #128+H2:++:sta .b 
        lda B:bmi+:cmp #128    :bcc+:lda #255:jmp ++:+:clc:adc #128   :++:sta .m 
        
        ldy Yk:lda MB,y:clc:adc #1:sta.e1:sta.e2
        
        ldy Yk:lda MB,y:sta RM,y:sta RB,y
        ldx MA,y
        :-
.Y=*+1:     lda $FFFF,x
.s=*+1:     cmp #0 : bcc .out
.b=*+1:     cmp #0 : bcs .out
.m=*+1:     cmp #0 : bcc+
            lda didMirror : bne+
                txa:ldy Yk:sta RM,y
                +st8 didMirror, True
            :+
            lda <isStarted : bne+
                txa:ldy Yk:sta RA,y
                +st8 <isStarted, True
            :+
            inx
.e1=*+1:    cpx #W
	    bcc-
        rts
.out:
            lda isStarted : beq+
                dex:txa:ldy Yk:sta RB,y
                rts
            :+
            inx
.e2=*+1:    cpx #W
        bcc-
        rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; MA: first good pixel (static)
; MB: last  good pixel (static)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Temp state variables
isStarted = <X
didMirror = <M

!zone {
recalculate_bounds:
    ;;;; patch ;;;;;;;;;;;;;;;;;;;;;;;;;
    jsr set_position_markers
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
set_position_markers:
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




































