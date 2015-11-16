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

package org.tzi.use.gui.main;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.parser.USECompiler;
import org.tzi.use.gui.util.TextComponentWriter;
import org.tzi.use.gui.util.CloseOnEscapeKeyListener;
import org.tzi.use.config.Options;
import org.tzi.use.util.StringUtil;
import org.tzi.use.util.TeeWriter;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/** 
 * A dialog for entering and evaluating OCL expressions.
 * 
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class EvalOCLDialog extends JDialog {
    private MSystem fSystem;
    private JTextArea fTextIn;
    private JTextArea fTextOut;

    EvalOCLDialog(MSystem system, JFrame parent) {
	super(parent, "Evaluate OCL expression");
	fSystem = system;

	setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	// create text components and labels
	fTextIn = new JTextArea();
	JLabel textInLabel = new JLabel("Enter OCL expression:");
	textInLabel.setDisplayedMnemonic('O');
	textInLabel.setLabelFor(fTextIn);
	fTextOut = new JTextArea();
	fTextOut.setEditable(false);
	JLabel textOutLabel = new JLabel("Result:");
	textOutLabel.setLabelFor(fTextOut);

	// create panel on the left and add text components
	JPanel textPane = new JPanel();
	textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));

	JPanel p = new JPanel(new BorderLayout());
	p.add(textInLabel, BorderLayout.NORTH);
	p.add(new JScrollPane(fTextIn), BorderLayout.CENTER);
	textPane.add(p);
	textPane.add(Box.createRigidArea(new Dimension(0,5)));

	p = new JPanel(new BorderLayout());
	p.add(textOutLabel, BorderLayout.NORTH);
	p.add(new JScrollPane(fTextOut), BorderLayout.CENTER);
	textPane.add(p);
	textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	// create panel on the right containing buttons
	JPanel btnPane = new JPanel();
	JButton btnEval = new JButton("Evaluate");
	btnEval.setMnemonic('E');
	btnEval.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    evaluate(fTextIn.getText());
		}
	    });
	Dimension dim = btnEval.getMaximumSize();
	dim.width = Short.MAX_VALUE;
	btnEval.setMaximumSize(dim);
	JButton btnClear = new JButton("Clear Result");
	btnClear.setMnemonic('C');
	btnClear.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    fTextOut.setText(null);
		}
	    });
	JButton btnClose = new JButton("Close");
	btnClose.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    closeDialog();
		}
	    });
	dim = btnClose.getMaximumSize();
	dim.width = Short.MAX_VALUE;
	btnClose.setMaximumSize(dim);

	btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.Y_AXIS));
	btnPane.add(Box.createVerticalGlue());
	btnPane.add(btnEval);
	btnPane.add(Box.createRigidArea(new Dimension(0,5)));
	btnPane.add(btnClear);
	btnPane.add(Box.createRigidArea(new Dimension(0,5)));
	btnPane.add(btnClose);
	btnPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	JComponent contentPane = (JComponent) getContentPane();
	contentPane.add(textPane, BorderLayout.CENTER);
	contentPane.add(btnPane, BorderLayout.EAST);
	getRootPane().setDefaultButton(btnEval);

	pack();
	setSize(new Dimension(500, 200));
	setLocationRelativeTo(parent);
	fTextIn.requestFocus();
	
	// allow dialog close on escape key
	CloseOnEscapeKeyListener ekl = new CloseOnEscapeKeyListener(this);
	addKeyListener(ekl);
	fTextIn.addKeyListener(ekl);
	fTextOut.addKeyListener(ekl);
    }

    private void closeDialog() {
	setVisible(false);
	dispose();
    }

    private void evaluate(String in) {
	// clear previous results
	fTextOut.setText(null);

	// send error output to result window and msg stream
	StringWriter msgWriter = new StringWriter();
	PrintWriter out = new PrintWriter(
		  new TeeWriter(new TextComponentWriter(fTextOut),
				msgWriter), true);

	// compile query
	Expression expr = 
	    USECompiler.compileExpression(fSystem.model(),
					  new StringReader(in), 
					  "Error",
					  out,
					  fSystem.topLevelBindings());
	out.flush();
	fTextIn.requestFocus();

	// compile errors?
	if ( expr == null ) {
	    // try to parse error message and set caret to error position
	    String msg = msgWriter.toString();
	    //System.err.println("msg = " + msg);
	    //System.err.println("first : " + msg.indexOf(':'));
	    int colon1 = msg.indexOf(':');
	    if ( colon1 != -1 ) {
		int colon2 = msg.indexOf(':', colon1 + 1);
		int colon3 = msg.indexOf(':', colon2 + 1);
		// System.err.println("index = " + colon2 + " " + colon3);

		try {
		    int line = Integer.parseInt(msg.substring(colon1 + 1, colon2));
		    int column = Integer.parseInt(msg.substring(colon2 + 1, colon3));
		    int caret = 1 + StringUtil.nthIndexOf(in, line - 1, 
							  Options.LINE_SEPARATOR);
		    caret += column;
		    //System.err.println("line = " + line + ", column " + column + 
		    //", caret = " + caret);
		    
		    // sanity check
		    caret = Math.min(caret, fTextIn.getText().length());
		    fTextIn.setCaretPosition(caret);
		} catch (NumberFormatException ex) { }
	    }
	    return;
	}

	// evaluate it with current system state
	Evaluator evaluator = new Evaluator();
	Value val = evaluator.eval(expr, fSystem.state(), 
				   fSystem.topLevelBindings());

	// print result
	fTextOut.setText(val.toStringWithType());
    }
}
