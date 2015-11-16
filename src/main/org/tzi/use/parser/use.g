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

// set package for all generated classes
header { 
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

package org.tzi.use.parser; 
}

// ------------------------------------
//  USE parser
// ------------------------------------

{
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
}
class GParser extends Parser;
options {
    exportVocab = GUSE;
    defaultErrorHandler = true;
    buildAST = false;
    k = 3;
    codeGenMakeSwitchThreshold = 2;
    codeGenBitsetTestThreshold = 3;
}

{
    final static String Q_COLLECT  = "collect";
    final static String Q_SELECT   = "select";
    final static String Q_REJECT   = "reject";
    final static String Q_FORALL   = "forAll";
    final static String Q_EXISTS   = "exists";
    final static String Q_ISUNIQUE = "isUnique";
    final static String Q_SORTEDBY = "sortedBy";
    final static String Q_ANY      = "any";
    final static String Q_ONE      = "one";

    final static int Q_COLLECT_ID  = 1;
    final static int Q_SELECT_ID   = 2;
    final static int Q_REJECT_ID   = 3;
    final static int Q_FORALL_ID   = 4;
    final static int Q_EXISTS_ID   = 5;
    final static int Q_ISUNIQUE_ID = 6;
    final static int Q_SORTEDBY_ID = 7;
    final static int Q_ANY_ID      = 8;
    final static int Q_ONE_ID      = 9;

    final static HashMap queryIdentMap = new HashMap();

    static {
        queryIdentMap.put(Q_COLLECT,  new Integer(Q_COLLECT_ID));
        queryIdentMap.put(Q_SELECT,   new Integer(Q_SELECT_ID));
        queryIdentMap.put(Q_REJECT,   new Integer(Q_REJECT_ID));
        queryIdentMap.put(Q_FORALL,   new Integer(Q_FORALL_ID));
        queryIdentMap.put(Q_EXISTS,   new Integer(Q_EXISTS_ID));
        queryIdentMap.put(Q_ISUNIQUE, new Integer(Q_ISUNIQUE_ID));
        queryIdentMap.put(Q_SORTEDBY, new Integer(Q_SORTEDBY_ID));
        queryIdentMap.put(Q_ANY,      new Integer(Q_ANY_ID));
        queryIdentMap.put(Q_ONE,      new Integer(Q_ONE_ID));
    }

    protected boolean isQueryIdent(Token t) {
        return queryIdentMap.containsKey(t.getText());
    }
}

// grammar start

/* ------------------------------------
  model ::= 
    "model" id
    { enumTypeDefinition } 
    {   classDefinition 
      | associationDefinition 
      | "constraints" { invariant | prePost }
    } 
*/
model returns [ASTModel n]
{ 
    ASTEnumTypeDefinition e = null;
    ASTClass c = null;
    ASTAssociation a = null;
    ASTConstraintDefinition cons = null;
    ASTPrePost ppc = null;
    n = null;
}
:
    "model" name:IDENT { n = new ASTModel((MyToken) name); }
    ( e=enumTypeDefinition { n.addEnumTypeDef(e); } )*
    (   ( c=classDefinition { n.addClass(c); } )
      | ( a=associationDefinition { n.addAssociation(a); } )
      | ( "constraints"
          (   cons=invariant { n.addConstraint(cons); } 
            | ppc=prePost { n.addPrePost(ppc); } 
          )*  
        )
    )*
    EOF
    ;


/* ------------------------------------
  enumTypeDefinition ::= 
    "enum" id "{" idList "}" [ ";" ]
*/
enumTypeDefinition returns [ASTEnumTypeDefinition n]
{ List idList; n = null; }
:
    "enum" name:IDENT LBRACE idList=idList RBRACE ( SEMI )?
        { n = new ASTEnumTypeDefinition((MyToken) name, idList); }
    ;


/* ------------------------------------
  classDefinition ::= 
    [ "abstract" ] "class" id [ specialization ] 
    [ attributes ] 
    [ operations ] 
    [ constraints ]
    "end"

  specialization ::= "<" idList
  attributes ::= "attributes" { attributeDefinition }
  operations ::= "operations" { operationDefinition }
  constraints ::= "constraints" { invariantClause }
*/
classDefinition returns [ASTClass n]
{ List idList; boolean isAbstract = false; n = null; }
:
    ( "abstract" { isAbstract = true; } )? 
    "class" name:IDENT { n = new ASTClass((MyToken) name, isAbstract); }
    ( LESS idList=idList { n.addSuperClasses(idList); } )?
    ( "attributes" 
      { ASTAttribute a; }
      ( a=attributeDefinition { n.addAttribute(a); } )* 
    )?
    ( "operations" 
      { ASTOperation op; }
      ( op=operationDefinition { n.addOperation(op); } )* 
    )?
    ( "constraints"
      ( { ASTInvariantClause inv; } 
        inv=invariantClause { n.addInvariantClause(inv); }
      )*
    )?
    "end"
    ;


