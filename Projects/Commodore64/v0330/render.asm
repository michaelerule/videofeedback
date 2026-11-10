render:
!zone {

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; prepare_paint_map
    
    ; Get screen RAM index pointers
    ; "t": offset ±8 stride increments
    ; "i": start/end of fwd/rev rows
    ldy Yk
    +st16tabLHyzp rrtab_lo,rrtab_hi,hir
    +st16tabLHyzp rftab_lo,rftab_hi,hif
    +st16tabLHyzp tfset_lo,tfset_hi,htf
    +st16tabLHyzp trset_lo,trset_hi,htr
    
    ; Get dither depending on scanline
    ; Page align saves lo byte write 
    ; !! cannot use low as scratch !!
    tya
    and #1
    beq+
        ;+st8zp hp0+1,>pallet00
        ;+st8zp hp1+1,>pallet10
        ;+st8zp hp2+1,>pallet20
        ;+st8zp hp3+1,>pallet30
        +st16zp hp0,pallet00
        +st16zp hp1,pallet10
        +st16zp hp2,pallet20
        +st16zp hp3,pallet30
        jmp ++
    :+
        ;+st8zp hp0+1,>pallet01
        ;+st8zp hp1+1,>pallet11
        ;+st8zp hp2+1,>pallet21
        ;+st8zp hp3+1,>pallet31
        +st16zp hp0,pallet01
        +st16zp hp1,pallet11
        +st16zp hp2,pallet21
        +st16zp hp3,pallet31
    :++
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    jsr hmap
    jsr hpaint
    
    rts
}



    











