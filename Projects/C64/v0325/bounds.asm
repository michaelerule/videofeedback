
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














