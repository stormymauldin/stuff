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
import org.tzi.use.uml.ocl.type.Type;
import java.util.Stack;

/**
 * Stack for default context variable in nested expressions.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ExprContext {
    private int fId;

    /**
     * An entry has a name and a type.
     */
    class Entry {
	String fName;
	Type fType;

	Entry(String name, Type t) {
	    fName = name;
	    fType = t;
	}
    }

    /**
     * The stack of entries.
     */
    private Stack fEntries;	// (Entry)
    
    public ExprContext() {
	fEntries = new Stack();
    }

    /** 
     * Pushes a new entry on the stack.
     */
    public void push(String name, Type type) {
	fEntries.push(new Entry(name, type));
    }
    
    /** 
     * Pushes a new entry with generated name on the stack and returns
     * the name.
     */
    public String push(Type type) {
	String name = "$elem" + fId++;
	push(name, type);
	return name;
    }
    
    public boolean isEmpty() {
	return fEntries.empty();
    }
    /** 
     * Returns the top element of the stack.
     */
    public Entry peek() {
	return (Entry) fEntries.peek();
    }

    public void pop() {
	fEntries.pop();
    }

}
