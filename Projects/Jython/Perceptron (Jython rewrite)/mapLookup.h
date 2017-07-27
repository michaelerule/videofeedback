int x1 = mapping[2*i];
int y1 = mapping[2*i+1];
int x = T1*x1+T2*y1+T3+0x80>>8;
int y = T4*x1+T5*y1+T6+0x80>>8;		
switch (BOUNDARY)
{
	case 0://mirror
	    while (x<W_82) x+=W_82;
	    while (y<H_82) y+=H_82;
	    x=(x/W8&1)==1?x%W8:(W8-1)-x%W8;
	    y=(y/H8&1)==1?y%H8:(H8-1)-y%H8;
	    break;
	case 1://torus
	    while (x<W8)x+=W8;
	    while (y<H8)y+=H8;
		x%=W8;
		y%=H8;
		break;
	case 2://extend
		x = x<0?0:x>W8?W8:x;
		y = y<0?0:y>H8?H8:y;
		break;
	case 3://circle
	case 4://square
}
