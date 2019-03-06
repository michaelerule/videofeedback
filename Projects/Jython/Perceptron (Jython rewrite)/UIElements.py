#!/usr/bin/env jython
'''
Minimalist radio button style options. Each line is a list of options.
Clicking on any option will set it and inactivate the other options.
Global variables will be set to the current state
'''
from java.awt import RenderingHints
from java.awt.Font  import decode as getFont
from javax.swing import JTextField, JFrame, JPanel, JButton
from java.awt.image import BufferedImage
from javax.swing import BorderFactory
from java.util.Map import Entry
from javax.swing import UIManager
from javax.swing.plaf import ColorUIResource
from java.lang import IllegalArgumentException
BACKGROUND    = Color(0x49,0x49,0x49,0xff)
FOREGROUND    = Color(0xce,0xce,0xce,0xff)
SPACING       = int(18*DPISCALE)
HPADDING      = int(4*DPISCALE)
BALLRADIUS    = int(5*DPISCALE)
SLIDERSPACING = SPACING

colorKeys = []
for entry in UIManager.getLookAndFeelDefaults().entrySet():
    try:
	    if type(entry.value) is ColorUIResource:
		    print entry
		    if 'background' in str(entry).lower():
			    UIManager.put(entry,ColorUIResource(BACKGROUND))
			    print 'setting background to',hex(UIManager.get(entry).RGB & 0xffffff)
		    if 'foreground' in str(entry).lower():
			    print 'setting foreground'
			    UIManager.put(entry,ColorUIResource(FOREGROUND))
    except IllegalArgumentException:
        print 'java.lang.IllegalArgumentException ocurred; ignoring'
    
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

dummyImage    = BufferedImage(100,100,BufferedImage.TYPE_INT_RGB)
dummyGraphics = cleantext(dummyImage.graphics)
metrics  = dummyGraphics.getFontMetrics(myFont)#dummyGraphics.font)

class ControlLables(JPanel):
	def __init__(self,controls):
		JPanel.__init__(self,background=BACKGROUND,foreground=FOREGROUND)
		self.controls = controls
		self.active = [1 for i in self.controls.varmap]
		self.preferredSize = (LABELW,H)
		self.mousePressed = self.press
	def press(self,e):
		i = int(e.y/SLIDERSPACING)
		self.grabbed = None
		if i>=0 and i<len(self.active):
			self.active[i] = 1-self.active[i]
			self.repaint()
			self.controls.repaint()
	def paintComponent(self,g):
		self.super__paintComponent(g)
		cleantext(g)
		g.color = self.foreground
		D = BALLRADIUS*2
		P = SLIDERSPACING/2-BALLRADIUS
		for i,s in enumerate(self.controls.varmap):
			s = s.split('.')[-1]
			Y = i*SLIDERSPACING
			if self.active[i]==1:
				g.fillRect(P,Y+P,D,D)
			else:
				g.drawRect(P,Y+P,D-1,D-1)
			g.drawString(s,SLIDERSPACING,Y+SLIDERSPACING-BALLRADIUS+2)

def globalName(varname):
	return varname.replace('.','_')

