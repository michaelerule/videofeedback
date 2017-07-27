#!/usr/bin/env jython

from java.awt import Color, GraphicsEnvironment
from javax.swing import JFrame, JLabel
splash = JFrame('Starting')
l=JLabel('Perceptron',JLabel.CENTER)
l.background = Color.BLACK
l.foreground = Color.WHITE
splash.contentPane=l
splash.setDefaultLookAndFeelDecorated(0)
splash.resizable = 0
splash.undecorated =1 
splash.background = Color.BLACK
splash.foreground = Color.WHITE
splash.alwaysOnTop = 1
splash.pack()
dm = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.displayMode
splash.size = (200,100)
splash.setLocation(dm.width/2-100,dm.height/2-50)
splash.visible=1