/* ------------------------------------
  idList ::= id { "," id }
*/
idList returns [List idList]
{ idList = new ArrayList(); }
:
    id0:IDENT { idList.add((MyToken) id0); }
    ( COMMA idn:IDENT { idList.add((MyToken) idn); } )*
    ;


/* ------------------------------------
  attributeDefinition ::= 
    id ":" type [ ";" ]
*/
attributeDefinition returns [ASTAttribute n]
{ ASTType t; n = null; }
:
    name:IDENT COLON t=type ( SEMI )?
        { n = new ASTAttribute((MyToken) name, t); }
    ;


/* ------------------------------------
  operationDefinition ::= 
    id paramList [ ":" type [ "=" expression ] ] 
    { prePostClause } [ ";" ]
*/
operationDefinition returns [ASTOperation n]
{ List pl; ASTType t = null; ASTExpression e = null; 
  ASTPrePostClause ppc = null; n = null; 
}
:
    name:IDENT
    pl=paramList
    ( COLON t=type
      ( EQUAL e=expression )?
    )?
    { n = new ASTOperation((MyToken) name, pl, t, e); }
    ( ppc=prePostClause { n.addPrePostClause(ppc); } )*
    ( SEMI )?
    ;


/* ------------------------------------
  associationDefinition ::= 
    ( "association" | "aggregation" | "composition" ) 
    id "between"
    associationEnd associationEnd { associationEnd }
    "end"
*/
associationDefinition returns [ASTAssociation n]
{ ASTAssociationEnd ae; n = null; }
:
    { MyToken t = (MyToken) LT(1); }
    ( "association" | "aggregation" | "composition" )
    //    ( classDefinition | (name:IDENT { n = new ASTAssociation(t, (MyToken) name); }) )
    name:IDENT { n = new ASTAssociation(t, (MyToken) name); }
    "between"
    ae=associationEnd { n.addEnd(ae); }
    ( ae=associationEnd { n.addEnd(ae); } )+
    "end"
    ;


/* ------------------------------------
  associationEnd ::= 
    id "[" multiplicity "]" [ "role" id ] [ "ordered" ] [ ";" ]
*/
associationEnd returns [ASTAssociationEnd n]
{ ASTMultiplicity m; n = null; }
:
    name:IDENT LBRACK m=multiplicity RBRACK 
    { n = new ASTAssociationEnd((MyToken) name, m); } 
    ( "role" rn:IDENT { n.setRolename((MyToken) rn); } )?
    ( "ordered" { n.setOrdered(); } )?
    ( SEMI )?
    ;


/* ------------------------------------
  multiplicity ::= 
    multiplicityRange { "," multiplicityRange }

  multiplicityRange ::=
    multiplicitySpec [ ".." multiplicitySpec ]

  multiplicitySpec ::=
    "*" | INT
*/
multiplicity returns [ASTMultiplicity n]
{ ASTMultiplicityRange mr; n = null; }
:
    { 
	MyToken t = (MyToken) LT(1); // remember start position of expression
	n = new ASTMultiplicity(t); 
    }
    mr=multiplicityRange { n.addRange(mr); }
    ( COMMA mr=multiplicityRange  { n.addRange(mr); } )*
    ;

multiplicityRange returns [ASTMultiplicityRange n]
{ int ms1, ms2; n = null; }
:
    ms1=multiplicitySpec { n = new ASTMultiplicityRange(ms1); }
    ( DOTDOT ms2=multiplicitySpec { n.setHigh(ms2); } )?
    ;

multiplicitySpec returns [int m]
{ m = -1; }
:
      i:INT { m = Integer.parseInt(i.getText()); }
    | STAR  { m = -1; }
    ;


/* ------------------------------------
  constraintDefinition ::= 
    invariant | prePost
*/
//  constraintDefinition returns [ASTConstraintDefinition n]
//  { n = null; }
//  :
//      n=invariant // | prePost
//      ;

/* ------------------------------------
  invariant ::= 
    invariantContext invariantClause { invariantClause }

  invariantContext := 
    "context" [ id ":" ] simpleType
*/
invariant returns [ASTConstraintDefinition n]
{ n = null; ASTType t = null; ASTInvariantClause inv = null; }
:
    { n = new ASTConstraintDefinition(); }
    "context"
    ( v:IDENT COLON { n.setVarName((MyToken) v); } )? // requires k = 2

    t=simpleType { n.setType(t); }
    ( inv=invariantClause { n.addInvariantClause(inv); } )+
    ;


/* ------------------------------------
  invariantClause ::= 
    "inv" [ id ] ":" expression
*/
invariantClause returns [ASTInvariantClause n]
{ ASTExpression e; n = null; }
:
    "inv" ( name:IDENT )? COLON e=expression
    { n = new ASTInvariantClause((MyToken) name, e); }
    ;


