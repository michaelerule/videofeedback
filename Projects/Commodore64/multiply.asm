;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;________________________________________
; subroutine: mul16
; inputs: 
;     .x = multiplicand zp address
;     .y = multiplier zp address
; outputs: 
;     XY = 16-bit product
; notes: non-destructive to parent arguments
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;________________________________________
mul16:
    lda $00,x:sta <_lhs:lda $01,x:sta <_lhs+1   ; copy *x to internal _lhs
    lda $00,y:sta <_rhs:lda $01,y:sta <_rhs+1   ; copy *y to internal _rhs
    lda #0:sta <XY:sta <XY+1            ; XY = 0
    lda #16:sta <ctr                    ; ctr = 16
-
    lsr <_rhs+1:ror <_rhs               ; _rhs >>= 1
    bcc +                               ; if (carry)
    clc:lda <XY:adc <_lhs:sta <XY       ;     XY += _lhs (lo byte)
    lda <XY+1:adc <_lhs+1:sta <XY+1     ;     XY += _lhs (hi byte)
+
    asl <_lhs:rol <_lhs+1               ; _lhs <<= 1
    dec <ctr:bne -                      ; while (--ctr)
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;________________________________________
; subroutine: mul8to16
; inputs: 
;     .x = multiplicand zp address (8 bit)
;     .y = multiplier zp address   (8 bit)
; outputs: 
;     XY = 16-bit product
; notes: non-destructive to parent arguments
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;________________________________________
mul8to16:
    lda $00,x:sta <xtmp:lda #0:sta <xtmp+1      ; copy 8-bit x to 16-bit temp space
    lda $00,y:sta <ytmp                 ; copy 8-bit y to internal ytmp
    sta <XY:sta <XY+1                   ; XY = 0
    lda #8:sta <ctr                     ; ctr = 8
-
    lsr <ytmp                           ; ytmp >>= 1
    bcc +                               ; if (carry)
    clc:lda <XY:adc <xtmp:sta <XY       ;     XY += *x (lo byte)
    lda <XY+1:adc <xtmp+1:sta <XY+1     ;     XY += *x (hi byte)
+
    asl <xtmp:rol <xtmp+1               ; *x <<= 1
    dec <ctr:bne -                      ; while (--ctr)
    rts

