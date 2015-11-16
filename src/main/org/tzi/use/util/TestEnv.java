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

package org.tzi.use.util;
import java.io.PrintWriter;

/**
 * Framework for test routines.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 * @see		TestBase
 */

public final class TestEnv {
    private PrintWriter fOut;	// output messages to fOut
    private int fNumTests;	// total number of tests
    private int fNumFailures;	// total number of failures


    /** 
     * Creates a new <code>TestEnv</code> object.
     *
     * @param  out      destination of all output.
     */
    public TestEnv(PrintWriter out) {
	fOut = out;
	fNumTests = 0;
	fNumFailures = 0;
    }

    /** 
     * Performs a test and log its result.
     *
     * @param  what     a string describing the kind of test.
     * @param  result   test result value.
     * @param  expected the expected test result value.
     * @return <code>true</code> if result equals expected; 
     *         <code>false</code> otherwise.
     */
    public boolean test(String what, Object result, Object expected) {
	// if both values are null, the test succeeds
	if ( result == null && expected == null )
	    return succeed(what, result);

	// if exactly one is null, the test fails
	if ( result == null || expected == null )
	    return fail(what, result, expected);

	// both values are non-null
	if ( result.equals(expected) )
	    return succeed(what, result);
	else
	    return fail(what, result, expected);
    }

    /** 
     * Performs a test and log its result. Test values are booleans.
     *
     * @param  what     a string describing the kind of test.
     * @param  result   test result value.
     * @param  expected the expected test result value.
     * @return <code>true</code> if result equals expected; 
     *         <code>false</code> otherwise.
     */
    public boolean test(String what, boolean result, boolean expected) {
	if ( result == expected )
	    return succeed(what, Boolean.valueOf(result));
	else
	    return fail(what, Boolean.valueOf(result), Boolean.valueOf(expected));
    }

    /** 
     * Performs a test and log its result. Test values are ints.
     *
     * @param  what     a string describing the kind of test.
     * @param  result   test result value.
     * @param  expected the expected test result value.
     * @return <code>true</code> if result equals expected; 
     *         <code>false</code> otherwise.
     */
    public boolean test(String what, int result, int expected) {
	if ( result == expected )
	    return succeed(what, new Integer(result));
	else
	    return fail(what, new Integer(result), new Integer(expected));
    }

    /** 
     * Performs a test and log its result. Test values are doubles.
     *
     * @param  what     a string describing the kind of test.
     * @param  result   test result value.
     * @param  expected the expected test result value.
     * @return <code>true</code> if result equals expected; 
     *         <code>false</code> otherwise.
     */
    public boolean test(String what, double result, double expected) {
	if ( result == expected )
	    return succeed(what, new Double(result));
	else
	    return fail(what, new Double(result), new Double(expected));
    }

    /** 
     * Prints a header describing the subsequently performed tests. 
     */
    public void printHeader(String msg) {
	fOut.println("-- " + msg);
    }
    
    /** 
     * Prints a header describing the subsequently performed tests. 
     */
    public void fatal(String msg) {
	fOut.println("** FATAL ERROR:" + msg);
	throw new RuntimeException("Test aborted");
    }
    
    /** 
     * Finishes testing and print final statistics.
     *
     * @return 1 if any test errors occured, 0 otherwise. 
     */
    public int finish() {
	fOut.println("finished testing, total: " + fNumTests 
		     + ", failures: " + fNumFailures + ".");
	return ( fNumFailures > 0 ) ? 1 : 0;
    }


    private boolean succeed(String what, Object result) {
	fNumTests++;
	fOut.println("testing " + what + " -> `" + result + "', PASSED.");
	return true;
    }

    private boolean fail(String what, Object result, Object expected) {
	fNumTests++;
	fOut.println("testing " + what + " -> `" + result + "', " +
		     "FAILED - expected `" + expected + "'.");
	fNumFailures++;
	return false;
    }
}
