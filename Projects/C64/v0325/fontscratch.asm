

FONT_HEIGHT = 16

EQUATION_RAW_WIDTH = 56
EQUATION_PAD_WIDTH = 56
	!byte 56
	!byte 56
EQUATION:
	!byte $FF,$FF,$FF,$FF,$FF,$95,$69,$F6,$FF,$FF,$FF,$FF,$FF,$FF,$FE,$F9
	!byte $FF,$FF,$FF,$FF,$FF,$FE,$FD,$FF,$FF,$FF,$FF,$FF,$FF,$57,$A7,$DB
	!byte $EF,$9B,$67,$B7,$F7,$DB,$6F,$57,$FF,$FF,$FF,$FF,$FF,$EF,$DF,$DF
	!byte $FF,$FF,$FF,$FF,$FF,$E5,$9D,$7F,$E7,$DB,$9F,$6A,$55,$FF,$FF,$FF
	!byte $E6,$D5,$E6,$E9,$FA,$FE,$FF,$FF,$FF,$5F,$AE,$FD,$FD,$FF,$FF,$FF
	!byte $9F,$6F,$7F,$AB,$57,$FF,$FF,$FF,$FF,$FF,$FF,$FF,$FF,$FF,$FF,$FF
	!byte $57,$9B,$DF,$EF,$FF,$FF,$FF,$FF,$7F,$7F,$7F,$9F,$95,$FF,$FF,$FF

REALEQ_RAW_WIDTH = 40
REALEQ_PAD_WIDTH = 40
	!byte 40
	!byte 40
REALEQ:
	!byte $FF,$FF,$99,$65,$77,$77,$67,$A5,$FF,$FF,$BF,$6E,$6D,$6D,$6D,$BD
	!byte $FF,$FF,$BF,$7F,$BF,$FB,$E6,$D9,$FF,$FF,$BF,$6F,$9F,$DF,$DF,$DF
	!byte $FF,$FF,$FF,$FF,$FF,$FF,$FF,$57,$76,$F7,$F7,$E7,$DB,$FF,$FF,$FF
	!byte $6D,$6D,$6D,$59,$6D,$FD,$FE,$FF,$DF,$DF,$DF,$E5,$FB,$BF,$7F,$BF
	!byte $DF,$DF,$DF,$DF,$DF,$9F,$6F,$BF,$AB,$FF,$57,$AB,$FF,$FF,$FF,$FF

IMAGEQ_RAW_WIDTH = 40
IMAGEQ_PAD_WIDTH = 40
	!byte 40
	!byte 40
IMAGEQ:
	!byte $FF,$FF,$D6,$59,$7E,$7F,$6D,$DD,$FF,$FF,$6F,$7E,$FE,$6D,$6D,$FD
	!byte $FF,$FF,$BF,$7F,$7F,$FB,$E6,$D9,$FF,$FF,$BF,$6F,$6F,$DF,$DF,$DF
	!byte $FF,$FF,$FF,$FF,$FF,$FF,$FF,$57,$FD,$FD,$FD,$5E,$56,$F5,$FF,$FF
	!byte $BD,$AD,$6D,$6D,$6D,$BE,$FE,$FF,$DF,$DF,$DF,$E5,$FB,$7F,$7F,$BF
	!byte $DF,$DF,$DF,$DF,$DF,$6F,$6F,$BF,$AB,$FF,$57,$AB,$FF,$FF,$FF,$FF

FPSLABEL_RAW_WIDTH = 24
FPSLABEL_PAD_WIDTH = 24
	!byte 24
	!byte 24
FPSLABEL:
	!byte $FF,$FF,$FA,$E5,$DB,$DF,$DF,$D6,$FF,$FF,$FF,$FF,$FF,$FF,$AF,$5B
	!byte $FF,$FF,$FF,$FF,$FF,$FF,$EB,$97,$DA,$DF,$DF,$DF,$DF,$FF,$FF,$FF
	!byte $67,$77,$77,$67,$5B,$7F,$7F,$FF,$6B,$5F,$D7,$A7,$5B,$FF,$FF,$FF

LACELABEL_RAW_WIDTH = 48
LACELABEL_PAD_WIDTH = 48
	!byte 48
	!byte 48
