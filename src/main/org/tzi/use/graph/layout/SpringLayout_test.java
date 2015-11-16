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
import org.tzi.use.graph.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;


/**
 * Test class.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class SpringLayout_test {
    static Random random = new Random(10);

    class Node implements PlaceableNode {
	private double fX;
	private double fY;
	private int fNumber;
	Node(int n) {
	    fNumber = n;
	    fX = random.nextInt(300);
	    fY = random.nextInt(300);
	}
	public double x() { return fX; }
	public double y() { return fY; }
	public void setPosition(double x, double y) {
	    fX = x;
	    fY = y;
	}

	public int hashCode() {
	    return fNumber;
	}

	public boolean equals(Object o) {
	    if ( this == o )
		return true;
	    if ( o instanceof Node )
		return fNumber == ((Node) o).fNumber;
	    return false;
	}

	public String toString() {
	    return "(" + fNumber + ", " + fX + ", " + fY + ")";
	}
    }

    private DirectedGraph graph0() {
	final int N = 3;
	int[][] edges = { 
	    {1,2}, {1,3},
	};
	return newGraph(N, edges);
    }

    // p. 248
    private DirectedGraph graph1() {
	final int N = 13;
	int[][] edges = { 
	    {1,2}, {1,4}, {1,5}, 
	    {2,3}, {2,4}, {2,12}, 
	    {3,4}, {3,9}, {3,11},
	    {4,8},
	    {5,6}, {5,7}, {5,9},
	    {6,7}, {6,10}, {6,13},
	    {7,8}, {7,9},
	    {8,11}, {8,12},
	    {9,12},
	    {10,11}, {10,13},
	    {11,13},
	    {12,13},
	};
	return newGraph(N, edges);
    }

    // p. 295
    private DirectedGraph graph2() {
	final int N = 9;
	int[][] edges = { 
	    {1,2}, {2,3}, {2,5}, {3,6}, {4,1}, {4,5}, {5,8},
	    {6,5}, {6,9}, {7,4}, {8,7}, {9,8}
	};
	return newGraph(N, edges);
    }

    private DirectedGraph graph3() {
	final int N = 5;
	int[][] edges = { 
	    {1,2}, {1,3}, {1,4}, {1,5}, {2,3}, {2,4}, {3,4}
	};
	return newGraph(N, edges);
    }

    private DirectedGraph randomGraph(int N) {
	int[][] edges = new int[1 * N][2];

	Random r = new Random(1);
	for (int i = 0; i < edges.length; i++) {
	    int source = r.nextInt(N - 1);
	    int target = source + 1 + r.nextInt(N - source - 1);
	    edges[i][0] = source + 1;
	    edges[i][1] = target + 1;
	}	
	return newGraph(N, edges);
    }

    private DirectedGraph newGraph(int N, int[][] edges) {
	DirectedGraph g = new DirectedGraphBase(N);
	Object[] nodes = new Object[N];
	for (int i = 0; i < N; i++) {
	    nodes[i] = new Node(i + 1);
	    g.add(nodes[i]);
	}
	    
	for (int i = 0; i < edges.length; i++) {
	    // System.out.println(edges[i].length);
	    g.addEdge(new DirectedEdgeBase(nodes[edges[i][0] - 1], 
					   nodes[edges[i][1] - 1]));
	}
	return g;
    }

    private void run() {
  	DirectedGraph g;
//  	System.out.println(g);
//  	System.out.println();

	//g = randomGraph(20);
	g = graph3();
	System.out.println(g);
	System.out.println();
	SpringLayout l = new SpringLayout(g, 550, 450, 0, 0);

	GraphPanel gp = new GraphPanel(g);
	JFrame f = new JFrame("GraphPanel");

	// Layout the content pane
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JScrollPane(gp), BorderLayout.CENTER);
        f.setContentPane(contentPane);
	f.pack();
	f.setVisible(true);

	while ( true ) {
	    try { Thread.sleep(10); } catch (InterruptedException ex) {}
	    l.layout();
	    gp.repaint();
	}
    }

    public static void main(String[] args) {
	new SpringLayout_test().run();
    }


class GraphPanel extends JPanel {
    private DirectedGraph fGraph;

    public GraphPanel(DirectedGraph g) {
	fGraph = g;

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

	//Log.setTrace(true);
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

	// draw edges
	Iterator edgeIter = fGraph.edgeIterator();
	while ( edgeIter.hasNext() ) {
	    DirectedEdge edge = (DirectedEdge) edgeIter.next();
	    // Log.trace(this, edge.toString());
	    PlaceableNode source = (PlaceableNode) edge.source();
	    PlaceableNode target = (PlaceableNode) edge.target();
	    int x1 = (int) source.x();
	    int y1 = (int) source.y();
	    int x2 = (int) target.x();
	    int y2 = (int) target.y();
	    g.drawLine(x1, y1, x2, y2);
	}

	// draw nodes
	Iterator nodeIter = fGraph.iterator();
	while ( nodeIter.hasNext() ) {
	    PlaceableNode node = (PlaceableNode) nodeIter.next();
	    int x = (int) node.x();
	    int y = (int) node.y();
	    g.setColor(Color.orange);
	    g.fillRect(x - 10, y - 10, 20, 20);
	    g.setColor(Color.black);
	    g.drawRect(x - 10, y - 10, 20, 20);
	    g.drawString(node.toString(), x - 7, y + 8);
	}
    }
}
}

