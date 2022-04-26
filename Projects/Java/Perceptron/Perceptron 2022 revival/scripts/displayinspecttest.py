#!/usr/bin/env jython
# -*- coding: UTF-8 -*-

from java.lang   import *
from java.lang import System
from javax.swing import BorderFactory, KeyStroke
from java.awt    import *
from java.awt.event    import *
from javax.swing import *
from java.awt import Dimension, Font, Color, Cursor, BorderLayout, GraphicsEnvironment
from javax.swing import AbstractAction, JFrame, JTextArea, JPanel, JScrollPane, JRootPane
from java.awt import EventQueue  
from java.lang import Runnable  
from java.awt.event import MouseAdapter, KeyAdapter
from java.io import IOException
from java.util import Scanner
from javax.swing.border import EmptyBorder

import time
import traceback

def EDTsafe(function):
    '''
    Decorator which makes a function thread-safe with respect to the 
    event dispatch queue
    '''
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

def make_undecorated(jf):
    '''
    Makes a window undecorated, packs, and shows it
    '''
    jf.dispose()
    jf.contentPane.rootPane.windowDecorationStyle = JRootPane.NONE
    jf.undecorated = True
    jf.pack()  
    jf.visible = 1  
            
@EDTsafe  
def println(*args):
    '''
    Print to OUR terminal window
    '''
    global jf,tx,sp,txin,foo
    s = ' '.join(map(unicode,args))
    tx.append(s+'\n')
    b = sp.verticalScrollBar
    b.value = b.maximum
    #tx.caretPosition = tx.document.length

class dragger(MouseAdapter):
    '''
    Mouse Adapter for our terminal window
    '''
    def mouseClicked(self,e):
        print e.clickCount
    def mousePressed(self,e):
        global tx
        self.dx = jf.x - e.getXOnScreen()
        self.dy = jf.y - e.getYOnScreen()
        tx.cursor = Cursor(Cursor.MOVE_CURSOR)
    def mouseReleased(self,e):
        global tx
        tx.cursor = Cursor(Cursor.CROSSHAIR_CURSOR)
    def mouseDragged(self,e):
        jf.setLocation(e.getXOnScreen() + self.dx, e.getYOnScreen() + self.dy)

def execCmd(cmd):
    '''
    Execute a system command and return output
    '''
    s = Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
    return s.next() if s.hasNext() else ''

command_history = []
command_history_index = None
class typer(KeyAdapter):
    def keyPressed(self,e):
        global command_history, command_history_index
        if len(command_history)<=0: return
        if e.keyCode == e.VK_UP:
            if command_history_index is None:
                command_history_index = len(command_history)
            command_history_index = max(0,command_history_index-1)
            txin.text = command_history[command_history_index]
        elif e.keyCode == e.VK_DOWN:
            if command_history_index is None:
                command_history_index = len(command_history)-1
            command_history_index = min(len(command_history)-1,command_history_index+1)
            txin.text = command_history[command_history_index]

def handle_input():
    global command_history, command_history_index
    '''
    Handle a command line input
    '''
    global jf,tx,sp,txin,foo
    thetext = txin.text.strip()

    command_history.append(thetext)
    command_history_index = None

    txin.text = ''
    println('\n> '+thetext)
    if len(thetext)<=0:
        return
    key = thetext.split()[0].lower()
    while key in aliases:
        key = aliases.get(key,key)
    args = thetext.split()[1:]
    if key in commands:
        commands[key][1](args)
    else:
        try:
            println(execCmd(thetext))
        except IOException as e:
            println(e.localizedMessage)

class input_handler(AbstractAction):
    def actionPerformed(self,e):
        handle_input()
    
@EDTsafe  
def initializeApp():  
    global jf,tx,sp,txin,foo
    jf = JFrame("Hi",
        defaultCloseOperation=JFrame.EXIT_ON_CLOSE,
        preferredSize=Dimension(480,270))

    thefont  = Font(Font.MONOSPACED,Font.PLAIN, 10)
    noborder = BorderFactory.createEmptyBorder()

    txin = JTextArea(1,80,
        font=thefont,
        lineWrap=False,
        editable=True,
        background=Color.BLACK,
        foreground=Color.WHITE,
        caretColor=Color.WHITE,
        border=noborder)
    txin.addKeyListener(typer())
    
    tx = JTextArea(1,80,
        font=thefont,
        lineWrap=True,
        wrapStyleWord=True,
        editable=False,
        background=Color.BLACK,
        foreground=Color.LIGHT_GRAY,
        caretColor=Color.BLACK,
        cursor = Cursor(Cursor.CROSSHAIR_CURSOR),
        border = noborder)
    tx.caret.deinstall(tx);
    
    foo = JPanel(layout=BorderLayout(),border=noborder)
    foo.add(tx,  BorderLayout.CENTER)
    foo.add(txin,BorderLayout.SOUTH )
    
    sp = JScrollPane(foo,  
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
        border=noborder,
        viewportBorder = noborder)
    jf.contentPane.add(sp)
    jf.contentPane.border = noborder
    
    mydragger = dragger()
    tx.addMouseListener(mydragger)
    tx.addMouseMotionListener(mydragger)
    
    txin.inputMap.put(KeyStroke.getKeyStroke("ENTER"), "text-submit");
    txin.actionMap.put("text-submit", input_handler())
    
    make_undecorated(jf)
    
    txin.requestFocusInWindow()
    printhelp()
    
