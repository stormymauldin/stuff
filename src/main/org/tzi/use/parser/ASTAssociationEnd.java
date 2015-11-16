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
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MMultiplicity;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ASTAssociationEnd extends AST {
    private MyToken fName;
    private ASTMultiplicity fMultiplicity;
    private MyToken fRolename;	// optional: may be null!
    private boolean fOrdered;

    public ASTAssociationEnd(MyToken name, ASTMultiplicity mult) {
	fName = name;
	fMultiplicity = mult;
	fOrdered = false;
    }

    public void setRolename(MyToken rolename) {
	fRolename = rolename;
    }

    public void setOrdered() {
	fOrdered = true;
    }

    public MAssociationEnd gen(Context ctx, int kind) throws SemanticException {
	// lookup class at association end in current model
	MClass cls = ctx.model().getClass(fName.getText());
	if ( cls == null )
	    // this also renders the rest of the association useless
	    throw new SemanticException(fName, "Class `" + fName.getText() +
				       "' does not exist in this model.");
	
	MMultiplicity mult = fMultiplicity.gen(ctx);
	if ( fOrdered && ! mult.isCollection() ) {
  	    ctx.reportWarning(fName, "Specifying `ordered' for " +
	      "an association end targeting single objects has no effect.");
	    fOrdered = false;
	}

	MAssociationEnd aend = ctx.modelFactory().createAssociationEnd(cls, 
	       ( fRolename != null ) ? fRolename.getText() : cls.nameAsRolename(), 
	       mult, kind, fOrdered);

	return aend;
    }

    public String toString() {
	return "FIXME";
    }
}
