switch(GRADIENT) {
	case 0:
		break;
	case 1: {
		int alpha = col*256/WIDTH*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
		alpha = alpha<0?0:alpha>0xff?0xff:alpha;
		c = CLIRP32(alpha,0,c);
		}
		break;
	case 2: {
		int alpha = row*256/HEIGHT*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
		alpha = alpha<0?0:alpha>0xff?0xff:alpha;
		c = CLIRP32(alpha,0,c);
		}
		break;
	case 3: {
		int alpha = ((row-HEIGHT/2)*256/HEIGHT*GRADIENT_BIAS+(col-WIDTH/2)*256/WIDTH*GRADIENT_GAIN>>7);
		alpha = alpha*alpha>>8;
		alpha = alpha<0?0:alpha>0xff?0xff:alpha;
		c = CLIRP32(alpha,0,c);
		}
		break;
	case 4: {
		int alpha = (row-HEIGHT/2)*512/HEIGHT*GRADIENT_BIAS >> 6;
		int beta  = (col-WIDTH /2)*512/WIDTH *GRADIENT_GAIN >> 6;
		alpha = alpha*alpha + beta*beta >> 8;
		alpha = alpha<0?0:alpha>0xff?0xff:alpha;
		c = CLIRP32(alpha,0,c);
		}
		break;
	case 5: {
		if (abs((row-HEIGHT/2)*512/HEIGHT)>abs(GRADIENT_BIAS)) c=0;
		if (abs((col-WIDTH /2)*512/WIDTH )>abs(GRADIENT_GAIN)) c=0;
		}
		break;
	default:
		break;
}


if (Recurrent.INVERT==1) c=~c;
switch(DUPLICATION) {
	case 0:
		bufferOut.setElem(i,c);
	break;
	case 1://mirror: write
		bufferOut.setElem(i,c);
		bufferOut.setElem(row*WIDTH+WIDTH-1-col,c);
	break;
	case 2://julia
		bufferOut.setElem(i,c);
		if (row>0)
		bufferOut.setElem((HEIGHT-row)*WIDTH+WIDTH-1-col,c);
	break;
	case 3://quadrants
		bufferOut.setElem(i,c);
		int ty1 = (HEIGHT-1)*WIDTH-row*WIDTH;
		int tx1 = WIDTH-1-col;
		bufferOut.setElem(row*WIDTH+tx1,c);
		bufferOut.setElem(ty1+tx1,c);
		bufferOut.setElem(ty1+col,c);
	break;
	case 4://gohsts
		c = (c>>1)&0x7f7f7f;
		int tx = row*WIDTH+WIDTH-1-col;
		if (col>halfway) c += bufferOut.getElem(i);
		bufferOut.setElem(i,c);
		bufferOut.setElem(tx,c);
	break;
}
