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
import org.tzi.use.uml.ocl.type.ObjectType;
import org.tzi.use.uml.ocl.value.ObjectValue;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.util.StringUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A command for creating objects of a given type and binding them to
 * identifiers.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public final class MCmdCreateObjects extends MCmd {
    private MSystemState fSystemState;
    private List fVarNames;	// (String) variable names
    private ObjectType fType;

    // undo information
    private List fObjects;

    /**
     * Creates a command for creating objects of a given type and
     * binding them to identifiers which are given as a list of names.
     */
    public MCmdCreateObjects(MSystemState systemState, List names, ObjectType type) {
	super(true);
	fSystemState = systemState;
	fVarNames = names;
	fType = type;
    }

    /** 
     * Executes command and stores undo information.
     *
     * @exception CommandFailedException if the command failed.
     */
    public void execute() throws CommandFailedException {
	fObjects = new ArrayList();
	VarBindings varBindings = fSystemState.system().varBindings();
	Iterator it = fVarNames.iterator();
	while ( it.hasNext() ) {
	    String varname = (String) it.next();
	    if ( varBindings.getValue(varname) != null )
		throw new CommandFailedException("Object `" + varname + 
						 "' already exists.");

	    // create object of specified type
	    try {
		MObject obj = fSystemState.createObject(fType.cls(), varname);
		fObjects.add(obj);
		// bind variable to object
		varBindings.push(varname, new ObjectValue(fType, obj));
	    } catch (MSystemException ex) {
		throw new CommandFailedException(ex.getMessage());
	    }
	}
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
	if ( fObjects == null )
	    throw new CannotUndoException("no undo information");
	
	VarBindings varBindings = fSystemState.system().varBindings();
	Iterator it1 = fObjects.iterator();
	Iterator it2 = fVarNames.iterator();
	while ( it1.hasNext() ) {
	    fSystemState.deleteObject((MObject) it1.next());
	    String varname = (String) it2.next();
	    varBindings.remove(varname);
	}
    }

    /**
     * Fill a StateChangeEvent with information about this command's
     * effect.  
     *
     * @param undoChanges get information about undo action of command.
     */
    public void getChanges(StateChangeEvent sce, boolean undoChanges) {
	if ( fObjects == null )
	    throw new IllegalStateException("command not executed");
	
	Iterator objIterator = fObjects.iterator();
	while ( objIterator.hasNext() )
	    if ( undoChanges )
		sce.addDeletedObject((MObject) objIterator.next());
	    else
		sce.addNewObject((MObject) objIterator.next());
    }

    /**
     * Returns a short name of command, e.g. 'Create class foo' for
     * display in an undo menu item.  
     */
    public String name() {
	return "Create object " + 
	    (( fVarNames.size() > 1 ) ? "s" : "") + 
	    StringUtil.fmtSeq(fVarNames.iterator(), ",") + 
	    " : " + fType;
    }

    /**
     * Returns a string that can be read and executed by the USE shell
     * achieving the same effect of this command.  
     */
    public String getUSEcmd() {
	return "!create " + 
	    StringUtil.fmtSeq(fVarNames.iterator(), ",") + 
	    " : " + fType;
    }

    /**
     * Returns a general name of command, e.g. 'Create Class'.
     */
    public String toString() {
	return "Create object";
    }
}
