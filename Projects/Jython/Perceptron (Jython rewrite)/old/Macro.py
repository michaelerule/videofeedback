#!/usr/bin/env jython
# skeletal implementation of #define

__MACRO_DEBUG = 1

def substitute(line,macro,(args,definition)):
	location = line.find(macro)
	if __MACRO_DEBUG:
		print line,macro,location
	if not len(args):	
		return line.replace(macro,definition)
	vals,residual = parseArgs(line[location:])
	if __MACRO_DEBUG:
		print vals,residual
	if not len(vals)==len(args):
		print line,macro,definition,vals,len(vals),args,len(args)
		assert 0
	for i,arg in enumerate(args):
		 definition=definition.replace(arg,'#%d'%i)
	for i,val in enumerate(vals):
		 definition=definition.replace('#%d'%i,val)
	if __MACRO_DEBUG:
		print definition
	return definition + residual

def substituteLine(line,context):
	if __MACRO_DEBUG:
		print line
	while any(m in line for m in context):
		for macro in context:
			if macro in line:
				try:
					suffix = line[line.find(macro):]
					suffix = substitute(suffix,macro,context[macro])
					line   = line[:line.find(macro)]+suffix
				except Exception, e:
					print e
	return line

def parseArgs(string):
	parenCount = 0
	limits = []
	for i,c in enumerate(string):
		if c=='(':
			parenCount +=1
		if c in '(,)' and parenCount==1:
			limits += [i]
		if c==')':
			parenCount -=1
			if parenCount == 0:
				break
	if len(limits):
		ending = string[limits[-1]+1:]
	else:
		ending = None
	return [string[a+1:b].strip() for a,b in zip(limits[:-1],limits[1:])],ending

def addMacro(line,context):
	tokens = line.split()
	macro = tokens[1]
	definition = ''.join(tokens[2:])
	assert not macro in context
	assert len(definition)
	args = parseArgs(macro)[0]
	if macro.find('(')!=-1:
		macro = macro[0:macro.find('(')]
	context[macro] = (args,definition)
	return macro

def parseMacros(lines):
	context = {}
	parsed = []
	for line in lines:
		whitespace = ''
		for c in line:
			if c in ' \t':
				whitespace+=c
			else:
				break
		line = line.strip()
		if __MACRO_DEBUG:
			print line
		if '#define'==line[:7]:
			macro = addMacro(line,context)
			if __MACRO_DEBUG:
				print line,macro,context[macro]
		else:
			parsed.append(whitespace+substituteLine(line,context)+'\n')
	return parsed

if __name__ == "__main__":
	import sys
	print sys.argv[-1]
	print '\n'.join(parseMacros(open(sys.argv[-1],'r').readlines()))

	
	
