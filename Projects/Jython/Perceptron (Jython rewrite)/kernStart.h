int [] bufferIn = din;
DataBuffer bufferOut  = out .getRaster().getDataBuffer();
DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
int random = rng.nextInt();
int rstart = HEIGHT/nthreads*thn;
int rstop  = thn==nthreads-1? HEIGHT: HEIGHT/nthreads*(thn+1);
int cstart = 0;
int cstop  = WIDTH;
int halfway = WIDTH/2;
switch(DUPLICATION) {
	case 1://mirror: only the left half of screen is scanned, it is then doubled over
		rstart = HEIGHT/nthreads*thn;
		rstop  = thn==nthreads-1? HEIGHT: HEIGHT/nthreads*(thn+1);
		cstart = 0;
		cstop  = WIDTH/2;
	break;
	case 2://julia: only the top hald of the screen is scanned
		rstart = HEIGHT/2/nthreads*thn;
		rstop  = thn==nthreads-1? HEIGHT/2+1: (HEIGHT/2+1)/nthreads*(thn+1);
		cstart = 0;
		cstop  = WIDTH;
	break;
	case 3://quadrants: only the top left quadrant is scanned
		rstart = HEIGHT/2/nthreads*thn;
		rstop  = thn==nthreads-1? HEIGHT/2+1: (HEIGHT/2+1)/nthreads*(thn+1);
		cstart = 0;
		cstop  = WIDTH/2;
	break;
	case 4: //Gohsts
		
	break;
}
