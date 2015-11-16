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
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.mm.MPrePostCondition;
import org.tzi.use.uml.mm.MInvalidModelException;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.ocl.type.ObjectType;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTPrePostClause extends AST {
    MyToken fToken;		// pre or post
    MyToken fName;		// optional
    ASTExpression fExpr;

    public ASTPrePostClause(MyToken tok, MyToken name, ASTExpression e) {
	fToken = tok;
	fName = name;
	fExpr = e;
    }

    void gen(Context ctx, MClass cls, MOperation op) {
	boolean isPre = fToken.getText().equals("pre");

	// enter context variable into scope of invariant
	ObjectType ot = TypeFactory.mkObjectType(cls);
	Symtable vars = ctx.varTable();
	vars.enterScope();

	try {
	    // create pseudo-variable "self"
	    vars.add("self", ot, null);
	    ctx.exprContext().push("self", ot);
	    // add special variable `result' in postconditions with result value
	    if ( ! isPre && op.hasResultType() )
		vars.add("result", op.resultType(), null);

	    ctx.setInsidePostCondition(! isPre);
	    Expression expr = fExpr.gen(ctx);
	    ctx.setInsidePostCondition(false);

	    String ppcName = null;
	    if ( fName != null )
		ppcName = fName.getText();
		    
	    MPrePostCondition ppc = 
		ctx.modelFactory().createPrePostCondition(ppcName, op, 
							  isPre, expr);
	    ctx.model().addPrePostCondition(ppc);
	} catch (MInvalidModelException ex) {
	    ctx.reportError(fExpr.getStartToken(), ex);
	} catch (ExpInvalidException ex) {
	    ctx.reportError(fExpr.getStartToken(), ex);
	} catch (SemanticException ex) {
	    ctx.reportError(ex);
	}
	vars.exitScope(); 
	ctx.exprContext().pop();
    }
}
