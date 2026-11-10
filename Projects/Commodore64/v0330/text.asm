!source "fontdata.asm"
!source "tabfps.asm"

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
CHWIDTH         = 8
LEFTMARGIN      = CHWIDTH*GRIDX
TEXTLINE        = SCR_W*2
VINSET          = (21-YBLOCKS)>>1
TOP_ROW         = BITMAP_A   + SCR_W*VINSET
BOTTOM_ROW      = BITMAP_A   + SCR_W*(25-2-VINSET-1+(YBLOCKS&1))

EQ_LABEL        = TOP_ROW    + LEFTMARGIN

LACE_LABEL      = BOTTOM_ROW + LEFTMARGIN
LACE_VAL_POS    = LACE_LABEL + 7*CHWIDTH
LOOP_LABEL      = LACE_LABEL   + TEXTLINE
LOOP_VAL_POS    = LACE_VAL_POS + TEXTLINE

RE_LABEL        = BOTTOM_ROW     + SCR_W/2 + 2*CHWIDTH
RE_LABEL_VALUE  = RE_LABEL       + IMAGEQ_PAD_WIDTH
IM_LABEL        = RE_LABEL       + TEXTLINE
IM_LABEL_VALUE  = RE_LABEL_VALUE + TEXTLINE

FPS_LOCATION    = TOP_ROW + SCR_W/2 + 8*6
FPS_LABEL       = TOP_ROW + SCR_W - 8*7

FS_LABEL_HEX    = FPS_LOCATION - 1*8 - 2*8*4
IM_LABEL_HEX    = FS_LABEL_HEX - 3*8
RE_LABEL_HEX    = IM_LABEL_HEX - 3*8

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
DIGIT_0    = 0
DIGIT_1    = 1
DIGIT_2    = 2
DIGIT_3    = 3
DIGIT_4    = 4
DIGIT_5    = 5
DIGIT_6    = 6
DIGIT_7    = 7
DIGIT_8    = 8
DIGIT_9    = 9
DIGIT_A    = $0A
DIGIT_B    = $0B
DIGIT_C    = $0C
DIGIT_D    = $0D
DIGIT_E    = $0E
DIGIT_F    = $0F
MINUS_SIGN = 16
PERIOD     = 17
SPACE      = 18
CHTRUE     = 19
CHFALSE    = DIGIT_F

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

text_screen_ptr     :   !word 0
text_digit_value    :   !word 0
text_temp           :   !word 0
text_byte_value     :   !word 0

;text_screen_ptr     =P
;text_digit_value    =D
;text_temp           =T
;text_byte_value     =B

