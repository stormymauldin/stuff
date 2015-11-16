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
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.ocl.value.VarBindings;
import java.io.PrintWriter;

/**
 * Context information available when walking the abstract syntax tree.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

class Context {
    private Symtable fVarTable;	// declared variable names
    private Symtable fTypeTable; // declared type names
    // implicit context for some expressions (self or element variable
    // in iterate-based expressions)
    private ExprContext fExprContext; 
    private int fErrorCount;
    private String fFilename;
    private PrintWriter fErr;
    private MModel fModel;
    private MClass fCurrentClass;
    private ModelFactory fModelFactory;
    private MSystemState fSystemState;
    private boolean fInsidePostCondition;

    public Context(String filename, PrintWriter err, 
		   VarBindings globalBindings,
		   ModelFactory factory) {
	fFilename = filename;
	fErr = err;
	fVarTable = new Symtable(globalBindings);
	fTypeTable = new Symtable();
	fExprContext = new ExprContext();
	fModelFactory = factory;
    }

    public String filename() {
	return fFilename;
    }

    public ModelFactory modelFactory() {
	return fModelFactory;
    }

    public Symtable varTable() {
	return fVarTable;
    }

    public Symtable typeTable() {
	return fTypeTable;
    }

    public ExprContext exprContext() {
	return fExprContext;
    }

    public void setModel(MModel model) {
	fModel = model;
    }

    public MModel model() {
	return fModel;
    }

    public void setSystemState(MSystemState systemState) {
	fSystemState = systemState;
    }

    public MSystemState systemState() {
	return fSystemState;
    }

    public void setCurrentClass(MClass cls) {
	fCurrentClass = cls;
    }

    public MClass currentClass() {
	return fCurrentClass;
    }

    void setInsidePostCondition(boolean state) {
	fInsidePostCondition = state;
    }

    boolean insidePostCondition() {
	return fInsidePostCondition;
    }

    public int errorCount() {
	return fErrorCount;
    }

    public void reportWarning(MyToken t, String msg) {
	fErr.println(fFilename + ":" + t.getLine() + ":" + 
		     t.getColumn() + ": Warning: " + msg);
    }
    
    public void reportError(MyToken t, String msg) {
	fErrorCount++;
	fErr.println(fFilename + ":" + t.getLine() + ":" + 
		     t.getColumn() + ": " + msg);
    }
    
    public void reportError(MyToken t, Exception ex) {
	reportError(t, ex.getMessage());
    }
    
    public void reportError(SemanticException ex) {
	fErrorCount++;
	fErr.println(ex.getMessage());
    }

}
