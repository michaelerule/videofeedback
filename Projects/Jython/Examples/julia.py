#!/usr/bin/env jython

from javax.swing    import *
from java.awt       import *
from java.awt.image import *
from java.awt.event import *

FUNCTION  = lambda z:z**2             # Recurrence ( should be even )
FIELDS    = 4                         # Field size in complex plane
S         = 256                       # Buffer size, must be power of 2
CENTER    = complex(1,1)*FIELDS*0.5   # Center of plane ( defaults to 0 )
CLIP      = S-1                       # Used for wrapping buffer coordinates
LEN       = S*S                       # Total buffer size
QUIT      = LEN/2                     # Amount of buffer to actually compute

def render(source,target,mapping,hue,offx,offy):
	'''
	Uses the previous frame, along with the cached mapping, to compute the
	next frame. Off-screen pixels default to the color given by hue.
	offx and offy define a constant shift of the recurrence function
	'''
	color     = Color.getHSBColor(float(hue),1.,1.).RGB
	miny,maxy = -offy,S-offy
	for i in xrange(QUIT):
		(x,y) = mapping[i]
		if y>=miny and y<maxy:
			c = source.getElem(((x+offx)&CLIP)+((y+offy)*S))
		else:
			c = color
		target.setElem(i,c)
		target.setElem(LEN-i-1,c)

def computePoint(x,y):
	'''
	Sends a point in buffer coordinates through the mapping function,
	converting back to buffer coordinates before returning
	'''
	z = FUNCTION(complex(x,y)/S*FIELDS-CENTER)*S/FIELDS
	return int(z.real+0.5)+S,int(z.imag+0.5)

class FractalMouseListener(MouseMotionAdapter):
	'''
	Updates the constant offset to correspond to the mouse location
	'''
	def mouseMoved(self,e):
		global offx,offy,F
		offx,offy = e.x*S/F.width,e.y*S/F.height

# Program start : 
# declare two buffers for rendering ( buffer flipping approach )
# extract the dataBuffers underlying the BuffferedImages, for speedy access
# precompute the complex mapping in terms of buffer coordinates
# set the initial offset and  to 0

img,buf       = BufferedImage(S,S,BufferedImage.TYPE_INT_RGB),BufferedImage(S,S,BufferedImage.TYPE_INT_RGB)
mapping       = [computePoint(x,y) for y in xrange(S) for x in xrange(S)]
offx,offy     = 0,0
hue           = 0.0

# Windowing commands :
# Declare a new Jpanel, and give it our mouse motion listener
# Declare a new JFrame to contain the fractal JPanel, show it

F = JPanel()
F.addMouseMotionListener(FractalMouseListener())
jf = JFrame('Demo',defaultCloseOperation=JFrame.EXIT_ON_CLOSE,contentPane=F,size=(S,S),visible=1)

# Now, as long as the program is running, loop and render frames
# We advance the hue each frame, flip the buffers
# Render the next frame of the fractal and send it to the screen

while 1:
	hue = hue + 0.05
	img,buf = buf,img
	render(img.raster.dataBuffer,buf.raster.dataBuffer,mapping,hue,offx,offy)
	F.graphics.drawImage(img,0,0,F.width,F.height,None)


