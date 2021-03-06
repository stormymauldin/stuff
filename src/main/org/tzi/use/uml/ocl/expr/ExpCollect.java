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

/** 
 * OCL collect expression.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class ExpCollect extends ExpQuery {
    
    /**
     * Constructs a collect expression. <code>elemVarDecl</code> may be null.
     */
    public ExpCollect(VarDecl elemVarDecl,
		      Expression rangeExp, 
		      Expression queryExp) 
	throws ExpInvalidException
    {
	// result type is bag or sequence of query expression type
	super( rangeExp.type().isSequence()
	         ? (Type) TypeFactory.mkSequence(queryExp.type())
	         : (Type) TypeFactory.mkBag(queryExp.type()), 
	       ( elemVarDecl != null ) 
	         ? new VarDeclList(elemVarDecl) 
		 : new VarDeclList(true),
	       rangeExp, queryExp);
    }

    /** 
     * Return name of query expression.
     */
    public String name() {
	return "collect";
    }

    /**
     * Evaluates expression and returns result value.
     */
    public Value eval(EvalContext ctx) {
	ctx.enter(this);
	Value res = evalCollect(ctx);
	ctx.exit(this, res);
	return res;
    }
}

