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
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.sys.StateChangeEvent;
import org.tzi.use.uml.mm.MClass;
import java.awt.Color;
import java.util.Collection;
import java.util.Arrays;

/** 
 * A BarChartView showing the number of objects in the current system
 * state.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class ObjectCountView extends BarChartView implements View {
    private MSystem fSystem;
    private Object[] fClasses;
    private int[] fValues;

    public ObjectCountView(MSystem system) {
	super("Class", "# Objects", Color.blue);
	fSystem = system;
	Collection classes = fSystem.model().classes();
	fClasses = classes.toArray();
	Arrays.sort(fClasses);
	setNames(fClasses);
	fValues = new int[fClasses.length];
	fSystem.addChangeListener(this);
	update();
    }

    private void update() {
	MSystemState systemState = fSystem.state();
	for (int i = 0; i < fClasses.length; i++) {
	    fValues[i] = systemState.objectsOfClass((MClass) fClasses[i]).size();
	}
	setValues(fValues);
    }

    public void stateChanged(StateChangeEvent e) {
	update();
    }

    /**
     * Detaches the view from its model.
     */
    public void detachModel() {
	fSystem.removeChangeListener(this);
    }
}