LACELABEL:
	!byte $FF,$FF,$F5,$D6,$DB,$DD,$DD,$DD,$FF,$FF,$7F,$5F,$9F,$DF,$DF,$DF
	!byte $FF,$FB,$B7,$77,$F7,$B7,$77,$77,$FF,$FF,$FF,$FF,$FF,$AF,$5B,$F7
	!byte $FF,$FF,$FF,$FF,$FF,$EF,$9B,$67,$FF,$FF,$FF,$FF,$FF,$EF,$9B,$77
	!byte $DE,$DB,$D6,$D5,$F5,$FF,$FF,$FF,$DF,$9F,$DF,$9F,$7F,$FF,$FF,$FF
	!byte $77,$77,$77,$66,$F9,$FF,$FF,$FF,$97,$67,$77,$9B,$FE,$FF,$FF,$FF
	!byte $7F,$7F,$7E,$97,$EF,$FF,$FF,$FF,$67,$5B,$7F,$97,$FF,$FF,$FF,$FF

LOOPLABEL_RAW_WIDTH = 48
LOOPLABEL_PAD_WIDTH = 48
	!byte 48
	!byte 48
LOOPLABEL:
	!byte $FF,$FF,$F5,$D5,$DF,$DE,$DD,$DE,$FF,$FF,$7F,$5F,$9F,$DF,$DF,$DF
	!byte $FF,$FF,$FF,$FF,$FF,$EF,$9B,$66,$FF,$FF,$FD,$FD,$FD,$ED,$99,$6D
	!byte $FF,$FF,$FE,$FD,$FF,$BE,$6D,$9D,$FF,$FF,$EF,$DF,$DF,$97,$DB,$DF
	!byte $DF,$DD,$DD,$D5,$F5,$FF,$FF,$FF,$9F,$5F,$5F,$5F,$7F,$FF,$FF,$FF
	!byte $77,$77,$67,$9B,$FF,$FF,$FF,$FF,$7D,$7D,$7D,$7D,$FF,$FF,$FF,$FF
	!byte $DD,$DD,$DD,$6D,$BF,$FF,$FF,$FF,$DF,$DF,$DF,$DB,$E7,$FF,$FF,$FF

ZKEY_RAW_WIDTH = 16
ZKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
ZKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$A9,$FD,$6D,$BD
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$F5,$E9,$FD,$55,$57,$FF,$FF,$FF

XKEY_RAW_WIDTH = 16
XKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
XKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$99,$ED,$79,$75
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$B5,$ED,$99,$55,$57,$FF,$FF,$FF

CKEY_RAW_WIDTH = 16
CKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
CKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$65,$B9,$DD,$D5
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$D5,$ED,$B9,$65,$57,$FF,$FF,$FF

VKEY_RAW_WIDTH = 16
VKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
VKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$99,$DD,$DD,$DD
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$ED,$B9,$75,$55,$57,$FF,$FF,$FF

IKEY_RAW_WIDTH = 16
IKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
IKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$65,$75,$65,$75
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$75,$75,$75,$55,$57,$FF,$FF,$FF

JKEY_RAW_WIDTH = 16
JKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
JKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$69,$7D,$5D,$5D
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$5D,$DD,$B9,$65,$57,$FF,$FF,$FF

KKEY_RAW_WIDTH = 16
KKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
KKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$D5,$D9,$DD,$ED
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$F5,$ED,$DD,$55,$57,$FF,$FF,$FF

LKEY_RAW_WIDTH = 16
LKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
LKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$95,$D5,$D5,$D5
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$D5,$E9,$FD,$55,$57,$FF,$FF,$FF

FKEY_RAW_WIDTH = 16
FKEY_PAD_WIDTH = 16
	!byte 16
	!byte 16
FKEY:
	!byte $FF,$FF,$FF,$FD,$FD,$FD,$FD,$FD,$FF,$FF,$57,$55,$A9,$FD,$D5,$E5
	!byte $FD,$FD,$FD,$FD,$FF,$FF,$FF,$FF,$F5,$D5,$D5,$55,$57,$FF,$FF,$FF



