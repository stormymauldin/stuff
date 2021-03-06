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

package org.tzi.use.uml.ocl.type;
import java.util.Set;
import java.util.HashSet;

/**
 * The OCL Integer type.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class IntegerType extends BasicType {

    IntegerType() {
	super("Integer");
    }
    
    /** 
     * Returns true if this type is a subtype of <code>t</code>. 
     */
    public boolean isSubtypeOf(Type t) {
	return t.isNumber() || t.isOclAny();
    }

    /** 
     * Returns the set of all supertypes (including this type).
     */
    public Set allSupertypes() {
	Set res = new HashSet(3);
	res.add(TypeFactory.mkOclAny());
	res.add(TypeFactory.mkReal());
	res.add(this);
	return res;
    }

    /**
     * Returns true if the passed type is equal.
     */
    public boolean equals(Object obj) {
	return ( obj == this || obj instanceof IntegerType );
    }
}
