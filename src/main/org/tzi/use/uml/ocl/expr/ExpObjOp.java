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

package org.tzi.use.uml.ocl.expr;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.UndefinedValue;
import org.tzi.use.uml.ocl.value.ObjectValue;
import org.tzi.use.util.StringUtil;

/**
 * An operation defined by a class.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class ExpObjOp extends Expression {
    private MOperation fOp;
    private Expression[] fArgs;	// the arguments, first one is "receiver" object
    
    public ExpObjOp(MOperation op, Expression[] args) 
	 throws ExpInvalidException
    {
	super(op.resultType());
	fOp = op;
	fArgs = args;
	if ( ! args[0].type().isObjectType() )
	    throw new ExpInvalidException(
		   "Target expression of object operation must have " +
		   "object type, found `" + args[0].type() + "'.");

	// check for matching arguments
	VarDeclList params = fOp.paramList();
	if ( params.size() != (args.length - 1) )
	    throw new ExpInvalidException(
		   "Number of arguments does not match declaration of operation `" +
		   fOp.name() + "'. Expected " + params.size() + " argument(s), found " +
		   (args.length - 1) + ".");

	for (int i = 1; i < args.length; i++)
	    if ( ! args[i].type().isSubtypeOf(params.varDecl(i - 1).type()) )
		throw new ExpInvalidException(
		      "Type mismatch in argument `" + params.varDecl(i - 1).name() + 
		      "'. Expected type `" + params.varDecl(i - 1).type() + 
		      "', found `" + args[i].type() + "'.");
    }

    /**
     * Evaluates expression and returns result value.
     */
    public Value eval(EvalContext ctx) {
	ctx.enter(this);
	Value res = new UndefinedValue(type());
        Value val = fArgs[0].eval(ctx);
	// if we don't have an object we can't call its operation
	if ( ! val.isUndefined() ) {
	    // get object
	    ObjectValue objVal = (ObjectValue) val;
	    MObject obj = objVal.value();
	    MObjectState objState = isPre() ? 
		obj.state(ctx.preState()) : obj.state(ctx.postState());
	    if ( objState != null ) {
		// the object's type may be a subtype of the declared
		// type. The operation may be redefined in this
		// subclass. We have to get the possibly redefined
		// operation.
		MClass cls = obj.cls();
		MOperation op = cls.operation(fOp.name(), true);

		// evaluate arguments
		Value [] argValues = new Value[fArgs.length - 1];
		for (int i = 0; i < fArgs.length - 1; i++)
		    argValues[i] = fArgs[i + 1].eval(ctx);

		// bind the argument values to the operation's
		// parameters
		VarDeclList params = op.paramList();
		for (int i = 0; i < fArgs.length - 1; i++) {
		    VarDecl decl = params.varDecl(i);
		    ctx.pushVarBinding(decl.name(), argValues[i]);
		}

		// the operation's expression must be evaluated in context
		// of the target object. The "self" variable is bound to
		// the receiver object.
		ctx.pushVarBinding("self", objVal);
		Expression opExpr = op.expression();
		res = opExpr.eval(ctx);
		ctx.popVarBinding();
		for (int i = 1; i < fArgs.length; i++) 
		    ctx.popVarBinding();
	    }
	}
	ctx.exit(this, res);
	return res;
    }

    public String toString() {
	return fArgs[0] + "." + fOp.name() + atPre() +
	    "(" + StringUtil.fmtSeq(fArgs, 1, ", ") + ")";
    }
}