class Sliders(JPanel):
	def __init__(self,varnames,controlled):
		JPanel.__init__(self,background=BACKGROUND,foreground=FOREGROUND)
		self.doubleBuffered = 1
		self.varmap = varnames.split()
		self.controlled = controlled
		self.sliders = [globals()[globalName(i)] for i in self.varmap]
		self.mouseDragged = self.moveSlider
		self.mousePressed = self.press
		self.mouseResleased = self.release
		self.labels = ControlLables(self)
	def press(self,e):
		i = int(e.y/SLIDERSPACING)
		self.grabbed = None
		if i>=0 and i<len(self.sliders):
			self.grabbed = i+1
		self.moveSlider(e)
	def release(self,e):
		self.grabbed = None
	def moveSlider(self,e):
		if self.grabbed and self.labels.active[self.grabbed-1]:
			self.updateSlider(self.grabbed-1,e)
	def updateSlider(self,i,e):
		x = e.x
		x = BALLRADIUS if x<BALLRADIUS else self.width-BALLRADIUS-1 if x>self.width-2-1 else x
		v = (x-BALLRADIUS)*0x100/(self.width-2*BALLRADIUS-1)
		v = 0 if v<0 else 0x100 if v>0x100 else v
		self.sliders[i]=v
		globals()[globalName(self.varmap[i])] = v
		self.repaint()
	def paintComponent(self,g):
		self.super__paintComponent(g)
		cleantext(g)
		D = 2*BALLRADIUS
		w = (self.width-2*BALLRADIUS-1)
		for i,s in enumerate(self.sliders):
			X = s*w/0x100
			Y = SLIDERSPACING*i
			g.color = self.foreground
			g.drawLine(0,Y+SLIDERSPACING/2,self.width,Y+SLIDERSPACING/2)
			if self.labels.active[i]:
				g.color = self.background
				g.fillOval( X, Y+SLIDERSPACING/2-BALLRADIUS, D, D)
				g.color = self.foreground
				g.drawOval( X, Y+SLIDERSPACING/2-BALLRADIUS, D, D)
			else:
				g.color = self.foreground
				g.fillRect( X+BALLRADIUS-2, Y+SLIDERSPACING/2-2,5,5)

class Cascade(JPanel):
	def __init__(self):
		JPanel.__init__(self,BorderLayout(),background=BACKGROUND,foreground=FOREGROUND)
		self.tail = JPanel(BorderLayout(),background=BACKGROUND,foreground=FOREGROUND)
		self.add(self.tail,BorderLayout.NORTH)
	def append(self,name,component):
		print name
		j1 = self.tail
		j2 = JPanel(BorderLayout(),background=FOREGROUND)
		j2.add(JLabel(name,foreground=BACKGROUND),BorderLayout.NORTH)
		j2.add(component,BorderLayout.CENTER)
		j1.add(j2,BorderLayout.CENTER)
		j3 = JPanel(BorderLayout(),background=BACKGROUND,foreground=FOREGROUND)
		j3.add(j1,BorderLayout.NORTH)
		j1 = j3
		self.tail = j1
		self.add(self.tail,BorderLayout.NORTH)
		self.repaint()
				
class Options(JPanel):
	def __init__(self,options,offset=LABELW):
		JPanel.__init__(self,
			background=BACKGROUND,
			foreground=FOREGROUND,
			mouseClicked=self.click,
			preferredSize = (SLIDERW,(1+SPACING)*len(options)))
		self.labels  = [l for l in options] 
		self.options = [o.split() for o in options.values()]
		self.active  = [0 for o in options]
		self.OFFSET = offset
	def click(self,e):
		i = int(e.y/(1+SPACING))
		g = self.graphics
		if i>=0 and i<len(self.options):
			X = self.OFFSET
			for j,option in enumerate(self.options[i]):
				X += metrics.stringWidth(option)+HPADDING*2
				if X>=e.x:
					break
			self.active[i]=j
			self.repaint()
			name = globalName(self.labels[i])
			globals()[name] = j
	def paintComponent(self,g):
		self.super__paintComponent(g)
		cleantext(g)
		g.color = self.foreground
		H = metrics.height
		for i,(l,s) in enumerate(zip(self.labels,self.options)):
			Y = i*SPACING
			g.drawString(l,HPADDING,Y+H)
			X = self.OFFSET
			for j,option in enumerate(s):
				W = metrics.stringWidth(option)+HPADDING*2
				if self.active[i]==j:
					g.fillRect(X,Y,W,SPACING+1)
					g.color = self.background
					g.drawString(option,X+HPADDING,Y+H-1)
					g.color = self.foreground
				else:
					g.drawRect(X,Y,W-1,SPACING)
					g.drawString(option,X+HPADDING,Y+H-1)
				X += W-1

def favicon(jf):
	ico = BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB)
	g = ico.createGraphics()
	cleantext(g)
	g.color = BACKGROUND
	g.fillOval(2,2,28,28);
	g.color = FOREGROUND
	g.drawOval(2,2,28,28);
	jf.iconImage=ico

