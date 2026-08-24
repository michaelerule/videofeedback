;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; 64 registers total
; VIC-II (Video): 47 registers ($D000 to $D02E)
; SID (Audio): 29 registers ($D400 to $D41C)
; CIA 1 (I/O, IRQ): 16 registers ($DC00 to $DC0F)
; CIA 2 (I/O, NMI): 16 registers ($DD00 to $DD0F)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; THIS IS AN IO PORT DO NOT TOUCH IT!
DDR0  = $00
PORT1 = $01

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; VIC-II (Video): 47 registers ($D000 to $D02E)

; Sprite X/Y Coordinates

SPXS = $d000
SPYS = $d001
SP0X = $d000
SP0Y = $d001
SP1X = $d002
SP1Y = $d003
SP2X = $d004
SP2Y = $d005
SP3X = $d006
SP3Y = $d007
SP4X = $d008
SP4Y = $d009
SP5X = $d00a
SP5Y = $d00b
SP6X = $d00c
SP6Y = $d00d
SP7X = $d00e
SP7Y = $d00f

; Sprite Control & Features
SPMSB    = $d010 ; Most Significant Bits for Sprite X Coordinates
SPON     = $d015 ; Sprite Enable Register
SP_YEXP  = $d017 ; Sprite Double Height (Y Expansion)
SP_PRIO  = $d01b ; Sprite to Background Priority
SP_MULTI = $d01c ; Sprite Multicolor Mode Select
SP_XEXP  = $d01d ; Sprite Double Width (X Expansion)

; Control & Status Registers
VIC_CTL1       = $d011 ; Control Register 1 (Raster MSB, Screen Height, ECM, BMM, DEN)
RASTER_LINE    = $d012 ; Current Raster Line (Read) / Target Raster Line (Write)
LIGHT_PEN_X    = $d013 ; Light Pen X Position
LIGHT_PEN_Y    = $d014 ; Light Pen Y Position
VIC_CTL2       = $d016 ; Control Register 2 (Res, Screen Width, MCM, CSEL)
VIC_CONFIG     = $d018 ; VIC-II Memory Matrix Controller (Video Matrix / Char Base)
VIC_INT_FLG    = $d019 ; Interrupt Flag Register
VIC_INT_MSK    = $d01a ; Interrupt Mask Register (Enable/Disable VIC Interrupts)

; Collision Registers (Reading clears them)
COLL_SP_SP     = $d01e ; Sprite-to-Sprite Collision Status
COLL_SP_BG     = $d01f ; Sprite-to-Background Collision Status

; System Colors
BORDER_COL   = $d020 ; Border Color
GLOBALBG_COL = $d021 ; Background Color 0 (Standard Background)
BG_COL1      = $d022 ; Background Color 1 (Multicolor / Extended Color Text)
BG_COL2      = $d023 ; Background Color 2 (Multicolor / Extended Color Text)
BG_COL3      = $d024 ; Background Color 3 (Extended Color Text)

; Extra Sprite Colors (Shared across all multicolor sprites)
SP_MCOL0   = $d025 ; Sprite Multicolor 0
SP_MCOL1   = $d026 ; Sprite Multicolor 1

; Individual Sprite Colors (Standard / Foreground colors)
SPCS = $d027
SP0C = $d027
SP1C = $d028
SP2C = $d029
SP3C = $d02a
SP4C = $d02b
SP5C = $d02c
SP6C = $d02d
SP7C = $d02e

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; SID (Audio): 29 registers ($D400 to $D41C)
; (skip)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; CIA 1 (I/O, IRQ): 16 registers ($DC00 to $DC0F)
CIA1_PORTA     = $dc00 ; Data Port A: Joystick 2 / Keyboard columns
CIA1_PORTB     = $dc01 ; Data Port B: Joystick 1 / Keyboard rows
CIA1_DDRA      = $dc02 ; Data Direction Register A
CIA1_DDRB      = $dc03 ; Data Direction Register B

; Real-Time Clock (RTC) 
CIA1_TOD_10TH  = $dc08 ; Time of Day: Tenths of a Second
CIA1_TOD_SEC   = $dc09 ; Time of Day: Seconds
CIA1_TOD_MIN   = $dc0a ; Time of Day: Minutes
CIA1_TOD_HR    = $dc0b ; Time of Day: Hours

