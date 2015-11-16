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
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.util.Queue;
import org.tzi.use.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Evaluation of expressions.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

final public class Evaluator {

    private EvalContext fEvalContext;
    private boolean fEnableEvalTree;

    /**
     * Creates a default Evaluator.
     */
    public Evaluator() {
    }
    
    /**
     * Turns on building an evaluation tree. The tree is used, e.g.,
     * in the evaluation browser.  
     */
    public void enableEvalTree() {
	fEnableEvalTree = true;
    }

    /**
     * Evaluates an expression in the specified system state context
     * with a set of initial variable bindings. Detailed information
     * is printed to evalLog.
     */
    public Value eval(Expression expr, 
		      MSystemState preState,
		      MSystemState postState,
		      VarBindings bindings, 
		      PrintWriter evalLog)
    {
	fEvalContext = new EvalContext(preState, postState, bindings, evalLog);
	Value res = evaluate(expr);
	if ( evalLog != null )
	    evalLog.flush();
	return res;
    }

    public Value eval(Expression expr, 
		      MSystemState postState,
		      VarBindings bindings, 
		      PrintWriter evalLog)
    {
	return eval(expr, postState, postState, bindings, evalLog);
    }

    /**
     * Evaluates an expression in the specified system state context
     * with a set of initial variable bindings.
     */
    public Value eval(Expression expr, 
		      MSystemState postState,
		      VarBindings bindings)
    {
	return eval(expr, postState, bindings, null);
    }

    /**
     * Evaluates an expression in the specified system state context.
     */
    public Value eval(Expression expr, MSystemState postState) {
	return eval(expr, postState, new VarBindings(), null);
    }


    private Value evaluate(Expression expr) {
	if ( fEnableEvalTree )
	    fEvalContext.enableEvalTree();

	if ( Log.isTracing() )
	    Log.trace("Evaluator.eval expr: " + expr);

	Value res = null;
	try {
	    res = expr.eval(fEvalContext);
	} catch (StackOverflowError ex) {
	    throw new RuntimeException(
	       "Stack overflow. The expression is probably nested" +
	       " too deep or contains an infinite recursion.");
	}
	return res;
    }

    public EvalNode getEvalNodeRoot() {
	return fEvalContext.getEvalNodeRoot();
    }

    /**
     * Evaluates a list of expressions with <code>numThreads</code>
     * threads running in parallel. This method spawns a thread doing
     * the evaluation and returns immediately with a synchronized
     * queue. The caller should use the Queue.get() method on this
     * queue to wait for results. The queue will block until results
     * become available. The order in which results are delivered is
     * the same order passed in as <code>exprList</code>, i.e.,
     * expressions are processed in FIFO order.
     *
     * If <code>numThreads == 1</code>, the expression list will be
     * processed sequentially with no thread overhead.
     *
     * @return Queue(Value) a queue of result values for each expression.  
     */
    public Queue evalList(int numThreads, 
			  ArrayList exprList, 
			  MSystemState systemState) {
	if ( numThreads < 1 )
	    new IllegalArgumentException("numThreads == " + numThreads);

	Queue result = new Queue();
	ThreadedEvaluator.Controller controller = 
	    new ThreadedEvaluator.Controller(numThreads, result, 
					     exprList, systemState);
	controller.start();
	return result;
    }
}

