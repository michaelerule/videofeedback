HOTSTART = 2
hotstart:
!pseudopc HOTSTART {

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
hmap:
        ldy Yk
        lda RB,y
        sta <he
        ldx RA,y
        dex
        :-: 
            inx
hy=*+1:     lda $FFFF,x : sta hs
hx=*+1:     ldy $FFFF,x 
hs=*+2:     lda $FF00,y
increment_point:
            adc #1
hz=*+1:     sta $FFFF,x
he=*+1:     cpx #0
        bne-
    rts
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
hpaint:
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

hz0=*+1:    ldy $FFFF,x
hp0=*+1:    lda $FF00,y

hz1=*+1:    ldy $FFFF,x
hp1=*+1:    ora $FF00,y

hz2=*+1:    ldy $FFFF,x
hp2=*+1:    ora $FF00,y

hz3=*+1:    ldy $FFFF,x
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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Re-use zero page registers
; We NEED 
;   u16: X,Y,T 
;   u8 : M,C,B
;   u16: S
; Try to stick to the above temp variables to avoid proliferation?
; Don't use hp* as scratch if lazy about resetting low byte
; hp0 hp1 hp2 hp3
; likewise high byte ok for uint8 I think?

; uint8
C = <(he)
M = <(hep)

; uint16
B=<(hir)    :B_=<(B+1)
D=<(hz0)    :D_=<(D+1)
E=<(hz1)    :E_=<(E+1)
F=<(hz2)    :F_=<(F+1)
G=<(hz3)    :G_=<(G+1)
I=<(hz0)    :I_=<(I+1)
K=<(htf)    :K_=<(K+1)
S=<(hif)    :S_=<(S+1)
T=<(htr)    :T_=<(T+1)
X=<(hx)     :X_=<(X+1)
Y=<(hy)     :Y_=<(Y+1)
;Z=<(hζ)     :Z_=<(Z+1)
Z: !word 0

; Available:
;    u8 hε, hs, high byte of hp*
; (H,W are compile time constants)

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
!if hep>255 {!error "zp pointer overspill: ",hep}
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















