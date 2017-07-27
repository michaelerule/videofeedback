#!/usr/bin/env jython
# coding=utf8

print "Starting"
execfile('Splash.py')

from java.awt       import Color,BorderLayout,FlowLayout,Cursor,GridBagConstraints,GridBagLayout,Toolkit,Point,Robot
from java.awt.event import MouseAdapter, MouseListener, MouseMotionListener, ComponentAdapter
from java.awt.image import BufferedImage
from java.awt.Color import BLACK,WHITE,YELLOW,RED
from java.awt.geom  import Line2D,Ellipse2D
from time           import sleep,clock
from cmath          import *
from math           import atan2
from jarray         import array,zeros
from threading      import Thread
from java.lang      import System
from javax.imageio  import ImageIO
from java.io        import File
import random

#static boolean ImageIO.write(im,formatName,output)  throws IOException

execfile('config.py')
execfile('UIElements.py')

def rescale(v,(a1,b1),(a2,b2)):
	return (v-a1)*(b2-a2)/float(b1-a1)+a2

robot = Robot()
Kernel = None

crosshairs  = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB),Point(0,0),"blank");
	
def setTransform(Kernel):
	rotation  = ROTATE*2*pi/0x100
	scale     = float(SCALE)/0x80
	tx        = rescale(TX,(0,0x100),(0,W))
	ty        = rescale(TY,(0,0x100),(0,H))
	ct        = int(cos(rotation).real*FIXED*scale+0.5)
	st        = int(sin(rotation).real*FIXED*scale+0.5)
	tx        = int((tx+W/2)*FIXED*FIXED+0.5)
	ty        = int((ty+H/2)*FIXED*FIXED+0.5)
	Kernel.T1 = ct
	Kernel.T2 = st
	Kernel.T3 = tx
	Kernel.T4 = -st
	Kernel.T5 = ct
	Kernel.T6 = ty

def setKernelParameters(Kernel):
	for group,variables in controlVars.iteritems():
		for var in variables.split():
			try:
				exec('Kernel.%s = %s'%(var,globalName(var)))
			except Exception, e:
				pass
				#print e
	for group,variables in optionVars.iteritems():
		try:
			exec('Kernel.%s = %s'%(group,globalName(group)))
		except Exception, e:
			pass
			#print e
	setTransform(Kernel)
	Kernel.Post.CONTRAST        *= 2
	Kernel.Recurrent.CONTRAST   *= 2
	Kernel.Homeostatic.CONTRAST *= 2
	Kernel.Recurrent.INVERT = invertColors
	Kernel.GRADIENT_GAIN = GRADIENT_GAIN
	Kernel.GRADIENT_BIAS = GRADIENT_BIAS

def setMap(function,Kernel):
	def screenToComplex(x,y):
		real = rescale(x,(0,W),rlim)
		imag = rescale(y,(0,H),ilim)
		return real+1j*imag
	def complexToScreen(z):
		real = z.real
		imag = z.imag
		x = rescale(real,rlim,(0,W))
		y = rescale(imag,ilim,(0,H))
		return x,y
	mapdata = []
	for y in range(H):
		for x in range(W):
			try:
				mx,my = complexToScreen(function(screenToComplex(x,y)))
			except:
				mx,my = x,y
			try:
				mx = int((mx-W/2)*FIXED+0.5) 
				my = int((my-H/2)*FIXED+0.5)
				mapdata.append(mx)
				mapdata.append(my)
			except:
				pass
	Kernel.mapping = array(mapdata,'i')

def reloadKernel(e):
	global Kernel
	import sys, os
	kversion = 'Kernel'+str(System.currentTimeMillis())
	kernelsource = list(open(kerneltemplate,'r').readlines())
	configure = [
	'#define KERNELVERSION %s\n'%kversion,
	'#define WIDTH %d\n'%W,
	'#define HEIGHT %d\n'%H]
	outfile = open(kversion+'.c','w')
	outfile.writelines(configure+kernelsource)
	outfile.flush()
	outfile.close()
	os.system("gcc -E ./%s.c | sed '/^\#/d' > %s.java"%(kversion,kversion))
	os.system('javac '+kversion+'.java')
	newKernel =  __import__(kversion)
	newKernel.prepare()
	setKernelParameters(newKernel)
	setMap(MAP,newKernel)
	Kernel = newKernel

