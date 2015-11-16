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
import org.tzi.use.util.Log;
import org.tzi.use.util.StringUtil;
import antlr.CharBuffer;
import antlr.ANTLRHashString;
import antlr.Token;
import antlr.CharStreamException;
import java.io.Reader;
import java.io.PrintWriter;

/** 
 * Lexer class derived from the ANTLR generated lexer. Keeps track of
 * token start columns.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class MyLexer extends GLexer {
    protected int fTokColumn = 1;
    private PrintWriter fErr;
    private MyParser fParser;

    public MyLexer(Reader in, String filename, PrintWriter err) {
	super(new CharBuffer(in));
	setFilename(filename);
	fErr = err;
    }

    void setParser(MyParser parser) {
	fParser = parser;
    }

    public void consume() throws CharStreamException {
	if ( inputState.guessing == 0 ) {
	    if ( text.length() == 0 ) {
		// remember token start column
		fTokColumn = getColumn();
	    }
	}
	super.consume();
    }

    protected Token makeToken(int t) {
	MyToken token = 
	    new MyToken(getFilename(), getLine(), fTokColumn);
	token.setType(t);
	if ( t == EOF )
	    token.setText("end of file or input");
	return token;
    }

    /** 
     * Overrides default error-reporting function.
     */
    public void reportError(antlr.RecognitionException ex) {
	fErr.println(getFilename() +":" + 
		     ex.getLine() + ":" +
		     // if this is a NoViableAltForCharException there
		     // is still no column information in antlr, so we
		     // use the current column
		     getColumn() + ": " + 
		     ex.getMessage());
	// continue but remember that we had errors
	fParser.incErrorCount();
    }

    /**
     * Returns true if word is a reserved keyword. 
     */
    public boolean isKeyword(String word) {
	ANTLRHashString s = new ANTLRHashString(word, this);
	boolean res = literals.get(s) != null;
	Log.trace(this, "keyword " + word + ": " + res);
	return res;
    }

    public void traceIn(String rname) throws CharStreamException {
	traceIndent();
	traceDepth += 1;
	System.out.println("> lexer " + rname + ": c == '" + 
			   StringUtil.escapeChar(LA(1), '\'') + "'");
    }

    public void traceOut(String rname) throws CharStreamException {
	traceDepth -= 1;
	traceIndent();
	System.out.println("< lexer " + rname + ": c == '" +
			   StringUtil.escapeChar(LA(1), '\'') + "'");
    }
}
