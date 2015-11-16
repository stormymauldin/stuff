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

package org.tzi.use.gui.views;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MLink;
import org.tzi.use.uml.sys.StateChangeEvent;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.util.StringUtil;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.print.PageFormat;


/** 
 * A graph showing an object diagram with objects and links in the
 * current system state.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class NewObjectDiagramView extends JPanel implements View, PrintableView {

    private MSystem fSystem;
    private MainWindow fMainWindow;

    NewObjectDiagram fObjectDiagram;

    public NewObjectDiagramView(MainWindow mainWindow, MSystem system) {
	fMainWindow = mainWindow;
	fSystem = system;
	fSystem.addChangeListener(this);

        setLayout(new BorderLayout());

	fObjectDiagram = new NewObjectDiagram(this, mainWindow.logWriter());
	add("Center", fObjectDiagram);

	initState();
    }

    /**
     * Does a full update of the view.
     */
    private void initState() {
	MSystemState systemState = fSystem.state();
	Set objects = systemState.allObjects();
	Iterator it = objects.iterator();
	while ( it.hasNext() ) {
	    MObject obj = (MObject) it.next();
	    fObjectDiagram.addObject(obj);
	}
	Set links = systemState.allLinks();
	it = links.iterator();
	while ( it.hasNext() ) {
	    MLink link = (MLink) it.next();
	    fObjectDiagram.addLink(link);
	}
	fObjectDiagram.repaint();
    }

    /**
     * Does an incremental update of the view.
     */
    public void stateChanged(StateChangeEvent e) {
	//System.out.println("StateChangeEvent: " + e);
	Iterator it;

	it = e.getNewObjects().iterator();
	while ( it.hasNext() )
	    fObjectDiagram.addObject((MObject) it.next());

	it = e.getNewLinks().iterator();
	while ( it.hasNext() )
	    fObjectDiagram.addLink((MLink) it.next());

	it = e.getDeletedLinks().iterator();
	while ( it.hasNext() )
	    fObjectDiagram.deleteLink((MLink) it.next());

	it = e.getDeletedObjects().iterator();
	while ( it.hasNext() )
	    fObjectDiagram.deleteObject((MObject) it.next());

	// attribute value changes are always recognized since a
	// repaint forces object nodes to ask for the current
	// attribute values.
	fObjectDiagram.repaint();
    }

    /**
     * Returns the set of all associations that exist between the
     * specified classes. 
     *
     * @return Set(MAssociation) 
     */
    Set getAllAssociationsBetweenClasses(Set classes) {
	return fSystem.model().getAllAssociationsBetweenClasses(classes);
    }

    /**
     * Returns true if there is a link of the specified association
     * connecting the given set of objects.
     */
    boolean hasLinkBetweenObjects(MAssociation assoc, Set objects) {
	return fSystem.state().hasLinkBetweenObjects(assoc, objects);
    }

    /**
     * Executes a command for inserting a new link.
     */
    void insertLink(String assocName, Set objects) {
	MModel model = fSystem.model();
	MAssociation assoc = model.getAssociation(assocName);

	// have to sort the object names according to the order
	// defined in the association
	String objNameList = null;
	java.util.List assocEnds = assoc.associationEnds();
	boolean numObjectsEqualsNumAssocEnds = objects.size() == assocEnds.size();
	Iterator aendIter = assocEnds.iterator();
	while ( aendIter.hasNext() ) {
	    MAssociationEnd aend = (MAssociationEnd) aendIter.next();
	    Iterator objIter = objects.iterator();
	    while ( objIter.hasNext() ) {
		MObject obj = (MObject) objIter.next();
		if ( obj.cls().equals(aend.cls()) ) {
		    if ( objNameList == null )
			objNameList = obj.name();
		    else
			objNameList += "," + obj.name();
		    if ( numObjectsEqualsNumAssocEnds )
			objIter.remove();
		    break;
		}
	    }
	}
	
	String cmd = "insert (" + objNameList + ") into " + assocName;
	fMainWindow.execCmd(cmd);
    }

    /**
     * Executes a command for deleting selected objects.
     */
    void deleteObjects(Set objects) {
	String cmd = "destroy " + StringUtil.fmtSeq(objects.iterator(), ",");
	fMainWindow.execCmd(cmd);
    }

    MSystem system() {
	return fSystem;
    }

    /**
     * Detaches the view from its model.
     */
    public void detachModel() {
	fSystem.removeChangeListener(this);
    }

    void createObject(String clsName) {
	ArrayList names = new ArrayList(1);
	names.add(fSystem.uniqueObjectNameForClass(clsName));
	fMainWindow.createObject(clsName, names);
    }

    public void printView(PageFormat pf) {
	fObjectDiagram.printDiagram(pf);
    }
}
