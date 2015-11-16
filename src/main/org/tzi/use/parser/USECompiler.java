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
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystemState;
import java.io.Reader;
import java.io.PrintWriter;
import java.util.List;

/** 
 * Compiler for USE specifications, expressions, and commands. The
 * class has a set of static methods providing a simple interface to
 * the lexer, parser and code generation process.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author      Mark Richters 
 */

public class USECompiler {

    /**
     * Compiles a specification.
     *
     * @param  in the source to be compiled
     * @param  inName name of the source stream
     * @param  err output stream for error messages
     * @return MModel null if there were any errors
     */
    public static MModel compileSpecification(Reader in, 
					      String inName,
					      PrintWriter err,
					      ModelFactory factory) {
	MModel model = null;
	MyLexer lexer = new MyLexer(in, inName, err);
	MyParser parser = new MyParser(inName, lexer, err);
	try {
	    // Parse the specification
	    //Log.verbose("Parsing...");
	    ASTModel astModel = parser.model();
	    if ( parser.errorCount() == 0 ) {

		// Generate code
		//Log.verbose("Translating...");
		Context ctx = new Context(inName, err, null, factory);
		model = astModel.gen(ctx);
		if ( ctx.errorCount() > 0 )
		    model = null;
	    }
	} catch(antlr.RecognitionException e) {
	    err.println(parser.getFilename() +":" + 
			e.getLine() + ":" + e.getColumn() + ": " + 
			e.getMessage());
	} catch(antlr.TokenStreamRecognitionException e) {
	    err.println(parser.getFilename() +":" + 
			e.recog.getLine() + ":" + e.recog.getColumn() + ": " + 
			e.recog.getMessage());
	} catch(antlr.TokenStreamException ex) {
	    err.println(parser.getFilename() +":" + ex.getMessage());
	}
	err.flush();
	return model;
    }

    /**
     * Compiles an expression.
     *
     * @param  in the source to be compiled
     * @param  inName name of the source stream
     * @param  err output stream for error messages
     * @return Expression null if there were any errors
     */
    public static Expression compileExpression(MModel model, 
					       Reader in, 
					       String inName, 
					       PrintWriter err,
					       VarBindings globalBindings) {
	Expression expr = null;
	MyLexer lexer = new MyLexer(in, inName, err);
	MyParser parser = new MyParser(inName, lexer, err);
	try {
	    // Parse the input expression
	    ASTExpression astExpr = parser.expressionOnly();
	    //System.err.println("***" + astExpr.toString());
	    if ( parser.errorCount() == 0 ) {

		// Generate code
		Context ctx = new Context(inName, err, globalBindings, null);
		ctx.setModel(model);
		expr = astExpr.gen(ctx);

		// check for semantic errors
		if ( ctx.errorCount() > 0 )
		    expr = null;
	    }
	} catch(antlr.RecognitionException e) {
	    err.println(parser.getFilename() +":" + 
			e.getLine() + ":" +
			e.getColumn() + ": " + 
			e.getMessage());
	} catch(SemanticException e) {
	    err.println(e.getMessage());
	} catch(antlr.TokenStreamRecognitionException e) {
	    err.println(parser.getFilename() +":" + 
			e.recog.getLine() + ":" + e.recog.getColumn() + ": " + 
			e.recog.getMessage());
	} catch(antlr.TokenStreamException ex) {
	    err.println(ex.getMessage());
	}
	err.flush();
	return expr;
    }

    /**
     * Compiles a list of object manipulation commands.
     *
     * @param  in the source to be compiled
     * @param  inName name of the source stream
     * @param  err output stream for error messages
     * @return List(Cmd) or null if there were any errors
     */
    public static List compileCmdList(MModel model,
				      MSystemState systemState,
				      Reader in, 
				      String inName,
				      PrintWriter err) {
	List cmdList = null;
	MyLexer lexer = new MyLexer(in, inName, err);
	MyParser parser = new MyParser(inName, lexer, err);
	try {
	    // Parse the command
	    ASTCmdList astCmdList = parser.cmdList();
	    if ( parser.errorCount() == 0 ) {

		// Generate code
		Context ctx = new Context(inName, err, 
					  systemState.system().topLevelBindings(), 
					  null);
		ctx.setModel(model);
		ctx.setSystemState(systemState);
		cmdList = astCmdList.gen(ctx);

		// check for semantic errors
		if ( ctx.errorCount() > 0 )
		    cmdList = null;
	    }
	} catch(antlr.RecognitionException e) {
	    err.println(parser.getFilename() +":" + 
			e.getLine() + ":" +
			e.getColumn() + ": " + 
			e.getMessage());
	} catch(SemanticException ex) {
	    err.println(ex.getMessage());
	} catch(antlr.TokenStreamRecognitionException e) {
	    err.println(parser.getFilename() +":" + 
			e.recog.getLine() + ":" + e.recog.getColumn() + ": " + 
			e.recog.getMessage());
	} catch(antlr.TokenStreamException ex) {
	    err.println(ex.getMessage());
	}
	err.flush();
	return cmdList;
    }
}
