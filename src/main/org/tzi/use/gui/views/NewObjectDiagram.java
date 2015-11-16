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
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.MLink;
import org.tzi.use.uml.sys.MLinkEnd;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.graph.*;
import org.tzi.use.graph.layout.SpringLayout;
import org.tzi.use.graph.layout.PlaceableNode;
import org.tzi.use.gui.util.Selection;
import org.tzi.use.gui.util.Selectable;
import org.tzi.use.gui.util.ExtFileFilter;
import org.tzi.use.util.Log;
import org.tzi.use.util.FilterIterator;
import org.tzi.use.util.UnaryPredicate;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.print.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;

/** 
 * A panel drawing UML object diagrams.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

class NewObjectDiagram extends JPanel 
    implements MouseListener, MouseMotionListener, 
	       DropTargetListener,
	       Printable 
{
    private boolean fDoAntiAliasing = false;

    // these are set in DnD code to preset the position of a new node
    private double fNextNodeX = 200;
    private double fNextNodeY = 200;

    // color settings
    private static final Color OBJECTNODE_COLOR = new Color(0xe0, 0xe0, 0xe0);
    private static final Color OBJECTNODE_SELECTED_COLOR = Color.orange;
    private static final Color OBJECTNODE_FRAME_COLOR = Color.black;
    private static final Color OBJECTNODE_LABEL_COLOR = Color.black;

    private static final Color DIAMONDNODE_COLOR = Color.white;
    private static final Color DIAMONDNODE_FRAME_COLOR = Color.black;
    private static final Color DIAMONDNODE_LABEL_COLOR = Color.black;

    private static final Color EDGE_COLOR = Color.red;
    private static final Color EDGE_LABEL_COLOR = Color.darkGray;


    /** 
     * Base class of all node types in the diagram.
     */
    abstract class NodeBase implements PlaceableNode {
	abstract void draw(Graphics g, FontMetrics fm);
	/**
	 * Returns a unique identifier of this node. It is used for
	 * storing/loading of position information to/from a
	 * properties file. 
	 */
	abstract String ident();
    }


    /** 
     * A node representing an object.
     */
    class ObjectNode extends NodeBase implements Selectable {
	private double fX = fNextNodeX;
	private double fY = fNextNodeY;
	private int fHeight;
	private int fWidth;
	private boolean fIsSelected;
	private MObject fObject;
	private String fLabel;
	private MAttribute[] fAttributes;
	private String[] fValues;

	ObjectNode(MObject obj) {
	    fObject = obj;
	    MClass cls = obj.cls();
	    fLabel = obj.name() + ":" + cls.name();
	    Set allAttributes = cls.allAttributes();
	    final int N = allAttributes.size();
	    fAttributes = new MAttribute[N];
	    fValues = new String[N];
	    System.arraycopy(allAttributes.toArray(), 0, 
			     fAttributes, 0, N);
	    Arrays.sort(fAttributes);
	}

	MObject object() {
	    return fObject;
	}

	/**
	 * Checks whether the box occupies a given point.
	 */
	boolean occupies(int x, int y) {
	    int x1 = (int) fX;
	    int y1 = (int) fY;
	    Rectangle r = new Rectangle(x1 - fWidth / 2, y1 - fHeight / 2, 
					fWidth, fHeight);
	    return r.contains(x, y);
	}

	public void setSelected(boolean on) {
	    fIsSelected = on;
	}

	/**
	 * Draws a box with an underlined label.
	 */
	public void draw(Graphics g, FontMetrics fm) {
	    int x = (int) fX;
	    int y = (int) fY;

	    int labelWidth = fm.stringWidth(fLabel);
	    fWidth = labelWidth;
	    fHeight = fm.getHeight();
	    if ( fShowAttrValues ) {
		MObjectState objState = fObject.state(fParent.system().state());
		for (int i = 0; i < fAttributes.length; i++) {
		    fValues[i] = fAttributes[i].name() + "=" +
			((Value) objState.attributeValue(fAttributes[i])).toString();
		    fWidth = Math.max(fWidth, fm.stringWidth(fValues[i]));
		}
		fHeight = fHeight * (fAttributes.length + 1) + 3;
	    }

	    fWidth += 10;
	    fHeight += 4;
	    if ( fIsSelected )
		g.setColor(OBJECTNODE_SELECTED_COLOR);
	    else
		g.setColor(OBJECTNODE_COLOR);
	    g.fillRect(x - fWidth / 2, y - fHeight / 2, fWidth, fHeight);
	    g.setColor(OBJECTNODE_FRAME_COLOR);
	    g.drawRect(x - fWidth / 2, y - fHeight / 2, fWidth - 1, fHeight - 1);

	    x -= labelWidth/2;
	    y = y - (fHeight/2) + fm.getAscent() + 2;
	    g.setColor(OBJECTNODE_LABEL_COLOR);
	    g.drawString(fLabel, x, y);
	    // underline label
	    g.drawLine(x, y + 1, x + labelWidth, y + 1);

	    if ( fShowAttrValues ) {
		// compartment divider
		x = (int) fX;
		g.drawLine(x - fWidth / 2, y + 3, x + fWidth / 2 - 1, y + 3);
		x -= (fWidth-10)/2;
		y += 3;
		for (int i = 0; i < fAttributes.length; i++) {
		    y += fm.getHeight();
		    g.drawString(fValues[i], x, y);
		}
	    }
	}

	// PlaceableNode interface
	public double x() { return fX; }
	public double y() { return fY; }
	public void setPosition(double x, double y) { 
	    fX = x;
	    fY = y;
	}
	String ident() {
	    return "Object." + fObject.name();
	}
    }

    /** 
     * A pseude-node representing a diamond in an n-ary association.
     */
    class DiamondNode extends NodeBase {
	private double fX = fNextNodeX;
	private double fY = fNextNodeY;
	private MAssociation fAssoc;
	private String fLabel;

	DiamondNode(MAssociation assoc) {
	    fAssoc = assoc;
	    fLabel = assoc.name();
	}

	MAssociation association() {
	    return fAssoc;
	}

	/**
	 * Draws a diamond with an underlined label.
	 */
	public void draw(Graphics g, FontMetrics fm) {
	    int x = (int) fX;
	    int y = (int) fY;
	    int[] xpoints = {      x, x + 20,      x, x - 20};
	    int[] ypoints = { y - 10,      y, y + 10,      y};
	    g.setColor(DIAMONDNODE_COLOR);
	    g.fillPolygon(xpoints, ypoints, xpoints.length);
	    g.setColor(DIAMONDNODE_FRAME_COLOR);
	    g.drawPolygon(xpoints, ypoints, xpoints.length);

	    int w = fm.stringWidth(fLabel);
	    x = x - w / 2;
	    g.setColor(EDGE_LABEL_COLOR);
	    g.drawString(fLabel, x, y - 14);
	    g.drawLine(x, y - 12, x + w, y - 12);
	}

	// PlaceableNode interface
	public double x() { return fX; }
	public double y() { return fY; }
	public void setPosition(double x, double y) { 
	    fX = x;
	    fY = y;
	}
	String ident() {
	    return "Association." + fAssoc.name();
	}
    }

    /** 
     * Base class of all edge types in the diagram.
     */
    abstract class EdgeBase extends DirectedEdgeBase {
	EdgeBase(Object source, Object target) {
	    super(source, target);
	}
	abstract void draw(Graphics g, FontMetrics fm);
    }


    /** 
     * An edge representing a binary link.
     */
    class BinaryLinkEdge extends EdgeBase {
	private String fLabel;
	private String fSourceLabel;
	private String fTargetLabel;

	public BinaryLinkEdge(String label, Object source, Object target,
			      String sourceLabel, String targetLabel) {
	    super(source, target);
	    fLabel = label;
	    fSourceLabel = sourceLabel;
	    fTargetLabel = targetLabel;
	}

	/**
	 * Draws a straightline edge between source and target node.
	 */
	public void draw(Graphics g, FontMetrics fm) {
	    ObjectNode n1 = (ObjectNode) source();
	    ObjectNode n2 = (ObjectNode) target();
	    int x1 = (int) n1.x();
	    int y1 = (int) n1.y();
	    int x2 = (int) n2.x();
	    int y2 = (int) n2.y();
	    g.setColor(EDGE_COLOR);
	    if ( n1.equals(n2) ) {
		// reflexive edge, draw as a rectangle
		int w = fm.stringWidth(fLabel);
		int ew = Math.max(30, w);
		g.drawRect(x1 + 10, y1 - 30, ew, 30);

		g.setColor(EDGE_LABEL_COLOR);
		int x = x1 + 10 + ew / 2 - w / 2;
		int y = y1 - 34;
		g.drawString(fLabel, x, y);

		// underline association name
		g.drawLine(x, y + 1, x + w, y + 1);

		if ( fShowRolenames ) {
		    w = fm.stringWidth(fSourceLabel);
		    g.drawString(fSourceLabel, x1 + 10 - w,  y1 - 15);
		    g.drawString(fTargetLabel, x1 + 10 + ew + 2,  y1 - 15);
		}
	    } else {
		g.drawLine(x1, y1, x2, y2);
		g.setColor(EDGE_LABEL_COLOR);
		
		// draw association name
		int w = fm.stringWidth(fLabel);
		int x = x1 + (x2-x1) / 2 - w / 2;
		int y = y1 + (y2-y1) / 2 - 4;
		g.drawString(fLabel, x, y);

		// underline association name
		g.drawLine(x, y + 1, x + w, y + 1);

		if ( fShowRolenames ) {
		    // simple approximation of role name placement
		    if ( y2 > y1 ) {
			y1 += 30;
			y2 -= 20;
		    } else {
			y1 -= 20;
			y2 += 30;
		    }
		    if ( x2 < x1 ) {
			x1 -= fm.stringWidth(fSourceLabel);
			x2 -= fm.stringWidth(fTargetLabel);
		    }
		    g.drawString(fSourceLabel, x1,  y1);
		    g.drawString(fTargetLabel, x2,  y2);
		}
	    }
	}
    }

    /** 
     * An edge being part of link. This edge connects an ObjectNode
     * with a DiamondNode in case of an nary link, or an ObjectNode
     * with a PseudoNode in case of an association class.
     */
    class HalfLinkEdge extends EdgeBase {
	private String fTargetLabel;

	/**
	 * Constructs a new edge. Source is a pseude-node, target is
	 * an object node. 
	 */
	public HalfLinkEdge(NodeBase source, NodeBase target, 
			    String targetLabel) {
	    super(source, target);
	    fTargetLabel = targetLabel;
	}

	/**
	 * Draws a straightline edge between source and target node.
	 */
	public void draw(Graphics g, FontMetrics fm) {
	    NodeBase n1 = (NodeBase) source();
	    NodeBase n2 = (NodeBase) target();
	    int x1 = (int) n1.x();
	    int y1 = (int) n1.y();
	    int x2 = (int) n2.x();
	    int y2 = (int) n2.y();
	    g.setColor(EDGE_COLOR);
	    g.drawLine(x1, y1, x2, y2);
	    if ( fShowRolenames ) {
		// simple approximation of role name placement
		if ( y2 > y1 ) {
		    y2 -= 20;
		} else {
		    y2 += 30;
		}
		if ( x2 < x1 ) {
		    x2 -= fm.stringWidth(fTargetLabel);
		}
		g.setColor(EDGE_LABEL_COLOR);
		g.drawString(fTargetLabel, x2,  y2);
	    }
	}
    }

    private DirectedGraph fGraph; // the graph of the object diagram
    private Map fObjectToNodeMap; // (MObject -> ObjectNode)
    private Map fBinaryLinkToEdgeMap; // (MLink -> BinaryLinkEdge)
    private Map fNaryLinkToDiamondNodeMap; // (MLink -> DiamondNode)
    private NewObjectDiagramView fParent;
    private LayoutThread fLayoutThread;
    private volatile SpringLayout fLayouter;
    private final Object fLock = new Object(); // for thread synchronization
    private DropTarget fDropTarget;
    private boolean fDoAutoLayout = true;
    private boolean fShowRolenames = false;
    private boolean fShowAttrValues = false;
    private Selection fSelection;
    private PrintWriter fLog;

    /**
     * Creates a new empty diagram.
     */
    NewObjectDiagram(NewObjectDiagramView parent, PrintWriter log) {
	fGraph = new DirectedGraphBase();
	fObjectToNodeMap = new HashMap();
	fBinaryLinkToEdgeMap = new HashMap();
	fNaryLinkToDiamondNodeMap = new HashMap();
	fParent = parent;
	fDropTarget = new DropTarget(this, this);
	fSelection = new Selection();
	fLog = log;
	
	setLayout(null);
	setBackground(Color.white);
	setPreferredSize(new Dimension(400,400));

	addMouseListener(this);
	addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    // need a new layouter to adapt to new window size
		    fLayouter = null;
		}
	    });

	startLayoutThread();
    }

    /**
     * Draws objects and links.
     */
    public synchronized void drawDiagram(Graphics g) {
	if ( fDoAntiAliasing )
	    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					      RenderingHints.VALUE_ANTIALIAS_ON);

	Dimension d = getSize();
	g.setColor(getBackground());
	g.fillRect(0, 0, d.width, d.height);
	
	// draw frame
