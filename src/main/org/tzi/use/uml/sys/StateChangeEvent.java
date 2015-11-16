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
import java.util.EventObject;
import java.util.List;
import java.util.ArrayList;

/**
 * Class providing information about a state change. Used to gather
 * change information from commands. A list of StateChangeInfo objects
 * fully documents the differences between two system states.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class StateChangeEvent extends EventObject {
    private List fNewObjects;
    private List fDeletedObjects;
    private List fModifiedObjects;
    private List fNewLinks;
    private List fDeletedLinks;

    /**
     * Constructs a StateChangeEvent object.
     */
    StateChangeEvent(Object source) {
        super(source);
	fNewObjects = new ArrayList();
	fDeletedObjects = new ArrayList();
	fModifiedObjects = new ArrayList();
	fNewLinks = new ArrayList();
	fDeletedLinks = new ArrayList();
    }

    public List getNewObjects() {
	return fNewObjects;
    }

    public List getDeletedObjects() {
	return fDeletedObjects;
    }

    public List getModifiedObjects() {
	return fModifiedObjects;
    }

    public List getNewLinks() {
	return fNewLinks;
    }

    public List getDeletedLinks() {
	return fDeletedLinks;
    }

    /**
     * Returns true if the number of objects or links has changed.
     */
    public boolean structureHasChanged() {
	return ! fNewObjects.isEmpty() 
	    || ! fDeletedObjects.isEmpty() 
	    || ! fNewLinks.isEmpty() 
	    || ! fDeletedLinks.isEmpty();
    }

    void addNewObject(MObject obj) {
	fNewObjects.add(obj);
    }

    void addDeletedObject(MObject obj) {
	fDeletedObjects.add(obj);
    }

    void addModifiedObject(MObject obj) {
	fModifiedObjects.add(obj);
    }

    void addNewLink(MLink link) {
	fNewLinks.add(link);
    }

    void addDeletedLink(MLink link) {
	fDeletedLinks.add(link);
    }

    public String toString() {
	return "new objects: " + fNewObjects +
	    ", deleted objects: " + fDeletedObjects +
	    ", modified objects: " + fModifiedObjects +
	    ", new links: " + fNewLinks +
	    ", deleted links: " + fDeletedLinks;
    }
}

