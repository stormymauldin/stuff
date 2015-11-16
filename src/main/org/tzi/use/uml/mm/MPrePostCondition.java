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
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;


/**
 * Pre- or postconditions are attached to an operation and hold OCL
 * expressions.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public final class MPrePostCondition extends MModelElementImpl {
    private MOperation fOp;	//  operation to be constrained
    private Expression fExpr;	//  boolean expression
    private boolean fIsPre;

    /**
     * Constructs a new pre- or postcondition.
     */
    MPrePostCondition(String name, MOperation op, boolean isPre, 
		      Expression constraint) 
	throws ExpInvalidException
    {
	super(name, isPre ? "pre" : "post");
	fOp = op;
	fIsPre = isPre;
	fExpr = constraint;
	fExpr.assertBoolean();
    }


    /** 
     * Returns the operation for which the constraint is specified.
     */
    public MOperation operation() {
	return fOp;
    }

    /** 
     * Returns the class of the operation for which the constraint is
     * specified.  
     */
    public MClass cls() {
	return fOp.cls();
    }

    /** 
     * Returns true if this is a precondition, false if this is a
     * postcondition.
     */
    public boolean isPre() {
	return fIsPre;
    }

    /** 
     * Returns the expression used as pre- or postcondition.
     */
    public Expression expression() {
	return fExpr;
    }

    /**
     * Returns a string representation of this model element.
     */
    public String toString() {
	return fOp.name() + "::" + name();
    }

    /**
     * Compares just the model element's name.
     */
    public int compareTo(Object o) {
	if ( o == this )
	    return 0;
	if ( ! (o instanceof MPrePostCondition) )
	    throw new ClassCastException();
	return toString().compareTo(((MPrePostCondition) o).toString());
    }

    /**
     * Process this element with visitor.
     */
    public void processWithVisitor(MMVisitor v) {
  	v.visitPrePostCondition(this);
    }
}
