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


/**
 * A spring embedder layout algorithm. See, e.g. G. Di Battista et
 * al.: "Graph Drawing", pp. 303, Prentice Hall, 1999.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class SpringLayout {
    private DirectedGraph fGraph; // the graph to be layouted
    private double fWidth;	// maximum width of layout
    private double fHeight;	// maximum height of layout
    private double fMarginX;	// margin on left/right side of the drawing area
    private double fMarginY;	// margin on top/bottom side of the drawing area
    private double fEdgeLen = 120.0;
    private Object[] fNodes;
    private double[] fXn;
    private double[] fYn;

    /**
     * Constructs a new SpringLayouter.
     * 
     * @param width width of drawing area
     * @param height height of drawing area
     * @param marginx margin on left/right side of the drawing area
     * @param marginy margin on top/bottom side of the drawing area
     */
    public SpringLayout(DirectedGraph g, 
			double width, double height,
			double marginx, double marginy) {
	fGraph = g;
	fWidth = width;
	fHeight = height;
	fMarginX = marginx;
	fMarginY = marginy;

	fNodes = fGraph.toArray();
	fXn = new double[fNodes.length];
	fYn = new double[fNodes.length];
    }

    /**
     * Sets a new default length for edges.
     */
    public void setEdgeLen(double len) {
	fEdgeLen = len;
    }

    /**
     * Calculates a layout. This method may be called repeatedly for
     * refining the layout if the graph does not change between calls.
     */
    public void layout() {
	final int N = fNodes.length;
    	final double k1 = 1.0;
	final double k2 = 100.0 * 100.0;

	double xc = 0.0;
	double yc = 0.0;
	for (int i = 0; i < N; i++) {
	    PlaceableNode v = (PlaceableNode) fNodes[i];
//  	    System.out.println("v = " + v);
	    double xv = v.x();
	    double yv = v.y();

	    // spring force

	    //System.out.println("  sourceNodeSet = " + fGraph.sourceNodeSet(v));
	    Iterator uIter = fGraph.sourceNodeSet(v).iterator();
	    double sumfx1 = 0.0;
	    double sumfy1 = 0.0;
	    while ( uIter.hasNext() ) {
		PlaceableNode u = (PlaceableNode) uIter.next();
//  		System.out.println("spring  u = " + u);
		double xu = u.x();
		double yu = u.y();
		double dx = xv - xu;
		double dy = yv - yu;
		double d = Math.sqrt(dx * dx + dy * dy);
		d = (d == 0) ? .0001 : d;
		double c = k1 * (d - fEdgeLen) / d;
		sumfx1 += c * dx;
		sumfy1 += c * dy;
	    }
//  	    System.out.println("sumfx1 = " + sumfx1);
//  	    System.out.println("sumfy1 = " + sumfy1);
	    

	    // electrical force

	    uIter = fGraph.iterator();
	    double sumfx2 = 0.0;
	    double sumfy2 = 0.0;
	    while ( uIter.hasNext() ) {
		PlaceableNode u = (PlaceableNode) uIter.next();
		if ( u == v )
		    continue;
		//System.out.println("electrical  u = " + u);
		double xu = u.x();
		double yu = u.y();
		double dx = xv - xu;
		double dy = yv - yu;
		double d = dx * dx + dy * dy;
		if ( d > 0 ) {
		    double c = k2 / (d * Math.sqrt(d));
		    sumfx2 += c * dx;
		    sumfy2 += c * dy;
		}
	    }
//  	    System.out.println("sumfx2 = " + sumfx2);
//  	    System.out.println("sumfy2 = " + sumfy2);

	    // store new positions
  	    fXn[i] = xv - Math.max(-5, Math.min(5, sumfx1 - sumfx2));
  	    fYn[i] = yv - Math.max(-5, Math.min(5, sumfy1 - sumfy2));

	    // for determining the center of the graph
	    xc += fXn[i];
	    yc += fYn[i];
	}

	// offset from center of graph to center of drawing area
	double dx = fWidth / 2 - xc / N;
	double dy = fHeight / 2 - yc / N;

	// use only small steps for smooth animation
	dx = Math.max(-5, Math.min(5, dx));
	dy = Math.max(-5, Math.min(5, dy));

	// set new positions
	for (int i = 0; i < N; i++) {
	    PlaceableNode v = (PlaceableNode) fNodes[i];
	    // move each node towards center of drawing area and keep
	    // it within bounds
  	    double x = Math.max(fMarginX, Math.min(fWidth - fMarginX, fXn[i] + dx));
  	    double y = Math.max(fMarginY, Math.min(fHeight - fMarginY, fYn[i] + dy));
	    v.setPosition(x, y);
	}
    }
}

