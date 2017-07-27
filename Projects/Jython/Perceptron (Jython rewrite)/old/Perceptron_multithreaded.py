#!/usr/bin/env jython
execfile('Splash.py')

from java.awt       import Color,BorderLayout
from java.awt.event import MouseAdapter, MouseListener, MouseMotionListener, ComponentAdapter
from java.awt.image import BufferedImage
from java.awt.Color import BLACK,WHITE,YELLOW,RED
from javax.swing    import JFrame, JPanel, JButton
from time           import sleep,clock
from math           import sin,cos,exp,e,pi
from jarray         import array,zeros
from threading      import Thread
from java.lang      import System
execfile('Macro.py')
execfile('Sliders.py')

# define the problem
W,H = 700,700
LENGTH = W*H
rlim = (-4,4)
ilim = (-4,4)

MAP        = lambda z:z

Post_HUE               = 0
Post_SATURATION        = 128
Post_BRIGHTNESS        = 128
Post_CONTRAST          = 128
Post_INVERT            = 0
Post_WEIGHT            = 0
Recurrent_HUE          = 0
Recurrent_SATURATION   = 128
Recurrent_BRIGHTNESS   = 128
Recurrent_CONTRAST     = 128
Recurrent_INVERT       = 0
Recurrent_WEIGHT       = 0
Homeostatic_BRIGHTNESS = 128
Homeostatic_CONTRAST   = 128
Homeostatic_WEIGHT     = 0

HTAU       = 0

ROTATE     = 0
SCALE      = 128
TX         = 0
TY         = 0

THREADS    = 256*2/16

MOTIONBLUR = 10
BLUR       = 0
SHARPEN    = 0
NOISE      = 50

controlVars = {
'Post Color' :'Post.HUE Post.SATURATION Post.BRIGHTNESS Post.CONTRAST Post.INVERT Post.WEIGHT',
'Color'      :'Recurrent.HUE Recurrent.SATURATION Recurrent.BRIGHTNESS Recurrent.CONTRAST Recurrent.INVERT Recurrent.WEIGHT',
'Geometry'   :'ROTATE SCALE TX TY',
'Effects'    :'MOTIONBLUR BLUR SHARPEN NOISE',
'Homeostasis':'Homeostatic.CONTRAST Homeostatic.BRIGHTNESS HTAU Homeostatic.WEIGHT',
'System'     :'THREADS'}

def rescale(v,(a1,b1),(a2,b2)):
	return (v-a1)*(b2-a2)/float(b1-a1)+a2
		
FIXED = 2**9
	
def setTransform(Kernel):
	rotation = ROTATE*2*pi/0x100
	scale    = float(SCALE)/0x80
	tx       = rescale(TX,(0,0x100),(0,W))
	ty       = rescale(TY,(0,0x100),(0,H))
	ct = int(cos(rotation)*FIXED*scale+0.5)
	st = int(sin(rotation)*FIXED*scale+0.5)
	tx = int((tx+W/2)*FIXED*FIXED+0.5)
	ty = int((ty+H/2)*FIXED*FIXED+0.5)
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
				print e
	setTransform(Kernel)
	Kernel.Post.CONTRAST        *= 2;
	Kernel.Recurrent.CONTRAST   *= 2;
	Kernel.Homeostatic.CONTRAST *= 2;
	Kernel.Post.BRIGHTNESS        *= Kernel.Post.CONTRAST;
	Kernel.Recurrent.BRIGHTNESS   *= Kernel.Recurrent.CONTRAST;
	Kernel.Homeostatic.BRIGHTNESS *= Kernel.Homeostatic.CONTRAST;

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
			mx,my = complexToScreen(function(screenToComplex(x,y)))
			mx = int((mx-W/2)*FIXED+0.5)
			my = int((my-H/2)*FIXED+0.5)
			mapdata.append(mx)
			mapdata.append(my)
	Kernel.mapping = array(mapdata,'i')

