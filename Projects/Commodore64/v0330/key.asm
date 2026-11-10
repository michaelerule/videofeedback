;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!if 0 {

Keyboard is an 8×8 grid, 64 possible button presses. 
CIA1 chip has 2 memmapped 8 bit ports (register.asm)

    CIA1_PORTA = $dc00 ; Joystick 2 / Keyboard cols
    CIA1_PORTB = $dc01 ; Joystick 1 / Keyboard rows
    CIA1_DDRA  = $dc02 ; DDRA
    CIA1_DDRB  = $dc03 ; DDRB

    DDR=0(1) is input(output)

PORTA is active low:

    0 pulls down PORTB if K pressed
    0xFF is [OFF] (LoL)

Joystick 2 controls the low 5 bits of PORTA:

    PORTA lines internally pulled UP (1)
    Joy2 actions pull LOW (0)

      |B0 |B1 |B2 |B3 |B4 |B5 |B6 |B7 | Joy2
    --|---|---|---|---|---|---|---|---|-----
    A0| Dl| Rt|CSh| F7| F1| F3| F5|CSv| Up
    A1| 3 | W | A | 4 | Z | S | E |SHl| Down
    A2| 5 | R | D | 6 | C | F | T | X | Left
    A3| 7 | Y | G | 8 | B | H | U | V | Right
    A4| 9 | I | J | 0 | M | K | O | N | Fire
    A5| + | P | L | - | . | : | @ | , |
    A6| £ | * | ; |Hom|SHr| = | ↑ | / |
    A7| 1 | ← |Ctl| 2 |Spc|c64| Q | Run/Stop
 
       |A0 |A1 |A2 |A3 |A4 |A5 |A6 | A7 
    ---|---|---|---|---|---|---|---|----
    B0 | Dl| 3 | 5 | 7 | 9 | + | £ | 1  
    B1 | Rt| W | R | Y | I | P | * | ←  
    B2 |CSh| A | D | G | J | L | ; | Ctl
    B3 | F7| 4 | 6 | 8 | 0 | - |Hom| 2  
    B4 | F1| Z | C | B | M | . |SHr|Spc 
    
    B5 | F3| S | F | H | K | : | = |c64 
    B6 | F5| E | T | U | O | @ | ↑ | Q  
    B7 |CSv|SH | X | V | N | , | / | Run/Stop

    CSh = Curosor left/right
    CSv = Curosor Up/Down 
    SHl = Left Shift
    Shr = Right Shift

These Ks can be scanned while using Joy2:

      |B0 |B1 |B2 |B3 |B4 |B5 |B6 |B7 
    --|---|---|---|---|---|---|---|---
    A5| + | P | L | - | . | : | @ | , 
    A6| £ | * | ; |Hom|SHr| = | ↑ | / 
    A7| 1 | ← |Ctl| 2 |Spc|c64| Q | Run/Stop

These Ks can be scanned while using Joy1:

       |A0 |A1 |A2 |A3 |A4 |A5 |A6 | A7 
    ---|---|---|---|---|---|---|---|----
    B5 | F3| S | F | H | K | : | = |c64 
    B6 | F5| E | T | U | O | @ | ↑ | Q  
    B7 |CSv|SH | X | V | N | , | / | Run/Stop
    
These Ks can be scanned while using Both:

      |B5 |B6 |B7 
    --|---|---|---
    A5| : | @ | , 
    A6| = | ↑ | / 
    A7|c64| Q | Run/Stop

Left/right paddle buttons use bits 5,6. Run/Stop
always triggers a non-maskable interrupt, and 
cannot be handled like a normal K. So with two
joysticks with paddle buttons, we have no Ks. 

In theory we can detect the impact of joysticks.
To read we pull a line low, and look for pulled
down lines on the other port from closed Ks.

The joysticks and paddles will pull down lines
on their own. 

Joystick 2 on port A

Any of 0b00011111 may be pulled low
and of 0b01111111 if paddles are also connected

If our toggling a bit does not change read value
can we infer that line is being pulled low?
}
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; These are the lines of A we must pull low
; In order to read the K
    
