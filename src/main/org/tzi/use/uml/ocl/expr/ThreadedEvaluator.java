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

package org.tzi.use.uml.ocl.expr;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.util.Queue;
import java.util.ArrayList;

/**
 * An evaluator with parallel execution.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class ThreadedEvaluator {
    private static final boolean DEBUG = false;
    //private static final boolean DEBUG = true;

    /**
     * A Controller maintains a set of worker threads each processing
     * a single expression at a time.
     */
    static class Controller extends Thread {
	private Queue fResultValueQueue;
	private ArrayList fExprList;
	MSystemState fSystemState;

	private int fNumWorkers; // number of parallel worker threads
	private Thread[] fWorkers;

	private int fNumJobs;	// number of jobs to process
	private Job[] fJobs;
	private int fNextJob;

	Controller(int numWorkers, Queue resultQueue, 
		       ArrayList exprList, MSystemState systemState) {
	    fResultValueQueue = resultQueue;
	    fExprList = exprList;
	    fSystemState = systemState;
	    fNumJobs = fExprList.size();
	    fNumWorkers = Math.min(numWorkers, fNumJobs);
	    fWorkers = new Thread[fNumWorkers];
	    fJobs = new Job[fNumJobs];
	    for (int i = 0; i < fNumJobs; i++)
		fJobs[i] = new Job((Expression) exprList.get(i));
	}

	synchronized Job getNextJob() {
	    if ( fNextJob < fNumJobs )
		return fJobs[fNextJob++];
	    else 
		return null;
	}

	public void run() {
	    // spawn worker threads
	    for (int i = 0; i < fNumWorkers; i++) {
		if ( DEBUG )
		    System.err.println("Starting Worker " + i);
		fWorkers[i] = new Worker(this, i);
		fWorkers[i].start();
	    }

	    // wait on results in fifo order
	    for (int i = 0; i < fNumJobs; i++) {
		if ( DEBUG )
		    System.err.println("Waiting on result " + i + " of " + fNumJobs);
		Job job = fJobs[i];
		Value v = job.getResult();
		if ( DEBUG )
		    System.err.println("Got result " + i + " of " + fNumJobs);
		fResultValueQueue.append(v);
	    }

	    // wait on worker threads to finish (not really needed)
//  	    for (int i = 0; i < fNumWorkers; i++) {
//  		try {
//  		    if ( DEBUG )
//  			System.err.println("Waiting on Worker " + i);
//  		    fWorkers[i].join();
//  		    if ( DEBUG )
//  			System.err.println("Worker " + i + " joined");
//  		} catch (InterruptedException e) {
//  		    System.err.println("Interrupted thread: " + fWorkers[i]);
//  		}
//  	    }
	}
    }

    /**
     * A Job is a container for an expression and its result value. It
     * synchronizes access to the result value.  
     */
    static class Job {
	Expression fExpr;	// expression to evaluate
	Value fValue;		// result

	Job(Expression expr) {
	    fExpr = expr;
	}

	synchronized void setResult(Value v) {
	    fValue = v;
	    this.notifyAll();
	}

	synchronized Value getResult() {
	    while ( fValue == null ) {
		try {
		    this.wait();
		} catch (InterruptedException e) {
		}
	    }
	    return fValue;
	}

	public String toString() {
	    return fExpr.toString();
	}
    }

    /**
     * A Worker object receives jobs from the controller and processes
     * them.  
     */
    static class Worker extends Thread {
	private int fNumber;
	private Controller fController;

	public Worker(Controller controller, int i) {
	    fController = controller;
	    fNumber = i;
	}

	public void run() {
	    if ( DEBUG )
		System.err.println("Worker " + fNumber + " starts running.");
	    int jobsProcessed = 0;
	    Job job;
	    while ( (job = fController.getNextJob()) != null ) {
		if ( DEBUG )
		    System.err.println("Worker " + fNumber + " working on job " + job);
		Evaluator evaluator = new Evaluator();
		Value v = evaluator.eval(job.fExpr, fController.fSystemState);
		if ( DEBUG )
		    System.err.println("Worker " + fNumber + " finished job " + job);
		job.setResult(v);
		jobsProcessed++;
	    }
	    if ( DEBUG )
		System.err.println("Worker " + fNumber + " finished (" + 
				   jobsProcessed + " jobs processed)");
	}
    }
}

