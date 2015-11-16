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
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.StateChangeEvent;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.util.Log;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

/** 
 * A view for showing and changing object properties (attributes).
 * 
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class ObjectPropertiesView extends JPanel implements View {
    private static final String NO_OBJECTS_AVAILABLE = "(No objects available.)";

    private MainWindow fMainWindow;
    private MSystem fSystem;
    private MObject fObject;

    private JComboBox fObjectComboBox;
    private JTable fTable;
    private JScrollPane fTablePane;
    private JButton fBtnApply;
    private JButton fBtnReset;
    private TableModel fTableModel;
    private ObjectComboBoxActionListener fObjectComboBoxActionListener;

    private MAttribute[] fAttributes;
    private String[] fValues;
    private Map fAttributeValueMap;

    /**
     * The table model.
     */
    class TableModel extends AbstractTableModel {
        final String[] columnNames = { "Attribute", "Value" };

	TableModel() {
	    update();
	}

	public String getColumnName(int col) {
            return columnNames[col];
        }

	public int getColumnCount() { 
	    return 2; 
	}
	public int getRowCount() { 
	    return fAttributes.length;
	}
	public Object getValueAt(int row, int col) { 
	    if ( col == 0 )
		return fAttributes[row];
	    else
		return fValues[row];
	}
	public boolean isCellEditable(int row, int col) {
	    return col == 1; 
	}

	public void setValueAt(Object value, int row, int col) {
	    Log.trace(this, "row = " + row + ", col = " + col + ", value = " + value);
	    fValues[row] = value.toString();
	    fireTableCellUpdated(row, col);
	}

	private void update() {
	    // initialize table model
	    if ( haveObject() ) {
		MObjectState objState = fObject.state(fSystem.state());
		fAttributeValueMap = objState.attributeValueMap();
		final int N = fAttributeValueMap.size();
		fAttributes = new MAttribute[N];
		fValues = new String[N];
		System.arraycopy(fAttributeValueMap.keySet().toArray(), 0, 
				 fAttributes, 0, N);
		Arrays.sort(fAttributes);
		for (int i = 0; i < N; i++)
		    fValues[i] = 
			((Value) fAttributeValueMap.get(fAttributes[i])).toString();
	    } else {
		fAttributes = new MAttribute[0];
		fValues = new String[0];
	    }
	    fireTableDataChanged();
	}
    }

    class ObjectComboBoxActionListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    JComboBox cb = (JComboBox) e.getSource();
	    String objName = (String) cb.getSelectedItem();
	    Log.trace(this, "fObjectComboBox.actionPerformed(): " + objName);
	    if ( objName != NO_OBJECTS_AVAILABLE )
		selectObject(objName);
	}
    }

    public ObjectPropertiesView(MainWindow parent, MSystem system) {
	super(new BorderLayout());
	fMainWindow = parent;
	fSystem = system;
	fSystem.addChangeListener(this);

	// create combo box with available objects
	fObjectComboBox = new JComboBox();
	fObjectComboBoxActionListener = new ObjectComboBoxActionListener();

	// create table of attribute/value pairs
        fTableModel = new TableModel();
	fTable = new JTable(fTableModel);
	fTable.setPreferredScrollableViewportSize(new Dimension(250, 70));
	fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	fTablePane = new JScrollPane(fTable);

	// create buttons
	fBtnApply = new JButton("Apply");
	fBtnApply.setMnemonic('A');
	fBtnApply.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    applyChanges();
		}
	    });
	fBtnReset = new JButton("Reset");
	fBtnReset.setMnemonic('R');
	fBtnReset.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    update();
		}
	    });

	// layout the buttons centered from left to right
	JPanel buttonPane = new JPanel();
	buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
	buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	buttonPane.add(Box.createHorizontalGlue());
	buttonPane.add(fBtnApply);
	buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
	buttonPane.add(fBtnReset);
	buttonPane.add(Box.createHorizontalGlue());
	
	// layout panel
	add(fObjectComboBox, BorderLayout.NORTH);
	add(fTablePane, BorderLayout.CENTER);
	add(buttonPane, BorderLayout.SOUTH);
	setSize(new Dimension(300, 300));

	updateGUIState();
    }

    /**
     * Applies changes by setting new attribute values. Entries may be
     * arbitrary OCL expressions. 
     */
    private void applyChanges() {
	if ( ! haveObject() )
	    return;
	// build command for changed attributes
	String line = null;
	for (int i = 0; i < fAttributes.length; i++) {
	    String oldValue = 
		((Value) fAttributeValueMap.get(fAttributes[i])).toString();
	    if ( ! oldValue.equals(fValues[i]) ) {
		if ( line == null )
		    line = "";
		else
		    line += "; ";
		line += "set " + fObject.name() + "." + fAttributes[i].name() + 
		    " = " + fValues[i];
	    }
	}
	fMainWindow.execCmd(line);
	update();
    }

    private boolean haveObject() {
	return fObject != null && fObject.exists(fSystem.state());
    }

    /**
     * Initializes and updates the list of available objects.
     */
    private void updateGUIState() {
	Log.trace(this, "updateGUIState1");

	// temporarily turn off action listener, since setting the
	// model triggers a select action which cannot be
	// distinguished from a user initiated selection
	fObjectComboBox.removeActionListener(fObjectComboBoxActionListener);

	// build list of names of currently existing objects
	MSystemState state = fSystem.state();
	Set allObjects = state.allObjects();
	ArrayList livingObjects = new ArrayList();
	Iterator objectIterator = allObjects.iterator();
	while ( objectIterator.hasNext() ) {
	    MObject obj = (MObject) objectIterator.next();
	    if ( obj.exists(state) )
		livingObjects.add(obj.name());
	}
	if ( livingObjects.isEmpty() ) {
	    livingObjects.add(NO_OBJECTS_AVAILABLE);
	    fObjectComboBox.setEnabled(false);
	    fObject = null;
	} else
	    fObjectComboBox.setEnabled(true);
	Object[] objNames = livingObjects.toArray();
	Arrays.sort(objNames);

	// create combo box with available objects
	fObjectComboBox.setModel(new DefaultComboBoxModel(objNames));
	// try to keep selection
	if ( haveObject() )
	    fObjectComboBox.setSelectedItem(fObject.name());
	fObjectComboBox.addActionListener(fObjectComboBoxActionListener);
	Log.trace(this, "updateGUIState2");
    }

    /**
     * An object has been selected from the list. Update the table of
     * properties.
     */
    private void selectObject(String objName) {
	MSystemState state = fSystem.state();
	fObject = state.objectByName(objName);
	fTableModel.update();
    }


    private void update() {
	updateGUIState();
	fTableModel.update();
    }
    
    /** 
     * Called due to an external change of state.
     */
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

