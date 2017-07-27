#!/usr/bin/env jython
# coding=utf8

from java.awt       import Color,BorderLayout,FlowLayout,Cursor,GridBagConstraints,GridBagLayout,Toolkit,Point,Robot
from java.awt.event import MouseAdapter, MouseListener, MouseMotionListener, ComponentAdapter
from java.awt.image import BufferedImage
from java.awt.Color import BLACK,WHITE,YELLOW,RED
from java.awt.geom  import Line2D,Ellipse2D
from time           import sleep,clock
from cmath          import *
from jarray         import array,zeros
from threading      import Thread
from java.lang      import System
from javax.imageio  import ImageIO
from java.io        import File
from javax.swing    import JFrame, JPanel
from java.awt import RenderingHints

def cleantext(g):
	try:
		g.setRenderingHint(
		    RenderingHints.KEY_TEXT_ANTIALIASING,
        	RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
	except:
		g.setRenderingHint(
		    RenderingHints.KEY_TEXT_ANTIALIASING,
		    RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
	g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)
	return g

b = BufferedImage(300,200,BufferedImage.TYPE_INT_RGB)
g = b.graphics
x = 4.0
for i in range(5,20):
	x += 9
	y = 15
	g.color = WHITE
	g.draw(Ellipse2D.Double(x-9,y-9,2*9,2*9))
	g.fill(Ellipse2D.Double(x-4,y-4,8,8))
	g.draw(Line2D.Float(x-9,y,x+9,y))
	g.draw(Line2D.Float(x,y-9,x,y+9))
	y = 45
	g.fill(Ellipse2D.Double(x-9,y-9,2*9,2*9))
	g.color = BLACK
	g.fill(Ellipse2D.Double(x-4,y-4,8,8))
	g.draw(Line2D.Float(x-9,y,x+9,y))
	g.draw(Line2D.Float(x,y-9,x,y+9))
	x += 9*3+5
cleantext(g)
x = 4.0
sx = 0.55
sy = 0.45
for i in range(5,20):
	x += 9
	y = 75
	g.color = RED
	g.fill(Ellipse2D.Double(x-11+sx,y-11+sy,2*11,2*11))
	g.color = WHITE
	g.draw(Ellipse2D.Double(x-9+sx,y-9+sy,2*9,2*9))
	g.fill(Ellipse2D.Double(x-4+sx,y-4+sy,8,8))
	g.draw(Line2D.Float(x-9+sx,y+sy,x+9+sx,y+sy))
	g.draw(Line2D.Float(x+sx,y-9+sy,x+sx,y+9+sy))
	y = 105
	g.fill(Ellipse2D.Double(x-9+sx,y-9+sy,2*9,2*9))
	g.color = BLACK
	g.fill(Ellipse2D.Double(x-4+sx,y-4+sy,8,8))
	g.draw(Line2D.Float(x-9+sx,y+sy,x+9,y+sy))
	g.draw(Line2D.Float(x+sx,y-9+sy,x+sx,y+9+sy))
	x += 14


class Test(JPanel):
	def __init__(self):
		self.preferredSize = (3*300,3*200)
	def paintComponent(self,g):
		g.color = BLACK
		g.drawImage(b,0,0,self.width,self.height,None)
		
		
j =  JFrame('test',defaultCloseOperation=JFrame.EXIT_ON_CLOSE,resizable=0)
j.contentPane = Test()
j.pack()
j.visible = 1

