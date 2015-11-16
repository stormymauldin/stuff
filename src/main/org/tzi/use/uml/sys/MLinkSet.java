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

package org.tzi.use.uml.sys;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A link set contains instances of an association.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public final class MLinkSet {
    private MAssociation fAssociation; // The type of all links of this set.
    private Set fLinks;		// (MLink)

    MLinkSet(MAssociation assoc) {
	fAssociation = assoc;
	fLinks = new HashSet();
    }

    /**
     * Copy constructor.
     */
    MLinkSet(MLinkSet x) {
	fAssociation = x.fAssociation;
	fLinks = new HashSet();
	fLinks.addAll(x.fLinks);
    }

    /**
     * Selects all links whose link ends at <code>aend</code> connect
     * <code>obj</code>. 
     *
     * @return Set(MLink)
     */
    Set select(MAssociationEnd aend, MObject obj) {
	Set res = new HashSet();

	Iterator it = fLinks.iterator();
	while ( it.hasNext() ) {
	    MLink link = (MLink) it.next();
	    MLinkEnd linkEnd = link.linkEnd(aend);
	    if ( linkEnd.object().equals(obj) )
		res.add(link);
	}
	return res;
    }

    /**
     * Removes all links whose link ends at <code>aend</code> connect
     * <code>obj</code>. 
     *
     * @return Set(MLink) the set of removed links
     */
    Set removeAll(MAssociationEnd aend, MObject obj) {
	Set res = new HashSet();
	Iterator it = fLinks.iterator();
	while ( it.hasNext() ) {
	    MLink link = (MLink) it.next();
	    MLinkEnd linkEnd = link.linkEnd(aend);
	    if ( linkEnd.object().equals(obj) ) {
		res.add(link);
		it.remove();
	    }
	}
	return res;
    }


    /**
     * Returns the association describing this link set.
     */
    public MAssociation association() {
	return fAssociation;
    }

    /**
     * Returns the number of links in this set.
     */
    public int size() {
	return fLinks.size();
    }

    /**
     * Returns the set of links in this set.
     *
     * @return Set(MLink)
     */
    public Set links() {
	return fLinks;
    }

    /**
     * Checks whether a link is in this set.
     *
     * @return true if the link is in this set.
     */
    boolean contains(MLink link) {
	return fLinks.contains(link);
    }

    /**
     * Adds a link to this link set.
     *
     * @return true if the link set did not already contain the link.
     */
    boolean add(MLink link) {
	return fLinks.add(link);
    }

    /**
     * Returns true if there is a link connecting the given set of
     * objects.  
     */
    public boolean hasLinkBetweenObjects(Set objects) {
	Iterator linkIter = fLinks.iterator();
	while ( linkIter.hasNext() ) {
	    MLink link = (MLink) linkIter.next();
	    if ( link.linkedObjects().equals(objects) )
		return true;
	}
	return false;
    }

    /**
     * Removes a link from this link set.
     *
     * @return true if the link set did contain the link.
     */
    boolean remove(MLink link) {
	return fLinks.remove(link);
    }

//      /**
//       * Dump all information about this link set to stdout.
//       */
//      public void dump() {
//  	Iterator it = fLinks.iterator();
//  	while ( it.hasNext() ) {
//  	    MLink link = (MLink) it.next();
//  	    System.out.println(link);
//  	}
//      }


}

