#!/usr/bin/env ipython3
from pylab import *
from numpy import *

VERBOSE = True

exec(open('shared_constants.asm').read())

def write_table_bytes(f,a,cspan=16):
    a = array(a).ravel()
    nrows = int(ceil(a.shape[0]/cspan))
    for row in range(nrows):
        bb = a[row*cspan:(row+1)*cspan]
        s = ','.join(['$%02X'%b for b in bb])
        f.write('\t!byte ' + s + '\n')
    f.write('\n')
    
################################################################################
'''
The base timer tic frequency is:
    PAL   50Hz  0.985 MHz  1.015 μs
    NTSC  60Hz  1.023 MHz  0.978 μs
'''
screen_Hz, tic_MHz, us_per_tic = {
    'PAL' :(50,0.985,1.015),
    'NTSC':(60,1.023,0.978)
}['PAL']


##################################as##############################################
'''
It would be nice to poll less? 
A joystick can be interpreted as a velocity. The animation updates are expensive. 
'''
cursor_tic_delay      = 1
cursor_event_divider  = 1 + cursor_tic_delay
cusor_poll_per_s      = 35*cursor_event_divider
us_per_cursor_poll    = 1000*1000/cusor_poll_per_s
tics_per_poll         = us_per_cursor_poll/us_per_tic
tics_per_poll         = int(round(tics_per_poll))
assert tics_per_poll <= 0xffffd

################################################################################
'''
We are now robustly accumulating elapsed ticks in a uint32
frames per second should be  1/(1.015 * 256 * 256 / 1000 / 1000 * arange(256))
Right now FPS is higher than can be measured.
This means that we... 
'''

# right shift here must match number of asl32 in update_frame_duration
shiftp   = 5
overbit  = 2
overbite = (1<<overbit)
base_events_per_frame = maximum(0.0001,arange(0,256*overbite))

frames_per_second = 1/(us_per_tic * (256>>shiftp) * 256 / 1000 / 1000 * base_events_per_frame)
frames_per_second = minimum(99.99,frames_per_second)
frames_per_second = ['%07.4f'%i for i in frames_per_second]
fps_mi = [int(s.split('.')[1][-2:],16) for s in frames_per_second]
fps_lo = [int(s.split('.')[1][:2],16) for s in frames_per_second]
fps_hi = [int(s.split('.')[0],16) for s in frames_per_second]

with open("tabfps.asm", 'w') as f:
    f.write('\n')
    f.write('\n')
    f.write('TICDLY = %d\n'%cursor_tic_delay)
    f.write('TIC_PER_POLL = $%04X\n'%tics_per_poll)
    
    f.write('\n')
    f.write('\n')
    f.write('FPS_DIGITS_MI:\n')
    write_table_bytes(f,fps_mi)
    f.write('FPS_DIGITS_LO:\n')
    write_table_bytes(f,fps_lo)
    f.write('FPS_DIGITS_HI:\n')
    write_table_bytes(f,fps_hi)

frames_per_second = 0.5
lines_per_frame   = H2
lines_per_second  = lines_per_frame * frames_per_second
us_per_line       = 1000000/lines_per_second
tics_per_line     = us_per_line/us_per_tic





















