
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Call with uint16 zp X,Y = (x,y)
!if 0 {
def flip_screen_bit(x,y):
    xi = x%8
    xb = x//8
    yi = y%8
    yb = y//8
    I = BITMAP_A + 320*yb + yi + xb*8
    lda xbitmasks,xi
    eor *(I)
    sta *(I)
    rts
}
!zone {
    ; This uses XYC to pass parameters
    ; Internally it uses TS to preserver XY
    
    ; These are all uint16
    .xi = D
    .xb = E
    .yi = F
    .yb = G

    :_xbitmasks:
        !byte 128,64,32,16,8,4,2,1
        
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; I = BITMAP_A + 320*yb + yi + xb*8
    :calculate_bitmap_index:
        lda Y : +md8 : sta .yi
        lda Y : +d8  : sta .yb
        +t16zp X,.xb 
        lda .xb 
        and #$F8 
        sta .xb
        +st16     I,BITMAP_A
        +a168     I,.yi
        +a16      I,.xb
        ldx      .yb
        +a16x320  I
        rts

    !zone {
    :flip_screen_bit:
            jsr calculate_bitmap_index
            +t16 I,.o
            +t16 I,.s
            lda <(X)
            +md8
            tax
            lda _xbitmasks,x
    .o=*+1: eor $FFFF
    .s=*+1: sta $FFFF
            rts
    }

    !zone {
    :set_screen_bit:
            jsr calculate_bitmap_index
            +t16 I,.o
            +t16 I,.s
            lda <(X)
            +md8
            tax
            lda _xbitmasks,x
    .o=*+1: ora $FFFF
    .s=*+1: sta $FFFF
            rts
    }

    !zone {
    :clear_screen_bit:
            jsr calculate_bitmap_index
            +t16 I,.o
            +t16 I,.s
            lda <(X)
            +md8
            tax
            lda _xbitmasks,x
            eor #$FF
    .o=*+1: and $FFFF
    .s=*+1: sta $FFFF
            rts
    }

    !zone {
    :shift_xy_to_canvas:
        +a16lzp  X,GRIDX*8
        +a16lzp  Y,GRIDY*8
        rts
    }
    
    !zone {
    :set_canvas_px_4color:
        +t16zp X,T
        +t16zp Y,S
        +asl16zp X
        jsr shift_xy_to_canvas
        
        lda C
        and #2
        beq+
            jsr set_screen_bit
            jmp ++
        :+
            jsr clear_screen_bit
        :++
            
        +a16lzp X,1
        
        lda C
        and #1
        beq+
            jsr set_screen_bit
            jmp ++
        :+
            jsr clear_screen_bit
        :++
        +t16zp T,X
        +t16zp S,Y
        rts
    }
}
