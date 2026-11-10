;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!source "macros.asm" 
!source "constants.asm"
!source "registers.asm"
!source "colors.asm"

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Bank 3 graphics table locations
MSIZE   = H2*W
MSTART  = $0200
MAP     = MSTART
MAPY2   = MAP + 128
MAPY    = MAP + MSIZE
MAPX    = MAPY + MSIZE

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
* = $0801
!warn " -------------------------------  "
!warn "| BASIC start  ",*
    !byte $0b,$08,$0a,$00,$9e,$32,$30,$36,$31,$00,$00,$00
    sei
    lda #$35
    sta $01 
    jmp init
    jmp main


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
!set _p=* : !source "key.asm"      : !warn "| key        ",*-_p," : ",*
!set _p=* : !source "packindex.asm": !warn "| packindex  ",*-_p," : ",*
!set _p=* : !source "render.asm"   : !warn "| render     ",*-_p," : ",*
!set _p=* : !source "graphics.asm" : !warn "| graphics   ",*-_p," : ",*
!set _p=* : !source "main.asm"     : !warn "| main       ",*-_p," : ",*


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











































