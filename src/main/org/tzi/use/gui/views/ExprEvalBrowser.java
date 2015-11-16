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
import org.tzi.use.uml.ocl.expr.EvalNode;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

/** 
 * A tree view showing evaluation results of an expression. Each node
 * is adorned with the result of evaluating the sub-expression.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class ExprEvalBrowser extends JPanel {

    public ExprEvalBrowser(EvalNode root) {
        // create the nodes
	DefaultMutableTreeNode top = new DefaultMutableTreeNode(root);
        createNodes(top, root);

        // create tree
        final JTree tree = new JTree(top);
	tree.putClientProperty("JTree.lineStyle", "Angled");
	setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private void createNodes(DefaultMutableTreeNode treeParent, EvalNode node) {
	Iterator it = node.children().iterator();
	while ( it.hasNext() ) {
	    EvalNode child = (EvalNode) it.next();
	    DefaultMutableTreeNode treeChild = new DefaultMutableTreeNode(child);
	    treeParent.add(treeChild);
	    createNodes(treeChild, child);
	}
    }

    public static void create(EvalNode root) {
	final JFrame f = new JFrame("Evaluation browser");
        ExprEvalBrowser eb = new ExprEvalBrowser(root);

	// Layout the content pane
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(eb, BorderLayout.CENTER);

	// add close button
	Box btnBox = Box.createHorizontalBox();
        JButton closeBtn = new JButton("Close");
        btnBox.add(Box.createGlue());
        btnBox.add(closeBtn);
        btnBox.add(Box.createGlue());
	closeBtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    f.setVisible(false);
		    f.dispose();
		}
	    });

        contentPane.add(btnBox, BorderLayout.SOUTH);
        f.setContentPane(contentPane);
	f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	f.pack();
	f.setVisible(true);
    }
}
