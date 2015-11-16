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
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.BooleanValue;

/**
 * oclIsKindOf
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class ExpIsKindOf extends Expression {
    private Expression fSourceExpr;
    private Type fTargetType;
    
    public ExpIsKindOf(Expression sourceExpr, Type targetType)
	throws ExpInvalidException
    {
	super(TypeFactory.mkBoolean());
	fSourceExpr = sourceExpr;
	fTargetType = targetType;
    }

    /**
     * Evaluates expression and returns result value. 
     */
    public Value eval(EvalContext ctx) {
	ctx.enter(this);
  	Value res = BooleanValue.FALSE;
	Value v = fSourceExpr.eval(ctx);
	
	// Note: the value may be undefined, still the type test is valid!
	//System.err.println("value: " + v + ", type: " + v.type());
	if ( v.type().isSubtypeOf(fTargetType) )
	    res = BooleanValue.TRUE;
	ctx.exit(this, res);
  	return res;
    }

    public String toString() {
	return fSourceExpr + ".oclIsKindOf(" + fTargetType + ")";
    }
}
