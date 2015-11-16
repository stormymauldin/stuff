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

package org.tzi.use.main;
import org.tzi.use.uml.mm.*;
import org.tzi.use.uml.sys.*;
import org.tzi.use.uml.ocl.expr.*;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.parser.USECompiler;
import org.tzi.use.config.Options;
import org.tzi.use.util.Log;
import org.tzi.use.util.Report;
import org.tzi.use.util.NullWriter;
import org.tzi.use.util.StringUtil;
import org.tzi.use.util.input.LineInput;
import org.tzi.use.util.input.Readline;
import org.tzi.use.util.input.StreamReadline;
import org.tzi.use.util.input.SocketReadline;
import org.tzi.use.gui.views.ExprEvalBrowser;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.text.NumberFormat;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A shell for reading and executing user commands.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class Shell implements Runnable {
    private static final String PROMPT = "use> ";
    private static final String CONTINUE_PROMPT = "> ";

    /**
     * Stack of input methods for reading user input.
     */
    private Stack fReadlineStack;

    /**
     * Run program until true. Set by exit command.
     */
    private boolean fFinished = false;

    /**
     * If true read multiple lines into a single command before
     * processing.  
     */
    private boolean fMultiLineMode = false;

    /**
     * The session contains the system most commands act on.
     */
    private Session fSession;

    /**
     * The daVinci communication interface.
     */
    private DaVinciProcess fDaVinci;

    /**
     * Result of last check command.
     */
    private boolean fLastCheckResult = false;

    /**
     * Single-step commands.
     */
    private boolean fStepMode = false;

    /**
     * Constructs a new shell.
     */
    public Shell(Session session) {
	fReadlineStack = new Stack();
	// no need to listen on session changes since every command
	// explicitly retrieves the current system
	fSession = session;
	fDaVinci = new DaVinciProcess(Options.DAVINCI_PATH);
    }

    /** 
     * Returns the result of the last check command.
     */
    public boolean lastCheckResult() {
	return fLastCheckResult;
    }

    /**
     * Main loop for accepting input and processing it.
     */
    public void run() {
	String GNUReadlineNotAvailable;
	if ( Options.suppressWarningsAboutMissingReadlineLibrary )
	    GNUReadlineNotAvailable = null;
	else
	    GNUReadlineNotAvailable =
		"Apparently, the GNU readline library is not availabe on your system." +
		Options.LINE_SEPARATOR +
		"The program will continue using a simple readline implementation." +
		Options.LINE_SEPARATOR +
		"You can turn off this warning message by using the switch -nr";

	Readline rl = null;
	if ( ! Options.quiet ) {
	    rl = LineInput.getReadline(GNUReadlineNotAvailable);
	    rl.usingHistory();

	    // Read command history from previous sessions
	    try {
		rl.readHistory(Options.USE_HISTORY_PATH);
	    } catch (IOException ex) {
		// Fail silently if history file does not exist
	    }
	    fReadlineStack.push(rl);
	}
	
	if ( Options.cmdFilename != null )
	    cmdRead(Options.cmdFilename, true);
	else
	    Log.verbose("Enter `help' for a list of available commands.");

	while ( ! fFinished ) {
	    Thread.yield();
	    Log.resetOutputFlag();

	    String line = "";

	    // get current readline (may be e.g. console or file)
	    rl = (Readline) fReadlineStack.peek();
	    try {
		if ( fMultiLineMode ) {
		    while ( true ) {
			// use special prompt to emphasize multi-line input
			String oneLine = rl.readline(CONTINUE_PROMPT);
	    
			// end of input or a single dot terminates the input loop
			if ( oneLine == null || oneLine.equals(".") )
			    break;
			line += oneLine + Options.LINE_SEPARATOR;
		    };
		    fMultiLineMode = false;
		} else {
		    line = rl.readline(PROMPT);
		}
	    } catch (IOException ex) {
		Log.error("Cannot read line: " + ex.getMessage());
	    }

	    if ( line != null ) {
		processLineSafely(line);
	    } else {
		try {
		    rl.close();
		} catch (IOException ex) {
		    Log.error(ex.getMessage());
		}
		fReadlineStack.pop();
		fFinished = fReadlineStack.isEmpty();
		if ( fFinished && Options.quiet )
		    processLineSafely("check");
	    }
	}

	// clean up
	Log.verbose("Exiting...");

	// terminate daVinci
	fDaVinci.close();

	// Write command history to file
	if ( ! Options.quiet ) {
	    try {
		rl.writeHistory(Options.USE_HISTORY_PATH);
	    } catch (IOException ex) {
		Log.error("Can't write history file " + 
			  Options.USE_HISTORY_PATH + " : " + 
			  ex.getMessage());
	    }
	}
    }

    class NoSystemException extends Exception {
    }

    /**
     * Analyses a line of input and calls the method implementing a
     * command.  
     */
    private void processLineSafely(String line) {
	try {
	    processLine(line);
	} catch (NoSystemException ex) {
	    Log.error("No System available. Please load a model before executing this command.");
	} catch (Exception ex) {
	    System.err.println();
	    String nl = Options.LINE_SEPARATOR;
	    System.err.println(
"INTERNAL ERROR: An unexpected exception occured. This happened most probably" + nl +
"due to an error in the program. The program will try to continue, but may" + nl +
"not be able to recover from the error. Please send a bug report to mr@tzi.org" + nl +
"with a description of your last input and include the following output:");
	    System.err.println("Program version: " + Options.RELEASE_VERSION);
	    System.err.println("Project version: " + Options.PROJECT_VERSION);
	    System.err.print("Stack trace: ");
	    ex.printStackTrace();
	}
    }

    /**
     * Analyses a line of input and calls the method implementing a
     * command.  
     */
    private void processLine(String line) throws NoSystemException {
	line = line.trim();
	if ( line == null || line.length() == 0 
	     || line.startsWith("//") || line.startsWith("--") )
	    return;

	if ( fStepMode ) {
	    Log.println("[step mode: `return' continues, " + 
			"`escape' followed by `return' exits step mode.]");
	    try { 
		int c = System.in.read();
		if ( c == 0x1b )
		    fStepMode = false;
	    } catch (IOException ex) { }
	}

	if ( line.equals("help") )
	    cmdHelp();
	else if ( line.equals("q") || line.equals("quit") || line.equals("exit") )
	    cmdExit();
	else if ( line.startsWith("??") )
	    cmdQuery(line.substring(2).trim(), true);
	else if ( line.startsWith("?") )
	    cmdQuery(line.substring(1).trim(), false);
	else if ( line.startsWith("!") )
	    cmdExec(line.substring(1).trim());
	else if ( line.equals("\\") )
	    cmdMultiLine();
	else if ( line.equals("check") || line.startsWith("check ") )
	    cmdCheck(line);
	else if ( line.equals("genvcg") )
	    cmdGenVCG(null);
	else if ( line.startsWith("genvcg ") )
	    cmdGenVCG(line.substring(7));
	else if ( line.equals("genmm") )
	    cmdGenMM(null);
	else if ( line.startsWith("genmm ") )
	    cmdGenMM(line.substring(6));
	else if ( line.equals("genmonitor") )
	    cmdGenMonitor();
	else if ( line.equals("graph") )
	    cmdGraph();
	else if ( line.startsWith("info ") )
	    cmdInfo(line.substring(5));
	else if ( line.equals("net") )
	    cmdNet();
	else if ( line.startsWith("open ") )
	    cmdOpen(line.substring(5));
	else if ( line.startsWith("read ") )
	    cmdRead(line.substring(5), true);
	else if ( line.startsWith("readq ") )
	    cmdRead(line.substring(6), false);
	else if ( line.equals("reset") )
	    cmdReset();
	else if ( line.equals("step on") )
	    cmdStepOn();
	else if ( line.equals("undo") )
	    cmdUndo();
	else if ( line.equals("write") )
	    cmdWrite(null);
	else if ( line.startsWith("write ") )
	    cmdWrite(line.substring(6));
	else 
	    Log.error("Unknown command `" + line + "'. " + "Try `help'.");
    }

    /** 
     * Checks integrity constraints of current system state.
     */
    private void cmdCheck(String line) 
	throws NoSystemException 
    {
	boolean verbose = false;
	boolean details = false;
	ArrayList invNames = new ArrayList();
	StringTokenizer tokenizer = new StringTokenizer(line);
	// skip command
	tokenizer.nextToken();
	while ( tokenizer.hasMoreTokens() ) {
	    String token = tokenizer.nextToken();
	    if ( token.equals("-v") ) {
		verbose = true;
	    } else if ( token.equals("-d") ) {
		details = true;
	    } else {
		MClassInvariant inv = system().model().getClassInvariant(token);
		if ( inv == null )
		    Log.error("Model has no invariant named `" + token + "'.");
		else
		    invNames.add(token);
	    }
	}

	PrintWriter out;
	if ( Options.quiet && ! Options.quietAndVerboseConstraintCheck ) 
	    out = new PrintWriter(new NullWriter());
	else
	    out = new PrintWriter(Log.out());
	fLastCheckResult = 
	    system().state().check(out, verbose, details, invNames);
    }


    /** 
     * Executes an object manipulation command.
     */
    private void cmdExec(String line)
	throws NoSystemException 
    {
	if ( line.length() == 0 ) {
	    Log.error("Command expected after `!'. Try `help'.");
	    return;
	}

	// compile command
	MSystem system = system();
	List cmdList = 
	    USECompiler.compileCmdList(system.model(), system.state(),
				       new StringReader(line), "<input>",
				       new PrintWriter(System.err));
	
	// compile errors?
	if ( cmdList == null )
	    return;

	Iterator it = cmdList.iterator();
	while ( it.hasNext() ) {
	    MCmd cmd = (MCmd) it.next();
	    Log.trace(this, "--- Executing command: " + cmd);
	    try {
		system.executeCmd(cmd);
		fSession.executedCmd(cmd);
	    } catch (MSystemException ex) {
		Log.error(ex.getMessage());
	    }
	}
    }

    /** 
     * Terminates the program.
     */
    private void cmdExit() {
	fFinished = true;
    }

    /** 
     * Writes a VCG file for a class diagram of the model.
     */
    private void cmdGenVCG(String filename)
	throws NoSystemException 
    {
	MSystem system = system();
	PrintWriter out = null;
	try {
	    if ( filename == null )
		out = new PrintWriter(System.out);
	    else {
		out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	    }
	    ModelToGraph.write(out, system.model());
	} catch (IOException ex) {
	    Log.error(ex.getMessage());
	} finally {
	    if ( out != null ) {
		out.flush();
		if ( filename != null )
		    out.close();
	    }
	}
    }

    /** 
     * Prints commands for generating a metamodel instance.
     */
    private void cmdGenMM(String filename)
	throws NoSystemException 
    {
	MSystem system = system();
	PrintWriter out = null;
	try {
	    if ( filename == null )
		out = new PrintWriter(System.out);
	    else {
		out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	    }
	    MMVisitor v = new MMInstanceGenerator(out);
	    system.model().processWithVisitor(v);
	} catch (IOException ex) {
	    Log.error(ex.getMessage());
	} finally {
	    if ( out != null ) {
		out.flush();
		if ( filename != null )
		    out.close();
	    }
	}
    }

    /** 
     * Writes source files for aspect-based monitoring of applications.
     */
    private void cmdGenMonitor()
	throws NoSystemException 
    {
	MSystem system = system();
	String filename = "USEMonitor.java";
	PrintWriter out = null;
	try {
	    if ( filename == null )
		out = new PrintWriter(System.out);
	    else {
		out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		Log.verbose("writing file `" + filename + "'...");
	    }
	    new MonitorAspectGenerator(out, system.model()).write();
	    Log.verbose("done.");
	} catch (IOException ex) {
	    Log.error(ex.getMessage());
	} finally {
	    if ( out != null ) {
		out.flush();
		if ( filename != null )
		    out.close();
	    }
	}
    }

    /** 
     * Show object diagram with daVinci.
     */
    private void cmdGraph() {
	System.out.println("Command is not available in this version.");
//  	try {
//  	    if ( ! fDaVinci.isRunning() )
//  		fDaVinci.start();
//  	    fDaVinci.send("graph(new_placed(" + fSystem.state().getDaVinciGraph() + "))");
//  	} catch (IOException ex) {
//  	    Log.error(ex.getMessage());
//  	}
    }

    /** 
     * Prints help.
     */
    private void cmdHelp() {
	final String nl = Options.LINE_SEPARATOR;
	Log.println(
	   "Available commands:" + nl + nl +
	   "? EXPR           Compile and evaluate expression EXPR." + nl +
	   "?? EXPR          Like ? but with verbose output of subexpression results." + nl + 
	   "! CMD            Execute state manipulation command CMD. Available commands: " + nl +
	   "  create <id-list> : <class>               Create object(s)" + nl +
	   "  destroy <id-list>                        Destroy object(s)" + nl +
	   "  insert (<id-list>) into <association>    Insert link into association" + nl +
	   "  delete (<id-list>) from <association>    Delete link from association" + nl +
	   "  set <obj-id>.<attr-id> = <expr>          Set attribute value of object" + nl +
	   "  openter <obj-expr> <name>([<expr-list>]) Enter operation <name> in object <obj-expr>." + nl +
	   "  opexit [<result-expr>]                   Exit least recently entered operation." + nl +
	   "\\                Enter command over multiple lines," + nl +
	   "                 finish with a period on a single line." + nl +
	   "check [-v][-d][invariant_list] Check integrity constraints." + nl +
	   "  -v verbose output of subexpression results for failing invariants." + nl +
	   "  -d show instances making an invariant fail." + nl +
	   "  invariant_list check only given invariants." + nl +
	   "check verbose    Do an integrity check with verbose output of subexpression results." + nl +
	   "genmm [file]     Output commands for generating a metamodel instance." + nl +
	   "genmonitor       Writes file for aspect-based monitoring of applications." + nl +
	   "help             Print available commands." + nl +
	   "info SUBCOMMAND  Print info. SUBCOMMAND gives info about:" + nl +
	   "  class NAME       - class NAME." + nl +
	   "  model            - model." + nl +
	   "  state            - current system state." + nl +
	   "  opstack          - currently active operations." + nl +
	   "  prog             - running program." + nl +
	   "  vars             - global variables." + nl +
	   "net              Read commands from socket." + nl +
	   "open FILE        Read model specification from file FILE." + nl +
	   "read FILE        Read commands from file FILE." + nl +
	   "readq FILE       Read commands quietly from file FILE." + nl +
	   "reset            Reset system to empty state." + nl +
	   "step on          Activate single-step mode." + nl +
	   "q, quit or exit  Exit USE." + nl +
	   "undo             Undo last state manipulation command." + nl +
	   "write [file]     Output commands executed so far." + nl);
    }

    /** 
     * Prints information about various things.
     */
    private void cmdInfo(String line)
	throws NoSystemException 
    {
	StringTokenizer tokenizer = new StringTokenizer(line);
	try { 
	    String subCmd = tokenizer.nextToken();
	    if ( subCmd.equals("class") ) {
		String arg = tokenizer.nextToken();
		cmdInfoClass(arg);
	    } else if ( subCmd.equals("model") ) {
		cmdInfoModel();
	    } else if ( subCmd.equals("state") ) {
		cmdInfoState();
	    } else if ( subCmd.equals("opstack") ) {
		cmdInfoOpStack();
	    } else if ( subCmd.equals("prog") ) {
		cmdInfoProg();
	    } else if ( subCmd.equals("vars") ) {
		cmdInfoVars();
	    } else 
		Log.error("Syntax error in info command. Try `help'.");
	} catch (NoSuchElementException ex) {
	    Log.error("Missing argument to `info' command. Try `help'.");
	}
    }

    /** 
     * Prints information about a class.
     */
    private void cmdInfoClass(String classname)
	throws NoSystemException 
    {
	MSystem system = system();
	MClass cls = system.model().getClass(classname);
	if ( cls == null )
	    Log.error("Class `" + classname + "' not found.");
	else {
	    MMVisitor v = 
		new MMPrintVisitor(new PrintWriter(System.out, true));
	    cls.processWithVisitor(v);
	    int numObjects = system.state().objectsOfClass(cls).size();
	    System.out.println(numObjects + 
			       " object" + (( numObjects == 1 ) ? "" : "s") +
			       " of this class in current state.");
	}
    }

    /** 
     * Prints information about the model.
     */
    private void cmdInfoModel()
	throws NoSystemException 
    {
	MSystem system = system();
	MMVisitor v = 
	    new MMPrintVisitor(new PrintWriter(System.out, true));
	system.model().processWithVisitor(v);
	int numObjects = system.state().allObjects().size();
	System.out.println(numObjects + 
			   " object" + (( numObjects == 1 ) ? "" : "s") +
			   " total in current state.");
    }

    /** 
     * Prints the stack of active operations.
     */
    private void cmdInfoOpStack()
	throws NoSystemException 
    {
	MSystem system = system();
	List opStack = system.callStack();
	if ( opStack.isEmpty() )
	    Log.println("no active operations.");
	else {
	    Log.println("active operations: ");
	    int j = 1;
	    for (int i = opStack.size() - 1; i >= 0; i--) {
		MOperationCall opcall = (MOperationCall) opStack.get(i);
		MOperation op = opcall.operation();
		String s = j++ + ". " + op.cls().name() + "::" + op.signature() +
		    " | " +
		    opcall.targetObject().name() + "." + 
		    op.name() + "(" + 
		    StringUtil.fmtSeq(opcall.argValues(), ",") +
		    ")";
		Log.println(s);
	    }
	}
    }

    /** 
     * Prints information about the running program.
     */
    private void cmdInfoProg() {
	long total = Runtime.getRuntime().totalMemory();
	long free = Runtime.getRuntime().freeMemory();
	NumberFormat nf = NumberFormat.getInstance();
	Log.println("(mem: " + 
		    NumberFormat.getPercentInstance().format((double) free / (double) total) + 
		    " = " +
		    nf.format(free) + " bytes free, " + 
		    nf.format(total) + " bytes total)");
    }

    /** 
     * Prints information about the current system state.
     */
    private void cmdInfoState()
	throws NoSystemException 
    {
	MSystem system = system();
	MModel model = system.model();
	MSystemState state = system.state();
	NumberFormat nf = NumberFormat.getInstance();

	System.out.println("State: " + state.name());

	// generate report for objects
	Report report = new Report(3, "$l : $r $r");

	// header
	report.addRow();
	report.addCell("class");
	report.addCell("#objects");
	report.addCell("+ #objects in subclasses");
	report.addRuler('-');

	// data
	Iterator it = model.classes().iterator();
	long total = 0;
	int n;
	while ( it.hasNext() ) {
	    MClass cls = (MClass) it.next();
	    report.addRow();
	    String clsname = cls.name();
	    if ( cls.isAbstract() )
		clsname = '(' + clsname + ')';
	    report.addCell(clsname);
	    n = state.objectsOfClass(cls).size();
	    total += n;
	    report.addCell(nf.format(n));
	    n = state.objectsOfClassAndSubClasses(cls).size();
	    report.addCell(nf.format(n));
	}

	// footer
	report.addRuler('-');
	report.addRow();
	report.addCell("total");
	report.addCell(nf.format(total));
	report.addCell("");

	// print report
	report.printOn(System.out);
	System.out.println();

	// generate report for links
	report = new Report(2, "$l : $r");

	// header
	report.addRow();
	report.addCell("association");
	report.addCell("#links");
	report.addRuler('-');

	// data
	it = model.associations().iterator();
	total = 0;
	while ( it.hasNext() ) {
	    MAssociation assoc = (MAssociation) it.next();
	    report.addRow();
	    report.addCell(assoc.name());
	    n = state.linksOfAssociation(assoc).size();
	    report.addCell(nf.format(n));
	    total += n;
	}

	// footer
	report.addRuler('-');
	report.addRow();
	report.addCell("total");
	report.addCell(nf.format(total));

	// print report
	report.printOn(System.out);
    }

    /** 
     * Prints information about global variables.
     */
    private void cmdInfoVars()
	throws NoSystemException 
    {
	MSystem system = system();
	Iterator it = system.topLevelBindings().iterator();
	while ( it.hasNext() ) {
	    VarBindings.Entry entry = (VarBindings.Entry) it.next();
	    System.out.println(entry);
	}
    }

    /** 
     * Sets input to multi-line mode.
     */
    private void cmdMultiLine() {
	fMultiLineMode = true;
    }

    /** 
     * Reads commands from a socket.
     */
    private void cmdNet() {
	int port = 1777;
	try {
	    Log.verbose("waiting for connection on port " + port + "...");
	    ServerSocket socket = new ServerSocket(port);
	    Socket client = socket.accept();
	    InetAddress clientAddr = client.getInetAddress();
	    Log.verbose("connected to " + clientAddr.getHostName() + "/" + client.getPort());
	    Readline rl = new SocketReadline(client, true, "net>");
	    fReadlineStack.push(rl);
	} catch (IOException ex) {
	    Log.error("Can't bind or listen on port " + port + ".");
	}
    }

    /** 
     * Reads a specification file.
     */
    private void cmdOpen(String filename) {
	MModel model = null;
	Reader r = null;
	try {
	    Log.verbose("compiling specification...");
	    r = new BufferedReader(new FileReader(filename));
	    model = USECompiler.compileSpecification(
		     r, filename,
		     new PrintWriter(System.err),
		     new ModelFactory());
	} catch (FileNotFoundException e) {
	    Log.error("File `" + filename + "' not found.");
	} finally {
	    if ( r != null )
		try { r.close(); } catch (IOException ex) {}
	}

	// compile ok?
	if ( model != null ) {
	    // print some info about model
	    Log.verbose(model.getStats());

	    // create system
	    fSession.setSystem(new MSystem(model));
	}
    }

    /** 
     * Performs a query.
     */
    private void cmdQuery(String line, boolean verboseEval)
	throws NoSystemException 
    {
	Log.trace(this, line);
	if ( line.length() == 0 ) {
	    Log.error("Expression expected after `?'. Try `help'.");
	    return;
	}

	// compile query
	MSystem system = system();
	Expression expr = 
	    USECompiler.compileExpression(system.model(),
					  new StringReader(line), 
					  "<input>",
					  new PrintWriter(System.err),
					  system.topLevelBindings());
	
	// compile errors?
	if ( expr == null )
	    return;

	// evaluate it with current system state
	PrintWriter output = null;
	Evaluator evaluator = new Evaluator();
	if ( verboseEval ) {
	    Log.println("Detailed results of subexpressions:");
	    output = new PrintWriter(Log.out());
	    evaluator.enableEvalTree();
	}
	Value val = evaluator.eval(expr, system.state(), 
				   system.topLevelBindings(), 
				   output);

	// print result
	System.out.println("-> " + val.toStringWithType());
	if ( verboseEval && Options.doGUI )
	    ExprEvalBrowser.create(evaluator.getEvalNodeRoot());
    }

    /** 
     * Reads a file with commands and processes them.
     */
    private void cmdRead(String filename, boolean doEcho) {
	try { 
	    BufferedReader reader = 
		new BufferedReader(new FileReader(filename));
	    // read from file, echo each line as it is read
	    Readline rl;
	    if ( Options.quiet || ! doEcho )
		rl = new StreamReadline(reader, false, "");
	    else
		rl = new StreamReadline(reader, true, filename + "> ");
	    fReadlineStack.push(rl);
	} catch(FileNotFoundException e) {
	    Log.error("File `" + filename + "' not found.");
	}
    }

    /** 
     * Resets system to empty state.
     */
    private void cmdReset() 
	throws NoSystemException 
    {
	fSession.reset();
    }

    /** 
     * Activates step mode.
     */
    private void cmdStepOn() {
	fStepMode = true;
	Log.println("Step mode turned on.");
    }

    /** 
     * Undoes the last object manipulation command.
     */
    private void cmdUndo()
	throws NoSystemException 
    {
	MSystem system = system();
	try {
	    system.undoCmd();
	} catch (MSystemException ex) {
	    Log.error(ex.getMessage());
	}
    }

    /** 
     * Prints commands executed so far.
     */
    private void cmdWrite(String filename)
	throws NoSystemException 
    {
	MSystem system = system();
	PrintWriter out = null;
	try {
	    if ( filename == null )
		out = new PrintWriter(System.out);
	    else {
		out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	    }
	    out.println("-- Script generated by USE " +
			Options.RELEASE_VERSION);
	    out.println();
	    system.writeUSEcmds(out);
	} catch (IOException ex) {
	    Log.error(ex.getMessage());
	} finally {
	    if ( out != null ) {
		out.flush();
		if ( filename != null )
		    out.close();
	    }
	}
    }


    private MSystem system() throws NoSystemException {
	if ( ! fSession.hasSystem() )
	    throw new NoSystemException();
	return fSession.system();
    }
}