/* ------------------------------------
  prePost ::=
    prePostContext prePostClause { prePostClause }

  prePostContext := 
    "context" id "::" id paramList [ ":" type ]
*/
prePost returns [ASTPrePost n]
{ n = null; List pl = null; ASTType rt = null; ASTPrePostClause ppc = null; }
:
    "context" classname:IDENT COLON_COLON opname:IDENT pl=paramList ( COLON rt=type )?
    { n = new ASTPrePost((MyToken) classname, (MyToken) opname, pl, rt); }
    ( ppc=prePostClause { n.addPrePostClause(ppc); } )+
    ;

/* ------------------------------------
  prePostClause :=
    ("pre" | "post") [ id ] ":" expression
*/
prePostClause returns [ASTPrePostClause n]
{ ASTExpression e; n = null; }
:
    { MyToken t = (MyToken) LT(1); }
    ( "pre" | "post" )  ( name:IDENT )? COLON e=expression
    { n = new ASTPrePostClause(t, (MyToken) name, e); }
    ;


/* ------------------------------------
  paramList ::= 
    "(" [ variableDeclaration { "," variableDeclaration } ] ")"
*/
paramList returns [List paramList]
{ ASTVariableDeclaration v; paramList = new ArrayList(); }
:
    LPAREN
    ( 
      v=variableDeclaration { paramList.add(v); }
      ( COMMA v=variableDeclaration  { paramList.add(v); } )* 
    )?
    RPAREN
    ;


/* ------------------------------------
  variableDeclaration ::= 
    id ":" type
*/
variableDeclaration returns [ASTVariableDeclaration n]
{ ASTType t; n = null; }
:
    name:IDENT COLON t=type
    { n = new ASTVariableDeclaration((MyToken) name, t); }
    ;


/* ------------------------------------
  expressionOnly ::= 
    conditionalImpliesExpression
*/
expressionOnly returns [ASTExpression n]
{ n = null; }
:
    n=expression EOF
    ;


/* ------------------------------------
  expression ::= 
    { "let" id [ : type ] "=" expression "in" } conditionalImpliesExpression
*/
expression returns [ASTExpression n]
{ ASTLetExpression prevLet = null, firstLet = null; ASTType t = null; 
  ASTExpression e1, e2; n = null; 
}
:
    { MyToken tok = (MyToken) LT(1); /* remember start of expression */ }
    ( 
      "let" name:IDENT ( COLON t=type )? EQUAL e1=expression "in"
       { ASTLetExpression nextLet = new ASTLetExpression((MyToken) name, t, e1);
         if ( firstLet == null ) 
             firstLet = nextLet;
         if ( prevLet != null ) 
             prevLet.setInExpr(nextLet);
         prevLet = nextLet;
       }
    )*

    n=conditionalImpliesExpression
    { if ( n != null ) 
         n.setStartToken(tok);
      if ( prevLet != null ) { 
         prevLet.setInExpr(n);
         n = firstLet;
      }
    }
    ;


/* ------------------------------------
  conditionalImpliesExpression ::= 
    conditionalOrExpression { "implies" conditionalOrExpression }
*/
conditionalImpliesExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=conditionalOrExpression
    ( op:"implies" n1=conditionalOrExpression 
        { n = new ASTBinaryExpression((MyToken) op, n, n1); } 
    )*
    ;


/* ------------------------------------
  conditionalOrExpression ::= 
    conditionalXOrExpression { "or" conditionalXOrExpression }
*/
conditionalOrExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=conditionalXOrExpression
    ( op:"or" n1=conditionalXOrExpression
        { n = new ASTBinaryExpression((MyToken) op, n, n1); } 
    )*
    ;


/* ------------------------------------
  conditionalXOrExpression ::= 
    conditionalAndExpression { "xor" conditionalAndExpression }
*/
conditionalXOrExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=conditionalAndExpression
    ( op:"xor" n1=conditionalAndExpression
        { n = new ASTBinaryExpression((MyToken) op, n, n1); } 
    )*
    ;


/* ------------------------------------
  conditionalAndExpression ::= 
    equalityExpression { "and" equalityExpression }
*/
conditionalAndExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=equalityExpression
    ( op:"and" n1=equalityExpression
        { n = new ASTBinaryExpression((MyToken) op, n, n1); } 
    )*
    ;


/* ------------------------------------
  equalityExpression ::= 
    relationalExpression { ("=" | "<>") relationalExpression }
*/
equalityExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=relationalExpression 
    ( { MyToken op = (MyToken) LT(1); }
      (EQUAL | NOT_EQUAL) n1=relationalExpression
        { n = new ASTBinaryExpression(op, n, n1); } 
    )*
    ;


