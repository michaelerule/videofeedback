HOTSTART = 2
hotstart:
!pseudopc HOTSTART {

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Persistent allocations
DX: !byte 0
DY: !byte 0
Yk: !byte 0
ninterlace: !byte 1
IINTERLACE: !byte NPALLET-1
iinterlace: !byte 0
_ilace_   : !byte 0
pallet_number: !byte 0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!zone{
hmap:
        ; Set z,x,y base pointers for this row
        ldx Yk
        +ix128zp .x,MAPX ,Yk
        +ix256zp .y,MAPY2,Yk
        +ix256zp .z,MAP  ,Yk

        ; Shift loop termination: yxz must move
        ldy Yk
        ldx RA,y
        txa:clc:adc .x:sta .x
        txa:clc:adc .y:sta .y
        txa:clc:adc .z:sta .z
        
        ; Shift loop termination: 
        ; All pointers are +RA now
        ; Old code rendered [RA,RB]
        ; Must render [0,RB-RA]
        lda RB,y
        sec
        sbc RA,y
        tax
        clc
                
        :-: 
.y=*+1:     lda $FFFF,x : sta .g
.x=*+1:     ldy $FFFF,x 
.g=*+2:     lda $FF00,y
            adc #1
.z=*+1:     sta $FFFF,x
            dex
        bpl-
    rts
    
; reusable as scratch
_volatile_0_=.y
_volatile_1_=.x
_volatile_2_=.g
_volatile_3_=.z
}


!zone {
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
hpaint:
        ; 4 pixel load base pointers
        +ix256zp .z0,MAP,Yk
        +t16zp .z0,.z1
        inc .z1
        +t16zp .z1,.z2
        inc .z2
        +t16zp .z2,.z3
        inc .z3

        ; start=floor(min(RA,PA))
        ldy Yk
        lda RA,y
        cmp PA,y:bcc+:lda PA,y:+
        and #$FC
        sta <hep
        
        ; end=ceil(max(RB,PB))
        lda RB,y
        cmp PB,y:bcs+:lda PB,y:+
        ora #3
        tax
        inx
        
        :-:
            lda PREV4,x
            tax 

.z0=*+1:    ldy $FFFF,x
hp0=*+1:    lda $FF00,y

.z1=*+1:    ldy $FFFF,x
hp1=*+1:    ora $FF00,y

.z2=*+1:    ldy $FFFF,x
hp2=*+1:    ora $FF00,y

.z3=*+1:    ldy $FFFF,x
hp3=*+1:    ora $FF00,y

htf=*+1:    ldy $FFFF,x
hif=*+1:    sta $FFFF,y

            tay 
            lda RV,y
htr=*+1:    ldy $FFFF,x
hir=*+1:    sta $FFFF,y

hep=*+1:    cpx #0 
        bne -
rts

_volatile_4_=<(.z0)
_volatile_5_=<(.z1)
_volatile_6_=<(.z2)
_volatile_7_=<(.z3)
_volatile_8_=<(hp0)
_volatile_9_=<(hp1)
_volatile_A_=<(hp2)
_volatile_B_=<(hp3)
_volatile_C_=<(hif)
_volatile_D_=<(hir)
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; volatile (overlapping) allocations
M= <(_volatile_0_)
T= <(_volatile_1_)
B= <(_volatile_2_)
C= <(_volatile_3_)
D= <(_volatile_4_)
E= <(_volatile_5_)
F= <(_volatile_6_)
G= <(_volatile_7_)
I= <(_volatile_8_)
S= <(_volatile_9_)
X= <(_volatile_A_)
Y= <(_volatile_B_)
Z= <(_volatile_C_)
K= <(_volatile_D_)
K_=<(K+1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!if _final_smc_label_>255 {!error "zp pointer overspill: ",_final_smc_label_}
HOTSTOP = *
HOTSIZE = HOTSTOP - HOTSTART
!warn "@ HOTZONE START ",HOTSTART
!warn "@ HOTZONE SIZE  ",HOTSIZE
!warn "@ HOTZONE STOP  ",HOTSTOP
!warn "@ HOTZONE FREE  ",256-HOTSTOP
!if HOTSTOP > 255 {!warn "-!!Zero page code spills into stack"}
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

}
hotend:















