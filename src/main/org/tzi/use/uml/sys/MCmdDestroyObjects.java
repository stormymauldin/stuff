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
import org.tzi.use.util.cmd.CommandFailedException;
import org.tzi.use.util.cmd.CannotUndoException;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.CollectionValue;
import org.tzi.use.uml.ocl.value.ObjectValue;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.util.StringUtil;
import java.util.*;

/**
 * A command for destroying objects.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public final class MCmdDestroyObjects extends MCmd {
    private MSystemState fSystemState;
    private Expression fExpr;

    // undo information
    private List fObjectStates;	// (MObjectState)
    private Set fRemovedLinks;	// (MLink)

    /**
     * Creates a command for destroying objects whose names are given as
     * a list of identifiers.
     */
    public MCmdDestroyObjects(MSystemState systemState, Expression expr) {
	super(true);
	fSystemState = systemState;
	fExpr = expr;
    }

    private void destroyOne(VarBindings varBindings, Value v) 
	throws CommandFailedException 
    {
	ObjectValue oval = (ObjectValue) v;
	MObject obj = oval.value();
	MObjectState objState = obj.state(fSystemState);
	if ( objState == null )
	    throw new CommandFailedException("Object `" + obj.name() + 
					     "' does not exist anymore.");
	// delete object
	Set removedLinks = fSystemState.deleteObject(obj);
	
	// store undo information
	fObjectStates.add(objState);
	fRemovedLinks.addAll(removedLinks);
	
	// remove variable binding
	varBindings.remove(obj.name());
    }

    /** 
     * Executes command and stores undo information.
     *
     * @exception CommandFailedException if the command failed.
     */
    public void execute() throws CommandFailedException {
	fObjectStates = new ArrayList();
	fRemovedLinks = new HashSet();

	Evaluator evaluator = new Evaluator();
	VarBindings varBindings = fSystemState.system().topLevelBindings();
	Value v = evaluator.eval(fExpr, fSystemState, varBindings);
	if ( v.isObject() ) {
	    destroyOne(varBindings, v);
	} else if ( v.isCollection() ) {
	    CollectionValue coll = (CollectionValue) v;
	    Iterator elemIter = coll.iterator();
	    while ( elemIter.hasNext() ) {
		Value elem = (Value) elemIter.next();
		// additional check
		if ( ! elem.isObject() )
		    throw new CommandFailedException("Element in collection argument " +
			     "of destroy command " + 
			     "does not evaluate to an object, found `" + 
			     elem.toStringWithType() + "'.");
		destroyOne(varBindings, elem);
	    }
	} else
	    throw new CommandFailedException("Argument of destroy command " + 
					     "does not evaluate to an object, found `" + 
					     v.toStringWithType() + "'.");

    }

    /**
     * Undo effect of command.
     *
     * @exception CannotUndoException if the command cannot be undone or 
     *                                has not been executed before.
     */
    public void undo() throws CannotUndoException {
	// the CommandProcessor should prevent us from being called
	// without a successful prior execute, just be safe here
	if ( fObjectStates == null )
	    throw new CannotUndoException("no undo information");

	// recreate objects
	VarBindings varBindings = fSystemState.system().varBindings();
	Iterator objStateIter = fObjectStates.iterator();
	while ( objStateIter.hasNext() ) {
	    MObjectState objState = (MObjectState) objStateIter.next();
	    try {
		fSystemState.restoreObject(objState);
		MObject obj = objState.object();
		varBindings.push(obj.name(), 
				 new ObjectValue(TypeFactory.mkObjectType(obj.cls()), 
						 obj));
	    } catch (MSystemException ex) {
		throw new CannotUndoException(ex.getMessage());
	    }
	}

	// restore links
	Iterator linkIter = fRemovedLinks.iterator();
	while ( linkIter.hasNext() ) {
	    MLink link = (MLink) linkIter.next();
	    fSystemState.insertLink(link);
	}
    }

    /**
     * Fill a StateChangeEvent with information about this command's
     * effect.  
     *
     * @param undoChanges get information about undo action of command.
     */
    public void getChanges(StateChangeEvent sce, boolean undoChanges) {
	if ( fObjectStates == null )
	    throw new IllegalStateException("command not executed");
	
	Iterator objStateIter = fObjectStates.iterator();
	while ( objStateIter.hasNext() ) {
	    MObject obj = ((MObjectState) objStateIter.next()).object();
	    if ( undoChanges )
		sce.addNewObject(obj);
	    else
		sce.addDeletedObject(obj);
	}

	Iterator linkIter = fRemovedLinks.iterator();
	while ( linkIter.hasNext() )
	    if ( undoChanges )
		sce.addNewLink((MLink) linkIter.next());
	    else
		sce.addDeletedLink((MLink) linkIter.next());
    }

    /**
     * Returns a short name of command, e.g. 'Create class foo' for
     * display in an undo menu item.  
     */
    public String name() {
	return "Destroy object(s) " + fExpr;
    }

    /**
     * Returns a string that can be read and executed by the USE shell
     * achieving the same effect of this command.  
     */
    public String getUSEcmd() {
	return "!destroy " + fExpr;
    }

    /**
     * Returns a general name of command, e.g. 'Create Class'.
     */
    public String toString() {
	return "Destroy object";
    }
}
