; Can you beat 3.01 frame rate?

render:
!zone {

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; prepare_render_map:

    ; Map and paint will use the same row index
    +t8zp Yk, Ym
    +t8zp Yk, Yp

    ; Set z,x,y base pointers for this row
    ldx Yk
    +ix256zp hz,MAP  ,Yk
    +ix128zp hx,MAPX ,Yk
    +ix256zp hy,MAPY2,Yk
    +t16zp hz,hζ

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; prepare_paint_map
    ; Load screen mem index pointers
    ; "t": offset ±8 stride increments
    ; "i": start/end of fwd/rev rows
    ldy Yk
    +st16tabLHyzp rrtab_lo,rrtab_hi,hir
    +st16tabLHyzp rftab_lo,rftab_hi,hif
    +st16tabLHyzp tfset_lo,tfset_hi,htf
    +st16tabLHyzp trset_lo,trset_hi,htr
    
    ; Load dither depending on scanline
    tya
    and #1
    
    ; Page align saves lo byte write 
    ; !! cannot use low as scratch !!
    beq+
        +st8zp hp0+1,>pallet00
        +st8zp hp1+1,>pallet10
        +st8zp hp2+1,>pallet20
        +st8zp hp3+1,>pallet30
        jmp ++
    :+
        +st8zp hp0+1,>pallet01
        +st8zp hp1+1,>pallet11
        +st8zp hp2+1,>pallet21
        +st8zp hp3+1,>pallet31
    :++
    
    ; 4 pixel load base pointers
    +ix256zp hz0,MAP,Yk
    +t16zp   hz0,hz1
    +inc16zp hz1
    +t16zp   hz1,hz2
    +inc16zp hz2
    +t16zp   hz2,hz3
    +inc16zp hz3

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    jsr hmap
    jsr hpaint
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    !if 0 {
    ldy     Yk
    lda     RM,y
    +tA16zp X
    +t816   Yk,Y
    +a16l   Y,H2
    +st8    C,2
    jsr     set_canvas_px_4color
    +st8    C,0
    +a16lzp X,1
    jsr     set_canvas_px_4color
    }
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    rts
}


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; BAD IDEA
; WILL NOT WORK UNLESS WE CAN ADJUST Y
; NOT SURE HOW TO DO NON DESCRUCTIVE
; 



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Do half the work
; Calculate updates only for even bits of the map
; Map y row is always even
; Paint y row uses even map for %2∈{0,1} both
render_evens:
!zone {

    ; We do a normal render for the even line
    lda Yk
    and #$FE
    sta Yk
    jsr render
    
    ; For the odd line we just do hpaint
    ; Pointing to the even row
    
    ; tell hpaint to use even line's bounds
    +t8zp Yk, Yp
    
    ; Switch to odd line
    lda Yk
    ora #1
    sta Yk

    ; we use the odd lines screen pointers
    ; Load screen mem index pointers
    ; "t": offset ±8 stride increments
    ; "i": start/end of fwd/rev rows
    ldy Yk
    +st16tabLHyzp rrtab_lo,rrtab_hi,hir
    +st16tabLHyzp rftab_lo,rftab_hi,hif
    +st16tabLHyzp tfset_lo,tfset_hi,htf
    +st16tabLHyzp trset_lo,trset_hi,htr
    
    ; we use the odd dither patterns
    +st8zp hp0+1,>pallet01
    +st8zp hp1+1,>pallet11
    +st8zp hp2+1,>pallet21
    +st8zp hp3+1,>pallet31
    
    ; tell hpaint to read data from the even line
    +st16zp hz0,MAP
    lda <(hz0+1)
    clc
    adc <(Yp)
    sta <(hz0+1)
    
    +t16zp    hz0,hz1
    +inc16zp  hz1
    +t16zp    hz1,hz2
    +inc16zp  hz2
    +t16zp    hz2,hz3
    +inc16zp  hz3

    jsr hpaint
    rts
}







    
    ; Map and paint will use the same row index
    +t8zp Yk, Ym
    











