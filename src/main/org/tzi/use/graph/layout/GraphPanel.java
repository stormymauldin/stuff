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

package org.tzi.use.graph.layout;
import org.tzi.use.graph.DirectedGraph;
import org.tzi.use.graph.DirectedEdge;
import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import org.tzi.use.util.Log;

/** 
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class GraphPanel extends JPanel {
    private Layout fLayout;

    public GraphPanel(Layout l) {
	fLayout = l;

	setBackground(Color.white);
	setLayout(null);
	setMinimumSize(new Dimension(50, 50));
	setPreferredSize(new Dimension(600, 500));
    }

    /**
     * Draws the panel.
     */
    public void paintComponent(Graphics g) {
	super.paintComponent(g);

	Log.setTrace(true);
//    	Log.trace(this, getBounds().toString());
//    	Log.trace(this, getInsets().toString());
	
	// respect borders
	Insets insets = getInsets();
	Rectangle r = getBounds();
	r.x += insets.left;
	r.y += insets.top;
	r.width -= insets.left + insets.right;
	r.height -= insets.top + insets.bottom;

	// System.out.println("paintComponent" + count++);

    	g.setColor(Color.black);
	DirectedGraph graph = fLayout.graph();

	// draw edges
	Iterator edgeIter = graph.edgeIterator();
	while ( edgeIter.hasNext() ) {
	    DirectedEdge edge = (DirectedEdge) edgeIter.next();
	    // Log.trace(this, edge.toString());
	    LayoutNode source = (LayoutNode) edge.source();
	    LayoutNode target = (LayoutNode) edge.target();
	    int x1 = source.getX() * 80 + 30;
	    int y1 = 50 + source.fLayer * 50;
//  	    if ( source.isDummy() )
//  		x1 += 5;
	    int x2 = target.getX() * 80 + 30;
	    int y2 = 50 + target.fLayer * 50;
//  	    if ( target.isDummy() )
//  		x2 += 5;
	    g.drawLine(x1, y1, x2, y2);
	}

	// draw nodes
	Iterator nodeIter = graph.iterator();
	while ( nodeIter.hasNext() ) {
	    LayoutNode node = (LayoutNode) nodeIter.next();
	    if ( node.isDummy() ) 
		continue;
	    int x = node.getX() * 80 + 30;
	    int y = 50 + node.fLayer * 50;
	    g.setColor(Color.white);
	    g.fillRect(x - 10, y - 10, 20, 20);
	    g.setColor(Color.black);
	    g.drawRect(x - 10, y - 10, 20, 20);
	    g.drawString(node.toString(), x - 7, y + 8);
	}
    }
}
