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
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.mm.MInvalidModelException;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpUndefined;
import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.ocl.expr.VarDeclList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTOperation extends AST {
    private MyToken fName;
    private List fParamList;	// (ASTVariableDeclaration)
    private ASTType fType;	// (optional)
    private ASTExpression fExpr; // (optional)
    private MOperation fOperation; // the operation is generated in two passes
    private List fPrePostClauses;

    public ASTOperation(MyToken name, List paramList, 
			ASTType t, ASTExpression expr) {
	fName = name;
	fParamList = paramList;
	fType = t;
	fExpr = expr;
	fOperation = null;
	fPrePostClauses = new ArrayList();
    }

    void addPrePostClause(ASTPrePostClause ppc) {
	fPrePostClauses.add(ppc);
    }

    public MOperation genSignature(Context ctx) 
	throws SemanticException 
    {
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
	Type resultType = null;
	if ( fType != null )
	    resultType = fType.gen(ctx);
	fOperation = ctx.modelFactory().createOperation(fName.getText(), varDeclList,
							resultType);

	// HACK: set a temporary expression here, otherwise generating
	// the body expression will fail if the body contains a
	// recursive call to this operation, or a forward reference is
	// made to another operation
	if ( fExpr != null ) {
	    try {
		fOperation.setExpression(new ExpUndefined(fOperation.resultType()));
	    } catch (MInvalidModelException ex) {
		throw new RuntimeException("setting temporary expression failed");
	    }
	}
	return fOperation;
    }

    public void genFinal(Context ctx) 
	throws SemanticException 
    {
	// fOperation is null if genSignature exited with an Exception
	if ( fOperation == null )
	    return;

	// enter parameters into scope of expression
	Symtable vars = ctx.varTable();
	vars.enterScope();

	Iterator it = fParamList.iterator();
	while ( it.hasNext() ) {
	    ASTVariableDeclaration astDecl = (ASTVariableDeclaration) it.next();
	    VarDecl decl = astDecl.gen(ctx);
	    vars.add(astDecl.name(), decl.type());
	}

	try {
	    if ( fExpr != null ) {
		Expression expr = fExpr.gen(ctx);
		fOperation.setExpression(expr);
	    }

	    it = fPrePostClauses.iterator();
	    while ( it.hasNext() ) {
		ASTPrePostClause ppc = (ASTPrePostClause) it.next();
		ppc.gen(ctx, ctx.currentClass(), fOperation);
	    }
	} catch (MInvalidModelException ex) {
	    throw new SemanticException(fName, ex);
	} finally {
	    vars.exitScope(); 
	}
    }
}