def save_resolution():
    '''
    Save current screen resolution
    '''
    global INITIAL_WIDTH, INITIAL_HEIGHT
    m  = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.displayMode
    INITIAL_WIDTH, INITIAL_HEIGHT = m.width, m.height

def reset_resolution(*args):
    '''
    Restore screen to original resolution
    '''
    global INITIAL_WIDTH, INITIAL_HEIGHT
    display_ID = xGetPrimaryID()
    if len(args):
        display_ID = args[0]
    println('Setting display %s to resolution %d x %d'%(display_ID, INITIAL_WIDTH, INITIAL_HEIGHT))
    set_display_resolution(INITIAL_WIDTH, INITIAL_HEIGHT, display_ID)

def is_X11():
    '''
    Detect if we're on X11 inside Linux
    '''
    sysname = System.getProperty("os.name").lower()
    if not 'linux' in sysname:
        println('The system "%s" doesn\'t look like Linux'%sysname)
        return False
    ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    s  = ge.defaultScreenDevice.defaultConfiguration.toString().lower()
    if not 'x11' in s:
        println('Configuration %s doesn\'t look like X11'%s)
        return False
    return True
    
def xGetPrimaryID():
    '''
    Extract primary display ID from xrandr output
    '''
    result = execCmd('xrandr')
    for line in result.split('\n'):
        if len(line) and line[0]!=' ' and 'primary' in line:
            return line.split(' ')[0]
    return None

def xsetmode(screen_ID,w,h,refresh=None):
    '''
    Tries to set screen resolution using `xrandr`
    '''
    println('Attempting to set display mode %d x %d using `xrandr`'%(w,h))
    cmd = "xrandr --output %s --mode %dx%d"%(screen_ID,w,h)
    if not refresh is None:
        cmd += ' --refresh %s'%refresh
    println(cmd)
    result = execCmd(cmd)
    println(result)
    return result
    
def makefull(*args):
    '''
    Make the terminal fullscreen
    '''
    global jf
    d = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    d.setFullScreenWindow(jf)
    
def clearfull(*args):
    '''
    Make the terminal not fullscreen
    '''
    global jf
    d = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    d.setFullScreenWindow(None)
    
def list_resolutions_default():
    '''
    List screen resolutions using Java
    '''
    global INITIAL_WIDTH, INITIAL_HEIGHT
    preferred_ratio = float(INITIAL_WIDTH)/float(INITIAL_HEIGHT)
    ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    d  = ge.defaultScreenDevice
    println('Finding screen resolutions...')
    selected = dict()
    for m in d.displayModes:
        ratio = float(m.width)/float(m.height)
        if abs(ratio-preferred_ratio)<1e-6:
            w,h,r = m.width,m.height,m.refreshRate
            if not (w,h) in selected or abs(60-r)<abs(60-selected[w,h][-1]):
               selected[w,h] = (w,h,r)
    return selected

def list_resolutions_xrandr():
    '''
    List screen resolutions using `xrandr`
    '''
    global INITIAL_WIDTH, INITIAL_HEIGHT
    preferred_ratio = float(INITIAL_WIDTH)/float(INITIAL_HEIGHT)
    
    println('Finding screen resolutions via `xrandr`...')
    screen_ID = xGetPrimaryID()
    if screen_ID is None:
        println('Couldn\'t parse primary device ID from xrandr output')
        return None
    println('Primary screen\'s ID: "%s"'%screen_ID)
    
    result     = execCmd('xrandr')
    selected   = dict()
    collecting = False
    for line in result.split('\n'):
        if len(line)<=0: 
            continue
        if collecting and line[0]!=' ': 
            break
        if collecting:
            line = line.strip()
            keys = line.split()
            w,h = map(int,keys[0].split('x'))
            if abs(float(w)/float(h)-preferred_ratio)<1e-6:
                println(line)
                matched = None
                for refresh_rate in keys[1:]:
                    refresh_rate = refresh_rate.replace('+','').replace('*','')
                    if not len(refresh_rate)>=5: continue
                    asfloat      = float(refresh_rate)
                    if matched is None or abs(asfloat-60)<abs(matched-60):
                        matched = asfloat
                selected[w,h] = (w,h,'%5.2f'%matched)
        if line.startswith(screen_ID):
            collecting = True
            
    return selected

