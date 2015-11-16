/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  
 */

/* $ProjectHeader: use 2-1-0-release.1 Sun, 09 May 2004 13:57:11 +0200 mr $ */

package org.tzi.use.parser;
import java.io.PrintWriter;
import antlr.RecognitionException;
import antlr.TokenStreamException;

class MyParser extends GParser {
    private int fNest = 0;
    private int fErrorCount = 0;
    private PrintWriter fErr;

    MyParser(String inputName, MyLexer lexer, PrintWriter err) {
	super(lexer);
	setFilename(inputName);
	fErr = err;
	lexer.setParser(this);
    }

    int errorCount() {
	return fErrorCount;
    }

    void incErrorCount() {
	fErrorCount++;
    }

    /* Overridden methods. */

    public void reportError(RecognitionException ex) {
  	// Log.error(ex);
	fErr.println(getFilename() +":" + 
		     ex.getLine() + ":" +
		     ex.getColumn() + ": " + 
		     ex.getMessage());
	incErrorCount();
    }

    public void traceIn(String rname) throws TokenStreamException {
	for (int i = 0; i < fNest; i++)
	    System.out.print(" ");
	super.traceIn(rname);
	fNest++;
    }

    public void traceOut(String rname) throws TokenStreamException {
	fNest--;
	for (int i = 0; i < fNest; i++)
	    System.out.print(" ");
	super.traceOut(rname);
    }
}
