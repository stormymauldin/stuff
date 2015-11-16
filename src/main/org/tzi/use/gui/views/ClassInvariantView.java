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
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.sys.StateChangeEvent;
import org.tzi.use.uml.mm.MClassInvariant;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.config.Options;
import org.tzi.use.util.NullWriter;
import org.tzi.use.util.Queue;
import org.tzi.use.util.Log;
import java.io.PrintWriter;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.util.Arrays;
import java.util.ArrayList;

/** 
 * A table showing invariants and their results.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class ClassInvariantView extends JPanel implements View {
    private JTable fTable;
    private JLabel fLabel;	// message at bottom of view
    private JProgressBar fProgressBar;
    private MSystem fSystem;
    private MModel fModel;
    private MClassInvariant[] fClassInvariants;
    private Value[] fValues;

    private MyTableModel fMyTableModel;
    private int fSelectedRow = -1;
    private boolean fOpenEvalBrowserEnabled = false;

    /**
     * The table model.
     */
    class MyTableModel extends AbstractTableModel {
        final String[] columnNames = { "Invariant", "Result" };

	public String getColumnName(int col) {
            return columnNames[col];
        }

	public int getColumnCount() { 
	    return 2; 
	}
	public int getRowCount() { 
	    return fClassInvariants.length;
	}
	public Object getValueAt(int row, int col) { 
	    if ( col == 0 )
		return fClassInvariants[row];
	    else
		return fValues[row];
	}

        public Class getColumnClass(int c) {
	    if ( c == 1 )
		return Value.class;
	    else
		return Object.class;
        }
    }

    /**
     * Renderer for (boolean) values. Uses different colors for
     * different values.  
     */
    class ValueRenderer extends JLabel implements TableCellRenderer {
	final Color colorTrue = new Color(0, 0x80, 0);
	final Color colorFalse = new Color(0xc0, 0, 0);
	final Color colorUndefined = Color.gray;

        public Component getTableCellRendererComponent(
                                JTable table, Object obj, 
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
	    Color c = colorUndefined;
	    if ( obj == null )
		setText("n/a");
	    else {
		setText(obj.toString());
		if ( obj instanceof BooleanValue ) {
		    boolean b = ((BooleanValue) obj).value();
		    c = ( b ) ? colorTrue : colorFalse;
		}
	    }
	    this.setForeground(c);
            return this;
        }
    }


    public ClassInvariantView(MSystem system) {
	fSystem = system;
	fModel = fSystem.model();
	fSystem.addChangeListener(this);

	int n = fModel.classInvariants().size();

	// initialize array of class invariants
	fClassInvariants = new MClassInvariant[n];
	System.arraycopy(fModel.classInvariants().toArray(), 0,
			 fClassInvariants, 0, n);
	Arrays.sort(fClassInvariants);

	// initialize value array to undefined values
	fValues = new Value[n];
	clearValues();
	
        setLayout(new BorderLayout());
        fMyTableModel = new MyTableModel();
	fTable = new JTable();
	fTable.setModel(fMyTableModel);
        add(new JScrollPane(fTable), BorderLayout.CENTER);
	JPanel bottomPanel = new JPanel(new BorderLayout());
        fLabel = new JLabel();
	fLabel.setForeground(Color.black);
        bottomPanel.add(fLabel, BorderLayout.CENTER);
        fProgressBar = new JProgressBar(0, n);
	fProgressBar.setStringPainted(true);
        bottomPanel.add(fProgressBar, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

	fTable.setPreferredScrollableViewportSize(new Dimension(250, 70));
	fTable.setDefaultRenderer(Value.class, new ValueRenderer());

	// track selections
	fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	ListSelectionModel rowSM = fTable.getSelectionModel();
	rowSM.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                 if ( lsm.isSelectionEmpty() ) {
                     fSelectedRow = -1;
                 } else {
                     fSelectedRow = lsm.getMinSelectionIndex();
                 }
             }
         });

	// double click on table opens an ExprEvalBrowser on the
	// selected invariant
	setOpenEvalBrowserEnabled(false);
	fTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
		    if ( e.getClickCount() == 2 
			 && fSelectedRow >= 0 && 
			 fOpenEvalBrowserEnabled ) {

//  			ClassInvariantDetailsDialog dlg = 
//  			    new ClassInvariantDetailsDialog(fSystem,
//  							    fClassInvariants[fSelectedRow]);
//  			dlg.setVisible(true);

			// System.out.println("double click on: " + fSelectedRow);
			Expression expr = 
			    fClassInvariants[fSelectedRow].expandedExpression();
			Evaluator evaluator = new Evaluator();
			evaluator.enableEvalTree();
			Value v = evaluator.eval(expr, fSystem.state());
			ExprEvalBrowser.create(evaluator.getEvalNodeRoot());
		    }
                }
            });
	update();
    }

    private void clearValues() {
	for (int i = 0; i < fValues.length; i++)
	    fValues[i] = null;
    }

    private void setOpenEvalBrowserEnabled(boolean on) {
	if ( on ) {
	    fTable.setToolTipText("Double click to open evaluation browser");
	} else {
	    fTable.setToolTipText(null);
	}
	fOpenEvalBrowserEnabled = on;
    }

    private void update() {
	MSystemState systemState = fSystem.state();
	if ( ! systemState.checkStructure(new PrintWriter(new NullWriter())) ) {
	    clearValues();
	    setOpenEvalBrowserEnabled(false);
	    fLabel.setForeground(Color.red);
	    fLabel.setText("Model inherent constraints violated (see Log for details).");
	} else {
	    // check invariants
	    fLabel.setForeground(Color.black);
	    if ( Options.EVAL_NUMTHREADS > 1 )
		fLabel.setText("Working (using " + Options.EVAL_NUMTHREADS + 
			       " concurrent threads)...");
	    else
		fLabel.setText("Working...");

	    ArrayList exprList = new ArrayList();
	    for (int i = 0; i < fClassInvariants.length; i++)
		exprList.add(fClassInvariants[i].expandedExpression());

	    // start (possibly concurrent) evaluation
	    Evaluator evaluator = new Evaluator();
	    Queue resultValues = 
		evaluator.evalList(Options.EVAL_NUMTHREADS, exprList, systemState);

	    int numFailures = 0;
	    fProgressBar.setValue(0);
	    for (int i = 0; i < fClassInvariants.length; i++) {
		try {
		    Value v = (Value) resultValues.get();
		    boolean ok = v.isDefined() && ((BooleanValue) v).isTrue();
		    if ( ! ok )
			numFailures++;
		    fValues[i] = v;
		    fProgressBar.setValue(i + 1);
		    fProgressBar.repaint();
		} catch (InterruptedException ex) {
		    Log.error("InterruptedException: " + ex.getMessage());
		}
	    }
	    setOpenEvalBrowserEnabled(true);

	    // show summary of results
	    if ( numFailures == 0 ) {
		fLabel.setForeground(Color.black);
		fLabel.setText("Constraints ok.");
	    } else {
		fLabel.setForeground(Color.red);
		fLabel.setText(numFailures + " constraint" +
			       (( numFailures > 1 ) ? "s" : "") + 
			       " failed.");
	    }
	}
	fMyTableModel.fireTableDataChanged();
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