//  	g.setColor(Color.black);
//  	g.drawRect(0, 0, d.width, d.height);

	// draw edges first
	FontMetrics fm = g.getFontMetrics();
	Iterator edgeIterator = fGraph.edgeIterator();
	while ( edgeIterator.hasNext() ) {
	    EdgeBase e = (EdgeBase) edgeIterator.next();
	    e.draw(g, fm);
	}
	
	// draw nodes
	Iterator nodeIterator = fGraph.iterator();
	while ( nodeIterator.hasNext() ) {
	    NodeBase n = (NodeBase) nodeIterator.next();
	    n.draw(g, fm);
	}
    }

    /**
     * Draws the diagram.
     */
    public void paintComponent(Graphics g) {
	synchronized ( fLock ) {
	    Font f = Font.getFont("use.gui.view.objectdiagram", getFont());
	    g.setFont(f);
	    drawDiagram(g);
	}
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
  	job.setJobName("Object diagram");
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

    /**
     * Adds an object to the diagram.
     */
    void addObject(MObject obj) {
	// Find a random new position. getWidth and getheight return 0
	// if we are called on a new diagram.
	fNextNodeX = Math.random() * Math.max(100, getWidth());
	fNextNodeY = Math.random() * Math.max(100, getHeight());
//  	System.err.println("*** fNextNodeX = " + fNextNodeX + 
//  			   ", fNextNodeY = " + fNextNodeY);
	ObjectNode n = new ObjectNode(obj);
	synchronized ( fLock ) {
	    fGraph.add(n);
	    fObjectToNodeMap.put(obj, n);
	    fLayouter = null;
	}
    }

    /**
     * Deletes an object from the diagram.
     */
    void deleteObject(MObject obj) {
	ObjectNode n = (ObjectNode) fObjectToNodeMap.get(obj);
	if ( n ==  null )
	    throw new RuntimeException("no node for object `" + obj + 
				       "' in current state.");
	synchronized ( fLock ) {
	    fGraph.remove(n);
	    fObjectToNodeMap.remove(obj);
	    fLayouter = null;
	}
    }

    /**
     * Adds a link to the diagram.
     */
    void addLink(MLink link) {
	String label = link.association().name();
	if ( link.linkEnds().size() == 2 ) {
	    // binary link
	    Iterator linkEndIter = link.linkEnds().iterator();
	    MLinkEnd linkEnd1 = (MLinkEnd) linkEndIter.next();
	    MLinkEnd linkEnd2 = (MLinkEnd) linkEndIter.next();
	    MObject obj1 = linkEnd1.object();
	    MObject obj2 = linkEnd2.object();
	    BinaryLinkEdge e = 
		new BinaryLinkEdge(label,
				   fObjectToNodeMap.get(obj1), 
				   fObjectToNodeMap.get(obj2),
				   linkEnd1.associationEnd().name(), 
				   linkEnd2.associationEnd().name());
	    synchronized ( fLock ) {
		fGraph.addEdge(e);
		fBinaryLinkToEdgeMap.put(link, e);
		fLayouter = null;
	    }
	} else synchronized ( fLock ) {
	    // n-ary link: create a diamond node and n edges to objects
	    DiamondNode node = new DiamondNode(link.association());
	    fGraph.add(node);
	    fNaryLinkToDiamondNodeMap.put(link, node);
	    Iterator linkEndIter = link.linkEnds().iterator();
	    while ( linkEndIter.hasNext() ) {
		MLinkEnd linkEnd = (MLinkEnd) linkEndIter.next();
		MObject obj = linkEnd.object();
		HalfLinkEdge e = 
		    new HalfLinkEdge(node,
				     (NodeBase) fObjectToNodeMap.get(obj), 
				     linkEnd.associationEnd().name());
		fGraph.addEdge(e);
	    }
	    fLayouter = null;
	}
    }

    /**
     * Removes a link from the diagram.
     */
    void deleteLink(MLink link) {
	if ( link.linkEnds().size() == 2 ) {
	    BinaryLinkEdge e = (BinaryLinkEdge) fBinaryLinkToEdgeMap.get(link);
	    if ( e ==  null )
		throw new RuntimeException("no edge for link `" + link + 
					   "' in current state.");
	    synchronized ( fLock ) {
		fGraph.removeEdge(e);
		fBinaryLinkToEdgeMap.remove(link);
		fLayouter = null;
	    }
	} else {
	    DiamondNode n = (DiamondNode) fNaryLinkToDiamondNodeMap.get(link);
	    if ( n ==  null )
		throw new RuntimeException("no diamond node for n-ary link `" + 
					   link + "' in current state.");
	    synchronized ( fLock ) {
		// all dangling HalfLinkEdges are removed by the graph
		fGraph.remove(n);
		fNaryLinkToDiamondNodeMap.remove(link);
		fLayouter = null;
	    }
	}
    }

    private void startLayoutThread() {
	fLayoutThread = new LayoutThread();
	fLayoutThread.start();
    }

    private void stopLayoutThread() {
	fLayoutThread = null;
    }

    /**
     * Finds node occupying the given position.
     *
     * @return null if no such node could be found.
     */
    private ObjectNode findNode(int x, int y) {
	ObjectNode res = null;
	// ignore pseudo-nodes
	Iterator iter = new FilterIterator(fGraph.iterator(),
	   new UnaryPredicate() {
		   public boolean isTrue(Object obj) {
		       return obj instanceof ObjectNode;
		   }});

	while ( iter.hasNext() ) {
	    ObjectNode n = (ObjectNode) iter.next();
	    if ( n.occupies(x, y) ) {
		// Do not break here. We search in the same order
		// which is used for drawing. There may be another
		// node drawn on top of this node. That node should be
		// picked.
		res = n;
	    }
	}
	return res;
    }

    class ActionInsertLink extends AbstractAction {
	private Set fObjects;
	private String fAssocName;

	ActionInsertLink(String text, Set objects, String assocName) {
	    super(text);
	    fObjects = objects;
	    fAssocName = assocName;
	}

	public void actionPerformed(ActionEvent e) {
	    fParent.insertLink(fAssocName, fObjects);
	}
    }

    /**
     * Deletes the selected objects.
     */
    class ActionDelete extends AbstractAction {
	private Set fObjects;

	ActionDelete(String text, Set objects) {
	    super(text);
	    fObjects = objects;
	}

	public void actionPerformed(ActionEvent e) {
	    fParent.deleteObjects(fObjects);
	}
    }

    /**
     * Saves the current layout to a file.
     */
    class ActionSaveLayout extends AbstractAction {
	private JFileChooser fChooser;

	ActionSaveLayout() {
	    super("Save layout...");
	}

	public void actionPerformed(ActionEvent e) {
	    String path;
	    // reuse chooser if possible
	    if ( fChooser == null ) {
		path = System.getProperty("user.dir");
		fChooser = new JFileChooser(path);
		ExtFileFilter filter = 
		    new ExtFileFilter("olt", "USE object diagram layout");
		fChooser.addChoosableFileFilter(filter);
		fChooser.setDialogTitle("Save layout");
	    }
	    int returnVal = fChooser.showSaveDialog(fParent); 
	    if ( returnVal != JFileChooser.APPROVE_OPTION )
		return;

	    path = fChooser.getCurrentDirectory().toString();
	    File f = new File(path, fChooser.getSelectedFile().getName()); 
	    Log.verbose("File " + f);

	    if ( f.exists() ) {
		int n = JOptionPane.showConfirmDialog(fParent,
		      "Overwrite existing file " + f + "?",
		      "Please confirm",
		      JOptionPane.YES_NO_CANCEL_OPTION);
		if ( n != JOptionPane.YES_OPTION )
		    return;
	    }

	    // store node positions in property object
	    Properties prop = new Properties();
	    Iterator nodeIterator = fGraph.iterator();
	    while ( nodeIterator.hasNext() ) {
		NodeBase n = (NodeBase) nodeIterator.next();
		String id = n.ident();
		prop.setProperty(id + ".x", Double.toString(n.x()));
		prop.setProperty(id + ".y", Double.toString(n.y()));
	    }

	    OutputStream out = null;
	    try {
		out = new BufferedOutputStream(new FileOutputStream(f));
		prop.store(out, "USE object diagram layout");
		fLog.println("Wrote layout file " + f);
	    } catch (IOException ex) {
		JOptionPane.showMessageDialog(fParent,
					      ex.getMessage(), "Error",
					      JOptionPane.ERROR_MESSAGE);
	    } finally {
		if ( out != null )
		    try { out.close(); } catch (IOException ex) {}
	    }
	}
    }

    /**
     * Loads the current layout from a file.
     */
    class ActionLoadLayout extends AbstractAction {
	private JFileChooser fChooser;
	ActionLoadLayout() {
	    super("Load layout...");
	}

	public void actionPerformed(ActionEvent e) {
	    String path;
	    // reuse chooser if possible
	    if ( fChooser == null ) {
		path = System.getProperty("user.dir");
		fChooser = new JFileChooser(path);
		ExtFileFilter filter = 
		    new ExtFileFilter("olt", "USE object diagram layout");
		fChooser.addChoosableFileFilter(filter);
		fChooser.setDialogTitle("Load layout");
	    }
	    int returnVal = fChooser.showOpenDialog(fParent); 
	    if ( returnVal != JFileChooser.APPROVE_OPTION )
		return;

	    path = fChooser.getCurrentDirectory().toString();
	    File f = new File(path, fChooser.getSelectedFile().getName()); 
	    Log.verbose("File " + f);

	    Properties prop = new Properties();
	    InputStream in = null;
	    try {
		in = new BufferedInputStream(new FileInputStream(f));
		fLog.println("Reading layout file " + f);
		prop.load(in);
		Iterator nodeIterator = fGraph.iterator();
		while ( nodeIterator.hasNext() ) {
		    NodeBase n = (NodeBase) nodeIterator.next();
		    String id = n.ident();
		    String p1 = prop.getProperty(id + ".x");
		    String p2 = prop.getProperty(id + ".y");
		    if ( p1 == null || p2 == null )
			fLog.println("Warning: no layout information found for node `" +
				     id + "'.");
		    else try {
			double x = Double.parseDouble(p1);
			double y = Double.parseDouble(p2);
			n.setPosition(x, y);
		    } catch (NumberFormatException ex) {
			fLog.println("Warning: ignoring invalid entry for node `" +
				     id + "'.");
		    }
		}
		repaint();
	    } catch (IOException ex) {
		JOptionPane.showMessageDialog(fParent,
					      ex.getMessage(), "Error",
					      JOptionPane.ERROR_MESSAGE);
	    } finally {
		if ( in != null )
		    try { in.close(); } catch (IOException ex) {}
	    }
	}
    }

    private ActionLoadLayout fActionLoadLayout = new ActionLoadLayout();
    private ActionSaveLayout fActionSaveLayout = new ActionSaveLayout();

    /**
     * Creates and shows popup menu if mouse event is the trigger for
     * popups.  
     */
    private boolean maybeShowPopup(MouseEvent e) {
	if ( ! e.isPopupTrigger() ) 
	    return false;

	// create the popup menu
	JPopupMenu popupMenu; // context menu on right mouse click
        popupMenu = new JPopupMenu();

	// get all associations that exist between the classes of the
	// selected objects
	if ( ! fSelection.isEmpty() ) {
	    Set selectedObjects = new HashSet();
	    Set classesOfSelectedObjects = new HashSet();
	    Iterator nodeIterator = fSelection.iterator();
	    while ( nodeIterator.hasNext() ) {
		ObjectNode node = (ObjectNode) nodeIterator.next();
		selectedObjects.add(node.object());
		classesOfSelectedObjects.add(node.object().cls());
	    }
	    Set associations = 
		fParent.getAllAssociationsBetweenClasses(classesOfSelectedObjects);
	    Log.trace(this, "associations = " + associations);

	    // for each association, check if there already exists a
	    // link between the objects. Add "Delete link" menu item
	    // in this case, otherwise add an "Insert link" item.
	    Iterator assocIterator = associations.iterator();
	    boolean addedInsertLinkAction = false;
	    while ( assocIterator.hasNext() ) {
		MAssociation assoc = (MAssociation) assocIterator.next();
		JMenuItem mi;
		if ( ! fParent.hasLinkBetweenObjects(assoc, selectedObjects) ) {
		    popupMenu.add(new ActionInsertLink("Insert " + assoc.name() + 
						       " link",
						       selectedObjects,
						       assoc.name()));
		    addedInsertLinkAction = true;
		}
	    }
	    
	    if ( addedInsertLinkAction )
		popupMenu.addSeparator();

	    String txt;
	    if ( selectedObjects.size() == 1 ) 
		txt = ((MObject) selectedObjects.iterator().next()).name();
	    else
		txt = selectedObjects.size() + " objects";
	    popupMenu.add(new ActionDelete("Delete " + txt, selectedObjects));
	    popupMenu.addSeparator();
	}

        final JCheckBoxMenuItem cbAutoLayout = new JCheckBoxMenuItem("Auto-Layout");
	cbAutoLayout.setState(fDoAutoLayout);
	cbAutoLayout.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fDoAutoLayout = ev.getStateChange() == ItemEvent.SELECTED;
                }
	    });
        final JCheckBoxMenuItem cbRolenames = new JCheckBoxMenuItem("Show role names");
	cbRolenames.setState(fShowRolenames);
	cbRolenames.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fShowRolenames = ev.getStateChange() == ItemEvent.SELECTED;
		    repaint();
                }
	    });
        final JCheckBoxMenuItem cbAttrValues = 
	    new JCheckBoxMenuItem("Show attribute values");
	cbAttrValues.setState(fShowAttrValues);
	cbAttrValues.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fShowAttrValues = ev.getStateChange() == ItemEvent.SELECTED;
		    repaint();
                }
	    });
	popupMenu.add(cbAutoLayout);
	popupMenu.add(cbRolenames);
	popupMenu.add(cbAttrValues);
        final JCheckBoxMenuItem cbAntiAliasing = 
	    new JCheckBoxMenuItem("Anti-aliasing");
	cbAntiAliasing.setState(fDoAntiAliasing);
	cbAntiAliasing.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent ev) {
		    fDoAntiAliasing = ev.getStateChange() == ItemEvent.SELECTED;
		    repaint();
                }
	    });
	popupMenu.add(cbAntiAliasing);
	if ( fGraph.size() > 0 ) {
	    popupMenu.addSeparator();
	    popupMenu.add(fActionLoadLayout);
	    popupMenu.add(fActionSaveLayout);
	}
	popupMenu.show(e.getComponent(), e.getX(), e.getY());
 	return true;
    }

    private JWindow fObjectInfoWin = null;

    private void displayObjectInfo(MObject obj, MouseEvent e) {
	if ( fObjectInfoWin != null ) {
	    fObjectInfoWin.setVisible(false);
	    fObjectInfoWin.dispose();
	}
	    
	Box attrBox = Box.createVerticalBox();
	Box valueBox = Box.createVerticalBox();

	MObjectState objState = obj.state(fParent.system().state());
	Map attributeValueMap = objState.attributeValueMap();
	Iterator it = attributeValueMap.entrySet().iterator();
	while ( it.hasNext() ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    MAttribute attr = (MAttribute) entry.getKey();
	    Value v = (Value) entry.getValue();
	    attrBox.add(new JLabel(attr.name() + " = "));
	    valueBox.add(new JLabel(v.toString()));
	}

	fObjectInfoWin = new JWindow();
	JPanel contentPane = new JPanel();

	Border border = 
	    BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
					       BorderFactory.createLoweredBevelBorder());
	contentPane.setBorder(border);
	Box b = Box.createHorizontalBox();
	b.add(attrBox);
	b.add(valueBox);
	contentPane.add(b);
	fObjectInfoWin.setContentPane(contentPane);
	Point p = e.getPoint();
	SwingUtilities.convertPointToScreen(p, (JComponent) e.getSource());
	
	fObjectInfoWin.setLocation(p);//e.getPoint());
	fObjectInfoWin.pack();
	fObjectInfoWin.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {}


    private static final int DRAG_NONE = 0; 
    private static final int DRAG_ITEMS = 1; // drag selected items
    private Point fDragStart;
    private int fDragMode;
    private boolean fIsDragging = false;
    private Cursor fCursor;

    /**
     * event: on item            not on item
     * -------------------------------------------
     *    b1: select item        clear selection
     *        deselect others 
     *  S-b1: extend selection   -
     */
    public void mousePressed(MouseEvent e) {
	if ( maybeShowPopup(e) )
	    return;

	addMouseMotionListener(this);
	stopLayoutThread();

	// mouse over node?
	ObjectNode pickedObjectNode = findNode(e.getX(), e.getY());
	int modifiers = e.getModifiers();
	switch ( modifiers ) {
	case InputEvent.BUTTON1_MASK:
	    if ( pickedObjectNode != null ) {
		// If this item is not currently selected, remove all
		// other items from the selection and only select this
		// item. If this item is currently selected, do
		// nothing (and also leave all other selected items
		// untouched). In any case this click may be used to
		// start dragging selected items.
		if ( ! fSelection.isSelected(pickedObjectNode) ) {
		    // clear selection
		    fSelection.clear();
		    // add this component as the only selected item
		    fSelection.add(pickedObjectNode);
		    repaint();
		}
		// subsequent dragging events will move selected items
  		fDragMode = DRAG_ITEMS;
  		fDragStart = e.getPoint();
	    } else {
		// click in background, clear selection
		if ( fSelection.clear() )
		    repaint();
	    }
	    break;
	case InputEvent.SHIFT_MASK + InputEvent.BUTTON1_MASK:
	    if ( pickedObjectNode != null ) {
		// add this component to the selection
		fSelection.add(pickedObjectNode);
		repaint();
	    }
	    break;
	case InputEvent.BUTTON2_MASK:
	    if ( fSelection.size() == 1 ) {
		ObjectNode node = (ObjectNode) fSelection.iterator().next();
		displayObjectInfo(node.object(), e);
	    }
	    break;
	}
    }

    public void mouseReleased(MouseEvent e) {
        removeMouseMotionListener(this);
	startLayoutThread();

	if ( fObjectInfoWin != null ) {
	    fObjectInfoWin.setVisible(false);
	    fObjectInfoWin.dispose();
	    fObjectInfoWin = null;
	}
	    
	if ( fIsDragging ) {
	    e.getComponent().setCursor(fCursor);
	    fIsDragging = false;
	    fDragMode = DRAG_NONE;
	}
	maybeShowPopup(e);
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {
	// ignore dragging events which we are not interested in
	if ( fDragMode == DRAG_NONE )
	    return;

	// switch cursor at start of dragging
	if ( ! fIsDragging ) {
	    fCursor = getCursor();
	    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	    fIsDragging = true;
	}

	if ( fDragMode == DRAG_ITEMS ) {
	    Point p = e.getPoint();
	    int dx = p.x - fDragStart.x;
	    int dy = p.y - fDragStart.y;
	    // move all selected components to new position.
	    Iterator nodeIterator = fSelection.iterator();
	    while ( nodeIterator.hasNext() ) {
		ObjectNode node = (ObjectNode) nodeIterator.next();
		node.setPosition(node.x() + dx, node.y() + dy);
	    }
	    fDragStart = p;
	}
	repaint();
    }

    public void mouseMoved(MouseEvent e) { }


    // implementation of interface DragTargetListener
    public void dragEnter(DropTargetDragEvent dtde) {
	//Log.trace(this, "dragEnter");
	dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }
    public void dragOver(DropTargetDragEvent dtde) {
	//Log.trace(this, "dragOver");
	dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }
    public void dropActionChanged(DropTargetDragEvent dtde) {
	//Log.trace(this, "dropActionChanged");
	dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }
    public void dragExit(DropTargetEvent dte) {
	//Log.trace(this, "dragExit");
    }

    /**
     * Accepts a drag of a class from the ModelBrowser. A new object
     * of this class will be created.
     */
    public void drop(DropTargetDropEvent dtde) {
	//Log.trace(this, "dtde = " + dtde);
	try {
	    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
	    Transferable transferable = dtde.getTransferable();
                   
	    // we accept only Strings      
	    if ( transferable.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
		String s = (String) 
		    transferable.getTransferData(DataFlavor.stringFlavor);
		//Log.trace(this, "transfer = " + s);

		if ( s.startsWith("CLASS-") ) {
		    Point p = dtde.getLocation();
		    fNextNodeX = p.getX();
		    fNextNodeY = p.getY();
		    String clsName = s.substring(6);
		    fParent.createObject(clsName);
		}
	    }
	    dtde.dropComplete(true);
	} catch (IOException exception) {
	    exception.printStackTrace();
	    System.err.println( "Exception" + exception.getMessage());
	    dtde.dropComplete(false);
	} 
	catch (UnsupportedFlavorException ufException ) {
	    ufException.printStackTrace();
	    System.err.println( "Exception" + ufException.getMessage());
	    dtde.dropComplete(false);
	}
    }

    class LayoutThread extends Thread {
	public void run() {
	    Thread me = Thread.currentThread();
	    while ( fLayoutThread == me ) {
		if ( fDoAutoLayout ) 
		    synchronized ( fLock ) {
			autoLayout();
		    }
		try {
		    Thread.sleep(100);
		} catch (InterruptedException e) {
		    break;
		}
	    }
	}
    }

    /**
     * This is called by the LayoutThread to generate a new
     * layout. The layouter object can be reused as long as the graph
     * and the size of the drawing area does not change.  
     */
    private synchronized void autoLayout() {
	if ( fLayouter == null ) {
	    int w = getWidth();
	    int h = getHeight();
	    if ( w == 0 || h == 0 )
		return;
	    fLayouter = new SpringLayout(fGraph, w, h, 80, 20);
	    fLayouter.setEdgeLen(150);
	}
	fLayouter.layout();
	repaint();
    }
}
