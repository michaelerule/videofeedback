;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
:mask0: !byte 8,4,2,1
.F: !word 0
.R: !word 0
clear_start:    
    ; return if nothing to clear
    ldy Yk:lda PA,y:cmp RA,y:bcc +:rts:+
    
    lda Yk
    +d8
    tax
    +st16    .F,F0
    +a16x320 .F 
    +t16     .F,.if
    +st16    .R,R0
    +s16x320 .R 
    +t16     .R,.ir
    
    lda Yk
    +d2
    +md4
    sta <(C)
    
    bcs+
        +ix168 .tf,YF0,<(C)
        +ix168 .tr,YR1,<(C)
        jmp ++
    :+  
        +ix168 .tf,YF1,<(C)
        +ix168 .tr,YR0,<(C)
    :++
    
    !if (YBLOCKS & 1) {
        lda Yk
        +d4
        +md2
        beq+
            +a16320 .if 
            +s8l    .if,8
            jmp ++
        :+
            +a16320 .ir 
            +s8l    .ir,8
        :++
    }
    
    +ix256 .zs,MAP,Yk
    
    ldy Yk
    ldx PA,y
    lda RA,y
    sta.e+1
    
    txa
    +md4
    
    tay
    lda mask0,y
    sta <(M)
    
    dex
    :- 
        inx
        lda #0
.zs=*+1
        sta $FFFF,x
        lsr <(M)
        bcc +
            lda #CLEARCOL 
.tf=*+1:    ldy $FFFF,x
.if=*+1:    sta 0,y            
            ldy #CLEARCOL 
.tr=*+1:    ldy $FFFF,x
.ir=*+1:    sta 0,y
            lda #8
            sta <(M)
        :+
.e:     cpx #0:bne -
    rts
}