def list_resolutions(*args):
    '''
    List alternative resolutions with the same aspect ratio as the initial
    display configuration
    '''
    global INITIAL_WIDTH, INITIAL_HEIGHT
    if is_X11():
        try:
            resolutions = list_resolutions_xrandr()
        except:
            println(traceback.format_exc())
            resolutions = list_resolutions_default()
    else:
        resolutions = list_resolutions_default()
    println('Display modes with %d:%d aspect ratio:'%(INITIAL_WIDTH,INITIAL_HEIGHT))
    for w,h in sorted(resolutions.keys()):
        println('  %dx%d %s'%resolutions[w,h])
    return resolutions

def set_display_resolution(TARGET_WIDTH, TARGET_HEIGHT, display_id=0):
    '''
    Changes the resolution of the primary display
    '''
    println('I will try to set the screen resolution to %d x %d'%(TARGET_WIDTH, TARGET_HEIGHT))
    
    def find_resolution(resolutions):
        println('Searching for smallest screen that fits')
        selected = [(w,h,r) for w,h,r in sorted(resolutions.values())
                                  if w>=TARGET_WIDTH and h>=TARGET_HEIGHT]
        if len(selected)<=0:
            println('No sufficient resolution found')
        return selected
            
    d  = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    if d.isDisplayChangeSupported():
        println('Changing the display resolution via Java *is* supported')
        resolutions = list_resolutions_default()
        selected    = find_resolution(resolutions)
        
        for w,h,r in selected:
            println('I\'ll try for %d x %d (%s)'%(w,h,r))
            try:
                d.setDisplayMode(DisplayMode(w,h,DisplayMode.BIT_DEPTH_MULTI,r)); 
                break
            except InternalError:
                println('That didn\'t work, I\'ll try a different one')
        
    elif is_X11():
        println('Attempting via `xrandr`')
    
        resolutions = list_resolutions_xrandr()
        selected    = find_resolution(resolutions)
        
        w,h,r  = selected[0]
        result = xsetmode(xGetPrimaryID(),w,h,refresh=r)
        println(result)
    else:
        pass
        
def exit(*args):
    '''
    Close the program
    '''
    reset_resolution()
    System.exit(0)
    
def printhelp(*args):
    '''
    Print a help message on terminal
    '''
    global jf,tx,sp,txin,foo,commands,aliases
    println('Welcome to Perceptron Video Feedback terminal. '
            'You can type commands and press ENTER execute them. Perceptron '
            'commands are not case-sensitive. Unrecognized commands are forwarded '
            'to the system. These commands are defined:\n')
    for c in sorted(commands.keys()):
        println(('  '+c+':').ljust(20)+commands[c][0])

def printaliases(*args):
    '''
    Print a list of terminal command aliases
    '''
    global jf,tx,sp,txin,foo,commands,aliases
    keys = aliases.keys()
    padto = max(map(len,keys))+4
    for c in sorted(keys):
        v = c
        while v in aliases:
            v = aliases[v]
        println(('  '+c+':').ljust(padto)+v)
    

commands = {
    'exit':('Terminate the program'  ,exit),
    'help':('Print this help message',printhelp),
    'aliases':('List command abbreviations',printaliases),
    'listres':('List supported resolutions',list_resolutions),
    'setres':('Set screen resolution',lambda args:set_display_resolution(int(args[0]),int(args[1]))),
    'resetres':('Restore screen resolution',reset_resolution),
    'makefull':('Make this terimal a fullscreen window',makefull),
    'makesmall':('Make this terimal a normal window',clearfull),
}

aliases = {
    'h':'help',
    '?':'h',
    'quit':'exit',
    'q':'quit',
    'a':'aliases',
    'rr':'resetres',
    'lr':'listres',
    'sr':'setres',
    'mf':'makefull',
    'ms':'makesmall'
}

initializeApp()  
save_resolution()

list_resolutions([])

import time
while True:
    time.sleep(0.05)




