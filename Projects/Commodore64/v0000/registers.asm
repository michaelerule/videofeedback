;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; 64 registers total
; VIC-II (Video): 47 registers ($D000 to $D02E)
; SID (Audio): 29 registers ($D400 to $D41C)
; CIA 1 (I/O, IRQ): 16 registers ($DC00 to $DC0F)
; CIA 2 (I/O, NMI): 16 registers ($DD00 to $DD0F)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; VIC-II (Video): 47 registers ($D000 to $D02E)

; Sprite X/Y Coordinates
REG_SPRITE0_X      = $d000
REG_SPRITE0_Y      = $d001
REG_SPRITE1_X      = $d002
REG_SPRITE1_Y      = $d003
REG_SPRITE2_X      = $d004
REG_SPRITE2_Y      = $d005
REG_SPRITE3_X      = $d006
REG_SPRITE3_Y      = $d007
REG_SPRITE4_X      = $d008
REG_SPRITE4_Y      = $d009
REG_SPRITE5_X      = $d00a
REG_SPRITE5_Y      = $d00b
REG_SPRITE6_X      = $d00c
REG_SPRITE6_Y      = $d00d
REG_SPRITE7_X      = $d00e
REG_SPRITE7_Y      = $d00f

; Sprite Control & Features
REG_SPRITE_MSB     = $d010 ; Most Significant Bits for Sprite X Coordinates
REG_SPRITE_ENABLE  = $d015 ; Sprite Enable Register
REG_SPRITE_YEXP    = $d017 ; Sprite Double Height (Y Expansion)
REG_SPRITE_PRIO    = $d01b ; Sprite to Background Priority
REG_SPRITE_MULTI   = $d01c ; Sprite Multicolor Mode Select
REG_SPRITE_XEXP    = $d01d ; Sprite Double Width (X Expansion)

; Control & Status Registers
REG_VIC_CTL1       = $d011 ; Control Register 1 (Raster MSB, Screen Height, ECM, BMM, DEN)
REG_RASTER_LINE    = $d012 ; Current Raster Line (Read) / Target Raster Line (Write)
REG_LIGHT_PEN_X    = $d013 ; Light Pen X Position
REG_LIGHT_PEN_Y    = $d014 ; Light Pen Y Position
REG_VIC_CTL2       = $d016 ; Control Register 2 (Res, Screen Width, MCM, CSEL)
REG_VIC_CONFIG     = $d018 ; VIC-II Memory Matrix Controller (Video Matrix / Char Base)
REG_VIC_INT_FLG    = $d019 ; Interrupt Flag Register
REG_VIC_INT_MSK    = $d01a ; Interrupt Mask Register (Enable/Disable VIC Interrupts)

; Collision Registers (Reading clears them)
REG_COLL_SP_SP     = $d01e ; Sprite-to-Sprite Collision Status
REG_COLL_SP_BG     = $d01f ; Sprite-to-Background Collision Status

; System Colors
REG_BORDER_COLOR   = $d020 ; Border Color
REG_GLOBALBG_COLOR = $d021 ; Background Color 0 (Standard Background)
REG_BG_COLOR1      = $d022 ; Background Color 1 (Multicolor / Extended Color Text)
REG_BG_COLOR2      = $d023 ; Background Color 2 (Multicolor / Extended Color Text)
REG_BG_COLOR3      = $d024 ; Background Color 3 (Extended Color Text)

; Extra Sprite Colors (Shared across all multicolor sprites)
REG_SPRITE_MCOL0   = $d025 ; Sprite Multicolor 0
REG_SPRITE_MCOL1   = $d026 ; Sprite Multicolor 1

