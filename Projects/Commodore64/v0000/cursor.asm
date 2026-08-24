
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; Master Sprite Configuration
SPRITE_NUM   = 0                        ; Sprite slot (0-7) for cursor

; Specific to my program
REG_CURX       = REG_SPRITE0_X
REG_CURY       = REG_SPRITE0_Y
REG_SPRITE_COLOR   = REG_SPRITE0_COLOR

; Derived Sprite Bitmasks and Hardware Offsets
SPRITE_MASK  = 1 << SPRITE_NUM          ; %00000001 for 0, %00000010 for 1, etc.
CURX  = $d000 + (SPRITE_NUM * 2) ; Base X register for selected sprite
CURY  = $d001 + (SPRITE_NUM * 2) ; Base Y register for selected sprite
SPR_COLOR = $d027 + SPRITE_NUM       ; Precise color register for selected sprite

TOPGUTTER  = 50
LEFTGUTTER = 24
PXSTART    = GRIDX*8-1
PYSTART    = GRIDY*8
CCENTER    = 10

CURXMIN  = LEFTGUTTER + PXSTART - CCENTER
CURYMIN  = TOPGUTTER  + PYSTART - CCENTER
CURXMAX  = CURXMIN + 8*XBLOCKS-1
CURYMAX  = CURYMIN + 8*YBLOCKS-1
CURX0    = (SCREEN_W>>1) + LEFTGUTTER - CCENTER
CURY0    = (SCREEN_H>>1) + TOPGUTTER  - CCENTER - 4
CURCOLOR = WHITE

VIC_BANK_BASE     = (SCREEN_RAM & $c000)
SPRITE_BLOCK      = 253
SPRITE_STRIDE     = 64
SPRITE_RAM_OFFSET = SPRITE_BLOCK * SPRITE_STRIDE
SPRITE_RAM        = VIC_BANK_BASE + SPRITE_RAM_OFFSET
SPRITE_PTR        = (SPRITE_RAM>>6)&$FF
PAGE_SIZE         = 1024
NSPRITES          = 8
REG_SPRITE_PTR    = SCREEN_RAM + PAGE_SIZE - NSPRITES + SPRITE_NUM

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
init_sprite
    ldx #0
-   lda sprite_data,x
    sta SPRITE_RAM,x
    inx
    cpx #SPRITE_STRIDE
    bne -
    +setreg  REG_SPRITE_PTR,   SPRITE_PTR
    +setreg  CURX,      CURX0
    +setreg  CURY,      CURY0
    +setreg  SPR_COLOR,     CURCOLOR
    +orreg   REG_SPRITE_ENABLE,SPRITE_MASK
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; Joystick reading
JOY2 = REG_CIA1_PORTA                   ; Joystick 2 Port
CIA1_INPUT_MODE  = $00                  ; Configure all pins as inputs
CIA1_OUTPUT_MODE = $ff                  ; Configure all pins as outputs
CIA1_ROW7_MASK   = %01111111            ; $7F: Pull row 7 low to clamp keyboard lines
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================

; PAL Systems (Europe / 50Hz)
; Clock Speed: 985,248 Hz (0.985 MHz)
; Tick Duration: 1.015 microseconds (≈ 1.015 × 10⁻⁶ seconds)
; NTSC Systems (North America / 60Hz)
; Clock Speed: 1,022,727 Hz (1.023 MHz)
; Tick Duration: 0.978 microseconds (≈ 0.978 × 10⁻⁶ seconds)
JOY_SPEED_TIK = 1
poll_cursor
    ; Timing ;;;;;;;;;;;;;;;;;;;;;;;;;;;
    lda REG_CIA1_TA
    lda REG_CIA1_TA+1
    and #%10000000
    beq .continue
    rts
.continue:
    lda #$ff
    sta REG_CIA1_TA
    sta REG_CIA1_TA+1
    +setreg REG_CIA1_CRA, %00010001 
    ; This triggers about every 
    ; PAL  33.26 ms
    ; NTSC 32.05 ms
    ; Divide it dow further    
    dec poll_delay : bne .done
    lda #JOY_SPEED_TIK
    sta poll_delay

    ; Tracking ;;;;;;;;;;;;;;;;;;;;;;;;;
    +setreg  REG_CIA1_DDRB,  CIA1_INPUT_MODE   ; Set Keyboard Rows to input stream listener
    +setreg  REG_CIA1_DDRA,  CIA1_OUTPUT_MODE  ; Set Keyboard Columns to active output drivers
    +setreg  REG_CIA1_PORTA, CIA1_ROW7_MASK    ; Clear row 7 line to break matrix crosstalk
    lda REG_CIA1_PORTB:sta _T                  ; Query Port B to catch stable Joystick 2 inputs
    lsr _T:bcs .down
    lda CURY:cmp #CURYMIN:beq .down
    dec CURY
.down
    lsr _T:bcs .left
    lda CURY:cmp #CURYMAX:bcs .left
    inc CURY
.left
    lsr _T:bcs .right
    lda REG_SPRITE_MSB : and #SPRITE_MASK : bne .do_left
    lda CURX    : cmp #CURXMIN : beq .right 
.do_left
    sec:lda CURX:sbc #1:sta CURX
    bcs .right
    +xoreg   REG_SPRITE_MSB,   SPRITE_MASK
.right
    lsr _T:bcs .done
    lda REG_SPRITE_MSB:and #SPRITE_MASK:beq .do_right
    lda CURX:cmp #<(CURXMAX):beq .done
.do_right
    clc:lda CURX:adc #1:sta CURX
    bcc .done
    +xoreg   REG_SPRITE_MSB,   SPRITE_MASK
.done
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; Raw Sprite Artwork Data (24x21 Pixels Crosshair)
; aspect is 1.28?
sprite_data
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00111000,%00000000 ;
    !byte %00000000,%00111000,%00000000 ;
    !byte %00000000,%00111000,%00000000 ;
    !byte %00000001,%11111111,%00000000 ;
    !byte %11111111,%11101111,%11111110 ;
    !byte %00000001,%11111111,%00000000 ;
    !byte %00000000,%00111000,%00000000 ;
    !byte %00000000,%00111000,%00000000 ;
    !byte %00000000,%00111000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00010000,%00000000 ;
    !byte %00000000,%00000000,%00000000 ; hidden row?