class Viewpane(JPanel):
	def __init__(self):
		JPanel.__init__(self,
			FlowLayout(),
			background=BACKGROUND,
			foreground=FOREGROUND,
			preferredSize=(W,H),
			minimumSize=(W,H),
			size=(W,H),
			cursor = crosshairs)
	def paintComponent(self,g):
		global Kernel
		self.super__paintComponent(g)
		cleantext(g)
		message = 'Compiling kernel, please wait'
		if Kernel == None:
			metrics = g.getFontMetrics(g.font)
			H = metrics.height
			W = metrics.stringWidth(message)
			g.color = self.foreground
			g.drawString( message, (self.width-W)/2, (self.height-H)/2)
		else:
			img = Kernel.out
			g.drawImage(img,0,0,None)

class CursorMouse(MouseAdapter):
	def __init__(self):
		self.active = None
		self.jump = None
	def mousePressed(self,e):
		global cursors
		nearest = None
		distance = None
		for i,(x,y) in enumerate(cursors):
			d = (x-e.x)**2+(y-e.y)**2
			print x,y,e.x,e.y,d
			if distance is None or d<distance:
				nearest = i
				distance = d
		if not nearest is None:
			print nearest
			self.active = nearest
			viewpane.cursor = blankCursor
			p = viewpane.getLocationOnScreen()
			x,y = cursors[nearest]
			self.jump = (p.x+x,p.y+y)
	def mouseReleased(self,e):
		self.active = None
		viewpane.cursor = crosshairs
	def mouseDragged(self,e):
		global cursors,TX,TY,ROTATE,SCALE,GRADIENT_GAIN,GRADIENT_BIAS
		if not self.active is None:
			if not self.jump is None:
				robot.mouseMove(*self.jump)
				self.jump = None
			x = max(0,min(W,e.x))
			y = max(0,min(H,e.y))
			cursors[self.active]=(x,y)
			x = x-W/2
			y = y-H/2
			if self.active == 0:
				TX,TY = x,y
			if self.active == 1:
				r = sqrt(x**2+y**2)*2/(W+H)
				th = atan2(y,x)
				SCALE  = (r*256*2).real
				ROTATE = (th*256/(2*pi))#.real
				print ROTATE
			if self.active == 2:
				GRADIENT_GAIN = x*512/W
				GRADIENT_BIAS = y*512/H

def drawCursor(g,x,y,i,c1,c2):
	sx = 0.55
	sy = 0.48
	g.color = c1
	g.fill(Ellipse2D.Float(x-11+sx,y-11+sy,2*11,2*11))
	g.color = c2
	g.draw(Ellipse2D.Float(x-9+sx,y-9+sy,2*9,2*9))
	if i  %2: g.fill(Ellipse2D.Float(x-4+sx,y-4+sy,8,8))
	if i/2%2: g.draw(Line2D.Float(x-9+sx,y+sy,x+9+sx,y+sy))
	if i/4%2: g.draw(Line2D.Float(x+sx,y-9+sy,x+sx,y+9+sy))

def paintCursors(g):
	cleantext(g)
	for i,(x,y) in enumerate(cursors):			
		if cursormouse.active!=i:
			drawCursor(g,x,y,i,BACKGROUND,FOREGROUND)
	if not cursormouse.active is None:
		i = cursormouse.active
		x,y = cursors[i]
		drawCursor(g,x,y,i,FOREGROUND,BACKGROUND)

def save(e):
	global Kernel, doSnap
	if Kernel==None or Kernel.out==None:
		print 'oh hai'
	else:
		outfile = File(saveat+str(System.currentTimeMillis())+'.png')
		print outfile
		ImageIO.write(Kernel.out,'png',outfile)
	doSnap = 0

def queueSave(e):
	global doSnap
	doSnap = 1

def newMap(a):
	from math import sin,cos,exp,sqrt,tan,asin,acos,atan
	global mf,MAP,Kernel
	if '^' in mf.textf.text:
		mf.textf.text = mf.textf.text.replace('^','**')
	print mf.textf.text
	MAP = eval('lambda z:%s'%mf.textf.text)
	setMap(MAP,Kernel)

