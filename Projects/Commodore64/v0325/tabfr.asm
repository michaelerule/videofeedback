!align 255,0
!align 255,0
YFEVE:
	!byte $00,$02,$04,$06,$08,$0A,$0C,$0E,$10,$12,$14,$16,$18,$1A,$1C,$1E
	!byte $20,$22,$24,$26,$28,$2A,$2C,$2E,$30,$32,$34,$36,$38,$3A,$3C,$3E
	!byte $40,$42,$44,$46,$48,$4A,$4C,$4E,$50,$52,$54,$56,$58,$5A,$5C,$5E
	!byte $60,$62,$64,$66,$68,$6A,$6C,$6E,$70,$72,$74,$76,$78,$7A,$7C,$7E
	!byte $80,$82,$84,$86,$88,$8A,$8C,$8E,$90,$92,$94,$96,$98,$9A,$9C,$9E
	!byte $A0,$A2,$A4,$A6,$A8,$AA,$AC,$AE,$B0,$B2,$B4,$B6,$B8,$BA,$BC,$BE
	!byte $C0,$C2,$C4,$C6,$C8,$CA,$CC,$CE,$D0,$D2,$D4,$D6,$D8,$DA,$DC,$DE
	!byte $E0,$E2,$E4,$E6,$E8,$EA,$EC,$EE,$F0,$F2,$F4,$F6,$F8,$FA,$FC,$FE

!align 255,0
YREVE:
	!byte $FE,$FC,$FA,$F8,$F6,$F4,$F2,$F0,$EE,$EC,$EA,$E8,$E6,$E4,$E2,$E0
	!byte $DE,$DC,$DA,$D8,$D6,$D4,$D2,$D0,$CE,$CC,$CA,$C8,$C6,$C4,$C2,$C0
	!byte $BE,$BC,$BA,$B8,$B6,$B4,$B2,$B0,$AE,$AC,$AA,$A8,$A6,$A4,$A2,$A0
	!byte $9E,$9C,$9A,$98,$96,$94,$92,$90,$8E,$8C,$8A,$88,$86,$84,$82,$80
	!byte $7E,$7C,$7A,$78,$76,$74,$72,$70,$6E,$6C,$6A,$68,$66,$64,$62,$60
	!byte $5E,$5C,$5A,$58,$56,$54,$52,$50,$4E,$4C,$4A,$48,$46,$44,$42,$40
	!byte $3E,$3C,$3A,$38,$36,$34,$32,$30,$2E,$2C,$2A,$28,$26,$24,$22,$20
	!byte $1E,$1C,$1A,$18,$16,$14,$12,$10,$0E,$0C,$0A,$08,$06,$04,$02,$00

!align 255,0
YFODD:
	!byte $01,$03,$05,$07,$09,$0B,$0D,$0F,$11,$13,$15,$17,$19,$1B,$1D,$1F
	!byte $21,$23,$25,$27,$29,$2B,$2D,$2F,$31,$33,$35,$37,$39,$3B,$3D,$3F
	!byte $41,$43,$45,$47,$49,$4B,$4D,$4F,$51,$53,$55,$57,$59,$5B,$5D,$5F
	!byte $61,$63,$65,$67,$69,$6B,$6D,$6F,$71,$73,$75,$77,$79,$7B,$7D,$7F
	!byte $81,$83,$85,$87,$89,$8B,$8D,$8F,$91,$93,$95,$97,$99,$9B,$9D,$9F
	!byte $A1,$A3,$A5,$A7,$A9,$AB,$AD,$AF,$B1,$B3,$B5,$B7,$B9,$BB,$BD,$BF
	!byte $C1,$C3,$C5,$C7,$C9,$CB,$CD,$CF,$D1,$D3,$D5,$D7,$D9,$DB,$DD,$DF
	!byte $E1,$E3,$E5,$E7,$E9,$EB,$ED,$EF,$F1,$F3,$F5,$F7,$F9,$FB,$FD,$FF

!align 255,0
YRODD:
	!byte $FF,$FD,$FB,$F9,$F7,$F5,$F3,$F1,$EF,$ED,$EB,$E9,$E7,$E5,$E3,$E1
	!byte $DF,$DD,$DB,$D9,$D7,$D5,$D3,$D1,$CF,$CD,$CB,$C9,$C7,$C5,$C3,$C1
	!byte $BF,$BD,$BB,$B9,$B7,$B5,$B3,$B1,$AF,$AD,$AB,$A9,$A7,$A5,$A3,$A1
	!byte $9F,$9D,$9B,$99,$97,$95,$93,$91,$8F,$8D,$8B,$89,$87,$85,$83,$81
	!byte $7F,$7D,$7B,$79,$77,$75,$73,$71,$6F,$6D,$6B,$69,$67,$65,$63,$61
	!byte $5F,$5D,$5B,$59,$57,$55,$53,$51,$4F,$4D,$4B,$49,$47,$45,$43,$41
	!byte $3F,$3D,$3B,$39,$37,$35,$33,$31,$2F,$2D,$2B,$29,$27,$25,$23,$21
	!byte $1F,$1D,$1B,$19,$17,$15,$13,$11,$0F,$0D,$0B,$09,$07,$05,$03,$01


