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
import org.tzi.use.uml.sys.MCmd;
import org.tzi.use.uml.sys.MCmdSetAttribute;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpAttrOp;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTSetCmd extends ASTCmd {
    private ASTExpression fAttrExpr;
    private ASTExpression fSetExpr;

    public ASTSetCmd(ASTExpression attrExpr, ASTExpression setExpr) {
	fAttrExpr = attrExpr;
	fSetExpr = setExpr;
    }

    public MCmd gen(Context ctx) 
	throws SemanticException
    {
	Expression e = fAttrExpr.gen(ctx);
	if ( ! (e instanceof ExpAttrOp) )
	    throw new SemanticException(fAttrExpr.getStartToken(), 
		"Expression does not give a reference to an attribute.");
	ExpAttrOp attrExpr = (ExpAttrOp) e;
	Expression setExpr = fSetExpr.gen(ctx);
	// check type conformance of assignment
	if ( ! setExpr.type().isSubtypeOf(attrExpr.type()) )
	    throw new SemanticException(fSetExpr.getStartToken(), 
		"Type mismatch in assignment expression. " +
		"Expected type `" + attrExpr.type() + "', found `" +
		setExpr.type() + "'.");    

	return new MCmdSetAttribute(ctx.systemState(),
				    attrExpr, 
				    setExpr);
    }
}