/* ------------------------------------
  relationalExpression ::= 
    additiveExpression { ("<" | ">" | "<=" | ">=") additiveExpression }
*/
relationalExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=additiveExpression 
    ( { MyToken op = (MyToken) LT(1); }
      (LESS | GREATER | LESS_EQUAL | GREATER_EQUAL) n1=additiveExpression 
        { n = new ASTBinaryExpression(op, n, n1); } 
    )*
    ;


/* ------------------------------------
  additiveExpression ::= 
    multiplicativeExpression { ("+" | "-") multiplicativeExpression }
*/
additiveExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=multiplicativeExpression 
    ( { MyToken op = (MyToken) LT(1); }
      (PLUS | MINUS) n1=multiplicativeExpression
        { n = new ASTBinaryExpression(op, n, n1); } 
    )*
    ;


/* ------------------------------------
  multiplicativeExpression ::= 
    unaryExpression { ("*" | "/" | "div") unaryExpression }
*/
multiplicativeExpression returns [ASTExpression n]
{ ASTExpression n1; n = null; }
: 
    n=unaryExpression 
    ( { MyToken op = (MyToken) LT(1); }
      (STAR | SLASH | "div") n1=unaryExpression
        { n = new ASTBinaryExpression(op, n, n1); } 
    )*
    ;


/* ------------------------------------
  unaryExpression ::= 
      ( "not" | "-" | "+" ) unaryExpression
    | postfixExpression
*/
unaryExpression returns [ASTExpression n]
{ n = null; }
: 
      ( { MyToken op = (MyToken) LT(1); }
        ("not" | MINUS | PLUS ) 
        n=unaryExpression { n = new ASTUnaryExpression((MyToken) op, n); }
      )
    | n=postfixExpression
    ;


/* ------------------------------------
  postfixExpression ::= 
      primaryExpression { ( "." | "->" ) propertyCall }
*/
postfixExpression returns [ASTExpression n]
{ boolean arrow; n = null; }
: 
    n=primaryExpression 
    ( 
     ( ARROW { arrow = true; } | DOT { arrow = false; } ) 
     n=propertyCall[n, arrow] 
    )*
    ;


/* ------------------------------------
  primaryExpression ::= 
      literal
    | propertyCall
    | "(" expression ")"
    | ifExpression

  Note: propertyCall includes variables
*/

primaryExpression returns [ASTExpression n]
{ n = null; }
: 
      n=literal
    | n=propertyCall[null, false]
    | LPAREN n=expression RPAREN
    | n=ifExpression
    // HACK: the following requires k=3
    | id1:IDENT DOT "allInstances" 
        { n = new ASTAllInstancesExpression((MyToken) id1); }
    ;


/* ------------------------------------
  propertyCall ::= 
      queryId   "(" [ elemVarsDeclaration "|" ] expression ")"
    | "iterate" "(" elemVarsDeclaration ";" variableInitialization "|" expression ")"
    | id [ "(" actualParameterList ")" ]


  Note: source may be null (see primaryExpression).
*/
propertyCall[ASTExpression source, boolean followsArrow] returns [ASTExpression n]
{ n = null; }
:
      // this semantic predicate disambiguates operations from
      // iterate-based expressions which have a different syntax (the
      // OCL grammar is very loose here).
      { isQueryIdent(LT(1)) }? 
      n=queryExpression[source]
    | n=iterateExpression[source]
    | n=operationExpression[source, followsArrow]
    | n=typeExpression[source, followsArrow]
    ;


/* ------------------------------------
  queryExpression ::= 
    ("select" | "reject" | "collect" | "exists" | "forAll" | "isUnique" | "sortedBy" ) 
    "(" [ elemVarsDeclaration "|" ] expression ")"
*/

// Using lookahead k=2 instead of a syntactic predicate for
// disambiguating between variableDeclaration and expression (both may
// start with an IDENT) seems to be the better alternative in
// queryOperation. It avoids guessing (k=1) which turns off actions in
// almost all rules and blows up the generated parser.

queryExpression[ASTExpression range] returns [ASTExpression n]
{ 
    ASTElemVarsDeclaration decls = new ASTElemVarsDeclaration(); 
    n = null; 
}
:	
    op:IDENT 
    LPAREN 
    ( decls=elemVarsDeclaration BAR )? // requires k=2 
    n=expression
    RPAREN
    { n = new ASTQueryExpression((MyToken) op, range, decls, n); }
    ;


/* ------------------------------------
  iterateExpression ::= 
    "iterate" "(" 
    elemVarsDeclaration ";" 
    variableInitialization "|"
    expression ")"
*/
iterateExpression[ASTExpression range] returns [ASTExpression n]
{ 
    ASTElemVarsDeclaration decls = null; 
    ASTVariableInitialization init = null; 
    n = null;
}
:
    i:"iterate"
    LPAREN
    decls=elemVarsDeclaration SEMI
    init=variableInitialization BAR
    n=expression
    RPAREN
    { n = new ASTIterateExpression((MyToken) i, range, decls, init, n); }
    ;


