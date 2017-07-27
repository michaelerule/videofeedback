#!/usr/bin/env jython

def rescale(v,(a1,b1),(a2,b2)):
	return (v-a1)*(b2-a2)/float(b1-a1)+a2
def screenToComplex(x,y):
	real = rescale(x,(0,W),rlim)
	imag = rescale(y,(0,H),ilim)
	return real+1j*imag
def complexToScreen(z):
	real = z.real
	imag = z.imag
	x = rescale(real,rlim,(0,W))
	y = rescale(imag,ilim,(0,H))
	return int(x),int(y)
