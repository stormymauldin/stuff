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
import org.tzi.use.config.Options;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.CollectionType;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpAsType;
import org.tzi.use.uml.ocl.expr.ExpIsKindOf;
import org.tzi.use.uml.ocl.expr.ExpIsTypeOf;
import org.tzi.use.uml.ocl.expr.ExpVariable;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTTypeArgExpression extends ASTExpression {
    private MyToken fOpToken;
    private ASTExpression fSourceExpr; // may be null
    private ASTType fTargetType;
    private boolean fFollowsArrow;

    public ASTTypeArgExpression(MyToken opToken, 
				ASTExpression source, 
				ASTType targetType,
				boolean followsArrow) {
	fOpToken = opToken;
	fSourceExpr = source;
	fTargetType = targetType;
	fFollowsArrow = followsArrow;
    }

    public Expression gen(Context ctx) throws SemanticException {
	Expression res;
	Expression expr;
	Type t = fTargetType.gen(ctx);

	// check for empty source: do we have a context expression that
	// is implicitly assumed to be the source expression?
	if ( fSourceExpr != null )
	    expr = fSourceExpr.gen(ctx);
	else {
	    ExprContext ec = ctx.exprContext();
	    if ( ! ec.isEmpty() ) {
		// construct source expression
		ExprContext.Entry e = ec.peek();
		expr = new ExpVariable(e.fName, e.fType);
	    } else
		throw new SemanticException(fOpToken, "Need an object to apply `" +
					    fOpToken.getText() + "'.");
	}
	
	// this type expression cannot be applied to collections
	// (Collection is not a subtype of OclAny where this
	// expression is defined). However, if we find a source
	// expression of type Collection(T), this might be a shorthand
	// for the collect operation, e.g. `c.oclIsKindOf(Employee)'
	// is a valid shorthand for `c->collect(e |
	// e.oclIsKindOf(Employee))'

	if ( ! expr.type().isCollection() ) {
	    res = genExpr(expr, t);
	    if ( ! expr.type().isSubtypeOf(t) && ! t.isSubtypeOf(expr.type()) )
		ctx.reportWarning(fTargetType.getStartToken(), 
		  "Expression is always false since the expression's type `" +
		  expr.type() + 
		  "' is neither a subtype nor a supertype of the target type `" +
		  t + "'.");
	} else if ( ! fFollowsArrow ) {
	    if ( Options.disableCollectShorthand )
		throw new SemanticException(fOpToken, MSG_DISABLE_COLLECT_SHORTHAND);
	    
	    CollectionType cType = (CollectionType ) expr.type();
	    Type elemType = cType.elemType();
	    if ( elemType.isCollection() ) // nested collection?
		throw new SemanticException(fOpToken, "Operation `" +
					    fOpToken.getText() + 
					    "' cannot be applied to collection.");
	    
	    // transform c.oclIsKindOf(t) into c->collect($e | $e.oclIsKindOf(t))
	    ExpVariable eVar = new ExpVariable("$e", elemType);
	    Expression collectBody = genExpr(eVar, t);
	    res = genImplicitCollect(expr, collectBody, elemType);
	} else {
	    throw new SemanticException(fOpToken, "Operation `" +
					fOpToken.getText() + 
					"' cannot be applied to collection.");
	}

	return res;
    }

    private Expression genExpr(Expression expr, Type t) 
	throws SemanticException 
    {
	try {
	    switch ( fOpToken.getType() ) {
	    case GUSETokenTypes.LITERAL_oclAsType:
		return new ExpAsType(expr, t);
	    case GUSETokenTypes.LITERAL_oclIsKindOf:
		return new ExpIsKindOf(expr, t);
	    case GUSETokenTypes.LITERAL_oclIsTypeOf:
		return new ExpIsTypeOf(expr, t);
	    default:
		throw new RuntimeException("Unexpected token type: " + fOpToken);
	    }
	} catch (ExpInvalidException ex) {
	    throw new SemanticException(fTargetType.getStartToken(), ex.getMessage());
	}
    }
}
