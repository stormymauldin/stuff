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
import org.tzi.use.uml.ocl.expr.VarDeclList;
import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.ocl.type.Type;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTPrePost extends AST {
    private MyToken fClassName;
    private MyToken fOpName;
    private List fParamList;	// (ASTVariableDeclaration)
    private ASTType fResultType; // optional
    private List fPrePostClauses;

    public ASTPrePost(MyToken classname, MyToken opname, 
		      List paramList, ASTType resultType) {
	fClassName = classname;
	fOpName = opname;
	fParamList = paramList;
	fResultType = resultType;
	fPrePostClauses = new ArrayList();
    }

    void addPrePostClause(ASTPrePostClause ppc) {
	fPrePostClauses.add(ppc);
    }

    void gen(Context ctx) throws SemanticException {
	// find class
	MClass cls = ctx.model().getClass(fClassName.getText());
	if ( cls == null )
	    throw new SemanticException(fClassName, 
					"Undefined class `" + fClassName.getText() + 
					"'.");
	// find operation in class
	MOperation op = cls.operation(fOpName.getText(), false);
	if ( op == null )
	    throw new SemanticException(fOpName, 
					"Class `" + fClassName.getText() + 
					"' has no operation `" + fOpName.getText() + 
					"'.");


	// map params to VarDeclList
	VarDeclList varDeclList = new VarDeclList(false);
	Iterator it = fParamList.iterator();
	while ( it.hasNext() ) {
	    ASTVariableDeclaration astDecl = (ASTVariableDeclaration) it.next();
	    VarDecl decl = astDecl.gen(ctx);
	    try {
		varDeclList.add(decl);
	    } catch (IllegalArgumentException ex) {
		throw new SemanticException(astDecl.name(), "Redefinition of `" +
					    decl.name() + "'.");
	    }
	}

	// check for identical signature
	if ( ! op.paramList().equals(varDeclList) )
	    throw new SemanticException(fOpName, "This signature of operation `" + 
			fOpName.getText() +
			"' does not match its previous declaration in class `" +
			fClassName.getText() + "'.");

	Type resultType = null;
	if ( fResultType != null ) {
	    resultType = fResultType.gen(ctx);
	    if ( ! op.hasResultType() )
		throw new SemanticException(fResultType.getStartToken(), 
		    "Operation `" + 
		    fOpName.getText() +
		    "' has no result type in its previous declaration in class `" +
		    fClassName.getText() + "'.");

	    if ( ! resultType.equals(op.resultType()) )
		throw new SemanticException(fResultType.getStartToken(), 
		    "Expected result type `" + op.resultType() +
		    "', found `" + resultType + "'.");
	} else {
	    if ( op.hasResultType() )
		throw new SemanticException(fOpName, 
		    "Expected result type `" + op.resultType() + "'.");
	}

	// enter parameters into scope of expression
	Symtable vars = ctx.varTable();
	vars.enterScope();
	it = fParamList.iterator();
	while ( it.hasNext() ) {
	    ASTVariableDeclaration astDecl = (ASTVariableDeclaration) it.next();
	    VarDecl decl = astDecl.gen(ctx);
	    vars.add(astDecl.name(), decl.type());
	}

	it = fPrePostClauses.iterator();
	while ( it.hasNext() ) {
	    ASTPrePostClause ppc = (ASTPrePostClause) it.next();
	    ppc.gen(ctx, cls, op);
	}

	vars.exitScope(); 
    }
}
