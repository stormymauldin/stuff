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

package org.tzi.use.util;
import java.io.Writer;
import java.io.IOException;


/** 
 * A TeeWriter sends its output to two writers.
 * 
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class TeeWriter extends Writer {
    private Writer fOne;
    private Writer fTwo;

    /**
     * Creates a new tee writer.
     */
    public TeeWriter(Writer one, Writer two) {
	fOne = one;
	fTwo = two;
    }

    public void write(char cbuf[], int off, int len) throws IOException {
	fOne.write(cbuf, off, len);
	fTwo.write(cbuf, off, len);
    }

    public void flush() throws IOException {
	fOne.flush();
	fTwo.flush();
    }

    public void close() throws IOException {
	fOne.close();
	fTwo.close();
    }
}
