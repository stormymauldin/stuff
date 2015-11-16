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

package org.tzi.use.uml.mm;

/** 
 * An AssociationEnd stores information about the role a class plays
 * in an association.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class MAssociationEnd extends MModelElementImpl {
    private MAssociation fAssociation; // Owner of this association end
    private MClass fClass;	// associated class
    private MMultiplicity fMultiplicity; // multiplicity spec
    private int fKind; // none, aggregation, or composition
    private boolean fIsOrdered;	// use as Set or Sequence

    /** 
     * Creates a new association end. 
     *
     * @param cls       the class to be connected.
     * @param rolename  role that the class plays in this association.
     * @param mult      multiplicity of end.
     * @param kind      MAggregationKind
     * @param isOrdered use as Set or Sequence
     */
    public MAssociationEnd(MClass cls, 
			   String rolename, 
			   MMultiplicity mult, 
			   int kind,
			   boolean isOrdered) {
	super(rolename);
	fClass = cls;
	fMultiplicity = mult;
	setAggregationKind(kind);
	fIsOrdered = isOrdered;
    }

    private void setAggregationKind(int kind) {
	if ( kind != MAggregationKind.NONE 
	     && kind != MAggregationKind.AGGREGATION
	     && kind != MAggregationKind.COMPOSITION )
	    throw new IllegalArgumentException("Invalid kind");
	fKind = kind;
    }

    /**
     * Sets the owner association. This operation should be called by
     * an implementation of MAssociation.addAssociationEnd.
     *
     * @see MAssociation#addAssociationEnd
     */
    public void setAssociation(MAssociation assoc) {
	fAssociation = assoc;
    }

    /**
     * Returns the owner association.
     */
    public MAssociation association() {
	return fAssociation;
    }

    /**
     * Returns the associated class.
     */
    public MClass cls() {
	return fClass;
    }

    /**
     * Returns the multiplicity of this association end.
     */
    public MMultiplicity multiplicity() {
	return fMultiplicity;
    }

    /**
     * Returns the aggregation kind of this association end.
     */
    public int aggregationKind() {
	return fKind;
    }

    /**
     * Returns true if this association end is ordered.
     */
    public boolean isOrdered() {
	return fIsOrdered;
    }

    /**
     * Process this element with visitor.
     */
    public void processWithVisitor(MMVisitor v) {
	v.visitAssociationEnd(this);
    }

    public int hashCode() { 
	return name().hashCode() + fAssociation.hashCode() + fClass.hashCode();
    }

    /**
     * Two association end are equal iff they connect the same class
     * in the same association with the same role name.  
     */
    public boolean equals(Object obj) { 
	if ( obj == this )
	    return true;
	if ( obj instanceof MAssociationEnd ) {
	    MAssociationEnd aend = (MAssociationEnd) obj;
	    return name().equals(aend.name()) 
		&& fAssociation.equals(aend.association())
		&& fClass.equals(aend.cls());
	}
	return false;
    }
}
