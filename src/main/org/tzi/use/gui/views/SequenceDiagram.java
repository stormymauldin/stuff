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
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MCmd;
import org.tzi.use.uml.sys.MCmdOpEnter;
import org.tzi.use.uml.sys.MCmdOpExit;
import org.tzi.use.uml.sys.MOperationCall;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Properties;

/** 
 * A SequenceDiagram shows a UML sequence diagramm of events.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class SequenceDiagram extends JPanel implements Printable {
    private MSystem fSystem;
    private Font fFont;
    private int fNumSteps;	// number of time steps
    private Map fLifelines;	// (MObject -> Lifeline)
    private JPopupMenu fPopupMenu; // context menu on right mouse click
    private boolean fDoAntiAliasing = true;
    private boolean fShowValues = true;	// args and return values?
    private boolean fCompactDisplay = false; // omit steps not related to opcalls?

    private static final int Y_START = 60; // start of lifelines
    private static final int Y_STEP = 25; // step for time axis
    private static final int X_STEP = 140; // distance between lifelines
    private static final int FRAME_OFFSET = 4; // offset for nested activation frames

    private static final float dash1[] = { 5.0f };
    private static final BasicStroke dashedStroke = 
	new BasicStroke(1.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			5.0f, dash1, 0.0f);

    public SequenceDiagram(MSystem system) {
	fSystem = system;

	setBorder(BorderFactory.createEmptyBorder());
	fFont = Font.getFont("use.gui.view.sequencediagram", getFont());
	setBackground(Color.white);
	setLayout(null);
	setMinimumSize(new Dimension(50, 50));
	setPreferredSize(new Dimension(200, 100));

	// create the popup menu for options
        fPopupMenu = new JPopupMenu();
        final JCheckBoxMenuItem cbAntiAliasing = 
	    new JCheckBoxMenuItem("Anti-aliasing");
	cbAntiAliasing.setState(fDoAntiAliasing);
	cbAntiAliasing.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fDoAntiAliasing = ev.getStateChange() == ItemEvent.SELECTED;
		    repaint();
                }
	    });
	fPopupMenu.add(cbAntiAliasing);

        final JCheckBoxMenuItem cbShowValues = 
	    new JCheckBoxMenuItem("Show values");
	cbShowValues.setState(fShowValues);
	cbShowValues.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fShowValues = ev.getStateChange() == ItemEvent.SELECTED;
		    repaint();
                }
	    });
	fPopupMenu.add(cbShowValues);

        final JCheckBoxMenuItem cbCompactDisplay = 
	    new JCheckBoxMenuItem("Compact display");
	cbCompactDisplay.setState(fCompactDisplay);
	cbCompactDisplay.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fCompactDisplay = ev.getStateChange() == ItemEvent.SELECTED;
		    update();
		    repaint();
                }
	    });
	fPopupMenu.add(cbCompactDisplay);
	addMouseListener(new PopupListener(fPopupMenu));
    }

    /**
     * Draws the panel.
     */
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Font oldFont = g.getFont();
	g.setFont(fFont);

	drawDiagram((Graphics2D) g);

	// restore font
	g.setFont(oldFont);
    }


    /**
     * Draws the diagram.
     */
    public void drawDiagram(Graphics2D g) {
	if ( fDoAntiAliasing )
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			       RenderingHints.VALUE_ANTIALIAS_ON);

	FontMetrics fm = g.getFontMetrics();