; Individual Sprite Colors (Standard / Foreground colors)
REG_SPRITE0_COLOR  = $d027
REG_SPRITE1_COLOR  = $d028
REG_SPRITE2_COLOR  = $d029
REG_SPRITE3_COLOR  = $d02a
REG_SPRITE4_COLOR  = $d02b
REG_SPRITE5_COLOR  = $d02c
REG_SPRITE6_COLOR  = $d02d
REG_SPRITE7_COLOR  = $d02e

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; SID (Audio): 29 registers ($D400 to $D41C)
; (skip)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; CIA 1 (I/O, IRQ): 16 registers ($DC00 to $DC0F)
REG_CIA1_PORTA     = $dc00 ; Data Port A: Joystick 2 / Keyboard columns
REG_CIA1_PORTB     = $dc01 ; Data Port B: Joystick 1 / Keyboard rows
REG_CIA1_DDRA      = $dc02 ; Data Direction Register A
REG_CIA1_DDRB      = $dc03 ; Data Direction Register B

; Real-Time Clock (RTC) 
REG_CIA1_TOD_10TH  = $dc08 ; Time of Day: Tenths of a Second
REG_CIA1_TOD_SEC   = $dc09 ; Time of Day: Seconds
REG_CIA1_TOD_MIN   = $dc0a ; Time of Day: Minutes
REG_CIA1_TOD_HR    = $dc0b ; Time of Day: Hours

; Serial I/O & Interrupt Control
REG_CIA1_SDR       = $dc0c ; Serial Data Register (Shift Register)
REG_CIA1_ICR       = $dc0d ; Interrupt Control Register (Read: Flags / Write: Mask)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; CIA 2 (I/O, NMI): 16 registers ($DD00 to $DD0F)
REG_VIC_SELBANK    = $dd00 ; CIA 2 Port A (VIC Bank Select)
REG_CIA2_PORTB     = $dd01 ; Data Port B: User Port
REG_CIA2_DDRA      = $dd02 ; CIA 2 Data Direction Register A
REG_CIA2_DDRB      = $dd03 ; CIA 2 Data Direction Register B

; Real-Time Clock (RTC) 
REG_CIA2_TOD_10TH  = $dd08 ; Time of Day: Tenths of a Second
REG_CIA2_TOD_SEC   = $dd09 ; Time of Day: Seconds
REG_CIA2_TOD_MIN   = $dd0a ; Time of Day: Minutes
REG_CIA2_TOD_HR    = $dd0b ; Time of Day: Hours

; Serial I/O & Interrupt Control
REG_CIA2_SDR       = $dd0c ; Serial Data Register (Shift Register)
REG_CIA2_ICR       = $dd0d ; Interrupt Control Register (Read: Flags / Write: Mask)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Timers

; CIA 1 Timers (System IRQ / Keyboard / Joysticks)
REG_CIA1_TA        = $dc04 ; Timer A Low Byte
REG_CIA1_TA_HI     = $dc05 ; Timer A High Byte
REG_CIA1_TB        = $dc06 ; Timer B Low Byte
REG_CIA1_TB_HI     = $dc07 ; Timer B High Byte
REG_CIA1_CRA       = $dc0e ; Control Register Timer A
REG_CIA1_CRB       = $dc0f ; Control Register Timer B

; CIA 2 Timers (NMI / RS-232 / User Port)
REG_CIA2_TA        = $dd04 ; Timer A Low Byte
REG_CIA2_TA_HI     = $dd05 ; Timer A High Byte
REG_CIA2_TB        = $dd06 ; Timer B Low Byte
REG_CIA2_TB_HI     = $dd07 ; Timer B High Byte
REG_CIA2_CRA       = $dd0e ; Control Register Timer A
REG_CIA2_CRB       = $dd0f ; Control Register Timer B

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; CPU & Video System Registers (Outside standard I/O blocks)

; 6510 On-Chip Processor Port (Memory Banking / Tape)
REG_CPU_DDR0       = $0000 ; Processor Data Direction Register
REG_CPU_PORT0      = $0001 ; Processor Port (Bits 0-2 control RAM/ROM configuration)

; Hardware Color RAM Base
REG_COLOR_RAM      = $d800 ; Color Matrix Base (1024 nibbles, lower 4 bits active)

; Kernal Software Configuration Registers (Zero-Page / Status)
REG_TEXT_COLOR     = $0286 ; Current Foreground Text Color
REG_SCREEN_HI      = $0288 ; High Byte of Current Text Screen RAM ($04 = $0400)





















