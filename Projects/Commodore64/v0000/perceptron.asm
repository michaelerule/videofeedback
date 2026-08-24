;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!source "c64.asm"
; Like Arduino, this expects the user to define
;   c64setup: an initialization routine
;   c64loop:  the loop/kernel/rendering routine

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Project imports
!source "constants.asm"
!source "screen.asm" 
!source "cursor.asm"
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Cache locations
MAP_DX  = $a700
MAP_DY  = $8e00 
MAP     = $5c00 
ROWA   = MAP-H2
ROWB   = ROWA-H2

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Initialize program here
c64setup:
    jsr disable_basic_0xA000_to_0xC000
    jsr start_graphics
    jsr init_sprite
    lda#4:sta _N
    lda#0:sta _O
    ; ROWA[:]=0
    ldx#H2                              ; loop counter from H2 down to 1
-   lda#0:sta ROWA-(H2)-1,x            ; clear buffer byte
    dex:bne -                           ; loop until all bytes cleared
    ; ROWB[:]=0xFF
    ldx #H2
    lda #$ff
-   sta ROWB-(H2)-1,x
    dex
    bne -
    ;jsr draw_color_test_blocks
    rts
    
draw_color_test_blocks:
    lda #<SCREEN_RAM    : sta _E
    lda #>SCREEN_RAM    : sta _E+1
    lda #<REG_COLOR_RAM : sta _F
    lda #>REG_COLOR_RAM : sta _F+1
    lda #<BITMAP_A      : sta _C
    lda #>BITMAP_A      : sta _C+1
    ldx #0
.row_loop
    txa                         
    asl : asl : asl : asl       
    stx _T                      
    ora _T                      
    ldy #0 : sta (_E),y : iny : sta (_E),y 
    txa
    ldy #0 : sta (_F),y : iny : sta (_F),y 
    lda #$55 : ldy #15
-   sta (_C),y : dey : bpl -
    lda _E   : clc : adc #40  : sta _E   : bcc + : inc _E+1
+   lda _F   : clc : adc #40  : sta _F   : bcc + : inc _F+1
+   lda _C   : clc : adc #$40 : sta _C   
    lda _C+1 : adc #$01       : sta _C+1 
    inx : cpx #16 : bne .row_loop
    rts
    
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Render a frame (called repeatedly)
xStart = $A0
xDone  = $A1
bStart = $A2
bDone  = $A3
bPrev  = $A4

c64loop:
    ;dec _N:bne +:.stop:jmp .stop:+      ; spin lock to freeze at frame N-1
    +set16 _C, BITMAP_A + BHALF
    +set16 _D, BITMAP_A + BHALFR - 40*8 + 8*9
    +cp16to16 _C,_F                     ; start forward buffer
    +cp16to16 _D,_H                     ; start reverse buffer
    ; Set literal additions for the "+c" part of z←z²+c
    sec:lda CURX:sbc #<CURX0:lsr        ; X offset = (CURX-CURX0)>>1
    sta R2+1:eor#$ff:sta R1+1           ; W1-(x+r)&W1 = W1&(~x+r+1) (+1 via a carry)
    sec:lda CURY:sbc #<CURY0:asr        ; Y offset = (CURY-CURY0)>>1
    sta MS+1
    ; Look up tables
    +set16 _U,DX_CACHE
    +set16 _V,DY_CACHE
    +set16 _W,MAP
    ; Awful lazy interlacing solution
    inc _O:lda _O:and#7:sta _O          ; _O = (_O+1)%8 on zero page
	;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;________________________________________
    lda#H2:sta Yl                       ; LOOP rows Yl
.Yl:
    lda _O:eor Yl:and#7:beq+:jmp.skipY:+; Lazy interlacing code by skipping rows
	lda Yl:and#3:asl:sta Yf             ; Row interlace offset Yf = 2*(Yl%4)
	eor #6:sec:sbc#8:sta Yr             ; Reverse counter
    +cp16to16 _F,_E                     ; Return to start of row
    +cp16to16 _H,_G                     ; Return to end   of row
    ; Set pixel memory row start (we use Yf Yr to offset from here)
    lda _E   : sta w1+1 : sta w2+1 : sta w4+1 : sta w5+1
    lda _E+1 : sta w1+2 : sta w2+2 : sta w4+2 : sta w5+2
    lda _G   : sta w3+1 : sta w6+1
    lda _G+1 : sta w3+2 : sta w6+2
    ; Row pointers are reset to make it easier to change scanline order
    +cp16to16 _U,_X
    +cp16to16 _V,_Y
    +cp16to16 _W,_Z
    
    ;ldy Yl                              ; get current row index
    ;lda ROWA-(H2),y                     ; pull first valid column index
    ;sta m_skip+1                        ; self-modify the comparison target
	;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;________________________________________
	ldy Yl:lda ROWB-(H2),y:sta bPrev    ; bPrev = ROWB[Yl]
	
    lda#0
    sta xStart
    sta xDone
    sta bStart
    sta bDone
    sta Xi ; LOOP over col index Xi
