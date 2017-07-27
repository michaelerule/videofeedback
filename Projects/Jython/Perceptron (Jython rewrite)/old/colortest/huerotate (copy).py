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

rotated = BufferedImage(w,h,BufferedImage.TYPE_INT_RGB)

sqrt3 = sqrt(3.0)

for y in range(h):
	for x in range(w):
		rgb = Color(image.getRGB(x,y))
		r   = rgb.red  *0.00392156862745098
		g   = rgb.green*0.00392156862745098
		b   = rgb.blue *0.00392156862745098
		
		alpha = 0.5*(2*r-g-b)
		beta  = 0.5*sqrt3*(g-b)
		intensity = (r+g+b)/3.
		
		H = atan2(beta,alpha)
		C = sqrt(beta**2+alpha**2)
		H += 1
		beta = C*sin(H)
		alpha = C*cos(H)
		
		r = intensity + alpha*2/3
		b = 0.5*(3*intensity-r-2/sqrt3*beta)
		g = 2/sqrt3*beta+b
		
		r = 0 if r<0 else 1 if r>1 else r
		g = 0 if g<0 else 1 if g>1 else g
		b = 0 if b<0 else 1 if b>1 else b
		
		rotated.setRGB(x,y,Color(r,g,b).getRGB())

j = JFrame('hue rotate test',defaultCloseOperation=JFrame.EXIT_ON_CLOSE)
j.contentPane.layout = GridLayout(1,2)
j.contentPane.add(JLabel(ImageIcon(image)))
j.contentPane.add(JLabel(ImageIcon(rotated)))
j.pack()
j.visible=1


