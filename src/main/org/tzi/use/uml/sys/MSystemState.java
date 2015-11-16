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

package org.tzi.use.uml.sys;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MClassInvariant;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.util.Log;
import org.tzi.use.util.StringUtil;
import org.tzi.use.util.MultiMap;
import org.tzi.use.util.HashMultiMap;
import org.tzi.use.util.Bag;
import org.tzi.use.util.HashBag;
import org.tzi.use.util.Queue;
import org.tzi.use.config.Options;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


/**
 * A system state represents a valid instance of a model. It contains
 * a set of objects and links connecting objects. Methods allow
 * manipulation and querying of objects and links.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public final class MSystemState {
    private String fName;
    private MSystem fSystem;

    /**
     * The set of all object states.
     */
    private Map fObjectStates;	// (MObject -> MObjectState)
    
    /**
     * The set of objects partitioned by class. Must be kept in sync
     * with fObjectStates.
     */
    private MultiMap fClassObjects; // (MClass -> list of MObject)

    /**
     * The set of all links partitioned by association.
     */
    private Map fLinkSets;	// (MAssociation -> MLinkSet)


    /**
     * Creates a new system state with no objects and links.
     */
    MSystemState(String name, MSystem system) {
	fName = name;
	fSystem = system;
	fObjectStates = new HashMap();
	fClassObjects = new HashMultiMap();
	fLinkSets = new HashMap();

	// create empty link sets
	Iterator it = fSystem.model().associations().iterator();
	while ( it.hasNext() ) {
	    MAssociation assoc = (MAssociation) it.next();
	    MLinkSet linkSet = new MLinkSet(assoc);
	    fLinkSets.put(assoc, linkSet);
	}
    }

    /**
     * Creates a copy of an existing system state.
     */
    MSystemState(String name, MSystemState x) {
	fName = name;
	fSystem = x.fSystem;

	// deep copy of object states
	fObjectStates = new HashMap();
	Iterator it = x.fObjectStates.entrySet().iterator();
	while ( it.hasNext() ) {
	    Map.Entry e = (Map.Entry) it.next();
	    fObjectStates.put(e.getKey(), new MObjectState((MObjectState) e.getValue()));
	}

	fClassObjects = new HashMultiMap();
	fClassObjects.putAll(x.fClassObjects);

	fLinkSets = new HashMap();
	it = x.fLinkSets.entrySet().iterator();
	while ( it.hasNext() ) {
	    Map.Entry e = (Map.Entry) it.next();
	    fLinkSets.put(e.getKey(), new MLinkSet((MLinkSet) e.getValue()));
	}
    }

    /**
     * Returns the name of this state. The name is unique for
     * different states.
     */
    public String name() {
	return fName;
    }

    /**
     * Returns the owning system of this state.
     */
    public MSystem system() {
	return fSystem;
    }

    /**
     * Returns the set of all objects in this state.
     *
     * @return Set(MObject)
     */
    public Set allObjects() {
	return fObjectStates.keySet();
    }

    /**
     * Returns the set of objects of class <code>cls</code> currently
     * existing in this state.
     *
     * @return Set(MObject) 
     */
    public Set objectsOfClass(MClass cls) {
	Set res = new HashSet();
	res.addAll(fClassObjects.get(cls));
	return res;
    }

    /*
     * Returns the set of objects of class <code>cls</code> and all of
     * its subclasses.
     *
     * @return Set(MObject) 
     */
    public Set objectsOfClassAndSubClasses(MClass cls) {
	Set res = new HashSet();
	Set children = cls.allChildren();
	children.add(cls);

	Iterator it = children.iterator();
	while ( it.hasNext() ) {
	    MClass c = (MClass) it.next();
	    res.addAll(objectsOfClass(c));
	}
	return res;
    }
 
    /**
     * Returns the object with the specified name.
     *
     * @return null if no object with the specified name exists.
     */
    public MObject objectByName(String name) {
	// this is a slow linear search over all objects
	Set allObjects = allObjects();
	Iterator it = allObjects.iterator();
	while ( it.hasNext() ) {
	    MObject obj = (MObject) it.next();
	    if ( obj.name().equals(name) )
		return obj;
	}
	return null;
    }

    /**
     * Returns the set of all links in this state.
     *
     * @return Set(MLink)
     */
    public Set allLinks() {
	Set res = new HashSet();
	Iterator it = fLinkSets.values().iterator();
	while ( it.hasNext() ) {
	    MLinkSet ls = (MLinkSet) it.next();
	    res.addAll(ls.links());
	}
	return res;
    }

    /**
     * Returns the set of links of the specified association in this
     * state.  
     */
    public MLinkSet linksOfAssociation(MAssociation assoc) {
	return (MLinkSet) fLinkSets.get(assoc);
    }

    /**
     * Returns true if there is a link of the specified association
     * connecting the given set of objects.
     */
    public boolean hasLinkBetweenObjects(MAssociation assoc, Set objects) {
	MLinkSet linkSet = (MLinkSet) fLinkSets.get(assoc);
	return linkSet.hasLinkBetweenObjects(objects);
    }

    /**
     * Creates and adds a new object to the state. The name of the
     * object may be null in which case a unique name is automatically
     * generated.
     *
     * @return the created object.
     */
    public MObject createObject(MClass cls, String name) throws MSystemException {
	// create new object and initial state
	MObject obj = fSystem.createObject(cls, name);
	MObjectState objState = new MObjectState(obj);
	fObjectStates.put(obj, objState);
	fClassObjects.put(cls, obj);
	return obj;
    }

    /**
     * Restores a destroyed object.
     */
    public void restoreObject(MObjectState objState) throws MSystemException {
	// create new object and initial state
	MObject obj = objState.object();
	fObjectStates.put(obj, objState);
	fClassObjects.put(obj.cls(), obj);
	fSystem.addObject(obj);
    }

    /**
     * Deletes an object from the state. All links connected to the
     * object are removed.  
     *
     * @return Set(MLink) the set of removed links
     */
    public Set deleteObject(MObject obj) {
	Set res = new HashSet();
	MClass objClass = obj.cls();

	// get associations this object might be participating in
	Set assocSet = objClass.allAssociations();
	Iterator assocIter = assocSet.iterator();
	while ( assocIter.hasNext() ) {
	    MAssociation assoc = (MAssociation) assocIter.next();
	    MLinkSet linkSet = (MLinkSet) fLinkSets.get(assoc);

	    // check all association ends the objects' class is
	    // connected to
	    Iterator aendIter = assoc.associationEnds().iterator();
	    while ( aendIter.hasNext() ) {
		MAssociationEnd aend = (MAssociationEnd) aendIter.next();
		if ( objClass.isSubClassOf(aend.cls()) ) {
		    Set removedLinks = linkSet.removeAll(aend, obj);
		    res.addAll(removedLinks);
		}
	    }
	}
	fObjectStates.remove(obj);
	fClassObjects.remove(objClass, obj);
	fSystem.deleteObject(obj);
	return res;
    }

    /**
     * Creates and adds a new link to the state.
     *
     * @exception MSystemException link invalid or already existing
     * @return the newly created link.
     */
    public MLink createLink(MAssociation assoc, List objects)
	throws MSystemException 
    {
	MLink link = new MLinkImpl(assoc, objects);
	// get link set for association
	MLinkSet linkSet = (MLinkSet) fLinkSets.get(assoc);
	if ( linkSet.contains(link) )
	    throw new MSystemException("Link " + link + " already exists.");
	linkSet.add(link);
	return link;
    }

    /**
     * Inserts a link into the state.
     */
    public void insertLink(MLink link) {
	// get link set for association
	MLinkSet linkSet = (MLinkSet) fLinkSets.get(link.association());
	linkSet.add(link);
    }

    /**
     * Deletes a link from the state.
     */
    public void deleteLink(MLink link) {
	// get link set for association
	MLinkSet linkSet = (MLinkSet) fLinkSets.get(link.association());
	linkSet.remove(link);
    }


    /**
     * Deletes a link from the state. The link is indirectly specified
     * by the association and objects.
     *
     * @exception MSystemException link does not exist
     * @return the removed link.  
     */
    public MLink deleteLink(MAssociation assoc, List objects)
	throws MSystemException 
    {
	MLink link = new MLinkImpl(assoc, objects);
	// get link set for association
	MLinkSet linkSet = (MLinkSet) fLinkSets.get(assoc);
	if ( ! linkSet.contains(link) )
	    throw new MSystemException("Link " + link + " does not exist.");
	linkSet.remove(link);
	return link;
    }


    /**
     * Returns the state of an object in a specific system state.
     *
     * @return null if object does not exist in the state
     */
    MObjectState getObjectState(MObject obj) {
	return (MObjectState) fObjectStates.get(obj);
    }

    /**
     * Returns a list of objects at <code>dstEnd</code> which are
     * linked to <code>obj</code> at srcEnd.
     *
     * @return List(MObject) 
     */
    List getLinkedObjects(MObject obj, 
			  MAssociationEnd srcEnd, 
			  MAssociationEnd dstEnd) {
	ArrayList res = new ArrayList();

	// get association
	MAssociation assoc = dstEnd.association();
	
	// get link set for association
	MLinkSet linkSet = (MLinkSet) fLinkSets.get(assoc);

	// if link set is empty return empty result list
	Log.trace(this, "linkSet size of association `" + assoc.name() + 
		  "' = " + linkSet.size());
	if ( linkSet.size() == 0 )
	    return res;

	// select links with srcEnd == obj
	Set links = linkSet.select(srcEnd, obj);
	Log.trace(this, "linkSet.select for object `" + obj + 
		  "', size = " + links.size());

	// project tuples to destination end component 
	Iterator it = links.iterator();
	while ( it.hasNext() ) {
	    MLink link = (MLink) it.next();
	    MLinkEnd linkEnd = link.linkEnd(dstEnd);
	    res.add(linkEnd.object());
	}
	return res;
    }

    /**
     * Checks for a valid system state. Returns true if all
     * constraints hold for all objects. Prints result of
     * subexpressions for failing constraints to the log if
     * traceEvaluation is true.  
     */
    public boolean check(PrintWriter out, 
			 boolean traceEvaluation,
			 boolean showDetails,
			 List invNames) {
	boolean valid = true;
	Evaluator evaluator = new Evaluator();
	MModel model = fSystem.model();

	// model inherent constraints: check whether cardinalities of
	// association links match their declaration of multiplicities
	if ( ! checkStructure(out) )
	    return false;

	if ( Options.EVAL_NUMTHREADS > 1 )
	    out.println("checking invariants (using " + Options.EVAL_NUMTHREADS + 
			" concurrent threads)...");
	else
	    out.println("checking invariants...");
	out.flush();
	int numChecked = 0;
	int numFailed = 0;
	long tAll = System.currentTimeMillis();

	ArrayList invList = new ArrayList();
	ArrayList exprList = new ArrayList();
	if ( invNames.isEmpty() ) {
	    // get all invariants
	    Iterator it = model.classInvariants().iterator();
	    while ( it.hasNext() ) {
		MClassInvariant inv = (MClassInvariant) it.next();
		Expression expr = inv.expandedExpression();
		// sanity check
		if ( ! expr.type().isBoolean() )
		    throw new RuntimeException("Expression " + expr +
					       "has type " + expr.type() +
					       ", expected boolean type");
		invList.add(inv);
		exprList.add(expr);
	    }
	} else {
	    // get only specified invariants
	    Iterator it = invNames.iterator();
	    while ( it.hasNext() ) {
		String invName = (String) it.next();
		MClassInvariant inv = model.getClassInvariant(invName);
		if ( inv != null ) {
		    Expression expr = inv.expandedExpression();
		    // sanity check
		    if ( ! expr.type().isBoolean() )
			throw new RuntimeException("Expression " + expr +
						   "has type " + expr.type() +
						   ", expected boolean type");
		    invList.add(inv);
		    exprList.add(expr);
		}
	    }
	}

	// start (possibly concurrent) evaluation
	Queue resultValues = 
	    evaluator.evalList(Options.EVAL_NUMTHREADS, exprList, this);

	// receive results
	for (int i = 0; i < exprList.size(); i++) {
	    MClassInvariant inv = (MClassInvariant) invList.get(i);
	    numChecked++;
	    String msg = "checking invariant (" + numChecked + ") `" + 
		inv.cls().name() + "::" + inv.name() + "': ";
	    out.print(msg); // + inv.bodyExpression());
	    out.flush();
	    try {
		Value v = (Value) resultValues.get();
		boolean ok = v.isDefined() && ((BooleanValue) v).isTrue();
		if ( ok ) 
		    out.println("OK."); // (" + timeStr +").");
		else {
		    out.println("FAILED."); //  (" + timeStr +").");
		    out.println("  -> " + v.toStringWithType());
		    
		    // repeat evaluation with output of all subexpression results
		    if ( traceEvaluation ) {
			out.println("Results of subexpressions:");
			Expression expr = (Expression) exprList.get(i);
			evaluator.eval(expr, this, new VarBindings(), out);
		    }

		    // show instances violating the invariant by using
		    // the OCL expression C.allInstances->reject(self | <inv>)
		    if ( showDetails ) {
			out.println("Instances of " + inv.cls().name() + 
				    " violating the invariant:");
			Expression expr = inv.getExpressionForViolatingInstances();
			Value v1 = evaluator.eval(expr, this, new VarBindings());
			out.println("  -> " + v1.toStringWithType());
		    }
		    valid = false;
		    numFailed++;
		}
	    } catch (InterruptedException ex) {
		Log.error("InterruptedException: " + ex.getMessage());
	    }
	}

	long t = System.currentTimeMillis() - tAll;
	String timeStr = t % 1000 + "s";
	timeStr = (t / 1000) + "." + StringUtil.leftPad(timeStr, 4, '0');
	out.println("checked " + 
		    numChecked + " invariant" +
		    (( numChecked == 1 ) ? "" : "s") + 
		    " in " + timeStr + ", " + 
		    numFailed + " failure" +
		    (( numFailed == 1 ) ? "" : "s") + '.');
	out.flush();
	return valid;
    }

    /**
     * Checks model inherent constraints, i.e., checks whether
     * cardinalities of association links match their declaration of
     * multiplicities. 
     */
    public boolean checkStructure(PrintWriter out) {
	boolean res = true;
	out.println("checking structure...");
	out.flush();
	// check all associations
	Iterator it = fSystem.model().associations().iterator();
	while ( it.hasNext() ) {
	    MAssociation assoc = (MAssociation) it.next();
	    if ( assoc.associationEnds().size() != 2 ) {
		// check for n-ary links
		if ( ! naryAssociationsAreValid(out, assoc) )
		    res = false;
	    } else {
		// check both association ends
		Iterator it2 = assoc.associationEnds().iterator();
		MAssociationEnd aend1 = (MAssociationEnd) it2.next();
		MAssociationEnd aend2 = (MAssociationEnd) it2.next();

		if ( ! binaryAssociationsAreValid(out, assoc, aend1, aend2)
		     || ! binaryAssociationsAreValid(out, assoc, aend2, aend1) )
		    res = false;
	    }
	}
	//out.println("checking link cardinalities, done.");
	out.flush();
	return res;
    }

    private boolean naryAssociationsAreValid(PrintWriter out, 
					     MAssociation assoc) {
	boolean valid = true;
	int n = assoc.associationEnds().size();
	Set links = linksOfAssociation(assoc).links();

	// For each association end, we consider the set of all links
	// as a set of tuples where each tuple is a projection of a
	// link excluding the selected end. The numbers of equal
	// tuples must conform to the multiplicity specification of
	// the selected association end.

	Iterator aendIter1 = assoc.associationEnds().iterator();
	while ( aendIter1.hasNext() ) {
	    MAssociationEnd selEnd = (MAssociationEnd) aendIter1.next();
//  	    out.println("selEnd = " + selEnd);

	    // consider each link, put each projected tuple into a bag
	    Bag tupleBag = new HashBag();
	    Iterator linkIter = links.iterator();
	    while ( linkIter.hasNext() ) {
		MLink link = (MLink) linkIter.next();

		// project link onto remaining ends
		ArrayList tuple = new ArrayList(n - 1);
		Iterator aendIter2 = assoc.associationEnds().iterator();
		while ( aendIter2.hasNext() ) {
		    MAssociationEnd aend = (MAssociationEnd) aendIter2.next();
		    if ( ! aend.equals(selEnd) )
			tuple.add(link.linkEnd(aend));
		}
		tupleBag.add(tuple);
	    }
	    
	    // for each tuple in the bag, the number of occurrences
	    // must match the multiplicity of the selected association
	    // end
	    Iterator tupleIter = tupleBag.uniqueIterator();
	    while ( tupleIter.hasNext() ) {
		ArrayList tuple = (ArrayList) tupleIter.next();
		int num = tupleBag.occurrences(tuple);
//  		out.println("  tuple = [" + 
//  			    StringUtil.fmtSeq(tuple.iterator(), ", ") + 
//  			    "], num = " + num);
		if ( ! selEnd.multiplicity().contains(num) ) {
		    out.println("Multiplicity constraint violation in association `" + 
				assoc.name() + "':");
		    out.println("  Objects `" + 
				StringUtil.fmtSeq(tuple.iterator(), ", ") + 
				"' are connected to " + num + 
				" object" + 
				(( num == 1 ) ? "" : "s") +
				" of class `" + 
				selEnd.cls().name() + "'");
		    out.println("  but the multiplicity is specified as `" +
				selEnd.multiplicity() + "'.");
		    valid = false;
		}
	    }
   

	}
	return valid;
    }

    private boolean binaryAssociationsAreValid(PrintWriter out, 
					       MAssociation assoc,
					       MAssociationEnd aend1,
					       MAssociationEnd aend2) {
	boolean valid = true;

	// for each object of the association end's type get
	// the number of links in which the object participates
	MClass cls = aend1.cls();
	Set objects = objectsOfClassAndSubClasses(cls);
	Iterator it = objects.iterator();
	while ( it.hasNext() ) {
	    MObject obj = (MObject) it.next();
	    List objList = getLinkedObjects(obj, aend1, aend2);
	    int n = objList.size();
	    if ( ! aend2.multiplicity().contains(n) ) {
		out.println("Multiplicity constraint violation in association `" + 
			    assoc.name() + "':");
		out.println("  Object `" + obj.name() + "' of class `" + 
			    obj.cls().name() + 
			    "' is connected to " + n +
			    " object" + 
			    (( n == 1 ) ? "" : "s") +
			    " of class `" + 
			    aend2.cls().name() + "'");
		out.println("  but the multiplicity is specified as `" +
			    aend2.multiplicity() + "'.");
		valid = false;
	    }
	}
	return valid;
    }
}