.Xi
    ;lda Xi:m_skip cmp#0:bcs+:jmp.skipX:+
    ldx bDone:bne.FOO                   ; if (isDone) skip to foo
    ldy#0 :lda (_Y),y:cmp#$ff:beq.NO    ; if (a=_Y[i]=0xff) skip texture lookup
    :MS adc#0:bvs.NO:cmp#H:bcs.NO       ; if (0<=_Y[i]+_S<H) {
    cmp#H2:bcs +                        ;     if (iy<H2) {
    eor#$ff:adc#H-H2:tax                ;         x   = iy = H-1-(H>>1) - iy
    lsr:sta _A+1:lda#0:ror:sta _A       ;         _A  = iy*128
    lda#>MAP:adc _A+1:sta _A+1          ;         _A += MAP
    lda(_X),y:eor#$ff:R1 adc#0:and#W1   ;         y   = a  = W1-(_X[i]+r)%W
    jmp.loadcolor                       ;     } else {
+:  sbc#H2                              ;         x   = iy - H2
    lsr:sta _A+1:lda#0:ror:sta _A       ;         _A  = iy*128
    lda#>MAP:adc _A+1:sta _A+1          ;         _A += MAP
    lda(_X),y:R2 adc#0:and#W1           ;         y   = (ix+_R)%W
.loadcolor                              ;     }
    adc _A:sta _A                       ;     _A += ix
    lda#1:sta xStart                    ;     started = True
    ldy#0:lda (_A),y                    ;     a = color[_A]
    jmp.writecolor                      ; }
.NO:                                    ; else {
    ldx xStart:beq+:lda#1:sta xDone:+   ;     if (started) done=True
.FOO:
    lda#0                               ;     color = 0
.writecolor:                            ; }
    cmp#NCOLORS:beq+:adc#1:+            ; Increment pallet index (stop at end)
    ldy#0:sta(_Z),y                     ; Store color in MAP cache
    tax:lda C1:asl:asl:ora CM1,x:ldy C2 ; Shift color 1, place in color 2
    sta C2:tya:asl:asl:ora CM2,x:sta C1 ; Shift color 2, place in color 1
    
!if 0 {
    tax
    lda Xi
    eor Yl
    and #1
    bne .x1
    ; even cols
    lda C1:asl:asl:ora CM1,x:sta C1
    lda C2:asl:asl:ora CM2,x:sta C2
    jmp +    
.x1 ; odd cols
    lda C1:asl:asl:ora CM2,x:sta C1
    lda C2:asl:asl:ora CM2,x:sta C2
+
}

    ; Save color byte every 4 pixels
    lda Xi:and#3:cmp#3:beq+:jmp .continueX:+

    ;lda Xi:and #3:cmp #3:bne +          
	ldy Yf:w1 lda $0,y:cmp C1:beq.s1    ; Skip color1 write if no change
	lda C1:w2 sta$0,y                   ; Store forward
    tax:lda RV,x:ldy Yr:iny:w3 sta$0,y  ; Flip, store reverse
.s1
    ldy Yf:iny:w4 lda$0,y:cmp C2:beq.s2 ; Skip color2 write if no change
    lda C2:w5 sta$0,y                   ; Store forward
    tax:lda RV,x:ldy Yr:w6 sta$0,y      ; Flip, store reverse
.s2
	lda Yf:clc:adc #8:sta Yf            ; Incremement forward offset
	lda Yr:sec:sbc #8:sta Yr            ; Incremement reverse offset
	
    ; // Record the starting column
    ; if (xStart) {
    ;     if (!bStart) ROWA[Yl] = Xi-Xi%4
    ;     bStart = True
    ; }
    lda xStart:beq+
    lda bStart:bne++
    lda Xi:and#$fc:ldy Yl:sta ROWA-(H2),y
++: lda #1:sta bStart
+ :
    ; // Record the ending column
    ; if (xDone) {
    ;     if (!bDone) ROWB[Yl] = Xi-Xi%4
    ;     bDone = True;
    ; }   
    lda xDone:beq+
    lda bDone:bne++
    lda Xi:ora#3:clc:adc#4:ldy Yl:sta ROWB-(H2),y
++: lda #1:sta bDone
+ : 

    ; DOESNT WORK RIGHT
    ;lda Xi:ldy Yl:cmp bPrev:bcc+        ; Never break if Xi < ROWB[Yl] (need zero fill)
    ;ldx bDone:beq +:jmp.breakX          ; Break early after pixels written
+
    jmp .continueX                      ; End of code that runs every 4 pixels
.skipX                                  ; Code needed only for skipped pixels 
    lda Xi:and #3:cmp #3:bne +
	lda Yf:clc:adc #8:sta Yf            ; Incremement forward offset
	lda Yr:sec:sbc #8:sta Yr            ; Incremement reverse offset
+
.continueX
    inc _X
    inc _Y
    inc _Z
    +while_inc Xi, W, .Xi               ; ENDLOOP Xb col blocks
.breakX
    jsr enable_basic_0xA000_to_0xC000
    jsr poll_cursor
    jsr disable_basic_0xA000_to_0xC000
.skipY:
    +iadd16lit _U,W
    +iadd16lit _V,W
    +iadd16lit _W,W
    lda Yl:and #3:cmp #3:bne+           ; Every 4 pixel rows we increment block row
    lda _F:clc:adc#$40:sta _F:lda _F+1:adc#1:sta _F+1
    lda _H:sec:sbc#$40:sta _H:lda _H+1:sbc#1:sta _H+1
	;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;________________________________________
+:  +while_inc Yl, H, .Yl               ; ENDLOOP rows Yl
    rts


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!source "other_tables.asm"
!source "tables.asm"













































