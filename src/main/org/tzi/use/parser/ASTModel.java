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
import org.tzi.use.uml.ocl.type.EnumType;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MClass;
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

class ASTModel extends AST {
    private MyToken fName;
    private List fEnumTypeDefs;	// (ASTEnumTypeDefinition)
    private List fClasses;	// (ASTClass)
    private List fAssociations;	// (ASTAssociation)
    private List fConstraints;	// (ASTConstraintDefinition)
    private List fPrePosts;	// (ASTPrePost)

    public ASTModel(MyToken name) {
	fName = name;
	fEnumTypeDefs = new ArrayList();
	fClasses = new ArrayList();
	fAssociations = new ArrayList();
	fConstraints = new ArrayList();
	fPrePosts = new ArrayList();
    }

    public void addEnumTypeDef(ASTEnumTypeDefinition etd) {
	fEnumTypeDefs.add(etd);
    }

    public void addClass(ASTClass cls) {
	fClasses.add(cls);
    }

    public void addAssociation(ASTAssociation assoc) {
	fAssociations.add(assoc);
    }

    public void addConstraint(ASTConstraintDefinition cons) {
	fConstraints.add(cons);
    }

    public void addPrePost(ASTPrePost ppc) {
	fPrePosts.add(ppc);
    }

    public MModel gen(Context ctx) {
	MModel model = ctx.modelFactory().createModel(fName.getText());
	model.setFilename(ctx.filename());
	ctx.setModel(model);

	// (1a) add user-defined types to model
	Iterator it = fEnumTypeDefs.iterator();
	while ( it.hasNext() ) {
	    ASTEnumTypeDefinition e = (ASTEnumTypeDefinition) it.next();
	    EnumType enum;
	    try {
		enum = e.gen(ctx);
		model.addEnumType(enum);
	    } catch (SemanticException ex) {
		ctx.reportError(ex);
	    } catch (MInvalidModelException ex) {
		ctx.reportError(fName, ex);
	    }
	}

	// (1b) add empty classes to model
	it = fClasses.iterator();
	while ( it.hasNext() ) {
	    ASTClass c = (ASTClass) it.next();
	    try {
		MClass cls = c.genEmptyClass(ctx);
		model.addClass(cls);
	    } catch (SemanticException ex) {
		ctx.reportError(ex);
	    } catch (MInvalidModelException ex) {
		ctx.reportError(fName, ex);
	    }
	}

	// (2) add attributes and set generalization
	// relationships. The names of all classes are known at this
	// point
	it = fClasses.iterator();
	while ( it.hasNext() ) {
	    ASTClass c = (ASTClass) it.next();
	    c.genAttributesOperationSignaturesAndGenSpec(ctx);
	}

	// (3) add associations. Classes are known and can be
	// referenced by role names.
	it = fAssociations.iterator();
	while ( it.hasNext() ) {
	    ASTAssociation a = (ASTAssociation) it.next();
	    try {
		a.gen(ctx, model);
	    } catch (SemanticException ex) {
		ctx.reportError(ex);
	    }
	}

	// (4) generate constraints. All class interfaces are known
	// and association features are available for expressions.
	it = fClasses.iterator();
	while ( it.hasNext() ) {
	    ASTClass c = (ASTClass) it.next();
	    c.genConstraintsAndOperationBodies(ctx);
	}

	// (5) generate global constraints. All class interfaces are
	// known and association features are available for
	// expressions.
	it = fConstraints.iterator();
	while ( it.hasNext() ) {
	    ASTConstraintDefinition c = (ASTConstraintDefinition) it.next();
	    c.gen(ctx);
	}

	// (5b) generate pre-/postconditions.
	it = fPrePosts.iterator();
	while ( it.hasNext() ) {
	    ASTPrePost ppc = (ASTPrePost) it.next();
	    try {
		ppc.gen(ctx);
	    } catch (SemanticException ex) {
		ctx.reportError(ex);
	    }
	}

	return model;
    }

    public String toString() {
	return "(" + fName + ")";
    }
}
