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
import org.tzi.use.uml.ocl.expr.ExpTupleLiteral;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;
import org.tzi.use.util.StringUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTTupleLiteral extends ASTExpression {
    private List fItems;	// (ASTTupleItem)

    public ASTTupleLiteral(List items) {
	fItems = items;
    }

    public Expression gen(Context ctx) throws SemanticException {
	ExpTupleLiteral.Part[] parts = new ExpTupleLiteral.Part[fItems.size()];
	int i = 0;
  	for (Iterator it = fItems.iterator(); it.hasNext(); i++) {
	    ASTTupleItem ti = (ASTTupleItem) it.next();
	    Expression e = ti.expression().gen(ctx);
	    parts[i] = new ExpTupleLiteral.Part(ti.name().getText(), e);
	}
	return new ExpTupleLiteral(parts);
    }
}
