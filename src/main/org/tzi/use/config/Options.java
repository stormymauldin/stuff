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

package org.tzi.use.config;
import org.tzi.use.util.Log;
import org.tzi.use.util.TypedProperties;
import java.io.*;

/**
 * Global options for all packages.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class Options {

    // the internal version
    /* $Format: "public static final String PROJECT_VERSION = \"$ProjectHeader$\";"$ */
public static final String PROJECT_VERSION = "use 2-1-0-release.1 Sun, 09 May 2004 13:57:11 +0200 mr";

    // the release version
    /* $Format: "public static final String RELEASE_VERSION = \"$ReleaseVersion$\";"$ */
public static final String RELEASE_VERSION = "2.1.0";

    /**
     * Name of the file for system properties located in the 
     * etc subdirectory of the installation directory.
     */
    private static final String USE_PROP_FILE = "use.properties";

    /**
     * Name of the file for user properties.
     */
    private static final String USER_PROP_FILE = ".userc";

    /**
     * Name of the property giving the path to the davinci executable.
     */
    private static final String DAVINCI_PATH_P = "use.davinci.path";
    public static String DAVINCI_PATH = "daVinci";

    /**
     * Name of the property giving the number of parallel threads to
     * use for evaluating constraints.  
     */
    private static final String EVAL_NUMTHREADS_P = "use.eval.numthreads";
    public static int EVAL_NUMTHREADS = 1;

    /**
     * Name of the properties of page format for printing.
     */
    private static final String PRINT_PAGEFORMAT_WIDTH_P = 
	"use.print.pageformat.width";
    private static final String PRINT_PAGEFORMAT_HEIGHT_P = 
	"use.print.pageformat.height";
    private static final String PRINT_PAGEFORMAT_ORIENTATION_P = 
	"use.print.pageformat.orientation";
    public static double PRINT_PAGEFORMAT_WIDTH = 595.0;
    public static double PRINT_PAGEFORMAT_HEIGHT = 842.0;
    public static String PRINT_PAGEFORMAT_ORIENTATION = "portrait";

    /**
     * Path for command history file.
     */
    public static String USE_HISTORY_PATH = ".use_history";

    
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    public static String FILE_SEPARATOR = System.getProperty("file.separator");

    private static final String SPEC_FILE_EXTENSION = ".use";
    private static final String DIAGRAM_FILE_EXTENSION = ".dgm";

    /**
     * Name of the property giving the path to the monitor aspect template.
     */
    private static final String MONITOR_ASPECT_TEMPLATE_P = "use.monitor-aspect-template.path";
    public static String MONITOR_ASPECT_TEMPLATE = "etc/USEMonitor.java.template";

    /*
     * Global options set by command line arguments.
     */
    public static String homeDir = null;
    public static String iconDir = null;
    public static boolean compileOnly = false;
    public static boolean daVinciClassDiagram = false;
    public static boolean compileAndPrint = false;
    public static boolean doGUI = true;
    public static boolean suppressWarningsAboutMissingReadlineLibrary = false;
    public static boolean quiet = false;
    public static boolean quietAndVerboseConstraintCheck = false;
    public static boolean disableCollectShorthand = false;

    // non-standard extensions
    public static boolean extClassDiagram = false;

    /**
     * The application's properties set.
     */
    private static TypedProperties props = null;

    /** no instances */
    private Options() {}

    private static void printHelp() {
	System.out.println("usage: use [options] [spec_file] [cmd_file]");
	System.out.println();
	System.out.println("options:");
	System.out.println("  -c            compile only");
	System.out.println("  -cp           compile and print specification");
	System.out.println("  -daVinciClass output a daVinci graph representing the class diagram");
	System.out.println("  -disableCollectShorthand flag use of OCL shorthand notation as error");
	System.out.println("  -nogui        do not use GUI");
	System.out.println("  -h            print help");
	System.out.println("  -H=path       home of use installation");
	System.out.println("  -nr           suppress warnings about missing readline library");
	System.out.println("  -q            reads spec_file, executes cmd_file, and checks constraints");
	System.out.println("                exit code is 1 if constraints fail, otherwise 0");
	System.out.println("  -qv           like -q but with verbose output of constraint check");
	System.out.println("  -v            print verbose messages");
	System.out.println("  -vt           print verbose messages with time info");
	System.out.println("  -V            print version info and exit");
	System.out.println();
	System.out.println("spec_file:      file containing a USE specification");
	System.out.println("cmd_file:       script file containing commands (will be executed on startup)");
	System.out.println();
	System.out.println("diagnostics:");
	System.out.println("  -debug        print lots of messages");
	System.out.println("  -p            print stack traces on errors");
	System.exit(1);
    }


    /**
     * The name of the file containing the specification.
     */
    public static String specFilename = null;

    /**
     * The name of a file containing script commands.
     */
    public static String cmdFilename = null;

    /**
     * The name of the file containing diagram information.
     */
    public static String diagramFilename = null;


    /**
     * Parses command line arguments and sets options accordingly.
     *
     * @return calls System.exit(1) in case of errors
     */
    public static void processArgs(String args[]) {
	// parse command options
	for (int i = 0; i < args.length; i++) {
	    if ( args[i].startsWith("-") ) {
		String arg = args[i].substring(1);
		if ( arg.equals("c") ) 
		    Options.compileOnly = true;
		else if ( arg.equals("cp") ) {
		    Options.compileOnly = true;
		    Options.compileAndPrint = true;
		} else if ( arg.equals("daVinciClass") ) 
		    Options.daVinciClassDiagram = true;
		else if ( arg.equals("disableCollectShorthand") ) 
		    Options.disableCollectShorthand = true;
		else if ( arg.equals("nogui") ) 
		    Options.doGUI = false;
		else if ( arg.startsWith("H=") ) 
		    homeDir = arg.substring(2);
		else if ( arg.equals("nr") ) 
		    suppressWarningsAboutMissingReadlineLibrary = true;
		else if ( arg.equals("XClassDiagram") ) 
		    extClassDiagram = true;
		else if ( arg.equals("q") ) {
		    Options.quiet = true;
		    Options.doGUI = false;
		} else if ( arg.equals("qv") ) {
		    Options.quiet = true;
		    Options.quietAndVerboseConstraintCheck = true;
		    Options.doGUI = false;
		} else if ( arg.equals("v") ) 
		    Log.setVerbose(true);
		else if ( arg.equals("vt") ) {
		    Log.setVerbose(true);
		    Log.setPrintTime(true);
		} else if ( arg.equals("V") ) {
		    System.out.println("release " + RELEASE_VERSION + " ("+
				       PROJECT_VERSION + ")");
		    System.exit(0);
		}
		else if ( arg.equals("debug") ) {
		    Log.setTrace(true);
		    Log.setPrintStackTrace(true);
		    Log.setVerbose(true);
		}
		else if ( arg.equals("p") ) 
		    Log.setPrintStackTrace(true);
		else if ( arg.equals("h") ) 
		    printHelp();
		else {
		    System.out.println("invalid argument `" + arg + 
				       "\', try `use -h' for help.");
		    System.exit(1);
		}
	    } else if ( specFilename == null )
		specFilename = args[i];
	    else if ( cmdFilename == null )
		cmdFilename = args[i];
	    else {
		System.err.println("extra argument `" + args[i] + "'");
		System.exit(1);
	    }
	}

	if ( homeDir == null ) {
	    System.err.println("missing path to USE installation, " + 
			       "try `use -h' for help.");
	    System.exit(1);
	}
	iconDir = homeDir + FILE_SEPARATOR + "images" + FILE_SEPARATOR;
	
	if ( quiet && (specFilename == null || cmdFilename == null) ) {
	    System.err.println("need specification file and command file with option -q,"
			       + LINE_SEPARATOR + "try `use -h' for help.");
	    System.exit(1);
	}
	    
	// load property files
	initProperties(homeDir);

	// args ok, print welcome message if in interactive mode
	if ( ! compileOnly && ! daVinciClassDiagram && ! quiet ) {
	    Log.println(
"use version " + Options.RELEASE_VERSION + ", Copyright (C) 1999-2004 Mark Richters"
/*+
"\nUSE comes with ABSOLUTELY NO WARRANTY; This is free software, and\n" + 
"you are welcome to redistribute it under certain conditions; see\n" +
"the file COPYING for details."
*/
);
	}
    }

    /** 
     * Read properties for use. First the property file from the
     * installation directory is read. Next, we search for a file in
     * the current working directory or in the user's home directory. */
    private static void initProperties(String useHome) {
	props = new TypedProperties(System.getProperties());

	// load the system properties
	File propFile = null;
	propFile = new File(useHome, "etc" + FILE_SEPARATOR + USE_PROP_FILE);
	if ( ! propFile.exists() ) {
	    System.err.println("property file `" + 
			       propFile.getAbsolutePath() + 
			       "' not found. Use -H to set the " +
			       "home of the use installation");
	    System.exit(1);
	}
	loadProperties(propFile);
	
	// load user properties if found
	propFile = new File(props.getProperty("user.dir", null), 
			    USER_PROP_FILE);
	if ( propFile.exists() )
	    loadProperties(propFile);
	else {
	    propFile = new File(props.getProperty("user.home", null), 
				USER_PROP_FILE);
	    if ( propFile.exists() )
		loadProperties(propFile);
	}

	// set values
	DAVINCI_PATH = props.getProperty(DAVINCI_PATH_P, DAVINCI_PATH);

	MONITOR_ASPECT_TEMPLATE = useHome + FILE_SEPARATOR + 
	    props.getProperty(MONITOR_ASPECT_TEMPLATE_P, 
			      MONITOR_ASPECT_TEMPLATE);

	EVAL_NUMTHREADS = 
	    props.getRangeIntProperty(EVAL_NUMTHREADS_P, 
				      EVAL_NUMTHREADS,
				      1, Integer.MAX_VALUE);
 
	PRINT_PAGEFORMAT_WIDTH = 
	    props.getRangeDoubleProperty(PRINT_PAGEFORMAT_WIDTH_P, 
					 PRINT_PAGEFORMAT_WIDTH,
					 72.0, 72.0 * 1000);

	PRINT_PAGEFORMAT_HEIGHT = 
	    props.getRangeDoubleProperty(PRINT_PAGEFORMAT_HEIGHT_P, 
					 PRINT_PAGEFORMAT_HEIGHT,
					 72.0, 72.0 * 1000);

	PRINT_PAGEFORMAT_ORIENTATION = 
	    props.getStringEnumProperty(PRINT_PAGEFORMAT_ORIENTATION_P, 
			"portrait", 
			new String[]{ "landscape", "portrait", "seascape" });

	USE_HISTORY_PATH = props.getProperty("user.home", ".") + 
	    props.getProperty("file.separator") + USE_HISTORY_PATH;

	//props.list(System.out);

	// add our properties to the system properties so that
	// Font.getFont() works with our application specific fonts.
	System.setProperties(props);
	// System.getProperties().list(System.out);
    }

    /**
     * Try to read a property file.
     * @param propFile The property file to read (must exist).
     */
    private static void loadProperties(File propFile) {
	Log.verbose("loading properties from: " + propFile.getAbsolutePath());
	FileInputStream fin = null;
	try {
	    fin = new FileInputStream(propFile);
	    props.load(fin);
	} catch (FileNotFoundException e) {
	    System.err.println("unable to load properties: " + 
			       propFile.getAbsolutePath());
	    System.exit(1);
	} catch (IOException e) {
	    System.err.println("unable to load properties: " +
			       propFile.getAbsolutePath());
	    System.exit(1);
	} 
	try {
	    if ( fin != null )
		fin.close();
	} catch (IOException e) {}
    }
}
