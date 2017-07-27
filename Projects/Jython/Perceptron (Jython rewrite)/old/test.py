#!/usr/bin/env jython

print 'starting',
execfile('Splash.py')

import Kernel as Kernel
from java.awt       import Color,BorderLayout
from java.awt.event import MouseAdapter, MouseListener, MouseMotionListener, ComponentAdapter
from java.awt.image import BufferedImage
from javax.swing    import JFrame, JPanel, JButton
from time           import sleep,clock
from math           import sin,cos,exp,e,pi
from jarray         import array,zeros
from threading      import Thread

print 'Imports done'

# define the problem
W,H = 800,800
LENGTH = W*H
rlim = (-4,4)
ilim = (-4,4)
FIXED = 256

execfile('./PointUtil.py')
execfile('./Sliders.py')

INVERT     = 1
HUE        = 0
LIGHTEN    = 0
DARKEN     = 0
BRIGHTNESS = 128
CONTRAST   = 0x40
ROTATE     = 0
SCALE      = 0x80
TX         = 0
TY         = 0
SATURATION = 0
MOTIONBLUR = 0
THREADS    = 0x100
SHARPEN    = 0
BLUR       = 0
NOISE      = 0

def setTransform():
	rotation = ROTATE*2*pi/0x100
	scale    = float(SCALE)/0x80
	tx       = rescale(TX,(0,0x100),(0,W))
	ty       = rescale(TY,(0,0x100),(0,H))
	ct = int(cos(rotation)*FIXED*scale)
	st = int(sin(rotation)*FIXED*scale)
	tx = int((tx+W/2)*FIXED*FIXED)
	ty = int((ty+H/2)*FIXED*FIXED)
	Kernel.T1 = ct
	Kernel.T2 = st
	Kernel.T3 = tx
	Kernel.T4 = -st
	Kernel.T5 = ct
	Kernel.T6 = ty

def setMap(function):
	mapdata = []
	for y in range(H):
		for x in range(W):
			mx,my = complexToScreen(function(screenToComplex(x,y)))
			mx = int((mx-W/2)*0x100)
			my = int((my-H/2)*0x100)
			mapdata.append(mx)
			mapdata.append(my)
	Kernel.mapping = array(mapdata,'i')

def setKernelParameters():
	for var in 'HUE SATURATION LIGHTEN DARKEN BRIGHTNESS CONTRAST MOTIONBLUR THREADS BLUR NOISE'.split():
		if hasattr(Kernel,var):
			eval('Kernel.%s = %s)'%(var,var))
	Kernel.INVERT = 0xffffff if INVERT else 0
	setTransform()

view = JFrame('test_view')
view.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
view.contentPane.setLayout(BorderLayout())
viewpane = JPanel()
viewpane.preferredSize = (W,H)
view.contentPane.add(viewpane,BorderLayout.CENTER)
controls = Sliders('HUE SATURATION LIGHTEN DARKEN BRIGHTNESS CONTRAST ROTATE SCALE TX TY MOTIONBLUR THREADS BLUR NOISE')
j1 = JPanel(BorderLayout())
j1.preferredSize = (300,H)
j1.add(controls,BorderLayout.CENTER)
j1.add(controls.labels,BorderLayout.WEST)
view.contentPane.add(j1,BorderLayout.EAST)
viewpane.preferredSize = (W,H)
viewpane.size = (W,H)
view.pack()
view.resizable=0 
view.setLocation( (dm.width-view.width)/2, (dm.height-view.height)/2)

Kernel.prepare(W,H)
setMap(lambda z:z)

view.visible=1
splash.visible=0

ptime = clock()
frate = 0

while 1:
	
	NTHREADS = THREADS*16/0x100+1
	BSIZE    = LENGTH/NTHREADS
	
	setKernelParameters()
	Kernel.convolve()

	def callback(istart,istop):
		return lambda:Kernel.render(istart,istop)
	renderers = [Thread(target=callback(i*BSIZE,(i+1)*BSIZE)) for i in range(NTHREADS)]
	[s.start() for s in renderers]
	[s.join()  for s in renderers]
	bufferIn,bufferOut = bufferOut,bufferIn
	
	g = bufferIn.graphics
	g.color = Color.YELLOW
	g.drawRect(0,0,W-1,H-1)
	g.color = Color.RED
	g.drawRect(0,0,W,H)
	g.color = Color.BLACK
	g.drawRect(1,1,W-2,H-2)
	
	viewpane.graphics.drawImage(bufferIn,0,0,None)
	
	ntime = clock()
	frate = 0.9*frate + 0.1/(ntime-ptime)
	print frate
	ptime = ntime


