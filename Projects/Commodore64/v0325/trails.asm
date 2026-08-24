

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; 3 bit (x8) fixed point (7,1) moving average
; Calculated as d*8+(c-d)
; With c-d *rounded outwards* for convergence
!macro lirp71 .d,.c {
    +s16   .c,.d
    lda    .c+1:bmi+:+a16l.c,7:+
    +asl16 .d
    +asl16 .d
    +asl16 .d
    +a16   .d,.c
    +lsr16 .d
    +lsr16 .d
    +lsr16 .d
    +t16   .d,.c
}

!macro lirp31 .d,.c {
    +s16   .c,.d
    lda    .c+1:bmi+:+a16l.c,3:+
    +asl16 .d
    +asl16 .d
    +a16   .d,.c
    +lsr16 .d
    +lsr16 .d
    +t16   .d,.c
}

!macro ema .d,.t {
    +lirp71 .d,.t
    ;+lirp31 .d,.t
}

!zone {
.dx6: !word DX0
.dy6: !word DY0
.dx5: !word DX0
.dy5: !word DY0
.dx4: !word DX0
.dy4: !word DY0
.dx3: !word DX0
.dy3: !word DY0
.dx2: !word DX0
.dy2: !word DY0
.dx1: !word DX0
.dy1: !word DY0
.dx0: !word DX0
.dy0: !word DY0
.temp:!word 0

DPREC = 2
DX0   = W <<DPREC
DY0   = H2<<DPREC
!macro shiftout .t {!for iter,1,DPREC {+lsr16 .t}}
!macro shiftin  .t {!for iter,1,DPREC {+asl16 .t}}

!macro store_cy .d,.t,.p {
    +t16     .d,.t
    +shiftout .t
    +a16l    .t,CYMIN
    lda      .t
    sta      .p
}

!macro store_cx .d,.t,.p,m {
    +t16     .d,.t
    +shiftout .t
    +st8     .t+1,0
    +a16l    .t,CXMIN
    ldx      .t
    lda      #m
    ldy      .t+1
    beq +
        ora SPMSB
        jmp ++
    :+
        eor #$FF
        and SPMSB
    :++
    stx .p
    sta SPMSB
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
move_dot: 
    
    +t816  CRY,.temp
    +s16l  .temp,CYMIN
    +shiftin .temp
    +ema .dy6,.temp
    +ema .dy5,.temp
    +ema .dy4,.temp
    +ema .dy3,.temp
    +ema .dy2,.temp
    +ema .dy1,.temp
    +ema .dy0,.temp
    
    ; 8 bit wrap on x avoids MSB load
    +t816  CRX,.temp
    +s8l   .temp,CXMIN
    +shiftin .temp
    +ema .dx6,.temp
    +ema .dx5,.temp
    +ema .dx4,.temp
    +ema .dx3,.temp
    +ema .dx2,.temp
    +ema .dx1,.temp
    +ema .dx0,.temp
    
    +store_cy .dy6,.temp,D6Y
    +store_cy .dy5,.temp,D5Y
    +store_cy .dy4,.temp,D4Y
    +store_cy .dy3,.temp,D3Y
    +store_cy .dy2,.temp,D2Y
    +store_cy .dy1,.temp,D1Y
    +store_cy .dy0,.temp,DTY
    
    +store_cx .dx6,.temp,D6X,D6M
    +store_cx .dx5,.temp,D5X,D5M
    +store_cx .dx4,.temp,D4X,D4M
    +store_cx .dx3,.temp,D3X,D3M
    +store_cx .dx2,.temp,D2X,D2M
    +store_cx .dx1,.temp,D1X,D1M
    +store_cx .dx0,.temp,DTX,DTM
    
    rts
}



















































