;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {

CRY_OLD:        !byte CRY0
.cdelay:        !byte 0
.rdelay:        !byte 0
.ccounter:      !byte $80
.temp:          !word 0
.time:          !word 0
.lasttime:      !word $ffff
.elapsed:       !byte 0,0,0,0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
read_clock:
    lda CIA1_TA+1 : sta .time+1
    lda CIA1_TA   : sta .time
    lda CIA1_TA+1 
    cmp .time+1
    bne read_clock
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Rollover proof? 
; Accumulate elapsed ticks in uint32
; Compare to TIC_PER_POLL
; If timeout lapsed, poll cursors
; FPS display accumulates elapsed
; Reset elapsed.
poll_cursor:

    ; elapsed += time - lasttime
    ; lasttime = time
    ; if (elapsed<timeout) return
    ; fps_ticker += elapsed
    ; elapsed     = 0
    
    jsr read_clock
    +t16    .lasttime, .temp
    +s16    .temp    , .time
    +a3216  .elapsed , .temp
    +t16    .time    , .lasttime
    
    lda     .elapsed+3 : 
    ora     .elapsed+2 : bne+
    +cmp16l .elapsed,TIC_PER_POLL
    bcs+
        rts
    :+
    +a32     fps_ticker, .elapsed
    +clear32 .elapsed
    
    lda .cdelay
    beq +
        dec .cdelay
        jmp ++
    :+
        +st8 .cdelay,TICDLY ; see make_timer_table.py
        jsr move_dot
        
    :++
    
    
    lda .rdelay
    beq +
        dec .rdelay
        jmp ++
    :+
        +st8 .rdelay,2
        jsr move_dot
        lda pallet_is_rotating
        beq +:jsr rotate_pallet:+
    :++
    
    ; These happen every time; Optimise? 
    jsr poll_keys
    jsr scan_joystick
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Joystick reading 01234 = udlrf
JU = %00000001
JD = %00000010
JL = %00000100
JR = %00001000
JF = %00010000
JOY2 = CIA1_PORTA; Joy2 Port
CIA1_IN  = $00
CIA1_OUT = $ff
CIA1_LO  = $00
CIA1_ROW7_MASK   = $7F ; pull row 7 lo: clamp key line
scan_joystick:
    +st8 CIA1_DDRA,  CIA1_IN
    lda  CIA1_PORTA
    eor  #$FF
    tax
    txa:and #JU :beq+: jsr go_north :+
    txa:and #JD :beq+: jsr go_south :+
    txa:and #JL :beq+: jsr go_west  :+
    txa:and #JR :beq+: jsr go_east  :+
    rts
go_north: lda CRY: cmp #CYMIN+1 : bcc+: dec CRY :+ : rts
go_south: lda CRY: cmp #CYMAX-1 : bcs+: inc CRY :+ : rts
go_west:  lda CRX: sec:sbc#CXMIN:tay: cpy #1   : bcc+ : dey :+ : jmp handle_msb
go_east:  lda CRX: sec:sbc#CXMIN:tay: cpy #255 : bcs+ : iny :+ 
handle_msb:
    tya
    clc : adc #<CXMIN : sta CRX
    lda #CRM
    bcc +       : ora SPMSB : jmp ++
    :+ eor #$FF : and SPMSB : ++
    sta SPMSB
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
get_cursor_position:
    
    +t88 DTY,CRY_OLD
    
    lda DTX
    lsr
    sec
    sbc #CRX0/2
    sta DX
    
    lda DTY
    cmp #CYMAX_PATCH
    bcc+
        lda #CYMAX_PATCH
    :+
    sec
    sbc #CRY0
    clc
    adc #H2-128
    sta DY
    
    rts


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Cursor trails
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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




































