YF0 = YFEVE-3
YR0 = YREVE-3
YF1 = YFODD-3
YR1 = YRODD-3

!align 255,0
tfset_lo:
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)
	!byte <(YF0 + 0 + 3)
	!byte <(YF1 + 0 + 3)
	!byte <(YF0 + 1 + 3)
	!byte <(YF1 + 1 + 3)
	!byte <(YF0 + 2 + 3)
	!byte <(YF1 + 2 + 3)
	!byte <(YF0 + 3 + 3)
	!byte <(YF1 + 3 + 3)

!align 255,0
tfset_hi:
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)
	!byte >(YF0 + 0 + 3)
	!byte >(YF1 + 0 + 3)
	!byte >(YF0 + 1 + 3)
	!byte >(YF1 + 1 + 3)
	!byte >(YF0 + 2 + 3)
	!byte >(YF1 + 2 + 3)
	!byte >(YF0 + 3 + 3)
	!byte >(YF1 + 3 + 3)

!align 255,0
trset_lo:
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)
	!byte <(YR1 + 0 + 3)
	!byte <(YR0 + 0 + 3)
	!byte <(YR1 + 1 + 3)
	!byte <(YR0 + 1 + 3)
	!byte <(YR1 + 2 + 3)
	!byte <(YR0 + 2 + 3)
	!byte <(YR1 + 3 + 3)
	!byte <(YR0 + 3 + 3)

!align 255,0
trset_hi:
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)
	!byte >(YR1 + 0 + 3)
	!byte >(YR0 + 0 + 3)
	!byte >(YR1 + 1 + 3)
	!byte >(YR0 + 1 + 3)
	!byte >(YR1 + 2 + 3)
	!byte >(YR0 + 2 + 3)
	!byte >(YR1 + 3 + 3)
	!byte >(YR0 + 3 + 3)

!align 255,0
rftab_lo:
	!byte $20,$20,$20,$20,$20,$20,$20,$20,$60,$60,$60,$60,$60,$60,$60,$60
	!byte $A0,$A0,$A0,$A0,$A0,$A0,$A0,$A0,$E0,$E0,$E0,$E0,$E0,$E0,$E0,$E0
	!byte $20,$20,$20,$20,$20,$20,$20,$20,$60,$60,$60,$60,$60,$60,$60,$60
	!byte $A0,$A0,$A0,$A0,$A0,$A0,$A0,$A0,$E0,$E0,$E0,$E0,$E0,$E0,$E0,$E0
	!byte $20,$20,$20,$20,$20,$20,$20,$20

!align 255,0
rftab_hi:
	!byte $EF,$EF,$EF,$EF,$EF,$EF,$EF,$EF,$F0,$F0,$F0,$F0,$F0,$F0,$F0,$F0
	!byte $F1,$F1,$F1,$F1,$F1,$F1,$F1,$F1,$F2,$F2,$F2,$F2,$F2,$F2,$F2,$F2
	!byte $F4,$F4,$F4,$F4,$F4,$F4,$F4,$F4,$F5,$F5,$F5,$F5,$F5,$F5,$F5,$F5
	!byte $F6,$F6,$F6,$F6,$F6,$F6,$F6,$F6,$F7,$F7,$F7,$F7,$F7,$F7,$F7,$F7
	!byte $F9,$F9,$F9,$F9,$F9,$F9,$F9,$F9

!align 255,0
rrtab_lo:
	!byte $E0,$E0,$E0,$E0,$E0,$E0,$E0,$E0,$A0,$A0,$A0,$A0,$A0,$A0,$A0,$A0
	!byte $60,$60,$60,$60,$60,$60,$60,$60,$20,$20,$20,$20,$20,$20,$20,$20
	!byte $E0,$E0,$E0,$E0,$E0,$E0,$E0,$E0,$A0,$A0,$A0,$A0,$A0,$A0,$A0,$A0
	!byte $60,$60,$60,$60,$60,$60,$60,$60,$20,$20,$20,$20,$20,$20,$20,$20
	!byte $E0,$E0,$E0,$E0,$E0,$E0,$E0,$E0

