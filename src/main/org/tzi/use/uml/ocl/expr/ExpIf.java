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
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.UndefinedValue;

/**
 * If-then-else-endif.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class ExpIf extends Expression {
    private Expression fCondition;
    private Expression fThenExp;
    private Expression fElseExp;
    
    public ExpIf(Expression condition,
		 Expression thenExp,
		 Expression elseExp)
	throws ExpInvalidException
    {
	// result type is type of then/else branch (must be identical)
	super(thenExp.type());
	fCondition = condition;
	fCondition.assertBoolean();
	fThenExp = thenExp;
	fElseExp = elseExp;
	if ( ! fThenExp.type().equals(fElseExp.type()) )
	    throw new ExpInvalidException(
		  "Branches of if expression have different type, " +
		  "found `" + fThenExp.type() + 
		  "' and `" + fElseExp.type() + "'.");
    }

    /**
     * Evaluates expression and returns result value. If the condition
     * evaluates to true return the value of the "then"
     * expression. Otherwise, if the condition evaluates to undefined
     * or false, the result is given by the "else" expression.
     *  
     */
    public Value eval(EvalContext ctx) {
	ctx.enter(this);
	Value res = new UndefinedValue(type());
	Value condValue = fCondition.eval(ctx);
	// condition may be undefined
	if ( condValue.isDefined() ) {
	    boolean cond = ((BooleanValue) condValue).value();
	    if ( cond )
		res = fThenExp.eval(ctx);
	    else
		res = fElseExp.eval(ctx);
	}
	ctx.exit(this, res);
	return res;
    }

    public String toString() {
	return "if " + fCondition + 
	    " then " + fThenExp + 
	    " else " + fElseExp +
	    " endif";
    }
}