/* ------------------------------------
  operationExpression ::= 
    id [ "@" "pre" ] [ "(" [ expression { "," expression } ] ")" ]
*/

// Operations always require parentheses even if no arguments are
// required. This makes it easier, for example, to distinguish a
// class-defined operation from an attribute access operation where
// both operations may have the same name.

operationExpression[ASTExpression source, boolean followsArrow] 
    returns [ASTOperationExpression n]
{ ASTExpression e; n = null; }
:
    name:IDENT 
    { n = new ASTOperationExpression((MyToken) name, source, followsArrow); }
    ( AT "pre" { n.setIsPre(); } ) ? 
    (
      LPAREN { n.hasParentheses(); }
      ( 
	     e=expression { n.addArg(e); }
	     ( COMMA e=expression { n.addArg(e); } )* 
	  )?
      RPAREN
    )?
    ;


/* ------------------------------------
  typeExpression ::= 
    ("oclAsType" | "oclIsKindOf" | "oclIsTypeOf") LPAREN type RPAREN
*/

typeExpression[ASTExpression source, boolean followsArrow] 
    returns [ASTTypeArgExpression n]
{ ASTType t = null; n = null; }
:
	{ MyToken opToken = (MyToken) LT(1); }
	( "oclAsType" | "oclIsKindOf" | "oclIsTypeOf" )
	LPAREN t=type RPAREN 
      { n = new ASTTypeArgExpression(opToken, source, t, followsArrow); }
    ;


/* ------------------------------------
  elemVarsDeclaration ::= 
    idList [ ":" type ]
*/
elemVarsDeclaration returns [ASTElemVarsDeclaration n]
{ List idList; ASTType t = null; n = null; }
:
    idList=idList
    ( COLON t=type )?
    { n = new ASTElemVarsDeclaration(idList, t); }
    ;


/* ------------------------------------
  variableInitialization ::= 
    id ":" type "=" expression
*/
variableInitialization returns [ASTVariableInitialization n]
{ ASTType t; ASTExpression e; n = null; }
:
    name:IDENT COLON t=type EQUAL e=expression
    { n = new ASTVariableInitialization((MyToken) name, t, e); }
    ;


/* ------------------------------------
  ifExpression ::= 
    "if" expression "then" expression "else" expression "endif"
*/
ifExpression returns [ASTExpression n]
{ ASTExpression cond, t, e; n = null; }
:
    i:"if" cond=expression "then" t=expression "else" e=expression "endif"
        { n = new ASTIfExpression((MyToken) i, cond, t, e); } 
    ;


/* ------------------------------------
  literal ::= 
      "true"
    | "false"
    | INT
    | REAL
    | STRING
    | "#" id
    | collectionLiteral
    | emptyCollectionLiteral
    | undefinedLiteral
    | tupleLiteral
*/
literal returns [ASTExpression n]
{ n = null; }
:
      t:"true"   { n = new ASTBooleanLiteral(true); }
    | f:"false"  { n = new ASTBooleanLiteral(false); }
    | i:INT    { n = new ASTIntegerLiteral((MyToken) i); }
    | r:REAL   { n = new ASTRealLiteral((MyToken) r); }
    | s:STRING { n = new ASTStringLiteral((MyToken) s); }
    | HASH enumLit:IDENT { n = new ASTEnumLiteral((MyToken) enumLit); }
    | n=collectionLiteral
    | n=emptyCollectionLiteral
    | n=undefinedLiteral
    | n=tupleLiteral
    ;


/* ------------------------------------
  collectionLiteral ::= 
    ( "Set" | "Sequence" | "Bag" ) "{" collectionItem { "," collectionItem } "}"
*/
collectionLiteral returns [ASTCollectionLiteral n]
{ ASTCollectionItem ci; n = null; }
:
    { MyToken op = (MyToken) LT(1); } 
    ( "Set" | "Sequence" | "Bag" ) 
    { n = new ASTCollectionLiteral(op); }
    LBRACE 
    ci=collectionItem { n.addItem(ci); } 
    ( COMMA ci=collectionItem { n.addItem(ci); } )* 
    RBRACE
    ;

/* ------------------------------------
  collectionItem ::=
    expression [ ".." expression ]
*/
collectionItem returns [ASTCollectionItem n]
{ ASTExpression e; n = new ASTCollectionItem(); }
:
    e=expression { n.setFirst(e); } 
    ( DOTDOT e=expression { n.setSecond(e); } )?
    ;


