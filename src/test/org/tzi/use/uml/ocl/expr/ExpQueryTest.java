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

import junit.framework.TestCase;

import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.ocl.value.BagValue;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.IntegerValue;
import org.tzi.use.uml.ocl.value.SetValue;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemState;

/**
 * Test ExpQuery and subclasses.
 * 
 * @version $ProjectVersion: 2-1-0-release.1 $
 * @author Mark Richters
 */

public class ExpQueryTest extends TestCase {
    private MSystemState fState;
    private Expression fSet123;
    private Expression fEGreater1;
    private Expression fE1NotEqualsE2;
    private Evaluator e;

    protected void setUp() throws Exception {
        fState = new MSystem(new ModelFactory().createModel("Test")).state();
        // create range
        Expression[] args1 =
            new Expression[] {
                new ExpConstInteger(1),
                new ExpConstInteger(2),
                new ExpConstInteger(3)};
        fSet123 = ExpStdOp.create("mkSet", args1);

        // create query expression
        Expression[] args2 =
            new Expression[] {
                new ExpVariable("e", TypeFactory.mkInteger()),
                new ExpConstInteger(1)};
        fEGreater1 = ExpStdOp.create(">", args2);

        Expression[] args3 =
            new Expression[] {
                new ExpVariable("e1", TypeFactory.mkInteger()),
                new ExpVariable("e2", TypeFactory.mkInteger())};
        fE1NotEqualsE2 = ExpStdOp.create("<>", args3);
        e = new Evaluator();
    }

    public void testSelect1() throws ExpInvalidException {
        Expression exp = new ExpSelect(null, fSet123, new ExpConstBoolean(true));
        Value[] values =
            new Value[] { new IntegerValue(1), new IntegerValue(2), new IntegerValue(3)};
        assertEquals(
            exp.toString(),
            new SetValue(TypeFactory.mkInteger(), values),
            e.eval(exp, fState));
    }

    public void testSelect2() throws ExpInvalidException {
        Expression exp =
            new ExpSelect(
                new VarDecl("e", TypeFactory.mkInteger()),
                fSet123,
                fEGreater1);
        Value[] values = new Value[] { new IntegerValue(2), new IntegerValue(3)};
        assertEquals(
            exp.toString(),
            new SetValue(TypeFactory.mkInteger(), values),
            e.eval(exp, fState));
    }

    public void testReject() throws ExpInvalidException {
        Expression exp =
            new ExpReject(
                new VarDecl("e", TypeFactory.mkInteger()),
                fSet123,
                fEGreater1);
        Value[] values = new Value[] { new IntegerValue(1)};
        assertEquals(
            exp.toString(),
            new SetValue(TypeFactory.mkInteger(), values),
            e.eval(exp, fState));
    }

    public void testExists1() throws ExpInvalidException {
        Expression exp =
            new ExpExists(
                new VarDecl("e", TypeFactory.mkInteger()),
                fSet123,
                fEGreater1);
        assertEquals(exp.toString(), BooleanValue.TRUE, e.eval(exp, fState));
    }

    public void testExists2() throws ExpInvalidException {
        VarDeclList elemVars = new VarDeclList(true);
        elemVars.add(new VarDecl("e1", TypeFactory.mkInteger()));
        elemVars.add(new VarDecl("e2", TypeFactory.mkInteger()));
        Expression exp = new ExpExists(elemVars, fSet123, fE1NotEqualsE2);
        assertEquals(exp.toString(), BooleanValue.TRUE, e.eval(exp, fState));
    }

    public void testForAll1() throws ExpInvalidException {
        Expression exp =
            new ExpForAll(
                new VarDecl("e", TypeFactory.mkInteger()),
                fSet123,
                fEGreater1);
        assertEquals(exp.toString(), BooleanValue.FALSE, e.eval(exp, fState));
    }

    public void testForAll2() throws ExpInvalidException {
        VarDeclList elemVars = new VarDeclList(true);
        elemVars.add(new VarDecl("e1", TypeFactory.mkInteger()));
        elemVars.add(new VarDecl("e2", TypeFactory.mkInteger()));
        Expression exp = new ExpForAll(elemVars, fSet123, fE1NotEqualsE2);
        assertEquals(exp.toString(), BooleanValue.FALSE, e.eval(exp, fState));
    }

    public void testCollect() throws ExpInvalidException {
        Expression[] args =
            new Expression[] {
                new ExpVariable("e", TypeFactory.mkInteger()),
                new ExpConstInteger(2)};
        Expression mult2Exp = ExpStdOp.create("*", args);
        Expression exp =
            new ExpCollect(new VarDecl("e", TypeFactory.mkInteger()), fSet123, mult2Exp);
        Value[] values =
            new Value[] { new IntegerValue(2), new IntegerValue(4), new IntegerValue(6)};
        assertEquals(
            exp.toString(),
            new BagValue(TypeFactory.mkInteger(), values),
            e.eval(exp, fState));
    }

    public void testIterate1() throws ExpInvalidException {
        // Set { 1..100 }->iterate(e; acc : Integer= 0 | acc + e);
        Expression[] args1 = new Expression[100];
        for (int i = 0; i < 100; i++)
            args1[i] = new ExpConstInteger(i + 1);
        Expression set1To100 = ExpStdOp.create("mkSet", args1);

        // create query expression
        Expression[] args2 =
            new Expression[] {
                new ExpVariable("acc", TypeFactory.mkInteger()),
                new ExpVariable("e", TypeFactory.mkInteger())};
        Expression accPlusE = ExpStdOp.create("+", args2);

        Expression exp =
            new ExpIterate(
                new VarDecl("e", TypeFactory.mkInteger()),
                new VarInitializer(
                    "acc",
                    TypeFactory.mkInteger(),
                    new ExpConstInteger(0)),
                set1To100,
                accPlusE);
        assertEquals(exp.toString(), new IntegerValue(5050), e.eval(exp, fState));
    }

    public void testIterate2() throws ExpInvalidException {
        // Set { 1..3 }->iterate(e1, e2; acc : Integer= 0 | acc + e1 * e2));
        Expression[] args1 = new Expression[3];
        for (int i = 0; i < 3; i++)
            args1[i] = new ExpConstInteger(i + 1);
        Expression set1To3 = ExpStdOp.create("mkSet", args1);

        // create query expression
        Expression[] args2 =
            new Expression[] {
                new ExpVariable("e1", TypeFactory.mkInteger()),
                new ExpVariable("e2", TypeFactory.mkInteger())};
        Expression e1Multe2 = ExpStdOp.create("*", args2);
        args2 =
            new Expression[] {
                new ExpVariable("acc", TypeFactory.mkInteger()),
                e1Multe2 };
        Expression add = ExpStdOp.create("+", args2);

        VarDeclList elemVars = new VarDeclList(true);
        elemVars.add(new VarDecl("e1", TypeFactory.mkInteger()));
        elemVars.add(new VarDecl("e2", TypeFactory.mkInteger()));
        Expression exp =
            new ExpIterate(
                elemVars,
                new VarInitializer(
                    "acc",
                    TypeFactory.mkInteger(),
                    new ExpConstInteger(0)),
                set1To3,
                add);
        assertEquals(exp.toString(), new IntegerValue(36), e.eval(exp, fState));
    }
}
