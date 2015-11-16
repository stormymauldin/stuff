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

package org.tzi.use.uml.ocl.value;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Variable bindings bind names to values. Bindings are kept on a
 * stack and can be retrieved by name. Main use is for expression
 * evaluation.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class VarBindings  {

    public class Entry {
	String fVarname;
	Value fValue;

	Entry(String varname, Value value) {
	    fVarname = varname;
	    fValue = value;
	}

	public String toString() {
	    return fVarname + " : " + fValue.type() + " = " + fValue;
	}
    }

    private ArrayList fBindings;

    /**
     * Creates an empty VarBindings.
     */
    public VarBindings() {
	fBindings = new ArrayList();
    }

    /**
     * Creates a VarBindings object and initializes it with the
     * VarBindings object passed as parameter.
     */
    public VarBindings(VarBindings bindings) {
	fBindings = (ArrayList) bindings.fBindings.clone();
    }

    /**
     * Adds all given bindings.
     */
    public void add(VarBindings bindings) {
	fBindings.addAll(bindings.fBindings);
    }


    public void push(String varname, Value value) {
	fBindings.add(new Entry(varname, value));
    }

    public void pop() {
	fBindings.remove(fBindings.size() - 1);
    }

    /**
     * Removes the latest added entry with given name.
     */
    public void remove(String varname) {
	// search in reverse order
	for (int i = fBindings.size() - 1; i >= 0; i--) {
	    Entry b = (Entry) fBindings.get(i);
	    if ( b.fVarname.equals(varname) ) {
		fBindings.remove(i);
		break;
	    }
	}
    }

    /** 
     * Searches current bindings for variable name. Visibility is
     * determined by the order of elements. Variable bindings may thus
     * be hidden by bindings at earlier positions.
     *
     * @return value for name binding or null if not bound
     */
    public Value getValue(String name) {
	// search in reverse order
	for (int i = fBindings.size() - 1; i >= 0; i--) {
	    Entry b = (Entry) fBindings.get(i);
	    if ( b.fVarname.equals(name) )
		return b.fValue;
	}
	return null;
    }

    /**
     * Returns an iterator over VarBindings.Entry objects.
     */
    public Iterator iterator() {
	return fBindings.iterator();
    }

    /**
     * Returns string representation of bindings useful for debugging.
     */
    public String toString() {
	return "VarBindings: " + fBindings.toString();
    }
}

