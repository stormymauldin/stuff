# $ProjectHeader: use 2-1-0-release.1 Sun, 09 May 2004 13:57:11 +0200 mr $

TOPDIR = ..

USE = $(TOPDIR)/bin/use -nogui -nr

INFILES := $(wildcard *.in)
OUTFILES := $(INFILES:.in=.out) 
CMDFILES := $(INFILES:.in=.cmd) 
RESULTFILES := $(INFILES:.in=.result) 

all: $(OUTFILES) $(CMDFILES)

%.cmd: %.in mkcmd.pl
	./mkcmd.pl < $< > $@

%.out: %.in mkout.pl
	./mkout.pl $(basename $<) < $< > $@

test: all $(RESULTFILES)

test-out1: t1.out
	$(USE) $(basename $<).use $(basename $<).cmd

test-out2: t2.out
	$(USE) $(basename $<).use $(basename $<).cmd

%.result: %.in %.use
	-rm -f $@
	$(USE) $(basename $<).use $(basename $<).cmd | tail +2 | diff -u $(basename $<).out - > $(basename $<).result

clean:
	rm -f $(RESULTFILES) *~ core

maintainer-clean: clean
	rm -f $(OUTFILES) $(CMDFILES)

