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
import org.tzi.use.uml.ocl.value.CollectionValue;
import org.tzi.use.uml.ocl.value.UndefinedValue;
import java.util.Iterator;

/** 
 * OCL iterate expression.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public
class ExpIterate extends ExpQuery {
    private VarInitializer fAccuInitializer;

    /**
     * Constructs an iterate expression.
     */
    public ExpIterate(VarDeclList elemVarDecls,
		      VarInitializer accuInitializer,
		      Expression rangeExp,
		      Expression queryExp) 
	throws ExpInvalidException
    {
	// result type is type of accumulator
	super(accuInitializer.type(), elemVarDecls, rangeExp, queryExp);
	fAccuInitializer = accuInitializer;

	if ( accuInitializer == null )
	    throw new ExpInvalidException(
		   "Need an accumulator declaration.");

	if ( elemVarDecls.containsName(accuInitializer.name()) ) 
	    throw new ExpInvalidException("Redefinition of variable `" + 
		  accuInitializer.name() + "'.");
    
	// iterExp must be type conform to accuExp
	if ( ! queryExp.type().isSubtypeOf(accuInitializer.type()) )
	    throw new ExpInvalidException(
		  "Iteration expression type `" + queryExp.type() +
		  "' does not match accumulator type `" + 
		  accuInitializer.type() + "'.");
    }

    /**
     * Constructs an iteration expression with one element variable.
     */
    public ExpIterate(VarDecl elemVarDecl,
		      VarInitializer accuInitializer,
		      Expression rangeExp, 
		      Expression queryExp) 
	throws ExpInvalidException
    {
	this(new VarDeclList(elemVarDecl), accuInitializer,
	     rangeExp, queryExp);
    }

    /** 
     * Return name of query expression.
     */
    public String name() {
	return "iterate";
    }

    public String toString() {
	String res = fRangeExp + "->" + this.name() + "(";
	if ( ! fElemVarDecls.isEmpty() ) {
	    res += fElemVarDecls + "; ";
	}
	res += fAccuInitializer + " | " + fQueryExp + ")";
	return res;
    }

    /**
     * Evaluates expression and returns result value.
     */
    public Value eval(EvalContext ctx) {
	ctx.enter(this);
	Value res = null;

	// evaluate range
	Value v = fRangeExp.eval(ctx);
	if ( v.isUndefined() )
	    return new UndefinedValue(type());
	CollectionValue rangeVal = (CollectionValue) v;

	// prepare result value
	Value accuVal = fAccuInitializer.initExpr().eval(ctx);

	// we need recursion for the permutation of assignments of
	// range values to all element variables.
	res = eval0(ctx, 0, rangeVal, accuVal);
	ctx.exit(this, res);
	return res;
    }

    private final Value eval0(EvalContext ctx, 
			      int nesting, 
			      CollectionValue rangeVal,
			      Value accuVal)
    {
	// loop over range elements
	Iterator collIter = rangeVal.iterator();
	while ( collIter.hasNext() ) {
	    Value elemVal = (Value) collIter.next();

	    // bind element variable to range element, if variable was
	    // declared
	    if ( ! fElemVarDecls.isEmpty() )
		ctx.pushVarBinding(fElemVarDecls.varDecl(nesting).name(), elemVal);

	    if ( ! fElemVarDecls.isEmpty() && nesting < fElemVarDecls.size() - 1) {
		// call recursively to iterate over range while
		// assigning each value to each element variable
		// eventually
		accuVal = eval0(ctx, nesting + 1, rangeVal, accuVal);
	    } else {
		// bind accumulator variable
		ctx.pushVarBinding(fAccuInitializer.name(), accuVal);

		// evaluate iterate expression and assign new accumulator value
		accuVal = fQueryExp.eval(ctx);

		ctx.popVarBinding();
	    }
		
	    if ( ! fElemVarDecls.isEmpty() )
		ctx.popVarBinding();
	}
	return accuVal;
    }
}

