;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Set up c64
;
; Like Arduino, this expects the user to define
;   c64setup: an initialization routine
;   c64loop:  the loop/kernel/rendering routine

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Macros should be imported before anything else
!source "macros.asm" 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; The main function must start at this location
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; BASIC startup sequence

; Moving this up to free bank 0 space for graphics?
* = $0801
    !byte $0b, $08, $0a, $00, $9e, $32, $30, $36, $31, $00, $00, $00

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;========================================
; Main
entry
    jsr c64setup
.forever
    jsr c64loop
    jmp .forever
     
     
; Force the $0C00 - $1000 Screen RAM 3 Reservation
; This forces ACME to fill everything from the current program position up to 
; $0C00 with a filler byte (0), leaving the area free to double buffer
; screen ram if we choose. 
!align $0C00, 0     
RESERVED_SCREEN3_START = *
!fill $0400, 0
     
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Further standard library imports
!source "registers.asm"
!source "multiply.asm" 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; BASIC ROM is 0xA000 to 0xC000-1
disable_basic_0xA000_to_0xC000:
    sei
    +andreg REG_CPU_PORT0, $fe
    rts
enable_basic_0xA000_to_0xC000:
    sei
    lda #$2f
    sta REG_CPU_DDR0
    lda REG_CPU_PORT0
    ora #$07
    sta REG_CPU_PORT0
    rts