class MapField(JPanel):
	def __init__(self,action):
		JPanel.__init__(self,BorderLayout(),background=BACKGROUND,foreground=FOREGROUND)
		self.label = Label('f(z) =')
		self.textf = JTextField(background=BACKGROUND,foreground=FOREGROUND,actionPerformed=action,border=BorderFactory.createEmptyBorder(),caretColor=FOREGROUND)
		self.add(self.label,BorderLayout.WEST)
		self.add(self.textf,BorderLayout.CENTER)

'''
class Flow(JPanel):
	def __init__(self):
		JPanel.__init__(self,background=BACKGROUND,foreground=FOREGROUND)
		self.buttonLine = []
		self.dx = 0
	for name,action in actions.iteritems():
		b = Button(name,eval(action))
		if dx+b.w>CONTROLW:
			buttonHolder = delimiter(*buttonLine)
			controls.append('',buttonHolder)
			buttonLine = []
			dx = 0
		dx+=b.w
		buttonLine.append(b)
'''

class Label(JPanel):
	def __init__(self,name):
		JPanel.__init__(self,background=BACKGROUND,foreground=FOREGROUND)
		self.name = name
		self.w = int(metrics.stringWidth(name))+HPADDING*2
		self.size = (self.w, SPACING)
		self.preferredSize = self.size
	def paintComponent(self,g):
		cleantext(g)
		g.color = self.background
		g.fillRect(0,0,self.width,self.height)
		g.color = self.foreground
		g.drawString(self.name, (self.width-self.w)/2+HPADDING, metrics.height-(SPACING-metrics.height)/2-1)

class Button(Label):
	def __init__(self,name,callback):
		Label.__init__(self,name)
		self.mousePressed  = self.press
		self.mouseReleased = self.release
		self.mouseEntered  = self.enter
		self.mouseExited   = self.leave
		self.callback = callback
		self.primed = 0
		self.tabled = 0
	def hilight(self):
		self.background,self.foreground = self.foreground,self.background
	def press(self,e):
		self.hilight()
		self.primed = 1
		self.repaint()
	def release(self,e):
		if not self.tabled: self.hilight()
		if self.primed and not self.callback is None: self.callback(e)
		self.primed = 0
		self.tabled = 0
		self.repaint()
	def enter(self,e):
		if self.tabled:
			self.hilight()
			self.primed=1
			self.tabled=0
	def leave(self,e):
		if self.primed:
			self.hilight()
			self.primed=0
			self.tabled=1

class Latch(Button):
	def __init__(self,name,varname):
		Button.__init__(self,name,self.flip)
		if not varname in globals(): globals()[varname]=0
		self.active = globals()[varname]
		self.varname = varname
		if self.active: self.hilight()
	def flip(self,e):
		self.hilight()
		self.active = 1-self.active
		globals()[self.varname]=self.active
		self.repaint()
		
class State(JPanel):
	def __init__(self,name,varname):
		JPanel.__init__(self,background=BACKGROUND,foreground=FOREGROUND)
		self.name = name
		self.varname = varname
		self.lw = int(metrics.stringWidth(name))+HPADDING*2
		self.w = self.lw+100
		self.size = (self.w, SPACING)
		self.preferredSize = self.size
	def paintComponent(self,g):
		self.super__paintComponent(g)
		cleantext(g)
		g.color = FOREGROUND
		g.drawString(self.name, HPADDING, metrics.height-3 )
		g.drawString(str(eval(self.varname)), self.lw, metrics.height-3 )
		
class delimiter(JPanel):
	def __init__(self,*components):
		JPanel.__init__(self,BorderLayout(),background=BACKGROUND,foreground=FOREGROUND)
		self.holder = JPanel(FlowLayout(FlowLayout.LEFT,1,1),background=FOREGROUND,foreground=FOREGROUND)
		self.mycomponents = components
		self.w = sum([c.width for c in components])+len(components)+1
		self.h = 0
		for c in components:
			if c.height>self.h: self.h=c.height
		self.size = self.w,self.h+2
		self.preferredSize = self.size
		for c in components:
			self.holder.add(c)
		self.add(self.holder,BorderLayout.WEST)
		

