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
import org.tzi.use.uml.ocl.expr.VarInitializer;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;
import org.tzi.use.uml.ocl.type.Type;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTVariableInitialization extends AST {
    private MyToken fName;
    private ASTType fType;
    private ASTExpression fExpr;

    public ASTVariableInitialization(MyToken name, ASTType type, 
				     ASTExpression expr) {
	fName = name;
	fType = type;
	fExpr = expr;
    }

    public VarInitializer gen(Context ctx) throws SemanticException {
	Type type = fType.gen(ctx);
	try {
	    return new VarInitializer(fName.getText(), type,
				      fExpr.gen(ctx));
	} catch (ExpInvalidException ex) {
	    throw new SemanticException(fName, ex);
	}
    }

    public MyToken nameToken() {
	return fName;
    }

    public String toString() {
	return "(" + fName + " " + fType + " " + fExpr + ")";
    }
}
