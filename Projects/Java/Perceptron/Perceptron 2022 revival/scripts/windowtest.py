#!/use/bin/env jython
# -*- coding: UTF-8 -*-

from java.lang   import *
from java.awt    import *
from javax.swing import *

import traceback

# Thread safe swing decorator
from java.awt import EventQueue  
from java.lang import Runnable  
def EDTsafe(function):  
    def safe(*args,**kwargs):  
        if EventQueue.isDispatchThread():  
            return function(*args,**kwargs)  
        else:  
            class foo(Runnable):  
                def run(self):  
                    self.result = function(*args,**kwargs)  
            f = foo()  
            EventQueue.invokeAndWait(f)  
            return f.result  
    safe.__name__ = function.__name__  
    safe.__doc__ = function.__doc__  
    safe.__dict__.update(function.__dict__)  
    return safe  

@EDTsafe  
def initializeApp():  
    global jf,tx,sp
    jf = JFrame("Hi",
        defaultCloseOperation=JFrame.EXIT_ON_CLOSE,
        preferredSize=Dimension(480,270))
    tx = JTextArea(1,80,
        font=Font("Monospace",Font.PLAIN, 10),
        lineWrap=False,
        editable=False,
        background=Color.BLACK,
        foreground=Color.WHITE,
        )
    tx.getCaret().deinstall(tx);
    sp = JScrollPane(tx,  
        JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
    jf.contentPane.add(sp)  
    # more unsafe initialization here  
    jf.pack()  
    jf.visible = 1  

initializeApp()  

for i in range(50):
    tx.append('Let\'s see\n')
    sp.viewport.viewPosition = Point(0,tx.document.length)


from java.awt.event import MouseAdapter
class dragger(MouseAdapter):
    def mouseClicked(self,e):
        print e.clickCount
    def mousePressed(self,e):
        self.dx = jf.x - e.getXOnScreen()
        self.dy = jf.y - e.getYOnScreen()
    def mouseDragged(self,e):
        jf.setLocation(e.getXOnScreen() + self.dx, e.getYOnScreen() + self.dy)
mydragger = dragger()
jf.contentPane.addMouseListener(mydragger)
jf.contentPane.addMouseMotionListener(mydragger)
tx.addMouseListener(mydragger)
tx.addMouseMotionListener(mydragger)

jf.dispose()
jf.contentPane.rootPane.windowDecorationStyle = JRootPane.NONE
jf.undecorated = True
jf.pack()  
jf.visible = 1  



import time
while True:
    time.sleep(0.05)
