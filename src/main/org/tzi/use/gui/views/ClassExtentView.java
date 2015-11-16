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
import org.tzi.use.gui.util.PopupListener;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MClassInvariant;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.StateChangeEvent;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.ObjectValue;
import org.tzi.use.uml.ocl.value.SetValue;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.config.Options;
import org.tzi.use.util.NullWriter;
import java.io.PrintWriter;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

/** 
 * A view showing all objects of a class, their properties
 * (attributes), and results of invariants.
 * 
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class ClassExtentView extends JPanel implements View, ActionListener {
    private MainWindow fMainWindow;
    private MSystem fSystem;
    private MClass fClass;
    private boolean fShowInvResults;

    private JPopupMenu fPopupMenu; // context menu on right mouse click
    private JTable fTable;
    private JScrollPane fTablePane;
    private TableModel fTableModel;

    public ClassExtentView(MainWindow parent, MSystem system) {
	super(new BorderLayout());
	fMainWindow = parent;
	fSystem = system;
	fSystem.addChangeListener(this);

	// get a class
	Collection classes = fSystem.model().classes();
	if ( ! classes.isEmpty() ) {
	    fClass = (MClass) classes.iterator().next();
	}

	// create table of attribute/value pairs
        fTableModel = new TableModel();
	fTable = new JTable(fTableModel);
	fTable.setPreferredScrollableViewportSize(new Dimension(250, 70));
	fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	fTablePane = new JScrollPane(fTable);

	// create the popup menu for options
        fPopupMenu = new JPopupMenu();
        final JCheckBoxMenuItem cbShowInvResults = 
	    new JCheckBoxMenuItem("Show results of invariants");
	cbShowInvResults.setState(fShowInvResults);
	cbShowInvResults.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fShowInvResults = ev.getStateChange() == ItemEvent.SELECTED;
		    fTableModel.initStructure();
		    fTableModel.initModel();
		    if ( fShowInvResults )
			fTableModel.updateInvariants();
		    fTableModel.fireTableStructureChanged();
		    fTableModel.fireTableDataChanged();
                }
	    });
	fPopupMenu.add(cbShowInvResults);
	JMenu submenu = new JMenu("Select class");
	// add all classes to submenu 
	Object[] c = classes.toArray();
	Arrays.sort(c);
	JMenu sub = submenu;
	for (int i = 0; i < c.length; i++) {
	    if ( i > 0 && i % 30 == 0 ) {
		// nested submenus for large model
		JMenu s = new JMenu("More...");
		sub.add(s);
		sub = s;
	    }
	    JMenuItem mi = new JMenuItem(((MClass) c[i]).name());
	    mi.addActionListener(this);
	    sub.add(mi);
	}
	fPopupMenu.add(submenu);
	fTable.addMouseListener(new PopupListener(fPopupMenu));
	fTablePane.addMouseListener(new PopupListener(fPopupMenu));

	// layout panel
	add(fTablePane, BorderLayout.CENTER);
    }

    /**
     * Called when a class was selected in the popup menu.
     */
    public void actionPerformed(ActionEvent e) {
	// System.err.println("*** ActionEvent = " + e.getActionCommand());
	String name = e.getActionCommand();
	// see if there's really a change
	if ( fClass.name().equals(name) )
	    return;

	fClass = fSystem.model().getClass(name);
	fTableModel.initStructure();
	fTableModel.initModel();
	if ( fShowInvResults )
	    fTableModel.updateInvariants();
	fTableModel.fireTableStructureChanged();
	fTableModel.fireTableDataChanged();
//  	TableColumn column = null;
//  	for (int i = fTableModel.getColumnCount() - 1; i >= 0; i--) {
//  	    column = fTable.getColumnModel().getColumn(i);
//  	    column.sizeWidthToFit();
//  	}
    }

   
    // icons used for invariant results
    private final static Icon fTrueIcon = 
	new ImageIcon(Options.iconDir + "InvTrue.gif");
    private final static Icon fFalseIcon = 
	new ImageIcon(Options.iconDir + "InvFalse.gif");
    private final static Icon fNotAvailIcon = 
	new ImageIcon(Options.iconDir + "InvNotAvail.gif");
	
    /**
     * The table model.
     */
    class TableModel extends AbstractTableModel {
        private ArrayList fColumnNames;
	private ArrayList fObjects;
	private MAttribute[] fAttributes;
	private MClassInvariant[] fClassInvariants;
	private Map fObjectValueStrMap;	// (MObject -> String)
	private Map fInvBadObjects; // (MClassInvariant -> Set(MObject))

	// need to cache all data because getValueAt is called often.

	TableModel() {
	    fObjects = new ArrayList();
	    fColumnNames = new ArrayList();
	    fObjectValueStrMap = new HashMap();
	    fInvBadObjects = new HashMap();
	    initStructure();
	    initModel();
	}

	void initStructure() {
	    fColumnNames.clear();
	    fInvBadObjects.clear();
	    if ( fClass == null )
		return;
	    
	    // prepare table: get attributes of class
	    Set attributes = fClass.allAttributes();
	    int n = attributes.size();
	    fAttributes = new MAttribute[n];
	    System.arraycopy(attributes.toArray(), 0, fAttributes, 0, n);
	    Arrays.sort(fAttributes);

	    // set columns
	    fColumnNames.add(fClass.name());
	    for (int i = 0; i < fAttributes.length; i++)
		fColumnNames.add(fAttributes[i].name());

	    // get all invariants for selected class
	    if ( fShowInvResults ) {
		Set invSet = fSystem.model().allClassInvariants(fClass);
		n = invSet.size();
		fClassInvariants = new MClassInvariant[n];
		System.arraycopy(invSet.toArray(), 0, fClassInvariants, 0, n);
		Arrays.sort(fClassInvariants);
		for (int i = 0; i < fClassInvariants.length; i++)
		    fColumnNames.add(fClassInvariants[i].name());
	    }
	}

	void initModel() {
	    // initialize object list
	    fObjects.clear();
	    fObjectValueStrMap.clear();
	    if ( fClass == null )
		return;

	    // get attribute values for all objects
	    Iterator objIter = fSystem.state().objectsOfClass(fClass).iterator();
	    while ( objIter.hasNext() ) {
		MObject obj = (MObject) objIter.next();
		addObject(obj);
	    }
	    sortRows();
	}
	
	void updateInvariants() {
	    if ( ! fSystem.state().checkStructure(new PrintWriter(new NullWriter())) ) {
		// cannot evaluate on ill-formed state
		for (int i = 0; i < fClassInvariants.length; i++) {
		    fInvBadObjects.put(fClassInvariants[i], null);
		}
	    } else {
		for (int i = 0; i < fClassInvariants.length; i++) {
		    Set badObjects = new HashSet();
		    Evaluator evaluator = new Evaluator();
		    Expression expr = 
			fClassInvariants[i].getExpressionForViolatingInstances();
		    Value v = evaluator.eval(expr, fSystem.state(), new VarBindings());

		    Iterator valIter = ((SetValue) v).collection().iterator();
		    while ( valIter.hasNext() ) {
			ObjectValue oVal = (ObjectValue) valIter.next();
			badObjects.add(oVal.value());
		    }
		    fInvBadObjects.put(fClassInvariants[i], badObjects);
		}
	    }
	}

	public String getColumnName(int col) {
            return (String) fColumnNames.get(col);
        }

	public int getColumnCount() { 
	    return fColumnNames.size();
	}

	public int getRowCount() { 
	    return fObjects.size();
	}

	public Class getColumnClass(int col) {
	    if ( col <= fAttributes.length )
		return String.class;
	    else
		return Icon.class;
	}

	public Object getValueAt(int row, int col) { 
	    // System.err.println("*** getValueAt (" + row + ", " + col + ")");
	    MObject obj = (MObject) fObjects.get(row);
	    if ( col == 0 )
		return obj.name();
	    else if ( col <= fAttributes.length ) {
		String[] values = (String[]) fObjectValueStrMap.get(obj);
		return values[col - 1];
	    } else {
		MClassInvariant inv = fClassInvariants[col - fAttributes.length - 1];
		Set badObjects = (Set) fInvBadObjects.get(inv);
		if ( badObjects == null )
		    return fNotAvailIcon;
		else if ( badObjects.contains(obj) )
		    return fFalseIcon;
		else
		    return fTrueIcon;
	    }
	}

	/**
	 * Adds a row for an object to the table.
	 */
	void addObject(MObject obj) {
	    fObjects.add(obj);
	    updateObject(obj);
	}

	/**
	 * Updates the row for the given object.
	 */
	void updateObject(MObject obj) {
	    MObjectState objState = obj.state(fSystem.state());
	    String[] values = new String[fAttributes.length];
	    for (int i = 0; i < fAttributes.length; i++)
		values[i] = 
		    ((Value) objState.attributeValue(fAttributes[i])).toString();

	    fObjectValueStrMap.put(obj, values);
	}

	/**
	 * Removes a row for an object from the table.
	 */
	void removeObject(MObject obj) {
	    fObjects.remove(obj);
	    fObjectValueStrMap.remove(obj);
	}

	void sortRows() {
	    Collections.sort(fObjects, new Comparator() {
		    public int compare(Object o1, Object o2) {
			return o1.toString().compareTo(o2.toString());
		    }});
	}
    }

    /** 
     * Called due to an external change of state.
     */
    public void stateChanged(StateChangeEvent e) {
	Iterator it;
	
	// incremental update of table model
	it = e.getNewObjects().iterator();
	while ( it.hasNext() ) {
	    MObject obj = (MObject) it.next();
	    if ( obj.cls().equals(fClass) )
		fTableModel.addObject(obj);
	}

	it = e.getDeletedObjects().iterator();
	while ( it.hasNext() ) {
	    MObject obj = (MObject) it.next();
	    if ( obj.cls().equals(fClass) )
		fTableModel.removeObject(obj);
	}

	it = e.getModifiedObjects().iterator();
	while ( it.hasNext() ) {
	    MObject obj = (MObject) it.next();
	    if ( obj.cls().equals(fClass) )
		fTableModel.updateObject(obj);
	}

	// change in number of rows?
	if ( e.structureHasChanged() )
	    fTableModel.sortRows();
	if ( fShowInvResults )
	    fTableModel.updateInvariants();
	fTableModel.fireTableDataChanged();
    }

    /**
     * Detaches the view from its model.
     */
    public void detachModel() {
	fSystem.removeChangeListener(this);
    }
}

