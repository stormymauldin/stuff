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
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.VarDeclList;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;
import java.util.List;

/** 
 * A ModelFactory creates instances of the Metamodel.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class ModelFactory {

    public MModel createModel(String name) {
	return new MModel(name);
    }

    public MClass createClass(String name, boolean isAbstract) {
	return new MClass(name, isAbstract);
    }

    public MClassInvariant createClassInvariant(String name, String var, 
						MClass cls, Expression inv) 
	throws ExpInvalidException
    {
	return new MClassInvariant(name, var, cls, inv);
    }

    public MPrePostCondition createPrePostCondition(String name, 
						    MOperation op, 
						    boolean isPre, 
						    Expression constraint) 
	throws ExpInvalidException
    {
	return new MPrePostCondition(name, op, isPre, constraint);
    }

    public MAttribute createAttribute(String name, Type t) {
	return new MAttribute(name, t);
    }

    public MOperation createOperation(String name, VarDeclList varDeclList, 
				      Type resultType) {
	return new MOperation(name, varDeclList, resultType);
    }


    public MAssociation createAssociation(String name) {
	return new MAssociationImpl(name);
    }

    public MGeneralization createGeneralization(MClass child, MClass parent) {
	return new MGeneralization(child, parent);
    }

    /** 
     * Creates a new association end. 
     *
     * @param cls       the class to be connected.
     * @param rolename  role that the class plays in this association.
     * @param mult      multiplicity of end.
     * @param kind      MAggregationKind
     * @param isOrdered use as Set or Sequence
     */
    public MAssociationEnd createAssociationEnd(MClass cls, 
						String rolename, 
						MMultiplicity mult, 
						int kind,
						boolean isOrdered) {
	return new MAssociationEnd(cls, rolename, mult, kind, isOrdered);
    }

    public MMultiplicity createMultiplicity() {
	return new MMultiplicity();
    }

    /**
     * Creates an association. The list <code>associationEnds</code> must
     * contain at least two association ends.  
     */
    public MAssociation createAssociation(String name, List associationEnds) {
	MAssociation assoc = new MAssociationImpl(name);
	//	assoc.addEnd(associationEnds
	return assoc;
    }
}
