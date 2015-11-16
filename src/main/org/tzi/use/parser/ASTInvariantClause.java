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
import org.tzi.use.uml.mm.MClassInvariant;
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

class ASTInvariantClause extends AST {
    MyToken fName;		// optional
    ASTExpression fExpr;

    public ASTInvariantClause(MyToken name, ASTExpression e) {
	fName = name;
	fExpr = e;
    }

    public String toString() {
	return fExpr.toString();
    }


    void gen(Context ctx, MyToken varName, MClass cls) {
	// enter context variable into scope of invariant
	ObjectType ot = TypeFactory.mkObjectType(cls);
	Symtable vars = ctx.varTable();
	vars.enterScope();

	String var = null;
	try {
	    if ( varName != null ) {
		var = varName.getText();
		vars.add(varName, ot);
		ctx.exprContext().push(var, ot);
	    } else {
		// create pseudo-variable "self"
		vars.add("self", ot, null);
		ctx.exprContext().push("self", ot);
	    }

	    Expression expr = fExpr.gen(ctx);
	    String invName = null;
	    if ( fName != null )
		invName = fName.getText();
		    
	    MClassInvariant inv = 
		ctx.modelFactory().createClassInvariant(invName, var, cls, expr);
	    ctx.model().addClassInvariant(inv);
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
