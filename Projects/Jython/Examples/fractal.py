#!/usr/bin/env jython
# -*- coding: UTF-8 -*-

from javax.swing import *
from java.awt.image import *

ITERATIONS = 10
SIZE       = 4
CENTER     = complex(1,1)*SIZE*0.5
ESCAPE     = 2**2
FUNCTION   = lambda z,zo:z**2+zo
PIXELATE   = 1

def escape(z,n):
	zo = complex(z)
	i = 0
	while i<n and z.real**2+z.imag**2<ESCAPE:
		z = FUNCTION(z,zo)
		i = i+1
	scale = 0xff*i/n
	return scale|(scale<<8)|(scale<<16)	

def screen2complex(x,y):
	return complex(x,y)*SIZE-CENTER

class Fractal(JPanel):
	buffer = None
	def paintComponent(self,g):
		if g:
			w,h=self.width/PIXELATE,self.height/PIXELATE
			sw,sh=1./w,1./h
			if not self.buffer or not (w,h) is (self.buffer.width,self.buffer.height):
				B = BufferedImage(w,h,BufferedImage.TYPE_INT_RGB)
				for y in xrange(h):
					for x in xrange(w):
						B.setRGB(x,y,escape(screen2complex(x*sw,y*sh),ITERATIONS))
				self.buffer = B		
			g.drawImage(self.buffer,0,0,self.width,self.height,None)

jf = JFrame('Demo',
	defaultCloseOperation=JFrame.EXIT_ON_CLOSE,
	contentPane=Fractal(),
	size=(300,300))
jf.visible = 1



