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
import org.tzi.use.uml.ocl.expr.ExpLet;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;
import org.tzi.use.uml.ocl.type.Type;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTLetExpression extends ASTExpression {
    private MyToken fVarToken;
    private ASTType fVarType;	// optional: may be null
    private ASTExpression fVarExpr;
    private ASTExpression fInExpr;

    public ASTLetExpression(MyToken varToken, 
			    ASTType type,
			    ASTExpression varExpr) {
	fVarToken = varToken;
	fVarType = type;
	fVarExpr = varExpr;
    }

    void setInExpr(ASTExpression inExpr) {
	fInExpr = inExpr;
    }	

    public Expression gen(Context ctx) throws SemanticException {
	Expression res = null;
	Expression varExpr = fVarExpr.gen(ctx);
	Type varType;

	if ( fVarType == null )
	    varType = varExpr.type();
	else {
	    varType = fVarType.gen(ctx);
	    if ( ! varExpr.type().isSubtypeOf(varType) )
		throw new SemanticException(fVarExpr.getStartToken(),
					    "Expected expression of type `" +
					    varType + "', found `" +
					    varExpr.type() + "'.");
	}

	Symtable vars = ctx.varTable();
	vars.enterScope();
	vars.add(fVarToken, varType);
	Expression inExpr = fInExpr.gen(ctx);
	vars.exitScope();

	try {
	    res = new ExpLet(fVarToken.getText(),
			     varType,
			     varExpr,
			     inExpr);
	} catch (ExpInvalidException ex) {
	    throw new SemanticException(fVarToken, ex);
	}

	return res;
    }
}