KA_DEL      = 1<<0
KA_3        = 1<<1
KA_5        = 1<<2
KA_7        = 1<<3
KA_9        = 1<<4
KA_PLUS     = 1<<5
KA_POUND    = 1<<6
KA_1        = 1<<7

KA_RET      = 1<<0
KA_W        = 1<<1
KA_R        = 1<<2
KA_Y        = 1<<3
KA_I        = 1<<4
KA_P        = 1<<5
KA_ASTERISK = 1<<6
KA_LEFTARR  = 1<<7

KA_CSLR     = 1<<0
KA_A        = 1<<1
KA_D        = 1<<2
KA_G        = 1<<3
KA_J        = 1<<4
KA_L        = 1<<5
KA_SEMICOL  = 1<<6
KA_CTRL     = 1<<7

KA_F7       = 1<<0
KA_4        = 1<<1
KA_6        = 1<<2
KA_8        = 1<<3
KA_0        = 1<<4
KA_HYPHEN   = 1<<5
KA_HOME     = 1<<6
KA_2        = 1<<7

KA_F1       = 1<<0
KA_Z        = 1<<1
KA_C        = 1<<2
KA_B        = 1<<3
KA_M        = 1<<4
KA_PERIOD   = 1<<5
KA_SHR      = 1<<6
KA_SPACE    = 1<<7

KA_F3       = 1<<0
KA_S        = 1<<1
KA_F        = 1<<2
KA_H        = 1<<3
KA_K        = 1<<4
KA_COLON    = 1<<5
KA_EQUAL    = 1<<6
KA_C64      = 1<<7

KA_F5       = 1<<0
KA_E        = 1<<1
KA_T        = 1<<2
KA_U        = 1<<3
KA_O        = 1<<4
KA_AT       = 1<<5
KA_UPARR    = 1<<6
KA_Q        = 1<<7

KA_CSUD     = 1<<0
KA_SHL      = 1<<1
KA_X        = 1<<2
KA_V        = 1<<3
KA_N        = 1<<4
KA_COMMA    = 1<<5
KA_SLASH    = 1<<6
KA_RUNSTOP  = 1<<7

; When we scan a line of A
; These are the bits in B we need to read

KB_DL       = 1<<0
KB_RT       = 1<<1
KB_CSH      = 1<<2
KB_F7       = 1<<3
KB_F1       = 1<<4
KB_F3       = 1<<5
KB_F5       = 1<<6
KB_CSV      = 1<<7

KB_3        = 1<<0
KB_W        = 1<<1
KB_A        = 1<<2
KB_4        = 1<<3
KB_Z        = 1<<4
KB_S        = 1<<5
KB_E        = 1<<6
KB_SHL      = 1<<7

KB_5        = 1<<0
KB_R        = 1<<1
KB_D        = 1<<2
KB_6        = 1<<3
KB_C        = 1<<4
KB_F        = 1<<5
KB_T        = 1<<6
KB_X        = 1<<7

KB_7        = 1<<0
KB_Y        = 1<<1
KB_G        = 1<<2
KB_8        = 1<<3
KB_B        = 1<<4
KB_H        = 1<<5
KB_U        = 1<<6
KB_V        = 1<<7

KB_9        = 1<<0
KB_I        = 1<<1
KB_J        = 1<<2
KB_0        = 1<<3
KB_M        = 1<<4
KB_K        = 1<<5
KB_O        = 1<<6
KB_N        = 1<<7

KB_PLUS     = 1<<0
KB_P        = 1<<1
KB_L        = 1<<2
KB_HYPHEN   = 1<<3
KB_PERIOD   = 1<<4
KB_COLON    = 1<<5
KB_AT       = 1<<6
KB_COMMA    = 1<<7

