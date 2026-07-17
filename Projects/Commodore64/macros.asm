;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; ACME assembler macros
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; uint16 loading, copying on zero page

; *dst=0xABCD
!macro set16 .to,.v {lda#<.v:sta.to:lda#>.v:sta.to+1}

; Copying 
!macro cp16to16 .x, .to {lda.x:sta.to:lda.x+1:sta.to+1}
!macro cp8to8   .x, .to {lda.x:sta.to}
!macro cp8to16  .x, .to {lda.x:sta.to:lda#0:sta.to+1}

; *dst=tab[x]
!macro load16 .to,.tab {lda.tab,x:sta.to:lda.tab+1,x:sta.to+1}

; *uint16 shifts
!macro asl16 .to {asl.to:rol.to+1}
!macro lsr16 .to {lsr.to+1:ror.to}
!macro asr16 .to {lda<.to+1:cmp#$80:ror<.to+1:ror<.to}

!macro lsr16_by_X.p {cpx#0:beq +:-:lsr<.p+1:ror<.p:dex:bne -:+}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; uint16 math on zero page

; 16 bit increment, decrement on zero page
!macro inc16 .p {inc.p:bne +:inc.p+1:+}
!macro dec16 .p {lda.p:bne +:dec.p+1:+ dec.p}

; ±= uint16 literal with *uint16
!macro iadd16lit .p, .v {clc:lda<.p:adc#<.v:sta<.p:lda<.p+1:adc#>.v:sta<.p+1}
!macro isub16lit .p, .v {sec:lda<.p:sbc#<.v:sta<.p:lda<.p+1:sbc#>.v:sta<.p+1}

; ±= uint8 in register A with *uint16
!macro iaddA16 .to {clc:adc.to:sta.to:bcc +:inc.to+1:clc:+}
!macro isubA16 .to {sta _T:lda.to:sec:sbc _T:sta.to:bcs +:dec.to+1:+}

; *uint16 ±= *uint16 
!macro iadd1616 .to, .x {clc:lda.to:adc.x:sta.to:lda.to+1:adc.x+1:sta.to+1}
!macro isub1616 .to, .x {sec:lda.to:sbc.x:sta.to:lda.to+1:sbc.x+1:sta.to+1}

; *uint16 ±= *uint8
!macro iadd168 .to, .x {clc:lda.to:adc.x:sta.to:bcc +:inc.to+1:+}

; requires multiply.asm
!macro mul16to16 .to, .x1, .x2 {ldx#.x1:ldy#.x2:jsr mul16:+copy16to16 XY,.to}


!macro add8 .p {clc:adc<.p}
!macro add  .v {clc:adc#.v}
!macro set  .p,.v {lda#.v:sta<.p}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Flow control
!macro do .p,.v {lda#.v:sta<.p}
!macro while_inc .p,.limit,.label {inc<.p:lda<.p:cmp#.limit:beq +:jmp.label:+}
!macro while_dec .p,.label {dec<.p:bne.label}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Register setting helpers
!macro setreg .r, .v { lda#.v : sta .r }
!macro orreg  .r, .v { lda .r : ora #.v : sta .r }
!macro andreg .r, .v { lda .r : and #.v : sta .r }
!macro xoreg  .r, .v { lda .r : eor #.v : sta .r } 


