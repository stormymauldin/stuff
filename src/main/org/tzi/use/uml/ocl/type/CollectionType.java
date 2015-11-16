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
import java.util.Iterator;

/**
 * Collection is the abstract base class for set, sequence, and bag.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 * @see		SetType
 * @see		SequenceType
 * @see		BagType
 */

public class CollectionType extends Type {

    private Type fElemType;

    protected CollectionType(Type elemType) {
	fElemType = elemType;
    }

    public Type elemType() {
	return fElemType;
    }

    /** 
     * Returns true if this type is a subtype of <code>t</code>. 
     */
    public boolean isSubtypeOf(Type t) {
	if ( ! t.isTrueCollection() )
	    return false;

	CollectionType t2 = (CollectionType) t;
	if ( fElemType.isSubtypeOf(t2.elemType()) )
	    return true;
	return false;
    }

    /** 
     * Returns the set of all supertypes (including this type).  If
     * this collection has type Collection(T) the result is the set of
     * all types Collection(T') where T' <= T.
     */
    public Set allSupertypes() {
	Set res = new HashSet();
	Set elemSuper = fElemType.allSupertypes();
	Iterator typeIter = elemSuper.iterator();
	while ( typeIter.hasNext() ) {
	    Type t = (Type) typeIter.next();
	    res.add(TypeFactory.mkCollection(t));
	}
	return res;
    }

    public String toString() {
	return "Collection(" + elemType() + ")";
    }

    public boolean equals(Object obj) {
	if ( obj.getClass() == CollectionType.class )
	    return ((CollectionType) obj).elemType().equals(elemType());
	return false;
    }
}
