#!/usr/bin/env jython

from java.awt import Color,GridLayout
from java.awt.image import BufferedImage
from java.io import File
from javax.imageio import ImageIO
from java.lang.Math import *
from javax.swing import JFrame,JPanel,ImageIcon,JLabel
import sys

image = ImageIO.read(File(sys.argv[1]))
w = image.width
h = image.height
n = w*h

j = JFrame('hue rotate test',defaultCloseOperation=JFrame.EXIT_ON_CLOSE)
j.contentPane.layout = GridLayout(3,4)

sqrt3 = sqrt(3.0)

for theta in range(12):
	
	th = 0.5*theta

	Q1 = sin(th)/sqrt(3)
	Q2 = (1-cos(th))/3
	
	rotated = BufferedImage(w,h,BufferedImage.TYPE_INT_RGB)

	for y in range(h):
		for x in range(w):
			rgb = Color(image.getRGB(x,y))
			r   = rgb.red  *0.00392156862745098
			g   = rgb.green*0.00392156862745098
			b   = rgb.blue *0.00392156862745098
			
			rb = r-b
			gr = g-r
			bg = b-g
			Z  = Q2*(bg-rb)+Q1*gr
			r1 = Q2*(gr-rb)-Q1*bg+r
			g1 = g+Z + (r-r1)
			b1 = b-Z
			
			r1 = 0 if r1<0 else 1 if r1>1 else r1
			g1 = 0 if g1<0 else 1 if g1>1 else g1
			b1 = 0 if b1<0 else 1 if b1>1 else b1
		
			rotated.setRGB(x,y,Color(r1,g1,b1).getRGB())

	j.contentPane.add(JLabel(ImageIcon(rotated)))

j.pack()
j.visible=1

"""
4 muls and 12 additions
8 muls 12 additions
7 muls 15 additions
intensity = (r+g+g+b)/4
alpha = r-(g+b)/2
beta  = g-b
a2 = Q1*alpha+Q2*beta
b2 = Q4*beta+Q3*alpha
r1 = intensity+a2
b1 = 0.5*(intensity*3-r1-b2)
g1 = b2+b1
"""
'''
r1 = Q0*r + QB*g + QC*b
b1 = QB*r + QC*g + Q0*b
g1 = QC*r + Q0*g + QB*b
'''
'''
I = r+g+b
r1 = Q2*(g+b-2*r) + Q1*(g-b) + r
g1 = Q2*(b+r-2*g) + Q1*(b-r) + g
b1 = Q2*(r+g-2*b) + Q1*(r-g) + b
'''