!align 255,0
rrtab_hi:
	!byte $ED,$ED,$ED,$ED,$ED,$ED,$ED,$ED,$EC,$EC,$EC,$EC,$EC,$EC,$EC,$EC
	!byte $EB,$EB,$EB,$EB,$EB,$EB,$EB,$EB,$EA,$EA,$EA,$EA,$EA,$EA,$EA,$EA
	!byte $E8,$E8,$E8,$E8,$E8,$E8,$E8,$E8,$E7,$E7,$E7,$E7,$E7,$E7,$E7,$E7
	!byte $E6,$E6,$E6,$E6,$E6,$E6,$E6,$E6,$E5,$E5,$E5,$E5,$E5,$E5,$E5,$E5
	!byte $E3,$E3,$E3,$E3,$E3,$E3,$E3,$E3

!align 255,0
NEXT4:
	!byte $04,$05,$06,$07,$08,$09,$0A,$0B,$0C,$0D,$0E,$0F,$10,$11,$12,$13
	!byte $14,$15,$16,$17,$18,$19,$1A,$1B,$1C,$1D,$1E,$1F,$20,$21,$22,$23
	!byte $24,$25,$26,$27,$28,$29,$2A,$2B,$2C,$2D,$2E,$2F,$30,$31,$32,$33
	!byte $34,$35,$36,$37,$38,$39,$3A,$3B,$3C,$3D,$3E,$3F,$40,$41,$42,$43
	!byte $44,$45,$46,$47,$48,$49,$4A,$4B,$4C,$4D,$4E,$4F,$50,$51,$52,$53
	!byte $54,$55,$56,$57,$58,$59,$5A,$5B,$5C,$5D,$5E,$5F,$60,$61,$62,$63
	!byte $64,$65,$66,$67,$68,$69,$6A,$6B,$6C,$6D,$6E,$6F,$70,$71,$72,$73
	!byte $74,$75,$76,$77,$78,$79,$7A,$7B,$7C,$7D,$7E,$7F,$80,$81,$82,$83
	!byte $84,$85,$86,$87,$88,$89,$8A,$8B,$8C,$8D,$8E,$8F,$90,$91,$92,$93
	!byte $94,$95,$96,$97,$98,$99,$9A,$9B,$9C,$9D,$9E,$9F,$A0,$A1,$A2,$A3
	!byte $A4,$A5,$A6,$A7,$A8,$A9,$AA,$AB,$AC,$AD,$AE,$AF,$B0,$B1,$B2,$B3
	!byte $B4,$B5,$B6,$B7,$B8,$B9,$BA,$BB,$BC,$BD,$BE,$BF,$C0,$C1,$C2,$C3
	!byte $C4,$C5,$C6,$C7,$C8,$C9,$CA,$CB,$CC,$CD,$CE,$CF,$D0,$D1,$D2,$D3
	!byte $D4,$D5,$D6,$D7,$D8,$D9,$DA,$DB,$DC,$DD,$DE,$DF,$E0,$E1,$E2,$E3
	!byte $E4,$E5,$E6,$E7,$E8,$E9,$EA,$EB,$EC,$ED,$EE,$EF,$F0,$F1,$F2,$F3
	!byte $F4,$F5,$F6,$F7,$F8,$F9,$FA,$FB,$FC,$FD,$FE,$FF,$00,$01,$02,$03

!align 255,0
PREV4:
	!byte $FC,$FD,$FE,$FF,$00,$01,$02,$03,$04,$05,$06,$07,$08,$09,$0A,$0B
	!byte $0C,$0D,$0E,$0F,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$1A,$1B
	!byte $1C,$1D,$1E,$1F,$20,$21,$22,$23,$24,$25,$26,$27,$28,$29,$2A,$2B
	!byte $2C,$2D,$2E,$2F,$30,$31,$32,$33,$34,$35,$36,$37,$38,$39,$3A,$3B
	!byte $3C,$3D,$3E,$3F,$40,$41,$42,$43,$44,$45,$46,$47,$48,$49,$4A,$4B
	!byte $4C,$4D,$4E,$4F,$50,$51,$52,$53,$54,$55,$56,$57,$58,$59,$5A,$5B
	!byte $5C,$5D,$5E,$5F,$60,$61,$62,$63,$64,$65,$66,$67,$68,$69,$6A,$6B
	!byte $6C,$6D,$6E,$6F,$70,$71,$72,$73,$74,$75,$76,$77,$78,$79,$7A,$7B
	!byte $7C,$7D,$7E,$7F,$80,$81,$82,$83,$84,$85,$86,$87,$88,$89,$8A,$8B
	!byte $8C,$8D,$8E,$8F,$90,$91,$92,$93,$94,$95,$96,$97,$98,$99,$9A,$9B
	!byte $9C,$9D,$9E,$9F,$A0,$A1,$A2,$A3,$A4,$A5,$A6,$A7,$A8,$A9,$AA,$AB
	!byte $AC,$AD,$AE,$AF,$B0,$B1,$B2,$B3,$B4,$B5,$B6,$B7,$B8,$B9,$BA,$BB
	!byte $BC,$BD,$BE,$BF,$C0,$C1,$C2,$C3,$C4,$C5,$C6,$C7,$C8,$C9,$CA,$CB
	!byte $CC,$CD,$CE,$CF,$D0,$D1,$D2,$D3,$D4,$D5,$D6,$D7,$D8,$D9,$DA,$DB
	!byte $DC,$DD,$DE,$DF,$E0,$E1,$E2,$E3,$E4,$E5,$E6,$E7,$E8,$E9,$EA,$EB
	!byte $EC,$ED,$EE,$EF,$F0,$F1,$F2,$F3,$F4,$F5,$F6,$F7,$F8,$F9,$FA,$FB

