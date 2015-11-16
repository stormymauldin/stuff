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
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.UndefinedValue;

/**
 * OCL Let-expression.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class ExpLet extends Expression {
    private String fVarname;
    private Type fVarType;
    private Expression fVarExpr;
    private Expression fInExpr;
    
    public ExpLet(String varname, 
		  Type varType,
		  Expression varExpr,
		  Expression inExpr)
	throws ExpInvalidException
    {
	super(inExpr.type());
	fVarname = varname;
	fVarType = varType;
	fVarExpr = varExpr;
	fInExpr = inExpr;
	if ( ! fVarExpr.type().isSubtypeOf(fVarType) )
	    throw new ExpInvalidException(
		  "Type of variable expression `" + fVarExpr.type() +
		  "' does not match declared type `" + fVarType + "'.");
    }

    /**
     * Evaluates expression and returns result value.
     */
    public Value eval(EvalContext ctx) {
	ctx.enter(this);
	Value res = null;
	Value varValue = fVarExpr.eval(ctx);
	ctx.pushVarBinding(fVarname, varValue);
	res = fInExpr.eval(ctx);
	ctx.popVarBinding();
	ctx.exit(this, res);
	return res;
    }

    public String toString() {
	return "let " + fVarname + " : " + fVarType + " = " + 
	    fVarExpr + " in " + fInExpr;
    }
}