/* ------------------------------------
  emptyCollectionLiteral ::= 
    "oclEmpty" "(" collectionType ")"

  Hack for avoiding typing problems with e.g. Set{}
*/
emptyCollectionLiteral returns [ASTEmptyCollectionLiteral n]
{ ASTType t = null; n = null; }
:
    "oclEmpty" LPAREN t=collectionType RPAREN
    { n = new ASTEmptyCollectionLiteral(t); }
    ;


/* ------------------------------------
  undefinedLiteral ::= 
    "oclUndefined" "(" type ")"

  OCL extension
*/
undefinedLiteral returns [ASTUndefinedLiteral n]
{ ASTType t = null; n = null; }
:
    "oclUndefined" LPAREN t=type RPAREN
    { n = new ASTUndefinedLiteral(t); }
    ;


/* ------------------------------------
  tupleLiteral ::= 
    "Tuple" "{" tupleItem { "," tupleItem } "}"
*/
tupleLiteral returns [ASTTupleLiteral n]
{ ASTTupleItem ti; n = null; List tiList = new ArrayList(); }
:
    "Tuple"
    LBRACE
    ti=tupleItem { tiList.add(ti); } 
    ( COMMA ti=tupleItem { tiList.add(ti); } )*
    RBRACE
    { n = new ASTTupleLiteral(tiList); }
    ;

/* ------------------------------------
  tupleItem ::= id ":" expression
*/
tupleItem returns [ASTTupleItem n]
{ ASTExpression e; n = null; }
:
    name:IDENT COLON e=expression 
    { n = new ASTTupleItem((MyToken) name, e); } 
    ;


/* ------------------------------------
  type ::= 
      simpleType 
    | collectionType
    | tupleType
*/
type returns [ASTType n]
{ n = null; }
:
    { MyToken tok = (MyToken) LT(1); /* remember start of type */ }
    (
      n=simpleType
    | n=collectionType
    | n=tupleType
    )
    { n.setStartToken(tok); }
    ;


/* ------------------------------------
  simpleType ::= id 

  A simple type may be a basic type (Integer, Real, Boolean, String),
  an enumeration type, an object type, or OclAny.
*/
simpleType returns [ASTSimpleType n]
{ n = null; }
:
    name:IDENT { n = new ASTSimpleType((MyToken) name); }
    ;


/* ------------------------------------
  collectionType ::= 
    ( "Collection" | "Set" | "Sequence" | "Bag" ) "(" type ")"
*/
collectionType returns [ASTCollectionType n]
{ ASTType elemType = null; n = null; }
:
    { MyToken op = (MyToken) LT(1); } 
    ( "Collection" | "Set" | "Sequence" | "Bag" ) 
    LPAREN elemType=type RPAREN
    { n = new ASTCollectionType(op, elemType); }
    ;


/* ------------------------------------
  tupleType ::= "Tuple" "(" tuplePart { "," tuplePart } ")"
*/
tupleType returns [ASTTupleType n]
{ ASTTuplePart tp; n = null; List tpList = new ArrayList(); }
:
    "Tuple" LPAREN 
    tp=tuplePart { tpList.add(tp); } 
    ( COMMA tp=tuplePart { tpList.add(tp); } )* 
    RPAREN
    { n = new ASTTupleType(tpList); }
    ;


/* ------------------------------------
  tuplePart ::= id ":" type
*/
tuplePart returns [ASTTuplePart n]
{ ASTType t; n = null; }
:
    name:IDENT COLON t=type
    { n = new ASTTuplePart((MyToken) name, t); }
    ;


// grammar for commands

/* ------------------------------------
  cmdList ::= cmd { cmd }
*/
cmdList returns [ASTCmdList cmdList]
{ cmdList = new ASTCmdList(); ASTCmd c; }
:
    c=cmd { cmdList.add(c); }
    ( c=cmd { cmdList.add(c); } )*
    EOF
    ;


/* ------------------------------------
  cmd ::= cmdStmt [ ";" ]
*/
cmd returns [ASTCmd n]
{ n = null; }
:
    n=cmdStmt ( SEMI )?
    ;


/* ------------------------------------
  cmdStmt ::= 
      createCmd 
    | destroyCmd 
    | insertCmd 
    | deleteCmd 
    | setCmd 
    | opEnterCmd
    | opExitCmd
    | letCmd
*/
cmdStmt returns [ASTCmd n]
{ n = null; }
:
      n=createCmd 
    | n=destroyCmd
    | n=insertCmd
    | n=deleteCmd
    | n=setCmd
    | n=opEnterCmd
    | n=opExitCmd
    | n=letCmd
    ;


/* ------------------------------------
  Creates one or more objects and binds variables to them.

  createCmd ::= "create" idList ":" simpleType
*/
createCmd returns [ASTCreateCmd n]
{ List idList; ASTType t; n = null; }
:
    "create" idList=idList 
    COLON t=simpleType
    { n = new ASTCreateCmd(idList, t); }
    ;


