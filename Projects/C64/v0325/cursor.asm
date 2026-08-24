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
get_new_cursor_position:
    
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



