KB_POUND    = 1<<0
KB_ASTERISK = 1<<1
KB_SEMICOL  = 1<<2
KB_HOME     = 1<<3
KB_SHR      = 1<<4
KB_EQUAL    = 1<<5
KB_UPARR    = 1<<6
KB_SLASH    = 1<<7

KB_1        = 1<<0
KB_LEFTARR  = 1<<1
KB_CTRL     = 1<<2
KB_2        = 1<<3
KB_SPACE    = 1<<4
KB_C64      = 1<<5
KB_Q        = 1<<6
KB_RUNSTOP  = 1<<7

; If this does not work try flipping DDR before pin
; Which lines became 1? 
; the xor should be 1 (now != then)
; and the bit should be set now

INPUT  = $00
OUTPUT = $ff
IDLE   = $ff

; Assume no joystick 1
;;JOYSAFE_MASK = %11100000
JOYSAFE_MASK = %11111111

last_A0_state: !byte 0
last_A1_state: !byte 0
last_A2_state: !byte 0
last_A3_state: !byte 0
last_A4_state: !byte 0
last_A5_state: !byte 0
last_A6_state: !byte 0
last_A7_state: !byte 0

A0OUT = (1<<0)
A1OUT = (1<<1)
A2OUT = (1<<2)
A3OUT = (1<<3)
A4OUT = (1<<4)
A5OUT = (1<<5)
A6OUT = (1<<6)
A7OUT = (1<<7)

A0LOW = <($FF^A0OUT)
A1LOW = <($FF^A1OUT)
A2LOW = <($FF^A2OUT)
A3LOW = <($FF^A3OUT)
A4LOW = <($FF^A4OUT)
A5LOW = <($FF^A5OUT)
A6LOW = <($FF^A6OUT)
A7LOW = <($FF^A7OUT)

!if 0 {

Appraoch:
    Get (inverted) scan row
    return keys down and keys pressed

}

; call +st8 CIA1_DDRB,  INPUT
; before any of these
; and 
; +st8 CIA1_DDRA, INPUT
; +st8 CIA1_PORTA,IDLE 
; after

Kpress_A0:
    +st8 CIA1_PORTA, A0LOW
    +st8 CIA1_DDRA,  A0OUT    
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A0_state
    sty last_A0_state
    sty K_
    and last_A0_state
    sta K
    rts
Kpress_A1:
    +st8 CIA1_PORTA, A1LOW
    +st8 CIA1_DDRA,  A1OUT      
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A1_state
    sty last_A1_state
    sty K_
    and last_A1_state
    sta K
    rts
Kpress_A2:
    +st8 CIA1_PORTA, A2LOW
    +st8 CIA1_DDRA,  A2OUT     
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A2_state
    sty last_A2_state
    sty K_
    and last_A2_state
    sta K
    rts
Kpress_A3:
    +st8 CIA1_PORTA, A3LOW
    +st8 CIA1_DDRA,  A3OUT     
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A3_state
    sty last_A3_state
    sty K_
    and last_A3_state
    sta K
    rts
Kpress_A4:
    +st8 CIA1_PORTA, A4LOW
    +st8 CIA1_DDRA,  A4OUT    
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A4_state
    sty last_A4_state
    sty K_
    and last_A4_state
    sta K
    rts
Kpress_A5:
    +st8 CIA1_PORTA, A5LOW
    +st8 CIA1_DDRA,  A5OUT    
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A5_state
    sty last_A5_state
    sty K_
    and last_A5_state
    sta K
    rts
Kpress_A6:
    +st8 CIA1_PORTA, A6LOW
    +st8 CIA1_DDRA,  A6OUT     
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A6_state
    sty last_A6_state
    sty K_
    and last_A6_state
    sta K
    rts
Kpress_A7:
    +st8 CIA1_PORTA, A7LOW
    +st8 CIA1_DDRA,  A7OUT    
    lda  CIA1_PORTB
    eor #$FF
    tay
    eor last_A7_state
    sty last_A7_state
    sty K_
    and last_A7_state
    sta K
    rts











    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


