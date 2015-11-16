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

package org.tzi.use.util.input;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A Readline implementation using a BufferedReader as source.
 * 
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public 
class StreamReadline implements Readline {
    private BufferedReader fBufferedReader;
    private String fStaticPrompt;
    private boolean fEchoInput;

    /**
     * Constructs a new StreamReadline. If echoInput is true, each
     * line read will be echoed to stdout. staticPrompt may be used to
     * overwrite the prompt passed to the readline method.
     */
    public StreamReadline(BufferedReader reader,
			  boolean echoInput,
			  String staticPrompt) {
	fBufferedReader = reader;
	fStaticPrompt = staticPrompt;
	fEchoInput = echoInput;
    }

    public StreamReadline(BufferedReader reader,
			  boolean echoInput) {
	this(reader, echoInput, null);
    }

    /**
     * Reads a line of text from the underlying BufferedReader.
     *
     * @return     A String containing the contents of the line, not including
     *             any line-termination characters, or null if the end of the
     *             stream has been reached.
     *
     * @exception  IOException  If an I/O error occurs.
     */
    public String readline(String prompt) throws IOException {
	if ( fStaticPrompt != null )
	    System.out.print(fStaticPrompt);
	else
	    System.out.print(prompt);
	String line = fBufferedReader.readLine();
	if ( fEchoInput )
	    if ( line != null )
		System.out.println(line);
	    else 
		System.out.println();
	return line;
    }	

    /**
     * Closes the Readline stream.
     */
    public void close() throws IOException {
	fBufferedReader.close();
    }

    // The following operations are just noops.

    public void usingHistory() {}

    public void readHistory(String filename) {}

    public void writeHistory(String filename) {}
}
