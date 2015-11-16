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

package org.tzi.use.uml.ocl.expr;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.UndefinedValue;
import org.tzi.use.uml.ocl.value.ObjectValue;
import org.tzi.use.uml.ocl.value.SetValue;
import org.tzi.use.uml.ocl.value.SequenceValue;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.ocl.type.ObjectType;
import org.tzi.use.uml.ocl.type.SetType;
import org.tzi.use.uml.ocl.type.SequenceType;
import java.util.List;
import java.util.Iterator;

/**
 * Navigation expression from one class to another.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class ExpNavigation extends Expression {
    private MAssociationEnd fSrcEnd;
    private MAssociationEnd fDstEnd;
    private Expression fObjExp;
    
    public ExpNavigation(Expression objExp,
			 MAssociationEnd srcEnd,
			 MAssociationEnd dstEnd)
	throws ExpInvalidException
    {
	// set result type later
	super(null);

	// let c be the class at dstEnd, then the result type is:
	// (1) c if the multiplicity is max. one
	// (2) Set(c) if the multiplicity is greater than 1 
	// (3) Sequence(c) if the association end is marked as {ordered}

	Type t = TypeFactory.mkObjectType(dstEnd.cls());
	if ( dstEnd.multiplicity().isCollection() ) {
	    if ( dstEnd.isOrdered() )
		t = TypeFactory.mkSequence(t);
	    else
		t = TypeFactory.mkSet(t);
	}
	setResultType(t);

	fSrcEnd = srcEnd;
	fDstEnd = dstEnd;
	fObjExp = objExp;
	if ( ! objExp.type().isObjectType() )
	    throw new ExpInvalidException(
		   "Target expression of navigation operation must have " +
		   "object type, found `" + objExp.type() + "'.");
    }

    /**
     * Evaluates expression and returns result value.
     */
    public Value eval(EvalContext ctx) {
	ctx.enter(this);
	Value res = new UndefinedValue(type());
        Value val = fObjExp.eval(ctx);

	// if we don't have an object we can't navigate 
	if ( ! val.isUndefined() ) {
	    // get the object
	    ObjectValue objVal = (ObjectValue) val;
	    MObject obj = objVal.value();
	    MSystemState state = isPre() ? ctx.preState() : ctx.postState();
	    MObjectState objState = obj.state(state);

	    // get objects at association end
	    List objList = obj.getLinkedObjects(state, fSrcEnd, fDstEnd);
	    Type resultType = type();
	    if ( resultType.isObjectType() ) {
		if ( objList.size() > 1 )
		    throw new RuntimeException("expected link set size 1 at " + 
					       "association end `" + fDstEnd + 
					       "', found: " + 
					       objList.size());
		if ( objList.size() == 1 ) {
		    obj = (MObject) objList.get(0);
		    if ( obj.exists(state) )
			res = new ObjectValue((ObjectType) type(), obj);
		}
	    } else if ( resultType.isSet() ) {
		res = new SetValue(((SetType) resultType).elemType(), 
				   oidsToObjectValues(state, objList));
	    } else if ( resultType.isSequence() ) {
		res = new SequenceValue(((SequenceType) resultType).elemType(), 
				   oidsToObjectValues(state, objList));
	    } else
		throw new RuntimeException("Unexpected association end type `" + 
					   resultType + "'");
	}

	ctx.exit(this, res);
	return res;
    }

    private Value[] oidsToObjectValues(MSystemState state, List objList) {
	Value[] res = new ObjectValue[objList.size()];
	Iterator it = objList.iterator();
	int i = 0;
	while ( it.hasNext() ) {
	    MObject obj = (MObject) it.next();
	    MObjectState objState = obj.state(state);
	    if ( objState != null )
		res[i++] = new ObjectValue(obj.type(), obj);
	}
	return res;
    }


    public String toString() {
	return fObjExp + "." + fDstEnd.name() + atPre();
    }
}

