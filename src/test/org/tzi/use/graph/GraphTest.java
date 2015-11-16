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

package org.tzi.use.graph;
import junit.framework.TestCase;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Arrays;


/**
 * Test Graph classes.
 *
 * @version     $ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters
 * @see         DirectedGraph
 */

public class GraphTest extends TestCase {

    public void test1() {
        DirectedGraph g;
        Integer n0, n1, n2, n3, n4, n5;
        DirectedEdge e01, e11, e12, e21, e23, e24a, e24b, e30, e42, e45;

        //          +-------+
        //          v       | ----\
        // 0 -----> 1 ----> 2 -----> 4 ----> 5
        // ^       ^-+      |\---- 
        // |                v
        // +--------------- 3
        
        // ----
//      env.printHeader("testing graph creation...");
        g = new DirectedGraphBase();
        g.add(n0 = new Integer(0));
        g.add(n1 = new Integer(1));
        g.add(n2 = new Integer(2));
        g.add(n3 = new Integer(3));
        g.add(n4 = new Integer(4));
        g.add(n5 = new Integer(5));
        assertEquals(6, g.size());

        // connect nodes
        g.addEdge(e01 = new DirectedEdgeBase(n0, n1));
        g.addEdge(e11 = new DirectedEdgeBase(n1, n1));
        g.addEdge(e12 = new DirectedEdgeBase(n1, n2));
        g.addEdge(e21 = new DirectedEdgeBase(n2, n1));
        g.addEdge(e23 = new DirectedEdgeBase(n2, n3));
        g.addEdge(e24a = new DirectedEdgeBase(n2, n4));
        g.addEdge(e24b = new DirectedEdgeBase(n2, n4));
        g.addEdge(e30 = new DirectedEdgeBase(n3, n0));
        g.addEdge(e42 = new DirectedEdgeBase(n4, n2));
        g.addEdge(e45 = new DirectedEdgeBase(n4, n5));
        assertEquals(10, g.numEdges());

        // ----
//      env.printHeader("testing graph connectivity...");
        assertEquals(1, g.numIncomingEdges(n0));
        assertEquals(3, g.numIncomingEdges(n1));
        assertEquals(2, g.numIncomingEdges(n2));
        assertEquals(1, g.numIncomingEdges(n3));
        assertEquals(2, g.numIncomingEdges(n4));
        assertEquals(1, g.numIncomingEdges(n5));

        assertEquals(1, g.numOutgoingEdges(n0));
        assertEquals(2, g.numOutgoingEdges(n1));
        assertEquals(4, g.numOutgoingEdges(n2));
        assertEquals(1, g.numOutgoingEdges(n3));
        assertEquals(2, g.numOutgoingEdges(n4));
        assertEquals(0, g.numOutgoingEdges(n5));

        // ----
//      env.printHeader("testing views...");
        assertEquals(3, g.targetNodeSet(n2).size());
        assertEquals(2, g.targetNodeSet(n4).size());
        assertEquals(0, g.targetNodeSet(n5).size());

        assertEquals(3, g.sourceNodeSet(n1).size());
        assertEquals(2, g.sourceNodeSet(n2).size());
        assertEquals(1, g.sourceNodeSet(n4).size());
        assertEquals(1, g.sourceNodeSet(n5).size());

        HashSet s = new HashSet();
        Integer[] ia;
        ia = new Integer[] { n0, n1, n2, n3, n4, n5 };
        s.addAll(Arrays.asList(ia));
        assertEquals(s, g.targetNodeClosureSet(n0));
        assertEquals(s, g.targetNodeClosureSet(n1));
        assertEquals(s, g.targetNodeClosureSet(n2));
        assertEquals(s, g.targetNodeClosureSet(n3));
        assertEquals(s, g.targetNodeClosureSet(n4));
        s = new HashSet();
        assertEquals(s, g.targetNodeClosureSet(n5));

        ia = new Integer[] { n0, n1, n2, n3, n4 };
        s.addAll(Arrays.asList(ia));
        assertEquals(s, g.sourceNodeClosureSet(n0));
        assertEquals(s, g.sourceNodeClosureSet(n1));
        assertEquals(s, g.sourceNodeClosureSet(n2));
        assertEquals(s, g.sourceNodeClosureSet(n3));
        assertEquals(s, g.sourceNodeClosureSet(n4));

        // ----
//      env.printHeader("testing connections...");
        assertEquals(true, g.existsPath(n0, n0));
        assertEquals(true, g.existsPath(n0, n1));
        assertEquals(true, g.existsPath(n0, n2));
        assertEquals(true, g.existsPath(n1, n1));
        assertEquals(true, g.existsPath(n3, n4));
        assertEquals(true, g.existsPath(n2, n1));
        assertEquals(true, g.existsPath(n3, n2));
        assertEquals(true, g.existsPath(n4, n3));
        assertEquals(true, g.existsPath(n0, n5));
        assertEquals(false, g.existsPath(n5, n3));

        s = new HashSet();
        DirectedEdge[] ea = new DirectedEdge[] { e23 };
        s.addAll(Arrays.asList(ea));
        assertEquals(s, g.edgesBetween(n2, n3));
        s = new HashSet();
        ea = new DirectedEdge[] { e24a, e24b, e42 };
        s.addAll(Arrays.asList(ea));
        assertEquals(s, g.edgesBetween(n2, n4));

        // ----
//      env.printHeader("testing cycles...");
        assertEquals(true, g.hasCycle());

        // ----
//      env.printHeader("testing node deletion...");
        assertEquals(true, g.remove(n0));
        assertEquals(5, g.size());
        assertEquals(8, g.numEdges());

        // ----
//      env.printHeader("testing edge deletion...");
        assertEquals(true, g.removeEdge(e23));
        assertEquals(5, g.size());
        assertEquals(7, g.numEdges());
    }
}

