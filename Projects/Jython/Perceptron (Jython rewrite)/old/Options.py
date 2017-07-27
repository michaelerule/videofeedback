#!/usr/bin/env jython
'''
Minimalist radio button style options. Each line is a list of options.
Clicking on any option will set it and inactivate the other options.
Global variables will be set to the current state
'''
from java.awt.Font  import decode as getFont
SPACING = 20
HPADDING = 4

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

class OptionsMouseListener(MouseListener):
	def __init__(self,options):
		self.options = options
	def mouseClicked(self,e):
		i = int(e.y/SPACING)
		g = self.options.graphics
		metrics = g.getFontMetrics(g.font)
		if i>=0 and i<len(self.options.options):
			X = self.options.OFFSET
			for j,option in enumerate(self.options.options[i]):
				X += metrics.stringWidth(option)+HPADDING*2
				if X>=e.x:
					break
			self.options.active[i] = j
			self.options.repaint()
				
class Options(JPanel):
	def __init__(self,options,offset=200):
		self.labels  = [l for l in options] 
		self.options = [o.split() for o in options.values()]
		self.active  = [0 for o in options]
		self.preferredSize = (100,SPACING*len(options))
		self.background = BACKGROUND
		self.foreground = FOREGROUND
		self.addMouseListener(OptionsMouseListener(self))
		self.OFFSET = offset
	def paintComponent(self,g):
		self.super__paintComponent(g)
		g.color = self.foreground
		metrics = g.getFontMetrics(g.font)
		H = metrics.height
		for i,(l,s) in enumerate(zip(self.labels,self.options)):
			Y = i*(SPACING-1)
			g.drawString(l,HPADDING,Y+H)
			X = self.OFFSET
			for j,option in enumerate(s):
				W = metrics.stringWidth(option)+HPADDING*2
				if self.active[i]==j:
					g.fillRect(X,Y,W,SPACING)
					g.color = self.background
					g.drawString(option,X+HPADDING,Y+H)
					g.color = self.foreground
				else:
					g.drawRect(X,Y,W-1,SPACING-1)
					g.drawString(option,X+HPADDING,Y+H)
				X += W-1




