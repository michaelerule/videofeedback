render:
!zone {

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; prepare_render_map:

    ; Map and paint will use the same row index
    +t8zp Yk, Yk
    +t8zp Yk, Yk

    ; Set z,x,y base pointers for this row
    ldx Yk
    +ix256zp hz,MAP  ,Yk
    +ix128zp hx,MAPX ,Yk
    +ix256zp hy,MAPY2,Yk

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
    
    rts
}



    











