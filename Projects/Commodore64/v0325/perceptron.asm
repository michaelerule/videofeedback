!set SHOW_SIZES = 0
PREPACKMAP = 1
FAKEMAP    = 2

!source "macros.asm" 
!source "constants.asm"
!source "registers.asm"
!source "colors.asm"

; High graphics bank table location configuration
MSIZE   = H2*W
MSTART  = $0200
MAP     = MSTART
MAPY2   = MAP + 128
MAPY    = MAP + MSIZE
MAPX    = MAPY + MSIZE
!warn " -------------------------------  "

* = $0801
!warn " (BASIC stub)                     "
!warn " -------------------------------  "
!warn "| BASIC start  ",*
    !byte $0b, $08, $0a, $00, $9e, $32, $30, $36, $31, $00, $00, $00
    sei
    lda #$35
    sta $01 
    !source "screen.asm"
    jsr init_sprite
    jsr init_row_bounds
    jsr move_kernel_to_zero_page
    jsr draw_labels
    jsr toggle_looping
    jsr increment_interlace
    jmp main
!warn " -------------------------------  "
!set _p=* : !source "hotsource.asm"  : !warn "| hotsource  ",*-_p," : ",*
!set _p=* : !source "sprites.asm"    : !warn "| sprites    ",*-_p," : ",*
!set _p=* : !source "spritedata.asm" : !warn "| spritedata ",*-_p," : ",*
!set _p=* : !source "scratch.asm"    : !warn "| scratch    ",*-_p," : ",*
!set _p=* : !source "fontscratch.asm": !warn "| scratch    ",*-_p," : ",*
!warn "| second scratch region ends  ",*
!warn " -------------------------------  "
!if *>MAP+MSIZE*2 {!error "scratch overflows into fix code memory"}


!warn " (map lookup tables)              "
!warn " -------------------------------  "
!warn "| map start  ",MAP
!source "tabmap.asm"
!warn "| map end    ",MAPX + MSIZE
!warn " -------------------------------  "
*=MAPX + MSIZE

!warn " -------------------------------  "
!set _p=* : !source "tabrev.asm"   : !warn "| tabrev     ",*-_p," : ",*
!set _p=* : !source "tabfr.asm"    : !warn "| tabfr      ",*-_p," : ",*
!set _p=* : !source "tabpallet.asm": !warn "| tabpallet  ",*-_p," : ",*

!set _p=* : !source "util.asm"     : !warn "| util       ",*-_p," : ",*
!set _p=* : !source "bounds.asm"   : !warn "| bounds     ",*-_p," : ",*
!set _p=* : !source "pallet.asm"   : !warn "| pallet     ",*-_p," : ",*
!set _p=* : !source "text.asm"     : !warn "| text       ",*-_p," : ",*
!set _p=* : !source "input.asm"    : !warn "| input      ",*-_p," : ",*
!set _p=* : !source "cursor.asm"   : !warn "| cursor     ",*-_p," : ",*
!set _p=* : !source "trails.asm"   : !warn "| trails     ",*-_p," : ",*
!set _p=* : !source "key.asm"      : !warn "| key        ",*-_p," : ",*
!set _p=* : !source "packindex.asm": !warn "| packindex  ",*-_p," : ",*
!set _p=* : !source "start.asm"    : !warn "| start      ",*-_p," : ",*
!set _p=* : !source "render.asm"   : !warn "| render     ",*-_p," : ",*
!set _p=* : !source "graphics.asm" : !warn "| graphics   ",*-_p," : ",*
!set _p=* : !source "main.asm"     : !warn "| main       ",*-_p," : ",*
!set _p=* : !source "test.asm"     : !warn "| test       ",*-_p," : ",*


!warn "| ",SCRAM-*," bytes FREE"
!warn "| you need ",H2*W," more bytes for 256 x stride"

!warn " -------------------------------  "
!if *>SCRAM {!error "code collides with screen ram"}

!warn " -------------------------------  "
!warn " (memory end if bank 3 graphics)  "
!warn " -------------------------------  "
!warn "| screen ram     ",SCRAM
!warn "| IO map start   ",$D000
!warn "| IO map stop    ",$DFFF
!warn "| Kern ROM start ",$E000
*=$E000
!warn "| Kern ROM stop  ",$EFFF
!warn "| bitmap RAM     ",BITMAP_A
!warn "| --- (end at FFF9) --- "
!warn " -------------------------------  "
;;!set LEFTOVER = $FFF9-*
;;!warn "    free bytes remaining : ", LEFTOVER
!warn ":"
!warn ":"


; Free stuff
; 
; Screen/color 40×25 = 1000 < 1024
;   24 byte gap at the end:
; 
;   Screen RAM +
;       $03E8–$03F7 (16 bytes free)
;       $03F8–$03FF ( 8 bytes sprite pointers)
;       IO reg $D015 sets sprites enabled
; 
;   Color RAM $D800–$DBFF
;       $DE80–$DFFF free (only retains 4 bits?)
;       May use 8 bit $D800–$DBFF by bit(2,0x01)=0
;       Must restore this bit to function
; 
; Bitmap RAM needs 40×25×8 = 8000 < 8192
;   0xFF40-0xFFF9 (Bank 3 last 6 resererved, 186 free)
;   You are using 0xFF40 for sprite0 (64 bytes)
;   0xFF80-0xFFF9 (122 free)
; 
; Those final 6 reserved bytes: 
;   $FFFA $FFFB NMI     Vector (cannot ignore; restore key)
;   $FFFC $FFFD RESET   Vector (cannot ignore; power on/reboot)
;   $FFFE $FFFF IRQ/BRK Vector (may ignore if SEI and no BRK opcodes)
;   CIA2 interrupt bits ALSO on NMI (but only if you've set them up): 
;       0: Timer A Underflow
;       1: Timer B Underflow
;       2: Real-Time Clock Alarm
;       3: Serial Port Shift Register Full/Empty 
;       4: The /FLAG Pin
; 
; Border 8×8 blocks without content:
;   Bytes pack 4 2-bit or 8 1-bit pixels
;   Global bgcolor limits hideable values
;   2 color: 
;       Cannot hide unless black is in pallet
;   4 color: 
;       May hide values 01 10 11
;       Permitted: 81 = sum(np.all(int32([
;            [*f'{i:08b}'] for i in range(256)
;            ]).reshape(256,4,2)@[2,1],1))
;       Cheaper to use half the bits
;           Store 4 bytes in every 8 byte block
;           (i)&0b01010101 | (i+1)&0b101010
;       8 blocks free in margin all 25 rows
;           800 bytes free
;           Sadly no jmp/branch opcodes safe
;       If reduced yblocks=16 5 free rows
;           800=5×40×4 more bytes
;           16*4 rows
;           16*8 Δy values
;           8192 bytes needed for bounds table
;           


; How branching works: 
;    after cmp v:
;    bcc a< v   inverse:    a>=v
;    bcs a>=v   inverse:    a< v
;    bne a!=v   inverse:    a==v
;    beq a==v   inverse:    a!=v
;    bpl sign(a-v) == 0
;    bmi sign(a-v) == 1
;    |a-v|<=128
;        bpl a>=v
;        bmi a<v
;    |a-v|>=128
;        bpl a<v
;        bmi a>=v















