!align 255,0
NEXT8:
	!byte $04,$05,$06,$07,$08,$09,$0A,$0B,$0C,$0D,$0E,$0F,$10,$11,$12,$13
	!byte $14,$15,$16,$17,$18,$19,$1A,$1B,$1C,$1D,$1E,$1F,$20,$21,$22,$23
	!byte $24,$25,$26,$27,$28,$29,$2A,$2B,$2C,$2D,$2E,$2F,$30,$31,$32,$33
	!byte $34,$35,$36,$37,$38,$39,$3A,$3B,$3C,$3D,$3E,$3F,$40,$41,$42,$43
	!byte $44,$45,$46,$47,$48,$49,$4A,$4B,$4C,$4D,$4E,$4F,$50,$51,$52,$53
	!byte $54,$55,$56,$57,$58,$59,$5A,$5B,$5C,$5D,$5E,$5F,$60,$61,$62,$63
	!byte $64,$65,$66,$67,$68,$69,$6A,$6B,$6C,$6D,$6E,$6F,$70,$71,$72,$73
	!byte $74,$75,$76,$77,$78,$79,$7A,$7B,$7C,$7D,$7E,$7F,$80,$81,$82,$83
	!byte $84,$85,$86,$87,$88,$89,$8A,$8B,$8C,$8D,$8E,$8F,$90,$91,$92,$93
	!byte $94,$95,$96,$97,$98,$99,$9A,$9B,$9C,$9D,$9E,$9F,$A0,$A1,$A2,$A3
	!byte $A4,$A5,$A6,$A7,$A8,$A9,$AA,$AB,$AC,$AD,$AE,$AF,$B0,$B1,$B2,$B3
	!byte $B4,$B5,$B6,$B7,$B8,$B9,$BA,$BB,$BC,$BD,$BE,$BF,$C0,$C1,$C2,$C3
	!byte $C4,$C5,$C6,$C7,$C8,$C9,$CA,$CB,$CC,$CD,$CE,$CF,$D0,$D1,$D2,$D3
	!byte $D4,$D5,$D6,$D7,$D8,$D9,$DA,$DB,$DC,$DD,$DE,$DF,$E0,$E1,$E2,$E3
	!byte $E4,$E5,$E6,$E7,$E8,$E9,$EA,$EB,$EC,$ED,$EE,$EF,$F0,$F1,$F2,$F3
	!byte $F4,$F5,$F6,$F7,$F8,$F9,$FA,$FB,$FC,$FD,$FE,$FF,$00,$01,$02,$03

!align 255,0
MA:
	!byte $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
	!byte $00,$00,$00,$00,$00,$00,$00,$00,$02,$04,$07,$09,$0B,$0C,$0E,$10
	!byte $11,$13,$14,$15,$16,$17,$18,$19,$1A,$1B,$1C,$1D,$1E,$1F,$1F,$20
	!byte $21,$21,$22,$22,$23,$24,$24,$25,$25,$26,$26,$26,$27,$27,$28,$28
	!byte $28,$29,$29,$29,$2A,$2A,$2A,$2B

!align 255,0
MB:
	!byte $7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F
	!byte $7F,$7F,$7F,$7F,$7F,$7F,$7F,$7F,$7D,$7B,$78,$76,$74,$73,$71,$6F
	!byte $6E,$6C,$6B,$6A,$69,$68,$67,$66,$65,$64,$63,$62,$61,$60,$60,$5F
	!byte $5E,$5E,$5D,$5D,$5C,$5B,$5B,$5A,$5A,$59,$59,$59,$58,$58,$57,$57
	!byte $57,$56,$56,$56,$55,$55,$55,$54