def reloadKernel(e):
	global Kernel
	import sys, os
	kversion = 'Kernel'+str(System.currentTimeMillis())
	kernelsource = list(open('Kernel32.h','r').readlines())
	configure = [
	'#define KERNELVERSION %s\n'%kversion,
	'#define WIDTH %d\n'%W,
	'#define HEIGHT %d\n'%H,
	'#define LENGTH (WIDTH*HEIGHT)\n',
	'#define W_1 (HEIGHT-1)\n',
	'#define H_1 (WIDTH-1)\n',
	'#define W8 (W_1*256)\n',
	'#define H8 (H_1*256)\n',
	'#define W_82 (W8*2)\n',
	'#define H_82 (H8*2)\n'
	]
	kernelsource = configure+kernelsource
	outfile = open(kversion+'.c','w')
	outfile.writelines(kernelsource)
	outfile.flush()
	outfile.close()
	#kernelsource = parseMacros(kernelsource)
	kfilename = kversion+'.java'
	#outfile = open(kfilename,'w')
	#outfile.writelines(kernelsource)
	#outfile.flush()
	#outfile.close()
	result = os.system("gcc -E ./%s.c | sed '/^\#/d' > %s.java"%(kversion,kversion))
	if result!=0:
		print "COMPILE ERROR"
		return
	result = os.system('javac '+kfilename)
	if result!=0:
		print "COMPILE ERROR"
		return
	newKernel =  __import__(kversion)
	#os.system('rm ./'+kfilename)
	#os.system('rm ./'+kversion+'.class')
	newKernel.prepare()#W,H)
	setKernelParameters(newKernel)
	setMap(MAP,newKernel)
	Kernel = newKernel

reloadKernel(None)

view = JFrame('test_view')
view.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
view.contentPane.setLayout(BorderLayout())
viewpane = JPanel()
viewpane.preferredSize = (W,H)
view.contentPane.add(viewpane,BorderLayout.CENTER)

j0 = JPanel(BorderLayout())
j1 = j0
for group,variables in controlVars.iteritems():
	print group
	controls = Sliders(variables,Kernel)
	j2 = JPanel(BorderLayout())
	j2.add(JLabel(group),BorderLayout.NORTH)
	j2.add(controls,BorderLayout.CENTER)
	j2.add(controls.labels,BorderLayout.WEST)
	controls.labels.preferredSize = (200,SLIDERSPACING*len(variables.split()))
	controls.preferredSize        = (300,SLIDERSPACING*len(variables.split()))
	j1.add(j2,BorderLayout.CENTER)
	j3 = JPanel(BorderLayout())
	j3.add(j1,BorderLayout.NORTH)
	j1 = j3

reloadButton = JButton('Recompile Kernel',actionPerformed=reloadKernel)
reloadButton.focusPainted = 0
reloadButton.background = BACKGROUND
reloadButton.foreground = FOREGROUND
reloadButton.borderPainted = 0

j1.background = BACKGROUND
j1.add(reloadButton,BorderLayout.SOUTH)
view.contentPane.add(j1,BorderLayout.EAST)

viewpane.preferredSize = (W,H)
viewpane.size = (W,H)
view.pack()
view.resizable=0 
view.setLocation( (dm.width-view.width)/2, (dm.height-view.height)/2)

view.visible   = 1
splash.visible = 0

ptime = clock()
frate = 0

RUNNING = 1
NTHREADS = 3

from java.util.concurrent.atomic import AtomicInteger
synchronizer = AtomicInteger(0)
started      = AtomicInteger(0)

def renderer(a,b):
	global synchronizer, RUNNING, ptime, ntime, frate
	while RUNNING:
		
		Kernel.render(a,b)
		Kernel.convolveStage1(a,b)
		
		while started.get()>0:
			pass
		if synchronizer.incrementAndGet()<b:
			while synchronizer.get()<b:
				pass
		started.incrementAndGet()			
		# we delegate thread-unsafe stuff to thread number 0
		if a==0:
			setKernelParameters(Kernel)
			Kernel.stat()
			g = Kernel.out.graphics
			g.color = YELLOW
			g.drawRect(0,0,W-1,H-1)
			g.color = RED
			g.drawRect(0,0,W,H)
			g.color = BLACK
			g.drawRect(1,1,W-2,H-2)
			viewpane.graphics.drawImage(Kernel.disp,0,0,None)
			ntime = clock()
			frate = 0.9*frate + 0.1/(ntime-ptime)
			print NTHREADS, frate
			ptime = ntime
		while started.get()<b:
			pass
			
		Kernel.convolveStage2(a,b)
		
		if synchronizer.decrementAndGet()>0:
			while synchronizer.get()>0:
				pass
		started.decrementAndGet()
		
		

renderers = [ Thread(target=(lambda a,b:lambda:renderer(a,b))(i,NTHREADS)) for i in range(NTHREADS) ]
[s.start() for s in renderers]
[s.join()  for s in renderers]



