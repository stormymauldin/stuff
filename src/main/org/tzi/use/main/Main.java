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
import org.tzi.use.config.Options;
import org.tzi.use.parser.USECompiler;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.mm.MMVisitor;
import org.tzi.use.uml.mm.MMPrintVisitor;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.util.Log;
import java.io.*;

import java.awt.Font;
import javax.swing.UIDefaults;
import javax.swing.ImageIcon;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

/**
 * Main class.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public final class Main {

    private static void initGUIdefaults() {
	MetalLookAndFeel.setCurrentTheme(new MyTheme());
    }

    public static void main(String args[]) {
	// read and set global options, setup application properties
	Options.processArgs(args);
	if ( Options.doGUI ) {
	    initGUIdefaults();
	}

	Session session = new Session();
	MModel model = null;
	MSystem system = null;

	// compile spec if filename given as argument
	if ( Options.specFilename != null ) {
	    Reader r = null;
	    try {
		Log.verbose("compiling specification...");
		r = new BufferedReader(new FileReader(Options.specFilename));
		model = USECompiler.compileSpecification(
			 r, Options.specFilename,
			 new PrintWriter(System.err),
			 new ModelFactory());
	    } catch (FileNotFoundException e) {
		Log.error("File `" + Options.specFilename + "' not found.");
		System.exit(1);
	    } finally {
		if ( r != null )
		    try { r.close(); } catch (IOException ex) {}
	    }

	    // compile errors?
	    if ( model == null ) {
		System.exit(1);
	    }
	    
	    if ( Options.compileOnly ) {
		Log.verbose("no errors.");
		if ( Options.compileAndPrint ) {
		    MMVisitor v = 
			new MMPrintVisitor(new PrintWriter(System.out, true));
		    model.processWithVisitor(v);
		}
		System.exit(0);
	    }
	    
	    // print some info about model
	    Log.verbose(model.getStats());

	    // create system
	    system = new MSystem(model);
	    session.setSystem(system);
	}

	// create thread for shell
	Shell sh = new Shell(session);
	Thread t = new Thread(sh);
	t.start();

	MainWindow mainWindow = null;
	if ( Options.doGUI ) {
	    mainWindow = MainWindow.create(session);
	} 

	// wait on exit from shell
	try {
	    t.join();
	} catch (InterruptedException ex) { }

	if ( mainWindow != null ) {
	    mainWindow.setVisible(false);
	    mainWindow.dispose();
	}

	int exitCode = 0;
	if ( Options.quiet && ! sh.lastCheckResult() )
	    exitCode = 1;
	System.exit(exitCode);
    }
}

/**
 * A theme with full control over fonts and customized tree display.
 */
class MyTheme extends DefaultMetalTheme {
    private FontUIResource controlFont;
    private FontUIResource systemFont;
    private FontUIResource userFont;
    private FontUIResource smallFont;

    MyTheme() {
	//System.out.println("font: " + Font.getFont("use.gui.controlFont"));
        controlFont = new FontUIResource(Font.getFont("use.gui.controlFont", 
						      super.getControlTextFont()));
        systemFont = new FontUIResource(Font.getFont("use.gui.systemFont", 
						      super.getSystemTextFont()));
        userFont = new FontUIResource(Font.getFont("use.gui.userFont", 
						      super.getUserTextFont()));
        smallFont = new FontUIResource(Font.getFont("use.gui.smallFont", 
						      super.getSubTextFont()));
    }
    public String getName() { return "USE"; }
    public FontUIResource getControlTextFont() { return controlFont;}
    public FontUIResource getSystemTextFont() { return systemFont;}
    public FontUIResource getUserTextFont() { return userFont;}
    public FontUIResource getMenuTextFont() { return controlFont;}
    public FontUIResource getWindowTitleFont() { return controlFont;}
    public FontUIResource getSubTextFont() { return smallFont;}

    public void addCustomEntriesToTable(UIDefaults table) {
	initIcon(table, "Tree.expandedIcon", "TreeExpanded.gif");
	initIcon(table, "Tree.collapsedIcon", "TreeCollapsed.gif");
	initIcon(table, "Tree.leafIcon", "TreeLeaf.gif");
	initIcon(table, "Tree.openIcon", "TreeOpen.gif");
	initIcon(table, "Tree.closedIcon", "TreeClosed.gif");
	table.put("Desktop.background", table.get("Menu.background"));
    }

    private void initIcon(UIDefaults table, String property, String iconFilename) {
	table.put(property, new ImageIcon(Options.iconDir + iconFilename));
    }
}


