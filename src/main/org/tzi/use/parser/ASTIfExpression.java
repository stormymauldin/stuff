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

package org.tzi.use.parser;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpIf;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTIfExpression extends ASTExpression {
    private MyToken fIfToken;
    private ASTExpression fCond;
    private ASTExpression fThen;
    private ASTExpression fElse;

    public ASTIfExpression(MyToken ifToken,
			   ASTExpression cond,
			   ASTExpression t, 
			   ASTExpression e) {
	fIfToken = ifToken;
	fCond = cond;
	fThen = t;
	fElse = e;
    }

    public Expression gen(Context ctx) throws SemanticException {
	Expression res;
	try {
	    res = new ExpIf(fCond.gen(ctx), 
			    fThen.gen(ctx),
			    fElse.gen(ctx));
	} catch (ExpInvalidException ex) {
	    throw new SemanticException(fIfToken, ex);
	}
	return res;
    }

    public String toString() {
	return "(" + fCond + " " + fThen + " " + fElse + ")";
    }
}
