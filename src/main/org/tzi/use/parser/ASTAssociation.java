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
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAggregationKind;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MInvalidModelException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTAssociation extends AST {
    private MyToken fKind;
    private MyToken fName;
    private List fAssociationEnds; // (ASTAssociationEnd)

    public ASTAssociation(MyToken kind, MyToken name) {
	fKind = kind;
	fName = name;
	fAssociationEnds = new ArrayList();
    }

    public void addEnd(ASTAssociationEnd ae) {
	fAssociationEnds.add(ae);
    }

    public void gen(Context ctx, MModel model) 
	throws SemanticException 
    {
	MAssociation assoc = ctx.modelFactory().createAssociation(fName.getText());
	String kindname = fKind.getText();
	int kind = MAggregationKind.NONE;
	if ( kindname.equals("association") )
	    ;
	else if ( kindname.equals("aggregation") )
	    kind = MAggregationKind.AGGREGATION;
	else if ( kindname.equals("composition") )
	    kind = MAggregationKind.COMPOSITION;

	Iterator it = fAssociationEnds.iterator();
	try {
	    while ( it.hasNext() ) {
		ASTAssociationEnd ae = (ASTAssociationEnd) it.next();

		// kind of association determines kind of first
		// association end
		MAssociationEnd aend = ae.gen(ctx, kind);
		assoc.addAssociationEnd(aend);

		// further ends are plain ends
		kind = MAggregationKind.NONE;
	    }
	    model.addAssociation(assoc);
	} catch (MInvalidModelException ex) {
	    throw new SemanticException(fName,
		"In " + MAggregationKind.name(assoc.aggregationKind()) + " `" +
		assoc.name() + "': " + 
		ex.getMessage());
	}
    }

    public String toString() {
	return "FIXME";
    }
}
