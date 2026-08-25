;;;; Remember, these aren't opcodes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
asr
lsl
asr

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; uint8 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro t88   .x,.p {lda.x:sta.p} ; Pick one? 
!macro t8    .x,.p {lda.x:sta.p}
!macro t8zp  .x,.p {lda<(.x):sta<(.p)}

!macro st8   .p,.v {lda #.v:sta .p}
!macro st8zp .p,.v {lda #.v:sta <(.p)}

!macro as  .p    {adc.p  :sta.p  }
!macro as1 .p    {adc.p+1:sta.p+1}
!macro a8  .p    {clc:adc.p}
!macro aA8 .p    {clc:adc.p:sta.p}
!macro sA8 .p    {eor#$ff:sec:adc.p:sta.p}
!macro a8l .p,.v {clc:lda.p:adc#.v:sta.p}
!macro s8l .p,.v {sec:lda.p:sbc#.v:sta.p}
!macro a88 .p,.v {clc:lda.p:adc.v:sta.p}
!macro s88 .p,.v {sec:lda.p:sbc.v:sta.p}

!macro t8y8 .x,.p {lda.x,y:sta.p}

!macro d2    {lsr}
!macro d4    {lsr:lsr}
!macro d8    {lsr:lsr:lsr}
!macro d16   {lsr:lsr:lsr:lsr}
!macro d32   {lsr:lsr:lsr:lsr:lsr}
!macro d64   {lsr:lsr:lsr:lsr:lsr:lsr}
!macro d128  {lsr:lsr:lsr:lsr:lsr:lsr:lsr}

!macro m2    {asl}
!macro m4    {asl:asl}
!macro m8    {asl:asl:asl}
!macro m16   {asl:asl:asl:asl}
!macro m32   {asl:asl:asl:asl:asl}
!macro m64   {asl:asl:asl:asl:asl:asl}
!macro m128  {asl:asl:asl:asl:asl:asl:asl}

!macro not   {eor#$ff}
!macro md2   {and#$01}
!macro md4   {and#$03}
!macro md8   {and#$07}
!macro md16  {and#$0f}
!macro md32  {and#$1f}
!macro md64  {and#$3f}
!macro md128 {and#$7f}

!macro nibble_lo {and #$0F}
!macro nibble_hi2lo {+d16}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; uint16 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

!macro st16    .p,.v {lda #<.v : sta.p : lda #>.v : sta.p+1}
!macro t16     .x,.p {lda.x:sta.p:lda.x+1:sta.p+1}
!macro tA16    .p    {sta  .p :lda#0:sta  .p+1 }
!macro tA16zp  .p    {sta<(.p):lda#0:sta<(.p+1)}
!macro t816    .x,.p {lda.x:+tA16.p}

;!macro ld16   .p,.t {lda.t,x:sta.p:lda.t+1,x:sta.p+1}

!macro a16l    .p,.v {clc:lda .p:adc#<.v:sta .p:lda .p+1:adc#>.v:sta .p+1}
!macro a16lzp  .p,.v {clc:lda<.p:adc#<.v:sta<.p:lda<.p+1:adc#>.v:sta<.p+1}
!macro s16l    .p,.v {sec:lda .p:sbc#<.v:sta .p:lda .p+1:sbc#>.v:sta .p+1}
!macro s16lzp  .p,.v {sec:lda<.p:sbc#<.v:sta<.p:lda<.p+1:sbc#>.v:sta<.p+1}

!macro sl16    .v,.p {sec:lda#<.v:sbc .p:sta .p:lda#>.v:sbc .p+1:sta .p+1}
!macro a16     .p,.x {clc:lda.p:adc.x:sta.p:lda.p+1:adc.x+1:sta.p+1}
!macro a168    .p,.x {clc:lda.p:adc.x:sta.p:bcc +:inc.p+1:+}
!macro s16     .p,.x {sec:lda.p:sbc.x:sta.p:lda.p+1:sbc.x+1:sta.p+1}
!macro s168    .p,.x {sec:lda.p:sbc.x:sta.p:lda.p+1:sbc#0  :sta.p+1}

!macro cmp16l  .p,.v {lda .p:cmp #<(.v):lda .p+1:sbc #>(.v)}

!macro asl16   .p {asl.p:rol.p+1}
!macro asl16zp .p {asl<(.p):rol<(.p+1)}
!macro lsr16   .p {lsr.p+1:ror.p}
!macro asr16   .p {lda<.p+1:cmp#$80:ror<.p+1:ror<.p}
!macro inc16   .p {inc.p:bne +:inc.p+1:+}
!macro dec16   .p {lda.p:bne +:dec.p+1:+ dec.p}
!macro aA16    .p {clc:adc.p:sta.p:bcc +:inc.p+1:clc:+}
!macro sA16    .p {sta T:lda.p:sec:sbc T:sta.p:bcs +:dec.p+1:+}
!macro a16xy   .p {clc:tya:adc.p:sta.p:txa:adc.p+1:sta.p+1}
!macro a16yx   .p {clc:txa:adc.p:sta.p:tya:adc.p+1:sta.p+1} 
!macro a16xa   .p {clc:   :adc.p:sta.p:txa:adc.p+1:sta.p+1}
!macro a16ya   .p {clc:   :adc.p:sta.p:tya:adc.p+1:sta.p+1} 

!macro a16320  .p {lda.p:clc:adc#$40:sta.p:lda.p+1:adc#1:sta.p+1}
!macro a16312  .p {clc:lda.p:adc#$38:sta.p:lda.p+1:adc#1:sta.p+1}
!macro s16320  .p {lda.p:sec:sbc#$40:sta.p:lda.p+1:sbc#1:sta.p+1}
!macro a16x128 .p {txa:and#1:ror:clc:+as.p:txa:+d2:adc.p+1:sta.p+1}
!macro s16x128 .p {txa:and#1:ror:+not:sec:+as.p:txa:+d2:+not:sec:adc.p+1:sta.p+1}

!macro a16x320 .p {
    txa:+md4:+m64:clc:+as.p
    txa:+as.p+1
    txa:+d4:clc:+as.p+1
}
!macro s16x320 .p {
    txa:+md4:+m64:+not:sec:+as.p
    txa:+not:sec:sbc#0:+as1.p
    txa:+d4:+not:sec:+as1.p
}
!macro inc16zp .p {
    inc <(.p)
    bne +
    inc <(.p+1)
    :+
}
!macro dec16zp .p {
    lda <(.p)
    bne +
    dec <(.p+1)
+   dec <(.p)
}


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; uint32 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

!macro clear32 .p    {lda #0:sta .p:sta .p+1:sta .p+2:sta .p+3}
!macro st32    .p,.v {lda #<(.v):sta .p:lda #>(.v):sta .p+1:lda #<((.v)>>16):sta .p+2:lda #>((.v)>>16):sta .p+3}

!macro t32     .x,.p {lda .x:sta .p:lda .x+1:sta .p+1:lda .x+2:sta .p+2:lda .x+3:sta .p+3}
!macro t1632   .x,.p {lda .x:sta .p:lda .x+1:sta .p+1:lda #0:sta .p+2:sta .p+3}
!macro t832    .x,.p {lda .x:sta .p:lda #0:sta .p+1:sta .p+2:sta .p+3}

!macro asl32   .p    {asl .p:rol .p+1:rol .p+2:rol .p+3}
!macro lsr32   .p    {lsr .p+3:ror .p+2:ror .p+1:ror .p}
!macro asr32   .p    {lda <.p+3:cmp #$80:ror <.p+3:ror <.p+2:ror <.p+1:ror <.p}

!macro inc32   .p    {inc .p:bne +:inc .p+1:bne +:inc .p+2:bne +:inc .p+3:+}
;!macro dec32   .p    {lda .p:bne +:lda .p+1:bne ++:lda .p+2:bne +++:dec .p+3:+++ dec .p+2:++ dec .p+1:+ dec .p}

!macro cmp32l  .p,.v {lda .p:cmp #<(.v):lda .p+1:sbc #>(.v):lda .p+2:sbc #<((.v)>>16):lda .p+3:sbc #>((.v)>>16)}

!macro a32 .d, .s {
    clc
    lda .d+0 : adc .s+0 : sta .d+0
    lda .d+1 : adc .s+1 : sta .d+1
    lda .d+2 : adc .s+2 : sta .d+2
    lda .d+3 : adc .s+3 : sta .d+3
}
!macro s32 .d, .s {
    sec
    lda .d+0 : sbc .s+0 : sta .d+0
    lda .d+1 : sbc .s+1 : sta .d+1
    lda .d+2 : sbc .s+2 : sta .d+2
    lda .d+3 : sbc .s+3 : sta .d+3
}
!macro a32l .p, .v {
    clc
    lda .p+0 : adc #<(.v)       : sta .p+0
    lda .p+1 : adc #>(.v)       : sta .p+1
    lda .p+2 : adc #<((.v)>>16) : sta .p+2
    lda .p+3 : adc #>((.v)>>16) : sta .p+3
}


!macro a3216 .d32, .s16 {
    clc
    lda .d32
    adc .s16
    sta .d32
    lda .d32+1
    adc .s16+1
    sta .d32+1
    bcc .done
        inc .d32+2
        bne .done
        inc .d32+3
    :.done
}



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; self-modify ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro smp  .x,.p { lda .x   : sta .p+1 : lda .x+1 : sta .p+2 }
!macro smpl .p,.v { lda #<.v : sta .p+1 : lda #>.v : sta .p+2 }
!macro sm8  .x,.p { lda .x   : sta .p+1 }
!macro smA  .p    { sta .p+1 }
!macro tsmp .x,.p {lda.x+1:sta.p+1:lda.x+2:sta.p+2}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; register ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro setreg .r, .v { lda #.v : sta .r }
!macro orreg  .r, .v { lda .r : ora #.v : sta .r }
!macro andreg .r, .v { lda .r : and #.v : sta .r }
!macro xoreg  .r, .v { lda .r : eor #.v : sta .r } 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; tables ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro ltya .p     {lda .p,y}
!macro lty  .p,.to {lda .p,y:sta .to}
!macro ltay .p,.to {tay:lda .p,y:sta .to}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; just for perceptron ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro ix128 .p,.t,.i {+st16 .p,.t:lda .i:+d2:tax:lda#0:ror:tay:+a16xy .p} 
!macro ix256    .p,.t,.i  {+st16    .p,.t : +a88  .p+1,.i } 
!macro ix168    .p,.t,.i  {+st16    .p,.t : +a168 .p  ,.i }
!macro st16tabLHyzp .l,.h,.p {
    lda .l,y : sta <.p 
    lda .h,y : sta <.p+1
}


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Zero page variants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro a16xyzp .p    {clc:tya:adc <(.p):sta <(.p):txa:adc <(.p+1):sta <(.p+1)}
!macro st16zp  .p,.v {lda#<.v:sta<(.p):lda#>.v:sta<(.p+1)}
!macro t16zp   .x,.p {lda <(.x):sta <(.p):lda <(.x+1):sta <(.p+1)}
!macro t16zphm .x,.p {lda <(.x):sta .p:lda <(.x+1):sta .p+1}
!macro ix128zp .p,.t,.i {+st16zp .p,.t:lda .i:+d2:tax:lda#0:ror:tay:+a16xyzp .p} 
!macro ix256zp .p,.t,.i {+st16zp .p,.t:lda <(.p+1):clc:adc   .i:sta    <(.p+1)} 



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Used for averaging frame rate
!macro lirp7132 .d,.c {
    +s32   .c,.d
    lda    .c+3:bmi+:+a32l.c,7:+
    +asl32 .d
    +asl32 .d
    +asl32 .d
    +a32   .d,.c
    +lsr32 .d
    +lsr32 .d
    +lsr32 .d
    +t32   .d,.c
}
!macro lirp3132 .d,.c {
    +s32   .c,.d
    lda    .c+3
    bmi+
        +a32l.c,3
    :+
    +asl32 .d
    +asl32 .d
    +a32   .d,.c
    +lsr32 .d
    +lsr32 .d
    +t32   .d,.c
}








!macro t8y .s,.d {lda .s,y:sta .d,y}


