!zone {
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; This is a uint32 with each byte named
fps_ticker:  !byte 0
frametime:   !byte 0
fps_index:   !byte 0
fps_overf:   !byte 0
; Also a uint32 
.t: !byte 0
    !byte 0
.d: !byte 0
.o: !byte 0
.previous_fps: !byte $ff,$ff,$ff,$0f
.temp: !word 0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Use frametime as index into digits
; # asl must match make_timer_table.py shift
update_frame_duration:
    +t32      fps_ticker,.t
    ;+lirp3132 .previous_fps,.t 
    +asl32 .t
    +asl32 .t
    +asl32 .t
    +asl32 .t
    +asl32 .t
    ; (.d,.o) act as uint16 here
    lda .o
    and #$FC
    beq+
        +st8 .d,$ff
        +st8 .o,$03
    :+
    +st16 .temp,FPS_DIGITS_HI
    +a16  .temp,.d
    +t16  .temp,.hi
    +st16 .temp,FPS_DIGITS_LO
    +a16  .temp,.d
    +t16  .temp,.lo
    +st16 .temp,FPS_DIGITS_MI
    +a16  .temp,.d
    +t16  .temp,.mi
.hi=*+1
    lda   FPS_DIGITS_HI
    sta   text_byte_value
    +st16 text_screen_ptr, FPS_LOCATION
    jsr   print_uint8
    lda #PERIOD
    sta   text_digit_value 
    +st16 text_screen_ptr, FPS_LOCATION + 2*8
    jsr draw_digit 
.lo=*+1
    lda   FPS_DIGITS_LO
    sta   text_byte_value
    +st16 text_screen_ptr, FPS_LOCATION + 8 + 2*8
    jsr   print_uint8
.mi=*+1
    lda   FPS_DIGITS_LO
    sta   text_byte_value
    +st16 text_screen_ptr, FPS_LOCATION + 8 + 2*8 + 2*8
    jsr   print_uint8
    
    ; Clear counter to zero
    +clear32 fps_ticker
rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; DX DY are uint8 for now
; H=??   ~~±1 ==> 64 horizontal px / unit 
; W=128 is ±2 ==> 32 horizontal px / unit
!zone {
update_dx_label:
    lda CRX
    sec
    sbc #CRX0;-1
    sta   text_byte_value
    +st16 text_screen_ptr, RE_LABEL_VALUE
    jsr   print_signed_decimal
rts
}
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
update_dy_label:
    lda #CRY0
    secds
    sbc CRY
    sta   text_byte_value
    +st16 text_screen_ptr, IM_LABEL_VALUE
    jsr   print_signed_decimal
rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone {
print_signed_decimal:
    lda text_byte_value
    bpl+ ; negative
        eor #$ff
        clc
        adc #1
        sta text_byte_value
        lda #MINUS_SIGN
        sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
        jmp print_decimal
    :+
        jsr print_decimal
        lda #DIGIT_0
        sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    rts
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!set oldpc=* :
!zone {
print_decimal:
    lda text_byte_value
    +d64
    sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    lda #PERIOD
    sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    lda text_byte_value
    +md64
    sta text_byte_value
    ldy text_byte_value
    ldx DECIMALS_0,y
    txa:+d16:+md16:sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    txa:     +md16:sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    ldy text_byte_value
    ldx DECIMALS_1,y
    txa:+d16:+md16:sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    txa:     +md16:sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    ldy text_byte_value
    ldx DECIMALS_2,y
    txa:+d16:+md16:sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
    txa:     +md16:sta text_digit_value : jsr draw_digit : +a16l text_screen_ptr,8
rts
}
!if SHOW_SIZES {!warn *-oldpc," print_decimal "}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; text_byte_value: uint8  text_byte_value value 0.256
; text_screen_ptr: uint16 start of graphics block memory
!set oldpc=* :
!zone { 
print_uint8:
    lda     text_byte_value
    +d16
    and     #$0F
    sta     text_digit_value
    jsr     draw_digit
    +a16l   text_screen_ptr,8
    lda     text_byte_value
    and     #$0F
    sta     text_digit_value
    jsr     draw_digit
rts
}
!if SHOW_SIZES {!warn *-oldpc," draw_uint8 "}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; text_screen_ptr: uint16 start of graphics block memory
; text_digit_value: uint8 text_digit_value number 0.9
!set oldpc=*
draw_digit:
!zone {
        +t16    text_screen_ptr,.d
        +t816   text_digit_value,.s
        +asl16  .s
        +asl16  .s
        +asl16  .s
        +asl16  .s
        +a16l   .s,DIGITS
        ldy #0
        :-
.s=*+1:     lda $FFFF
.d=*+1:     sta $FFFF
            +inc16 .s
            +inc16 .d
        iny
        cpy #8
        bne-
        +t16    .s,text_temp
}
!zone {hmm
        +t16    text_temp, .s
        +t16    text_screen_ptr, .d
        +a16320 .d
        ldy #0
        :-
.s=*+1:     lda $FFFF
.d=*+1:     sta $FFFF
            +inc16 .s
            +inc16 .d
        iny
        cpy #8
        bne-
}
rts
!if SHOW_SIZES {!warn *-oldpc," draw_label "}



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro print8 .where {
    sta   text_byte_value
    +st16 text_screen_ptr, .where
    jsr   print_uint8  
}


























