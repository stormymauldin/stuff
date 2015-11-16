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
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MModelElement;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MMVisitor;
import org.tzi.use.uml.mm.MMHTMLPrintVisitor;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Collection;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

/** 
 * A ModelBrowser provides a tree view of classes, associations, and
 * constraints in a model. The definition of a selected element is
 * shown in an HTML pane. A class can be dragged onto an object
 * diagram to create a new object of this class.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class ModelBrowser extends JPanel 
    implements DragSourceListener, DragGestureListener 
{
    private MModel fModel;
    private JTree fTree;
    private JEditorPane fHtmlPane;

    private DragSource fDragSource = null;

    // implementation of interface DragSourceListener
    public void dragEnter(DragSourceDragEvent dsde) {
	//Log.trace(this, "dragEnter");
    }
    public void dragOver(DragSourceDragEvent dsde) {
	//Log.trace(this, "dragOver");
    }
    public void dropActionChanged(DragSourceDragEvent dsde) {
	//Log.trace(this, "dropActionChanged");
    }
    public void dragExit(DragSourceEvent dse) {
	//Log.trace(this, "dragExit");
    }
    public void dragDropEnd(DragSourceDropEvent dsde) {
	//Log.trace(this, "dragDropEnd");
    }

    // implementation of interface DragSourceListener
    public void dragGestureRecognized(DragGestureEvent dge) {
	//Log.trace(this, "dragGestureRecognized");
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)
	    fTree.getLastSelectedPathComponent();

	if ( fModel == null || node == null ) 
	    return;

	Object nodeInfo = node.getUserObject();
	if ( node.isLeaf() && (nodeInfo instanceof MClass) ) {
	    MClass cls = (MClass) nodeInfo;
	    StringSelection text = new StringSelection("CLASS-" + cls.name());
	    fDragSource.startDrag (dge, DragSource.DefaultMoveDrop, text, this);
	}
    }

    /** 
     * Creates a browser with no model.
     */
    public ModelBrowser() {
	this(null);
    }

    /** 
     * Creates a model browser. The model may be null.
     */
    public ModelBrowser(MModel model) {
        // Create tree and nodes.
	setModel(model);

	fDragSource = new DragSource();
	fDragSource.createDefaultDragGestureRecognizer(fTree, 
						       DnDConstants.ACTION_MOVE,
						       this);


        // Allow one selection at a time.
        fTree.getSelectionModel().setSelectionMode(
		   TreeSelectionModel.SINGLE_TREE_SELECTION);
	fTree.putClientProperty("JTree.lineStyle", "Angled");
	fTree.setCellRenderer(new CellRenderer());

	// Enable tool tips
	ToolTipManager.sharedInstance().registerComponent(fTree);

        // Listen for when the selection changes.
        fTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		    fTree.getLastSelectedPathComponent();

                if ( fModel == null || node == null ) 
		    return;

                Object nodeInfo = node.getUserObject();
                if ( node.isLeaf() && (nodeInfo instanceof MModelElement) ) {
                    MModelElement me = (MModelElement) nodeInfo;
                    displayInfo(me);
                } 
            }
        });


        // Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(fTree);

        // Create the HTML viewing pane.
        fHtmlPane = new JEditorPane();
        fHtmlPane.setEditable(false);
	fHtmlPane.setContentType("text/html");
        JScrollPane htmlView = new JScrollPane(fHtmlPane);

        // Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					      treeView, htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(200);
        splitPane.setPreferredSize(new Dimension(500, 300));

	setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }


    /**
     * This renderer always displays root and categories as non-leaf
     * nodes even if they are empty.  
     */
    class CellRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree,
						      Object value,
						      boolean sel,
						      boolean expanded,
						      boolean leaf,
						      int row,
						      boolean hasFocus) {
	    super.getTreeCellRendererComponent(tree, value, sel,
					       expanded, leaf, row,
					       hasFocus);
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	    int level = node.getLevel();
	    this.setToolTipText(null);
	    // always display root and categories as non-leaf nodes
	    if ( level == 0 ) {
		if ( node.isLeaf() )
		    setIcon(getClosedIcon()); // we don't have a model
		else 
		    setIcon(getOpenIcon());
	    } else if ( level == 1 ) {
		if ( tree.isExpanded(new TreePath(node.getPath())) )
		    setIcon(getOpenIcon());
		else
		    setIcon(getClosedIcon());
	    } else {
                Object nodeInfo = node.getUserObject();
                if ( nodeInfo instanceof MClass ) {
                    MClass cls = (MClass) nodeInfo;
		    this.setToolTipText("Drag onto object diagram to create a new " +
					cls.name() + " object.");
		}

	    }
	    return this;
	}
    }



    /** 
     * Initializes the browser to a new model. The model may be null.  
     */
    public void setModel(MModel model) {
	fModel = model;

        // Create the nodes.
	DefaultMutableTreeNode top;
	if ( fModel != null ) {
	    top = new DefaultMutableTreeNode(model.name());
	    createNodes(top);
	} else {
	    top = new DefaultMutableTreeNode("No model available");
	}

        // Create new tree or reinitialize existing tree
	if ( fTree == null ) {
	    DefaultTreeModel treeModel = new DefaultTreeModel(top);
	    fTree = new JTree(treeModel);
	} else {
	    DefaultTreeModel treeModel = (DefaultTreeModel) fTree.getModel();
	    treeModel.setRoot(top);
	}
	// reset HTML pane
	if ( fHtmlPane != null )
	    fHtmlPane.setText("");
    }

    /** 
     * Displays info about the selected model element in the HTML pane.
     */
    private void displayInfo(MModelElement element) {
	StringWriter sw = new StringWriter();
	sw.write("<html><head>");
	sw.write("<style> <!-- body { font-family: sansserif; } --> </style>");
	sw.write("</head><body><font size=\"-1\">");
	MMVisitor v = new MMHTMLPrintVisitor(new PrintWriter(sw));
	element.processWithVisitor(v);
	sw.write("</body></html>");
	String spec = sw.toString();
	// System.out.println(spec);
	fHtmlPane.setText(spec);
    }

    private void createNodes(DefaultMutableTreeNode top) {
	addChildNodes(top, "Classes", fModel.classes());
	addChildNodes(top, "Associations", fModel.associations());
	addChildNodes(top, "Invariants", fModel.classInvariants());
	addChildNodes(top, "Pre-/Postconditions", fModel.prePostConditions());
    }

    private void addChildNodes(DefaultMutableTreeNode top, 
			       String name, 
			       Collection items) {
	DefaultMutableTreeNode category = new DefaultMutableTreeNode(name);
	top.add(category);
	Iterator it = items.iterator();
	while ( it.hasNext() ) {
	    DefaultMutableTreeNode child = new DefaultMutableTreeNode(it.next());
	    category.add(child);
	}
    }
}
