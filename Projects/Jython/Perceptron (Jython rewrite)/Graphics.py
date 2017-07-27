#!/usr/bin/env jython

from java.awt import GraphicsEnvironment

print GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.createCompatibleImage(100, 100).type



