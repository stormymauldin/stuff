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

package org.tzi.use.parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemState;

/**
 * Test USECompiler class. The TestDriver reads all files with
 * extension ".use" and processes them with the parser. If a file
 * <code>t.use</code> contains expected errors there should be a file
 * <code>t.fail</code> with the expected output messages. There are
 * four possible results:
 *
 * <ol>
 * <li> file parses ok and no failure file exists: PASSED.</li>
 *
 * <li> file parses ok and a failure file exists: FAILURE.</li>
 *
 * <li> file does not parse and a failure file exists: PASSED if the
 * actual output matches the expected output from the failure file,
 * otherwise FAILURE.</li>
 *
 * <li> file does not parse and no failure file exists: FAILURE.</li>
 * </ol>
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class USECompilerTest extends TestCase {
    // Set this to true to see more details about what is tested.
    private static final boolean VERBOSE = false;

    private static final String TEST_PATH =
        System.getProperty("basedir")
            + "/src/test/org/tzi/use/parser".replace('/', File.separatorChar);
    private static final String EXAMPLES_PATH =
        System.getProperty("basedir") + "/examples".replace('/', File.separatorChar);

    // java.io has a StringWriter but we need an OutputStream for
    // System.err
    class StringOutputStream extends OutputStream {
        private String fBuffer = "";

        public void write(int b) {
            fBuffer += (char) b;
        }

        public void reset() {
            fBuffer = "";
        }
        public String toString() {
            return fBuffer;
        }
    }

    public void testSpecification() {
        List fileList = new ArrayList();
        // get all files ending with ".use"
        File dir = new File(TEST_PATH);
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getPath().endsWith(".use");
            }
        });
        assertNotNull(files);
        fileList.addAll(Arrays.asList(files));

        // make sure we don't silently miss the input files
        assertEquals(
            "make sure that all test files can be found "
                + " (or update expected number if you have added test files)",
            20,
            fileList.size());

        // add all the example files which should have no errors
        dir = new File(EXAMPLES_PATH);
        files = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getPath().endsWith(".use");
            }
        });
        assertNotNull(files);
        fileList.addAll(Arrays.asList(files));

        // create a new stream for capturing output on stderr
        StringOutputStream errStr = new StringOutputStream();
        PrintWriter newErr = new PrintWriter(errStr);

        // compile each file and compare with expected result

        for (Iterator iter = fileList.iterator(); iter.hasNext();) {
            File specFile = (File) iter.next();
            String specFileName = specFile.getName();
            //System.out.print(specFileName + ": ");

            MModel model = null;
            try {
                Reader r = new BufferedReader(new FileReader(specFile));

                model =
                    USECompiler.compileSpecification(
                        r,
                        specFileName,
                        newErr,
                        new ModelFactory());

                // check for a failure file
                String failFileName =
                    specFileName.substring(0, specFileName.length() - 4) + ".fail";
                File failFile = new File(TEST_PATH, failFileName);

                if (failFile.exists()) {
                    if (model != null) {
                        fail(
                            "compilation of "
                                + specFileName
                                + " succeeded, "
                                + "but errors were expected (see "
                                + failFile
                                + ").");
                    } else {
                        // check whether error output equals expected
                        // output
                        BufferedReader failReader =
                            new BufferedReader(new FileReader(failFile));
                        String[] expect = errStr.toString().split("\n|(\r\n)");
//                        for (int i = 0; i < expect.length; i++) {
//                            System.out.println("[" + expect[i] + "]");
//                        }
                        int j = 0;
                        boolean ok = true;
                        try {
                            while (ok) {
                                String line = failReader.readLine();
                                if (line == null) {
                                    ok = j == expect.length;
                                    break;
                                }
                                ok = line.equals(expect[j]);
                                j++;
                            }
                        } catch (IOException ex) {
                            ok = false;
                        }

                        if (!ok) {
                            System.err.println("Expected: #############");
                            try {
                                failReader =
                                    new BufferedReader(new FileReader(failFile));
                                while (true) {
                                    String line = failReader.readLine();
                                    if (line == null)
                                        break;
                                    System.err.println(line);
                                }
                            } catch (IOException ex) {
                                System.err.println(ex.getMessage());
                            }
                            System.err.println("Got: ##################");
                            System.err.print(errStr.toString());
                            System.err.println("#######################");
                            fail(
                                "compilation of "
                                    + specFileName
                                    + " had errors, "
                                    + "but the expected error output differs.");
                        } else {
                            if (VERBOSE) {
                                System.out.println(specFileName + ": PASSED.");
                            }
                        }
                    }
                } else {
                    if (model == null) {
                        // unexpected failure
                        System.err.println("#######################");
                        System.err.print(errStr.toString());
                        System.err.println("#######################");
                        fail(
                            "compilation of "
                                + specFileName
                                + " had errors, but there is "
                                + "no file `"
                                + failFileName
                                + "'.");
                    } else {
                        if (VERBOSE) {
                            System.out.println(specFileName + ": PASSED.");
                        }
                    }
                }
                errStr.reset();
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static final String TEST_EXPR_FILE =
        System.getProperty("basedir")
            + "/src/test/org/tzi/use/parser/test_expr.in".replace('/', File.separatorChar);

    public void testExpression() throws IOException {
        //env.printHeader("testing expression compiler...");

        MModel model = new ModelFactory().createModel("Test");

        BufferedReader in = null;
        // read expressions and expected results from file
        in = new BufferedReader(new FileReader(TEST_EXPR_FILE));
        while (true) {
            String line = in.readLine();
            if (line == null)
                break;
            if (line.startsWith("##")) {
                if (VERBOSE) {
                    System.out.println("testing " + line.substring(2).trim());
                }
                continue;
            }
            if (line.length() == 0 || line.startsWith("#"))
                continue;

            String expStr = line;
            while (true) {
                line = in.readLine();
                if (line == null)
                    throw new RuntimeException("missing result line");
                if (line.startsWith("-> "))
                    break;
                expStr += " " + line.trim();
            }
            String resultStr = line.substring(3);

            if (VERBOSE) {
                System.out.println("expression: " + expStr);
            }
            Expression expr =
                USECompiler.compileExpression(
                    model,
                    new StringReader(expStr),
                    TEST_EXPR_FILE,
                    new PrintWriter(System.err),
                    new VarBindings());
            assertNotNull(expr + " compiles", expr);

            MSystemState systemState = new MSystem(model).state();
            //Log.setTrace(true);
            Value val = new Evaluator().eval(expr, systemState);
            assertEquals("evaluate", resultStr, val.toStringWithType());
        }
    }
}