; Serial I/O & Interrupt Control
CIA1_SDR       = $dc0c ; Serial Data Register (Shift Register)
CIA1_ICR       = $dc0d ; Interrupt Control Register (Read: Flags / Write: Mask)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; CIA 2 (I/O, NMI): 16 registers ($DD00 to $DD0F)
VIC_SELBANK    = $dd00 ; CIA 2 Port A (VIC Bank Select)
CIA2_PORTB     = $dd01 ; Data Port B: User Port
CIA2_DDRA      = $dd02 ; CIA 2 Data Direction Register A
CIA2_DDRB      = $dd03 ; CIA 2 Data Direction Register B

; Real-Time Clock (RTC) 
CIA2_TOD_10TH  = $dd08 ; Time of Day: Tenths of a Second
CIA2_TOD_SEC   = $dd09 ; Time of Day: Seconds
CIA2_TOD_MIN   = $dd0a ; Time of Day: Minutes
CIA2_TOD_HR    = $dd0b ; Time of Day: Hours

; Serial I/O & Interrupt Control
CIA2_SDR       = $dd0c ; Serial Data Register (Shift Register)
CIA2_ICR       = $dd0d ; Interrupt Control Register (Read: Flags / Write: Mask)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Timers

; CIA 1 Timers (System IRQ / Keyboard / Joysticks)
CIA1_TA        = $dc04 ; Timer A Low Byte
CIA1_TA_HI     = $dc05 ; Timer A High Byte
CIA1_TB        = $dc06 ; Timer B Low Byte
CIA1_TB_HI     = $dc07 ; Timer B High Byte
CIA1_CRA       = $dc0e ; Control Register Timer A
CIA1_CRB       = $dc0f ; Control Register Timer B

; CIA 2 Timers (NMI / RS-232 / User Port)
CIA2_TA        = $dd04 ; Timer A Low Byte
CIA2_TA_HI     = $dd05 ; Timer A High Byte
CIA2_TB        = $dd06 ; Timer B Low Byte
CIA2_TB_HI     = $dd07 ; Timer B High Byte
CIA2_CRA       = $dd0e ; Control Register Timer A
CIA2_CRB       = $dd0f ; Control Register Timer B

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; CPU & Video System Registers (Outside standard I/O blocks)

; 6510 On-Chip Processor Port (Memory Banking / Tape)
CPU_DDR0       = $0000 ; Processor Data Direction Register
CPU_PORT0      = $0001 ; Processor Port (Bits 0-2 control RAM/ROM configuration)

; Hardware Color RAM Base
CORAM      = $d800 ; Color Matrix Base (1024 nibbles, lower 4 bits active)

; Kernal Software Configuration Registers (Zero-Page / Status)
TEXT_COL     = $0286 ; Current Foreground Text Color
SCR_HI      = $0288 ; High Byte of Current Text Screen RAM ($04 = $0400)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; KERNAL OS Vectors & System Management ($0300 to $03FF)

; Core Hardware Interrupt Handlers
VEC_SYSTEM_IRQ     = $0314 ; Hardware IRQ Interrupt Vector (Default: $EA31)
VEC_SYSTEM_BRK     = $0316 ; BRK Instruction Execution Vector (Default: $FE66)
VEC_SYSTEM_NMI     = $0318 ; Non-Maskable Interrupt (NMI) Vector (Default: $FE47)

; Core Operating System Jumps
VEC_OPEN_FILE      = $031a ; Kernal OPEN  File Vector
VEC_CLOSE_FILE     = $031c ; Kernal CLOSE File Vector
VEC_CHKIN_STREAM   = $031e ; Kernal CHKIN (Set Input  Stream) Vector
VEC_CKOUT_STREAM   = $0320 ; Kernal CKOUT (Set Output Stream) Vector
VEC_CLRCH_STREAM   = $0322 ; Kernal CLRCH (Clear Channels) Vector
VEC_BASIN_STREAM   = $0324 ; Kernal CHRIN/BASIN  (Read  Character) Vector
VEC_BSOUT_STREAM   = $0326 ; Kernal CHROUT/BSOUT (Write Character) Vector
VEC_STOP_KEY       = $0328 ; Kernal STOP  Key Indicator Scan Vector
VEC_GETIN_STREAM   = $032a ; Kernal GETIN (Get Character from Queue) Vector
VEC_CLALL_STREAM   = $032c ; Kernal CLALL (Close All Files) Vector

; User Direct Hook Vectors
VEC_USR_FUNC       = $0310 ; BASIC USR Function Hook Vector
VEC_LOAD_DATA      = $0330 ; Kernal LOAD Routine Vector
VEC_SAVE_DATA      = $0332 ; Kernal SAVE Routine Vector

; Safe Memory Workspace Buffer
MEM_CASSETTE_BUF   = $033c ; 192-Byte Safe Storage Area ($033C to $03FB)







