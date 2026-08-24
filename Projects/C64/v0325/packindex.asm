;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Pack +c into map x table
!zone { 
:prep_x:
        +ix128 T, MAPX, Yk
        +t16   T,.gm
        +t16   T,.sm
        +t88   DX, .Dm
        
        ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
        ; MA: first good pixel (static)
        ; MB: last  good pixel (static)
        ; RM: 1st non-mirrored pixel    
        ; Skip if nothing mirrored
        ; Else .em=RM-1
        
        ldy Yk
        ldx MA,y : dex
        lda RM,y
        beq .nomir
        ;-------------------------------
        sta .em
        dec .em
        :- 
            inx
.gm=*+1:    lda $FFFF,x
            clc
.Dm=*+1:    adc #0
            and #$7F
            eor #$7F
.sm=*+1:    sta $FFFF,x
.em=*+1:    cpx #0
        bne-
        ;-------------------------------
        cpx #W1 : beq .fin
        ;-------------------------------
.nomir:
        +t16 T ,.gn
        +t16 T ,.sn
        +t88 DX,.Dn
        ldy Yk : lda MB,y : sta .en
        :- inx
.gn=*+1:    lda $FFFF,x
            clc
.Dn=*+1:    adc #0
            and #$7F
.sn=*+1:    sta $FFFF,x
.en=*+1:    cpx #0
        bne-

.fin:   rts
        ;-------------------------------
}
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Unpack +c from map x table
!zone { 
:post_x:
        +t88   DX, .Dm
        
        +ix128 T, MAPX, Yk
        +t16   T,.gm
        +t16   T,.sm
        
        ldy Yk
        ldx MA,y : dex
        lda RM,y
        beq .nomir
        ;-------------------------------
        sta .em
        dec .em
        :- 
            inx
.gm=*+1:    lda $FFFF,x
            eor #$7F
            sec
.Dm=*+1:    sbc #0 
            and #$7F
.sm=*+1:    sta $FFFF,x
.em=*+1:    cpx #0
        bne-
        ;-------------------------------
        cpx #W1 : beq .fin
        ;-------------------------------
.nomir:
        +t16 T ,.gn
        +t16 T ,.sn
        +t88 DX,.Dn
        ldy Yk : lda MB,y : sta .en
        :- 
            inx
.gn=*+1:    lda $FFFF,x
            sec
.Dn=*+1:    sbc #0 
            and #$7F
.sn=*+1:    sta $FFFF,x
.en=*+1:    cpx #0
        bne-
.fin:   rts
        ;-------------------------------
}



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {   
; stable human-checked reference code
xor_x_row:
        +ix128 T, MAPX, Yk
        +t16   T,.g
        +t16   T,.s
        ldy Yk : lda PM,y : cmp RM,y : beq .f : bcc +
            ldx RM,y
            sta .e
            jmp .loop
        :+
            tax
            lda RM,y
            sta .e
        :.loop
.g=*+1:     lda $FFFF,x : eor #$7f
.s=*+1:     sta $FFFF,x
            inx
.e=*+1:     cpx #$00
        bne .loop
.f:     rts
}




!if 0 {
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; repacking x is expensive; the 
; only thing that changed is mirrors. 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone { 
__prep_x:
        +ix128 T, MAPX, Yk
        +t16   T,.g
        +t16   T,.s
        ldy Yk
        ldx MA,y : dex
        lda RM,y : beq .f : sta .e : dec .e
        :-: inx
.g=*+1:     lda $FFFF,x : eor #$7F
.s=*+1:     sta $FFFF,x
.e=*+1:     cpx #0 : bne-
.f      rts
}
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone { 
__post_x:
        +ix128 T, MAPX, Yk
        +t16   T,.g
        +t16   T,.s
        ldy Yk
        ldx MA,y : dex
        lda RM,y : beq .f : sta .e : dec .e
        :-: inx
.g=*+1:     lda $FFFF,x : eor #$7F
.s=*+1:     sta $FFFF,x
.e=*+1:     cpx #0 : bne-
.f      rts
}
}



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Pack +c into map y table
!zone { 
prep_y:
        +t88   DY,.Dm
        +t88   DY,.Dn
        
        +ix256 T,MAPY2,Yk
        +t16   T,.gm
        +t16   T,.gn
        +t16   T,.sm
        +t16   T,.sn
        
        ldy Yk
        ldx RA,y : dex 
        
        lda RB,y
        sta .en

        lda RM,y
        beq .nomir
        ;-------------------------------
        sta .em
        dec .em
        :- 
            inx
.gm=*+1:    lda $FFFF,x
            clc
.Dm=*+1:    adc #0
            eor #$ff
            clc
            adc #H2 + (>MAP)
.sm=*+1:    sta $FFFF,x
.em=*+1:    cpx #0
        bne-
        ;-------------------------------
        cpx #W1 : beq .fin
        ;-------------------------------
.nomir:
        :- 
            inx
.gn=*+1:    lda $FFFF,x
            clc
.Dn=*+1:    adc #0
            sec
            sbc #H2-(>MAP)
.sn=*+1:    sta $FFFF,x
.en=*+1:    cpx #0
        bne-
.fin:   rts
}


        
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Unack +c from map y table
!zone { 
post_y:
        +ix256 T,MAPY2,Yk
        +t16   T,.lm
        +t16   T,.ln
        +t16   T,.sm
        +t16   T,.sn
        
        lda #((>MAP)+H2)-1                  ; +C shift for the mirrored section
        sec : sbc DY : sta .am
        lda #H2-(>MAP)                      ; +C shift for the non mirrored section
        sec : sbc DY : sta .an
        
        ldy Yk 
        ldx RA,y : dex                      ; Load start column into x register
        lda RB,y                            ; Set end counters
        sta .em 
        sta .en 
        
        ldy Yk
        lda RM,y
        beq .nomir
        ;-------------------------------
        sec
        sbc #1
        sta .em
        :- 
            inx
.lm=*+1:    lda $FFFF,x                     ; ((carry 0))
            sec
            eor #$ff
.am=*+1:    adc #0                          ; ((carry 1))
.sm=*+1:    sta $FFFF,x
.em=*+1:    cpx #0 
        bne-
        ;-------------------------------
        cpx #W1 : beq .fin
        ;-------------------------------
.nomir:
        :-                                  ; ((carry 0))
            clc
            inx
.ln=*+1:    lda $FFFF,x
            clc
.an=*+1:    adc #0                          ; ((carry 0))
.sn=*+1:    sta $FFFF,x
.en=*+1:    cpx #0 
        bne-
.fin:  rts
        ;-------------------------------
}