/* ------------------------------------
  Destroys one or more objects (expression may be a collection)

  destroyCmd ::= "destroy" expression
*/
destroyCmd returns [ASTDestroyCmd n]
{ ASTExpression e = null; n = null; }
:
    "destroy" e=expression
    { n = new ASTDestroyCmd(e); }
    ;


/* ------------------------------------
  Inserts a link (tuple of objects) into an association.

  insertCmd ::= "insert" "(" expression "," expression { "," expression } ")" "into" id
*/
insertCmd returns [ASTInsertCmd n]
{ ASTExpression e; List exprList = new ArrayList(); n = null; }
:
    "insert" LPAREN 
    e=expression { exprList.add(e); } COMMA
    e=expression { exprList.add(e); } ( COMMA e=expression { exprList.add(e); } )* 
    RPAREN "into" id:IDENT
    { n = new ASTInsertCmd(exprList, (MyToken) id); }
    ;


/* ------------------------------------
  Deletes a link (tuple of objects) from an association.

  deleteCmd ::= "delete" "(" expression "," expression { "," expression } ")" "from" id
*/
deleteCmd returns [ASTDeleteCmd n]
{ ASTExpression e; List exprList = new ArrayList(); n = null; }
:
    "delete" LPAREN
    e=expression { exprList.add(e); } COMMA
    e=expression { exprList.add(e); } ( COMMA e=expression { exprList.add(e); } )*
    RPAREN "from" id:IDENT
    { n = new ASTDeleteCmd(exprList, (MyToken) id); }
    ;


/* ------------------------------------

  Assigns a value to an attribute of an object. The first "expression"
  must be an attribute access expression giving an "l-value" for an
  attribute.

  setCmd ::= "set" expression ":=" expression 
*/
setCmd returns [ASTSetCmd n]
{ ASTExpression e1, e2; n = null; }
:
    "set" e1=expression COLON_EQUAL e2=expression
    { n = new ASTSetCmd(e1, e2); }
    ;


/* ------------------------------------
  A call of an operation which may have side-effects. The first
  expression must have an object type.

  opEnterCmd ::= 
    "openter" expression id "(" [ expression { "," expression } ] ")" 
*/
opEnterCmd returns [ASTOpEnterCmd n]
{ ASTExpression e; n = null; }
:
    "openter" 
    e=expression id:IDENT { n = new ASTOpEnterCmd(e, (MyToken) id); }
    LPAREN
    ( e=expression { n.addArg(e); } ( COMMA e=expression { n.addArg(e); } )* )?
    RPAREN 
    ;

/* ------------------------------------
  Command to exit an operation. A result expression is required if the
  operation to be exited declared a result type.

  opExitCmd ::= "opexit" [ expression ]
*/
opExitCmd returns [ASTOpExitCmd n]
{ ASTExpression e = null; n = null; }
:
    "opexit" 
    ( // ambiguity between let-expression and let-cmd. Default is to
      // match expression.
       options { warnWhenFollowAmbig=false; } : e=expression
    )?
    { n = new ASTOpExitCmd(e); }
    ;

/* ------------------------------------
  Command to bind a toplevel variable.

  letCmd ::= "let" id [ ":" type ] "=" expression
*/
letCmd returns [ASTLetCmd n]
{ ASTExpression e = null; ASTType t = null; n = null; }
:
    "let" name:IDENT ( COLON t=type )? EQUAL e=expression
     { n = new ASTLetCmd((MyToken) name, t, e); }
    ;


// ------------------------------------
// USE lexer
// ------------------------------------

class GLexer extends Lexer;
options {
    exportVocab = GUSE;

	// MyLexer.reportError depends on defaultErrorHandler == true for
	// providing correct column number information
    defaultErrorHandler = true;	

    // don't automatically test all tokens for literals
    testLiterals = false;    

    k = 2;
}

// Whitespace -- ignored
WS:
    ( ' '
    | '\t'
    | '\f'
    | (   "\r\n"
        | '\r'
	| '\n'
      )
      { newline(); }
    )
    { $setType(Token.SKIP); }
    ;

// Single-line comments
SL_COMMENT:
    ("//" | "--")
    (~('\n'|'\r'))* ('\n'|'\r'('\n')?)
    { $setType(Token.SKIP); newline(); }
    ;

// multiple-line comments
ML_COMMENT:
    "/*"
    ( options { generateAmbigWarnings = false; } : { LA(2)!='/' }? '*'
    | '\r' '\n' { newline(); }
    | '\r'	{ newline(); }
    | '\n'	{ newline(); }
    | ~('*'|'\n'|'\r')
    )*
    "*/"
    { $setType(Token.SKIP); }
    ;

