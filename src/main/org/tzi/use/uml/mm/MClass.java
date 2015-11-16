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

package org.tzi.use.uml.mm;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;

/**
 * MClass instances represent classes in a model.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class MClass extends MModelElementImpl {
    private boolean fIsAbstract; // abstract class?
    private Map fAttributes;	// (String attrname -> MAttribute)
    private Map fOperations;	// (String opname -> MOperation)
    private MModel fModel;	// Owner of this class

    // other classes reachable by associations 
    private Map fAssociationEnds; // (String name -> MAssociationEnd)
    //    private Set fAssociations;	// associations this class is participating in

    MClass(String name, boolean isAbstract) {
	super(name);
	fIsAbstract = isAbstract;
	fAttributes = new TreeMap();
	fOperations = new TreeMap();
	fAssociationEnds = new HashMap();
	//	fAssociations = new HashSet();
    }
    
    /**
     * Returns true if the class is marked abstract.
     */
    public boolean isAbstract() {
	return fIsAbstract;
    }

    /**
     * Returns the name of this class with lowercase first letter.
     */
    public String nameAsRolename() {
	return Character.toLowerCase(name().charAt(0)) + name().substring(1);
    }

    /**
     * Returns the model owning this class.
     */
    public MModel model() {
	return fModel;
    }

    /**
     * Sets the model owning this class. This method must be called by
     * MModel.addClass().  
     *
     * @see MModel#addClass
     */
    public void setModel(MModel model) {
	fModel = model;
    }

    /** 
     * Adds an attribute. The attribute must have a unique name within
     * the class and must not conflict with any attribute with the
     * same name defined in a superclass or subclass or with a
     * rolename of an association this class participates in.
     *
     * @exception MInvalidModelException the class already contains an
     *            attribute with the same name or a name clash
     *            occured.  
     */
    public void addAttribute(MAttribute attr) throws MInvalidModelException {
	// check if there already exists an attribute with the
	// specified name in this class
	String attrName = attr.name();
	if ( fAttributes.containsKey(attrName) )
	    throw new MInvalidModelException("Class `" + name() + 
	     "' already contains an attribute named `" + attrName + "'.");

	// check if attribute of same name is already defined in a
	// superclass
	if ( attribute(attrName, true) != null )
	    throw new MInvalidModelException("Attribute with name `" + 
		     attrName + 
		     "' already defined in a superclass.");
	
	// check if attribute of same name is defined in a subclass
	Iterator it = allChildren().iterator();
	while ( it.hasNext() ) {
	    MClass cls = (MClass) it.next();
	    if ( cls.attribute(attrName, false) != null )
		 throw new MInvalidModelException("Attribute `" + 
			  attrName + "' conflicts with existing attribute " +
			  "in class `" + cls.name() + "'.");
	}
	
	// add attribute
	fAttributes.put(attr.name(), attr);
	attr.setOwner(this);
    }

    /**
     * Returns the set of attributes defined for this class. Inherited
     * attributes are not included.  
     *
     * @return Set(MAttribute)
     */
    public Set attributes() {
	Set res = new HashSet(fAttributes.size());
	res.addAll(fAttributes.values());
	return res;
    }

    /**
     * Returns the set of all attributes (including inherited ones)
     * defined for this class.
     *
     * @return Set(MAttribute) 
     */
    public Set allAttributes() {
	Set result = new HashSet(fAttributes.size());

	// start with local attributes
	result.addAll(attributes());

	// add attributes from all superclasses
	Iterator it = allParents().iterator();
	while ( it.hasNext() ) {
	    MClass cls = (MClass) it.next();
	    result.addAll(cls.attributes());
	}
	return result;
    }


    /**
     * Returns the specified attribute. Attributes are also looked up
     * in superclasses if <code>searchInherited</code> is true.
     *
     * @return null if attribute does not exist.  
     */
    public MAttribute attribute(String name, boolean searchInherited) {
	MAttribute a = (MAttribute) fAttributes.get(name);
	if ( a == null && searchInherited ) {
	    Iterator it = allParents().iterator();
	    while ( it.hasNext() ) {
		MClass cls = (MClass) it.next();
		a = (MAttribute) cls.attribute(name, false);
		if ( a != null )
		    break;
	    }
	}
	return a;
    }


    /** 
     * Adds an operation. The operation name must be unique among all
     * operations of this class. Operations in superclasses may be
     * redefined if the signature matches.
     *
     * @exception MInvalidModelException the operation is not unique
     *            or is not a valid redefinition.
     */
    public void addOperation(MOperation op) throws MInvalidModelException {
	// check if there already exists an operation with the same
	// name in this class
	String opname = op.name();
	if ( fOperations.containsKey(opname) )
	    throw new MInvalidModelException("Class `" + name() + 
	     "' already contains an operation named `" + opname + "'.");

	// check if operation with same signature is already defined
	// in a superclass. A redefinition is only allowed with same
	// number and types of arguments.
	MOperation superOp = operation(opname, true);
	if ( superOp != null ) {
	    if ( ! superOp.equals(op) )
		throw new MInvalidModelException("Redefinition of operation `" + superOp +
						 "' requires same number and type of arguments.");
	}
	
	// check if operation with same signature is already defined
	// in a subclass. A redefinition is only allowed with same
	// number and types of arguments.
	Iterator it = allChildren().iterator();
	while ( it.hasNext() ) {
	    MClass cls = (MClass) it.next();
	    MOperation subOp = (MOperation) cls.operation(opname, false);
	    if ( subOp != null )
		if ( ! subOp.equals(op) )
		    throw new MInvalidModelException("Operation `" + op +
						     "' does not match its redefinition in class `" + 
						     cls + "'.");
	}
	
	fOperations.put(op.name(), op);
        op.setClass(this);
    }

    /**
     * Returns all operations defined for this class. Inherited
     * operations are not included.
     */
    public Collection operations() {
	return fOperations.values();
    }

    /**
     * Gets an operation by name. Operations are also looked up in
     * superclasses if <code>searchInherited</code> is true. This
     * method walks up the generalization hierarchy and selects the
     * first matching operation. Thus, if an operation is redefined,
     * this method returns the most specific one.
     *
     * @return null if operation does not exist. 
     */
    public MOperation operation(String name, boolean searchInherited) {
	MOperation op = (MOperation) fOperations.get(name);
	if ( op == null && searchInherited ) {
	    Iterator it = allParents().iterator();
	    while ( it.hasNext() ) {
		MClass cls = (MClass) it.next();
		op = (MOperation) cls.operation(name, false);
		if ( op != null )
		    break;
	    }
	}
	return op;
    }


    /**
     * Returns the association end that can be reached by the
     * OCL expression <code>self.rolename</code>.
     *
     * @return null if no such association end exists.
     */
    public MAssociationEnd navigableEnd(String rolename) {
	MAssociationEnd aend =
	    (MAssociationEnd) fAssociationEnds.get(rolename);
	if ( aend == null ) {
	    // recursively search superclasses
	    Iterator it = parents().iterator();
	    while ( it.hasNext() ) {
		MClass superclass = (MClass) it.next();
		aend = superclass.navigableEnd(rolename);
		if ( aend != null )
		    break;
	    }
	}
	return aend;
    }

    /**
     * Returns a map of all association ends that can be reached from
     * this class by navigation.
     *
     * @return Map(String, MAssociationEnd) 
     */
    Map navigableEnds() {
	Map res = new TreeMap();
	res.putAll(fAssociationEnds);
	// recursively add association ends in superclasses
	Iterator it = parents().iterator();
	while ( it.hasNext() ) {
	    MClass superclass = (MClass) it.next();
	    res.putAll(superclass.navigableEnds());
	}
	return res;
    }

    /**
     * Registers all association ends as navigable from this
     * class. This should be called by a MModel implementation when an
     * association is added to the model.
     *
     * @param associationEnds List(MAssociationEnd) 
     * @see MModel#addAssociation
     */
    void registerNavigableEnds(List associationEnds) {
	// System.out.println("registerNavigableEnds: " + name());
	Iterator it = associationEnds.iterator();
	while ( it.hasNext() ) {
	    MAssociationEnd aend = (MAssociationEnd) it.next();
	    // System.out.println("aend: " + aend.name());
	    fAssociationEnds.put(aend.name(), aend);
	}
    }


    /**
     * Returns the set of all direct parent classes (without this
     * class).
     *
     * @return Set(MClass) 
     */
    public Set parents() {
	return fModel.generalizationGraph().targetNodeSet(this);
    }

    /**
     * Returns the set of all parent classes (without this
     * class). This is the transitive closure of the generalization
     * relation.
     *
     * @return Set(MClass) 
     */
    public Set allParents() {
	return fModel.generalizationGraph().targetNodeClosureSet(this);
    }

    /**
     * Returns the set of all child classes (without this class). This
     * is the transitive closure of the generalization relation.
     *
     * @return Set(MClass) 
     */
    public Set allChildren() {
	return fModel.generalizationGraph().sourceNodeClosureSet(this);
    }

    /**
     * Returns the set of associations this class directly
     * participates in.
     *
     * @return Set(MAssociation).  
     */
    public Set associations() {
	Set res = new HashSet();
	Iterator aendIter = fAssociationEnds.values().iterator();
	while ( aendIter.hasNext() ) {
	    MAssociationEnd aend = (MAssociationEnd) aendIter.next();
	    res.add(aend.association());
	}
	return res;
    }

    /**
     * Returns the set of associations this class and all of its
     * parents participate in.
     *
     * @return Set(MAssociation).  
     */
    public Set allAssociations() {
	Set res = new HashSet();
	res.addAll(associations());
	Iterator parentIter = allParents().iterator();
	while ( parentIter.hasNext() ) {
	    MClass cls = (MClass) parentIter.next();
	    res.addAll(cls.associations());
	}
	return res;
    }

    /**
     * Returns true if this class is a child of
     * <code>otherClass</code> or equal to it.  
     */
    public boolean isSubClassOf(MClass otherClass) {
	return this.equals(otherClass) || allParents().contains(otherClass);
    }

    /**
     * Process this element with visitor.
     */
    public void processWithVisitor(MMVisitor v) {
	v.visitClass(this);
    }
}