//  	Log.trace(this, getBounds().toString());
//  	Log.trace(this, getInsets().toString());
	
	// respect borders
	Insets insets = getInsets();
	Rectangle r = getBounds();
	r.x += insets.left;
	r.y += insets.top;
	r.width -= insets.left + insets.right;
	r.height -= insets.top + insets.bottom;

	// System.out.println("paintComponent" + count++);
	int h = r.height - 10;
	int w = r.width - 10;

    	g.setColor(Color.black);
	// g.drawString("foobar", r.x, r.y + 10);

	int x = r.x + 20;
	int y = r.y + 20;

	/* draw actor */

	// head
	g.drawOval(x - 5, y - 10, 10, 10);
	// body
	g.drawLine(x, y, x, y + 20);
	// arms
	g.drawLine(x - 10, y + 10, x + 10, y + 10);
	// legs
	g.drawLine(x, y + 20, x - 10, y + 30);
	g.drawLine(x, y + 20, x + 10, y + 30);

	Iterator lifelineIter = fLifelines.values().iterator();
	while ( lifelineIter.hasNext() ) {
	    Lifeline ll = (Lifeline) lifelineIter.next();
	    ll.draw(g, fm);
	}

	// lifeline
	g.setColor(Color.white);
	g.fillRect(x - 5, Y_START, 10, fNumSteps * Y_STEP);
	g.setColor(Color.black);
	g.drawRect(x - 5, Y_START, 10, fNumSteps * Y_STEP);
    }

    /**
     * Each column in the sequence diagram is represented by a
     * lifeline.  
     */
    private class Lifeline {
	private int fColumn;
	private MObject fObj;
	private List fActivations;
	private int fActivationNesting;

	Lifeline(int col, MObject obj) {
	    fColumn = col;
	    fObj = obj;
	    fActivations = new ArrayList();
	}

	int column() {
	    return fColumn;
	}

	void enterActivation(Activation a) {
	    fActivations.add(a);
	    a.setNesting(fActivationNesting++);
	}

	void exitActivation() {
	    fActivationNesting--;
	}

	void draw(Graphics2D g, FontMetrics fm) {
	    int x = 20 + fColumn * X_STEP;
	    int y = Y_START - 30;
	    g.setColor(Color.black);

	    // draw object name
	    String label = fObj.name() + ":" + fObj.cls().name();
	    int labelWidth = fm.stringWidth(label);
	    int boxWidth = labelWidth + 10;
	    g.drawString(label, x - labelWidth / 2, y + 15);
	    // underline object name
	    g.drawLine(x - labelWidth / 2, y + 17, x + labelWidth / 2, y + 17);
	    // draw object box
	    g.drawRect(x - boxWidth / 2, y, boxWidth, 20);

	    // draw dashed lifeline
	    Stroke oldStroke = g.getStroke();
	    g.setStroke(dashedStroke);
	    g.drawLine(x, Y_START - 10, x, Y_START + fNumSteps * Y_STEP);
	    g.setStroke(oldStroke);
	    
	    // draw activations
	    Iterator activationIter = fActivations.iterator();
	    while ( activationIter.hasNext() ) {
		Activation a = (Activation) activationIter.next();
		a.drawFrame(g, x);
	    }

	    // draw message sends
	    activationIter = fActivations.iterator();
	    while ( activationIter.hasNext() ) {
		Activation a = (Activation) activationIter.next();
		a.drawMessageSend(g, fm, x);
	    }
	}
    }

    /**
     * An activation frame on a lifeline.
     */
    private class Activation {
	private Lifeline fOwner;
	private MOperationCall fOpCall;
	private int fStart;
	private int fEnd;
	private Activation fSrc;
	private int fNesting;

	Activation(int start, Lifeline owner, 
		   MOperationCall opcall, Activation src) {
	    fStart = start;
	    fOwner = owner;
	    fOpCall = opcall;
	    fSrc = src;
	}

	void setEnd(int end) {
	    fEnd = end;
	    fOwner.exitActivation();
	}

	void setNesting(int n) {
	    fNesting = n;
	}

	Lifeline owner() {
	    return fOwner;
	}

	void drawMessageSend(Graphics2D g, FontMetrics fm, int x) {
	    g.setColor(Color.black);
	    boolean isRecursive = fSrc != null && fSrc.owner() == fOwner;
	    int x1, x2, xd;
	    int y0 = Y_START + fStart * Y_STEP;
	    int x0 = 20;
	    // invoked by other object
	    if ( fSrc != null )
		x0 += fSrc.owner().column() * X_STEP;

	    if ( x0 < x ) {
		x0 += 5;
		if ( fSrc != null )
		    x0 += fSrc.fNesting * FRAME_OFFSET;
		x = x - 5 + fNesting * FRAME_OFFSET;
		x1 = x0;
		x2 = x;
	    } else if ( x0 > x ) {
		x0 -= 5;
		if ( fSrc != null )
		    x0 += fSrc.fNesting * FRAME_OFFSET;
		x = x + 5 + fNesting * FRAME_OFFSET;
		x1 = x;
		x2 = x0;
	    } else {
		x0 = x0 + 5 + fSrc.fNesting * FRAME_OFFSET;
		x = x + 5 + fNesting * FRAME_OFFSET;
		x1 = x0;
		x2 = x;
	    }

	    // draw message line
	    // recursive edge?
	    if ( isRecursive ) {
		g.drawArc(x2 - 40, y0 - Y_STEP / 3, 80, Y_STEP / 3, -90, 180);
		xd = +10;
	    } else {
		g.drawLine(x1, y0, x2, y0);
		xd = ( x0 <= x ) ? -10 : +10;
	    }
	    // arrow head
	    int[] xp = { x, x + xd, x + xd };
	    int[] yp = {y0, y0 - 4, y0 + 4 };
	    g.fillPolygon(xp, yp, xp.length);

	    // draw message name
	    String msgLabel = fOpCall.operation().name();
	    if ( fShowValues )
		msgLabel += "(" + StringUtil.fmtSeq(fOpCall.argValues(), ",") + ")";
	    if ( isRecursive ) 
		g.drawString(msgLabel, x1 + 15, y0 - Y_STEP / 3 - 2);
	    else {
		int labelWidth = fm.stringWidth(msgLabel);
		g.drawString(msgLabel, x1 + (x2 - x1 - labelWidth) / 2, y0 - 2);
	    }

	    // a return may be missing because the operation may still
	    // be active during animation
	    if ( fEnd != 0 ) {
		// red lines for failing postconditions
		if ( ! fOpCall.postconditionsOkOnExit() )
		    g.setColor(Color.red);
		
		y0 = Y_START + fEnd * Y_STEP;
		String resultLabel = null;
		if ( fShowValues ) {
		    Value result = fOpCall.resultValue();
		    if ( result != null )
			resultLabel = result.toString();
		}
		Stroke oldStroke = g.getStroke();
		g.setStroke(dashedStroke);
		if ( isRecursive ) {
		    // recursive edge
		    g.drawArc(x2 - 40, y0, 80, Y_STEP / 3, -90, 180);
		    xd = +10;
		    g.setStroke(oldStroke);

		    // arrow head
		    int y = y0 + Y_STEP / 3;
		    g.drawLine(x0, y, x0 + xd, y - 3);
		    g.drawLine(x0, y, x0 + xd, y + 3);

		    // result value text
		    if ( resultLabel != null )
			g.drawString(resultLabel, x2 + 15, y0 - 2);
		} else {
		    // dashed line
		    g.drawLine(x1, y0, x2, y0);
		    xd = ( x0 <= x ) ? +10 : -10;
		    g.setStroke(oldStroke);

		    // arrow head
		    g.drawLine(x0, y0, x0 + xd, y0 - 3);
		    g.drawLine(x0, y0, x0 + xd, y0 + 3);

		    // result value text
		    if ( resultLabel != null ) {
			int labelWidth = fm.stringWidth(resultLabel);
			g.drawString(resultLabel, x1 + (x2 - x1 - labelWidth) / 2, 
				     y0 - 2);
		    }
		}
	    }
	}

	void drawFrame(Graphics2D g, int x) {
	    int end = fEnd;
	    if ( end == 0 )	// frame still active
		end = fNumSteps;
	    x += fNesting * FRAME_OFFSET; // nested activation
	    g.setColor(Color.white);
	    g.fillRect(x - 5, Y_START + fStart * Y_STEP, 10, (end - fStart) * Y_STEP);
	    g.setColor(Color.black);
	    g.drawRect(x - 5, Y_START + fStart * Y_STEP, 10, (end - fStart) * Y_STEP);
	}
    }

    void update() {
	fLifelines = new HashMap();
	int column = 1;
	int eventStep = 1;
	Stack activationStack = new Stack();
	List cmds = fSystem.commands();
	Iterator cmdIter = cmds.iterator();
	while ( cmdIter.hasNext() ) {
	    MCmd cmd = (MCmd) cmdIter.next();
	    if ( cmd instanceof MCmdOpEnter) {
		MCmdOpEnter openter = (MCmdOpEnter) cmd;
		MOperationCall opcall = openter.operationCall();
		MObject obj = opcall.targetObject();

		Lifeline ll = (Lifeline) fLifelines.get(obj);
		// need new lifeline?
		if ( ll == null ) {
		    ll = new Lifeline(column++, obj);
		    fLifelines.put(obj, ll);
		}
		
		// get source of operation call
		Activation srcAct = null;
		if ( ! activationStack.empty() )
		    srcAct = (Activation) activationStack.peek();

		// add new activation and push on stack
		Activation a = new Activation(eventStep, ll, opcall, srcAct);
		ll.enterActivation(a);
		activationStack.push(a);
		eventStep++;
	    } else if ( cmd instanceof MCmdOpExit) {
		// finish current activation
		Activation a = (Activation) activationStack.pop();
		a.setEnd(eventStep);
		eventStep++;
	    } else if ( ! fCompactDisplay )
		eventStep++;
	}
	fNumSteps = eventStep + 1;
	setPreferredSize(new Dimension(20 + fLifelines.size() * X_STEP + X_STEP / 2,
				       Y_START + fNumSteps * Y_STEP + 10));
	revalidate();
	repaint();
    }

    /**
     * Prints the diagram. Implementation of Printable interface.
     */
    public int print(Graphics g, PageFormat pf, int pi)
	throws PrinterException
    {
        if ( pi >= 1 )
            return Printable.NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        g2d.translate(pf.getImageableWidth() / 2, pf.getImageableHeight() / 2);
        
        Dimension d = getSize();
        double scale = Math.min(pf.getImageableWidth() / d.width,
                                pf.getImageableHeight() / d.height);
	// fit to page
        if ( scale < 1.0 )
            g2d.scale(scale, scale);
        g2d.translate(-d.width / 2.0, -d.height / 2.0);

	Font f = Font.getFont("use.gui.print.diagramFont", getFont());
	g2d.setFont(f);

	drawDiagram(g2d);

        return Printable.PAGE_EXISTS;
    }



    void printDiagram(PageFormat pf) {
        PrinterJob job = PrinterJob.getPrinterJob();
  	job.setJobName("Sequence diagram");
	job.setPrintable(this, pf);
	
	if ( job.printDialog() ) {
	    // Print the job if the user didn't cancel printing
	    try { 
		job.print(); 
	    } catch (Exception ex) { 
		JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
					      JOptionPane.ERROR_MESSAGE);
	    }
	}
    }
}
