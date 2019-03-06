
# define the problem
NTHREADS = 2
saveat = "./shots/"
kerneltemplate = 'Kernel.h'
#W,H = 960,740
#W,H = 600,740
#W,H = 500,500
W,H = 1024,1024

SCREENSIZE = W+int(400*DPISCALE),H
CONTROLW = SCREENSIZE[0]-W
LABELW = int(100*DPISCALE)
SLIDERW = CONTROLW - LABELW
LENGTH = W*H
rlim = (-5,5)
ilim = (-5,5)

print cos(1+1j)
MAP = lambda z:50000*z/(z**5-15000)

Recurrent_HUE          = 0
Recurrent_SATURATION   = 128
Recurrent_INVERT       = 0
Recurrent_WEIGHT       = 0
Homeostatic_BRIGHTNESS = 128
Homeostatic_CONTRAST   = 128
Homeostatic_WEIGHT     = 0
Homeostatic_SATURATION = 128
Homeostatic_VARIATION  = 128
Homeostatic_CHARISMA   = 128
Homeostatic_VALUE      = 128

GRADIENT_GAIN          = 128
GRADIENT_BIAS          = 0

HTAU       = 0
ROTATE     = 0
SCALE      = 128
TX         = 0
TY         = 0
MOTIONBLUR = 10
BLUR       = 0
SHARPEN    = 0
NOISE      = 50
Viscosity  = 0

drawCursors = 1
trapCursors = 1
doSnap      = 0
recordLatch = 0
paused      = 0
invertColors= 0

controlVars = {
'Color'      :'Recurrent.HUE Recurrent.SATURATION',
'Effects'    :'MOTIONBLUR BLUR SHARPEN NOISE',
'Homeostasis':'Homeostatic.CONTRAST Homeostatic.BRIGHTNESS'
}

optionVars = {
'GRADIENT':'NO V H T R B',
'GRADIENT_COLOR':'WHITE BLACK YELLOW RAINBOW',
'INTERPOLATION':'NEAREST LINEAR CUBIC',
'BOUNDARY':'MIRROR TORUS EXTEND',
'HUE_MODE':'OFF HSV LUV ABV',
'PRECISION':'NIBBLE BYTE FLOAT',
'DUPLICATION':'OFF MIR JULIA QUAD GHOST'}

actions = {
'Recompile':'reloadKernel',
'Freeze':'None',
'Snap':'queueSave'}

latches = {
'Trap Cursors':'trapCursors',
'Draw Cursors':'drawCursors',
'Paused':'paused',
'Record':'recordLatch',
'Invert':'invertColors'
}

cursors = [(W/2,H/2) for i in range(3)]

maps = "z z**2 z**3 z**4 cos(z) sin(z)"

FIXED = 2**8

