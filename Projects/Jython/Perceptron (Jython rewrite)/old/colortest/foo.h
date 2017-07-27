
#define Q1 (ct/3.)
#define Q2 (0.5*sqrt3*st/3.)
#define Q3 (-st/sqrt3)
#define Q4 (0.5*ct)
#define r1 (QRR*r+QRG*g+QRB*b)

#define ct (cos(t)*2)
#define st (sin(t)*2)
#define sqrt3 sqrt(3)

QRR = 1./3+ct/3
QRG = 1./3-ct/6+st*sqrt3/6
QRB = 1./3-ct/6-st*sqrt3/6
QBR = 1./2-1./6-ct/6+st/sqrt3/2
QBG = 1./2-1./6+ct/12-sqrt3*st/12-ct/4-st/sqrt3/4
QBB = 1./2-1./6+ct/12+sqrt3*st/12+ct/4-st/sqrt3/4
QGR = 1./2-st/sqrt3-1./6-ct/6+st/sqrt3/2
QGG = 1./2+ct/2+st/sqrt3/2+-1./6+ct/12-sqrt3*st/12-ct/4-st/sqrt3/4
QGB = 1./2-ct/2+st/sqrt3/2-1./6+ct/12+sqrt3*st/12+ct/4-st/sqrt3/4

QRR = 0.666667 (cos(t)+0.5)
QRG = 0.57735 sin(t)-0.333333 cos(t)+0.333333
QRB = -0.57735 sin(t)-0.333333 cos(t)+0.333333
QBR = 0.57735 sin(t)-0.333333 cos(t)+0.333333
QBG = -0.57735 sin(t)-0.333333 cos(t)+0.333333
QBB = 0.666667 (cos(t)+0.5)
QGR = -0.57735 sin(t)-0.333333 cos(t)+0.333333
QGG = 0.666667 (cos(t)+0.5)
QGB = 0.57735 sin(t)-0.333333 cos(t)+0.333333

Q0 = 0.666667 (cos(t)+0.5)
Q1 = 0.57735 sin(t)
Q2 = -0.333333 cos(t)+0.333333

QRR = Q0
QRG = Q2+Q1
QRB = Q2-Q1

QBR = Q2+Q1
QBG = Q2-Q1
QBB = Q0

QGR = Q2-Q1
QGG = Q0
QGB = Q2+Q1

