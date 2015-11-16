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
import org.tzi.use.uml.ocl.expr.ExpStdOp;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;
import org.tzi.use.uml.ocl.expr.ExpNavigation;
import org.tzi.use.uml.ocl.expr.ExpCollect;
import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.CollectionType;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MClass;
import java.util.List;
import java.util.Iterator;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

abstract class ASTExpression extends AST {
    protected static final String MSG_DISABLE_COLLECT_SHORTHAND = 
	"The OCL shorthand notation for collect has been disabled. " +
	"Try `use -h' for help on enabling it.";

    // first token of expression useful for error reporting
    private MyToken fStartToken; 

    public void setStartToken(MyToken pos) {
	fStartToken = pos;
    }

    public MyToken getStartToken() {
	return fStartToken;
    }


    public abstract Expression gen(Context ctx) throws SemanticException;


    /**
     * Generates a predefined standard operation expression.
     */
    protected Expression genStdOperation(Context ctx,
					 MyToken token, 
					 String opname,
					 Expression[] args) 
	throws SemanticException
    {
	Expression res = null;
	try {
	    // lookup operation
	    res = ExpStdOp.create(opname, args);
	} catch (ExpInvalidException ex) {
	    throw new SemanticException(token, ex);
	}
	return res;
    }

    /**
     * Generates a predefined standard operation expression.
     */
    protected Expression genStdOperation(Context ctx,
					 MyToken token, 
					 String opname,
					 ASTExpression[] args) 
	throws SemanticException
    {
	Expression res;
	Expression[] expargs = new Expression[args.length];
	for (int i = 0; i < args.length; i++)
	    expargs[i] = args[i].gen(ctx);
	return genStdOperation(ctx, token, opname, expargs);
    }

    protected Expression genStdOperation(Context ctx,
					 MyToken token, 
					 String opname,
					 List args) 
	throws SemanticException
    {
	ASTExpression[] exparr = new ASTExpression[args.size()];
	System.arraycopy(args.toArray(), 0, exparr, 0, args.size());
	return genStdOperation(ctx, token, opname, exparr);
    }

    protected Expression genNavigation(MyToken rolenameToken,
				       MClass srcClass,
				       Expression srcExpr,
				       MAssociationEnd dstEnd) 
	throws SemanticException
    {
	Expression res = null;

	// find source end
	MAssociation assoc = dstEnd.association();
	Iterator it = assoc.associationEnds().iterator();
	MAssociationEnd srcEnd = null;
	while ( it.hasNext() ) {
	    MAssociationEnd aend = (MAssociationEnd) it.next();
	    if ( ! aend.equals(dstEnd) )
		if ( srcClass.isSubClassOf(aend.cls()) ) {
		    if ( srcEnd != null )
			// if already set, the navigation path is not unique
			throw new SemanticException(rolenameToken,
			    "The navigation path is ambiguous. " +
			    "A qualification of the source association is required.");
		    srcEnd = aend;
		}
	}
	if ( srcEnd == null )
	    throw new SemanticException(rolenameToken,
		"Identifier `" + rolenameToken.getText() +
		"' is not a role name.");
	try { 
	    res = new ExpNavigation(srcExpr, srcEnd, dstEnd);
	} catch (ExpInvalidException ex) {
	    throw new SemanticException(rolenameToken, ex);
	}
	return res;
    }

    /**
     * Transforms an expression <code>$e.foo</code> into an expression
     * <code>c->collect($e | $e.foo)</code> or <code>c->collect($e |
     * $e.foo)->flatten</code> if the result of the collect is a
     * nested collection.
     *
     * @param srcExpr the source collection
     * @param expr the argument expression for collect
     * @param elemType type of elements of the source collection 
     */
    protected Expression genImplicitCollect(Expression srcExpr, 
					    Expression expr, 
					    Type elemType) 
    {
	Expression res = null;
	try {
	    ExpCollect eCollect = 
		new ExpCollect(new VarDecl("$e", elemType), srcExpr, expr);
	    res = eCollect;
	    
	    // is result a nested collection?
	    if ( res.type().isCollection() ) {
		CollectionType t = (CollectionType) res.type();
		if ( t.elemType().isCollection() ) {
		    // add flatten
		    Expression[] args = { res };
		    res = ExpStdOp.create("flatten", args);
		}
	    }
	} catch (ExpInvalidException ex) {
	    throw new RuntimeException("genImplicitCollect failed: " + ex.getMessage());
	}
	return res;
    }
}
