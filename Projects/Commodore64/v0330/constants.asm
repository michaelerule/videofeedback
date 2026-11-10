

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!source "shared_constants.asm"


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
False = 0
True  = 1

SK = 160
SH = 200
BK = (SK>>3)-8
BH = (SH>>3)-8

BSTART = 8*(40*GRIDY+GRIDX)
BHALF  = BSTART + 40*(H2 & %11111000) + (H2 & %00000111)
BHALFR = BHALF  - 40*8 + (XBLOCKS-1)*8
BEND   = 8*(40*GRIDY-GRIDX+40*YBLOCKS)-1

c0  = W2*W2 - H2*H2 - W2 + H2
Cxy = W2*H2*4 - 2*(W2+H2) + 1

F0 = BITMAP_A + BHALF
R0 = BITMAP_A + BHALFR - 40*8 + 8*9


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; 4 colors mode: ½/½ color/screen ram
; Screen RAM pointers can switch
; Color ram is fixed
SCRAM_LENGTH        = $0400
SCRAM_CHARACTER_END = $03E8
SCRAM_SPRITES_START = $03F8

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Bank selection
; VIC_SELBANK at 0xDD00 is active lo
; Bits 0..1: bank # (^0b11 inverted)
BANK_NUM    = 3              ; 0..3
BANK_START  = $4000*BANK_NUM ; &(bank)
VIC_BANK    = 3-BANK_NUM     ; Lo bits for VIC_SELBANK
VIC_MASK    = %11111100      ; Mask for non-screen lines in VIC bank

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Changing the screen ram pointer requires changing the VIC_CONFG bits
; High nibble multiplied by 0x400 to determine where SCRAM starts. 
SCSEL = 3                               ; 0..7 3-bit screen ram offset selector
SCRAM = BANK_START + SCSEL*$400         ; physical screen ram address

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; In bitmap mode, bit 3 toggles whether bitmap memory starts at +0 or +0x2000
; (other bits do nothing). 
BMPSEL    = 1                           ; (0,1) --> +0 or +0x2000 address offset? 
BITMAP_A  = BANK_START + BMPSEL*$2000   ; Add to start of bank to get memory address
VIC_CONFG = (BMPSEL<<3)|(SCSEL<<4)      ; Flags specifyinge above (--> VIC_CONFIG)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; DDRA bits 1--0 are VIC bank direction (must be set to output)
VIC_CIA2_VAL = 3

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Set in VIC_CTL1 and VIC_CTL2 to use 4 color mode.
BITMAP_MODE_ENABLE = %00100000
MULTIC_MODE_ENABLE = %00010000
