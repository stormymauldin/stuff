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
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.ObjectType;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTConstraintDefinition extends AST {
    private MyToken fVarName;	// optional
    private ASTType fType;
    private ArrayList fInvariantClauses; // (ASTInvariantClause)

    public ASTConstraintDefinition() {
	fInvariantClauses = new ArrayList();
    }

    void addInvariantClause(ASTInvariantClause inv) {
	fInvariantClauses.add(inv);
    }

    void setVarName(MyToken tok) {
	fVarName = tok;
    }

    void setType(ASTType t) {
	fType = t;
    }

    void gen(Context ctx) {
	try {
	    Type t = fType.gen(ctx);
	    if ( ! t.isObjectType() )
		throw new SemanticException(fType.getStartToken(), 
					   "Expected an object type, found `" +
					   t + "'");
	    MClass cls = ((ObjectType) t).cls();
	    ctx.setCurrentClass(cls);
	    Iterator it = fInvariantClauses.iterator();
	    while ( it.hasNext() ) {
		ASTInvariantClause astInv = (ASTInvariantClause) it.next();
		astInv.gen(ctx, fVarName, cls);
	    }
	} catch (SemanticException ex) {
	    ctx.reportError(ex);
	} finally {
	    ctx.setCurrentClass(null);
	}
    }
}