frate = 0
view = JFrame('Perceptron',
	resizable=0,
	defaultCloseOperation=JFrame.EXIT_ON_CLOSE,
	background=BACKGROUND,
	foreground=FOREGROUND,
	layout=BorderLayout())
view.contentPane.size = SCREENSIZE
view.contentPane.preferredSize = SCREENSIZE	
viewholder = JPanel(GridBagLayout(),background=BACKGROUND,foreground=FOREGROUND)	
viewpane = Viewpane()
viewholder.add(viewpane,GridBagConstraints())
view.contentPane.add(viewholder,BorderLayout.CENTER)
cursormouse = CursorMouse()
viewpane.addMouseListener(cursormouse)
#viewpane.addMouseMotionListener(cursormouse)
viewpane.mouseDragged = lambda a: cursormouse.mouseDragged(a)
controls = Cascade()
for name,variables in controlVars.iteritems():
	sliders = Sliders(variables,Kernel)
	sliders.labels.preferredSize = (LABELW,SLIDERSPACING*len(variables.split()))
	sliders.preferredSize        = (SLIDERW,SLIDERSPACING*len(variables.split()))
	holder = JPanel(BorderLayout())
	holder.add(sliders,BorderLayout.CENTER)
	holder.add(sliders.labels,BorderLayout.WEST)
	controls.append(name,holder)
controls.append('Discrete',Options(optionVars))
frateDisp = State('Frame rate: ','frate')
controls.append('',frateDisp)

mf=MapField(newMap)
controls.append('',mf)

buttonLine = []
dx = 0
for name,action in actions.iteritems():
	b = Button(name,eval(action))
	if dx+b.w>CONTROLW:
		buttonHolder = delimiter(*buttonLine)
		controls.append('',buttonHolder)
		buttonLine = []
		dx = 0
	dx+=b.w
	buttonLine.append(b)
for name,varname in latches.iteritems():
	b = Latch(name,varname)
	if dx+b.w>CONTROLW:
		buttonHolder = delimiter(*buttonLine)
		controls.append('',buttonHolder)
		buttonLine = []
		dx = 0
	dx+=b.w
	buttonLine.append(b)
controls.append('',delimiter(*buttonLine))

view.contentPane.add(controls,BorderLayout.WEST)
view.pack()
view.setLocation( (dm.width-view.width)/2, (dm.height-view.height)/2)
favicon(view)
view.visible   = 1
splash.visible = 0
reloadKernel(None)
		
ptime = clock()
while 1:
	if not paused:
		setKernelParameters(Kernel)
	
		Kernel.convolveStage1(0,1)
		Kernel.convolveStage2(0,1)
		'''
		renderers = [ Thread(target=(lambda a,b:lambda:Kernel.convolveStage1(a,b))(i,NTHREADS)) for i in range(NTHREADS) ]
		[s.start() for s in renderers]
		[s.join()  for s in renderers]
		renderers = [ Thread(target=(lambda a,b:lambda:Kernel.convolveStage2(a,b))(i,NTHREADS)) for i in range(NTHREADS) ]
		[s.start() for s in renderers]
		[s.join()  for s in renderers]
		'''
		Kernel.stat()
	
		#Kernel.render(0,1)
		renderers = [ Thread(target=(lambda a,b:lambda:Kernel.render(a,b))(i,NTHREADS)) for i in range(NTHREADS) ]
		[s.start() for s in renderers]
		[s.join()  for s in renderers]
	
		g = cleantext(Kernel.out.graphics)
	
		if doSnap or recordLatch:
			save(None)
	
		if drawCursors and trapCursors:
			paintCursors(g)

		viewpane.graphics.drawImage(Kernel.out,0,0,None)
	
		if drawCursors and not trapCursors:
			paintCursors(viewpane.graphics)
		'''
		g.color = YELLOW
		g.drawRect(0,0,W-1,H-1)
		g.color = BLACK
		g.drawRect(1,1,W-2,H-2)
		g.color = RED
		g.drawRect(2,2,W-4,H-4)
		'''
		ntime = clock()
		frate = 0.95*frate + 0.05/(ntime-ptime)
		ptime = ntime
	
		frateDisp.repaint()
	else:
		sleep(0.2)