// Use paraphrases for nice error messages
//EOF 		options { paraphrase = "\"end of file\""; } : EOF;
ARROW 		options { paraphrase = "'->'"; }        : "->";
AT     		options { paraphrase = "'@'"; }         : '@';
BAR 		options { paraphrase = "'|'"; }         : '|';
COLON 		options { paraphrase = "':'"; }         : ':';
COLON_COLON	options { paraphrase = "'::'"; }        : "::";
COLON_EQUAL	options { paraphrase = "':='"; }        : ":=";
COMMA 		options { paraphrase = "','"; }         : ',';
DOT 		options { paraphrase = "'.'"; }         : '.';
DOTDOT 		options { paraphrase = "'..'"; }        : "..";
EQUAL 		options { paraphrase = "'='"; }         : '=';
GREATER 	options { paraphrase = "'>'"; }         : '>';
GREATER_EQUAL 	options { paraphrase = "'>='"; }        : ">=";
HASH 		options { paraphrase = "'#'"; }         : '#';
LBRACE 		options { paraphrase = "'{'"; }         : '{';
LBRACK 		options { paraphrase = "'['"; }         : '[';
LESS 		options { paraphrase = "'<'"; }         : '<';
LESS_EQUAL 	options { paraphrase = "'<='"; }        : "<=";
LPAREN 		options { paraphrase = "'('"; }         : '(';
MINUS 		options { paraphrase = "'-'"; }         : '-';
NOT_EQUAL 	options { paraphrase = "'<>'"; }        : "<>";
PLUS 		options { paraphrase = "'+'"; }         : '+';
RBRACE 		options { paraphrase = "'}'"; }         : '}';
RBRACK 		options { paraphrase = "']'"; }         : ']';
RPAREN		options { paraphrase = "')'"; }         : ')';
SEMI		options { paraphrase = "';'"; }         : ';';
SLASH 		options { paraphrase = "'/'"; }         : '/';
STAR 		options { paraphrase = "'*'"; }         : '*';

protected
INT:
    ('0'..'9')+
    ;

protected
REAL:
    INT ( '.' INT )? 
    ( ('e'|'E') ('+'|'-')? INT )?
    ;

RANGE_OR_INT:
      ( INT ".." )    => INT    { $setType(INT); }
    | ( INT '.' INT)  => REAL   { $setType(REAL); }
    | ( INT ('e'|'E'))  => REAL { $setType(REAL); }
    |   INT                     { $setType(INT); }
    ;

// String literals

STRING
{ char c1; StringBuffer s = new StringBuffer(); }
:	
    '\'' { s.append('\''); } 
    (   c1=ESC { s.append(c1); } 
      | 
        c2:~('\''|'\\') { s.append(c2); } 
    )* 
    '\'' { s.append('\''); } 
    { $setText(s.toString()); }
    ;

// escape sequence -- note that this is protected; it can only be called
//   from another lexer rule -- it will not ever directly return a token to
//   the parser
// There are various ambiguities hushed in this rule.  The optional
// '0'...'7' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched.  ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC returns [char c]
{ c = 0; int h0,h1,h2,h3; }
:
    '\\'
     (  'n' { c = '\n'; }
     | 'r' { c = '\r'; }
     | 't' { c = '\t'; }
     | 'b' { c = '\b'; }
     | 'f' { c = '\f'; }
     | '"' { c = '"'; }
     | '\'' { c = '\''; }
     | '\\' { c = '\\'; }
     | ('u')+ 
       h3=HEX_DIGIT h2=HEX_DIGIT h1=HEX_DIGIT h0=HEX_DIGIT 
	 { c = (char) (h0 + h1 * 16 + h2 * 16 * 16 + h3 * 16 * 16 * 16); }
     | o1:'0'..'3' { c = (char) Character.digit(o1, 8); } 
        ( options { warnWhenFollowAmbig = false; } : o2:'0'..'7' 
          { c = (char) (c * 8 + Character.digit(o2, 8)); } 
          ( options { warnWhenFollowAmbig = false; } : o3:'0'..'7' 
            { c = (char) (c * 8 + Character.digit(o3, 8)); } 
          )? 
        )?
     | d1:'4'..'7' { c = (char) Character.digit(d1, 8); } 
       ( options { warnWhenFollowAmbig = false; } : d2:'0'..'7' 
         { c = (char) (c * 8 + Character.digit(d2, 8)); } 
       )?
     )
     ;

// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT returns [int n]
{ n = 0; }
:
    (   c1:'0'..'9' { n = Character.digit(c1, 16); }
      | c2:'A'..'F' { n = Character.digit(c2, 16); }
      | c3:'a'..'f' { n = Character.digit(c3, 16); }
    )
    ;


// An identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer.

IDENT
    options { 
        testLiterals = true; 
	paraphrase = "an identifier";
    } :	
    ('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')*
    ;

// A dummy rule to force vocabulary to be all characters (except
// special ones that ANTLR uses internally (0 to 2)

protected
VOCAB:	
    '\3'..'\377'
    ;
