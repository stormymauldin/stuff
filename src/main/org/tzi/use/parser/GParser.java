// $ANTLR 2.7.1: "use.g" -> "GParser.java"$
 
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

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class GParser extends antlr.LLkParser
       implements GUSETokenTypes
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

protected GParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public GParser(TokenBuffer tokenBuf) {
  this(tokenBuf,3);
}

protected GParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public GParser(TokenStream lexer) {
  this(lexer,3);
}

public GParser(ParserSharedInputState state) {
  super(state,3);
  tokenNames = _tokenNames;
}

	public final ASTModel  model() throws RecognitionException, TokenStreamException {
		ASTModel n;
		
		Token  name = null;
		
		ASTEnumTypeDefinition e = null;
		ASTClass c = null;
		ASTAssociation a = null;
		ASTConstraintDefinition cons = null;
		ASTPrePost ppc = null;
		n = null;
		
		
		try {      // for error handling
			match(LITERAL_model);
			name = LT(1);
			match(IDENT);
			n = new ASTModel((MyToken) name);
			{
			_loop3:
			do {
				if ((LA(1)==LITERAL_enum)) {
					e=enumTypeDefinition();
					n.addEnumTypeDef(e);
				}
				else {
					break _loop3;
				}
				
			} while (true);
			}
			{
			_loop10:
			do {
				switch ( LA(1)) {
				case LITERAL_abstract:
				case LITERAL_class:
				{
					{
					c=classDefinition();
					n.addClass(c);
					}
					break;
				}
				case LITERAL_association:
				case LITERAL_aggregation:
				case LITERAL_composition:
				{
					{
					a=associationDefinition();
					n.addAssociation(a);
					}
					break;
				}
				case LITERAL_constraints:
				{
					{
					match(LITERAL_constraints);
					{
					_loop9:
					do {
						if ((LA(1)==LITERAL_context) && (LA(2)==IDENT) && (LA(3)==COLON||LA(3)==LITERAL_inv)) {
							cons=invariant();
							n.addConstraint(cons);
						}
						else if ((LA(1)==LITERAL_context) && (LA(2)==IDENT) && (LA(3)==COLON_COLON)) {
							ppc=prePost();
							n.addPrePost(ppc);
						}
						else {
							break _loop9;
						}
						
					} while (true);
					}
					}
					break;
				}
				default:
				{
					break _loop10;
				}
				}
			} while (true);
			}
			match(Token.EOF_TYPE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return n;
	}
	
	public final ASTEnumTypeDefinition  enumTypeDefinition() throws RecognitionException, TokenStreamException {
		ASTEnumTypeDefinition n;
		
		Token  name = null;
		List idList; n = null;
		
		try {      // for error handling
			match(LITERAL_enum);
			name = LT(1);
			match(IDENT);
			match(LBRACE);
			idList=idList();
			match(RBRACE);
			{
			switch ( LA(1)) {
			case SEMI:
			{
				match(SEMI);
				break;
			}
			case EOF:
			case LITERAL_constraints:
			case LITERAL_enum:
			case LITERAL_abstract:
			case LITERAL_class:
			case LITERAL_association:
			case LITERAL_aggregation:
			case LITERAL_composition:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n = new ASTEnumTypeDefinition((MyToken) name, idList);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		return n;
	}
	
	public final ASTClass  classDefinition() throws RecognitionException, TokenStreamException {
		ASTClass n;
		
		Token  name = null;
		List idList; boolean isAbstract = false; n = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_abstract:
			{
				match(LITERAL_abstract);
				isAbstract = true;
				break;
			}
			case LITERAL_class:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LITERAL_class);
			name = LT(1);
			match(IDENT);
			n = new ASTClass((MyToken) name, isAbstract);
			{
			switch ( LA(1)) {
			case LESS:
			{
				match(LESS);
				idList=idList();
				n.addSuperClasses(idList);
				break;
			}
			case LITERAL_constraints:
			case LITERAL_attributes:
			case LITERAL_operations:
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LITERAL_attributes:
			{
				match(LITERAL_attributes);
				ASTAttribute a;
				{
				_loop18:
				do {
					if ((LA(1)==IDENT)) {
						a=attributeDefinition();
						n.addAttribute(a);
					}
					else {
						break _loop18;
					}
					
				} while (true);
				}
				break;
			}
			case LITERAL_constraints:
			case LITERAL_operations:
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LITERAL_operations:
			{
				match(LITERAL_operations);
				ASTOperation op;
				{
				_loop21:
				do {
					if ((LA(1)==IDENT)) {
						op=operationDefinition();
						n.addOperation(op);
					}
					else {
						break _loop21;
					}
					
				} while (true);
				}
				break;
			}
			case LITERAL_constraints:
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LITERAL_constraints:
			{
				match(LITERAL_constraints);
				{
				_loop24:
				do {
					if ((LA(1)==LITERAL_inv)) {
						ASTInvariantClause inv;
						inv=invariantClause();
						n.addInvariantClause(inv);
					}
					else {
						break _loop24;
					}
					
				} while (true);
				}
				break;
			}
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LITERAL_end);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return n;
	}
	
	public final ASTAssociation  associationDefinition() throws RecognitionException, TokenStreamException {
		ASTAssociation n;
		
		Token  name = null;
		ASTAssociationEnd ae; n = null;
		
		try {      // for error handling
			MyToken t = (MyToken) LT(1);
			{
			switch ( LA(1)) {
			case LITERAL_association:
			{
				match(LITERAL_association);
				break;
			}
			case LITERAL_aggregation:
			{
				match(LITERAL_aggregation);
				break;
			}
			case LITERAL_composition:
			{
				match(LITERAL_composition);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			name = LT(1);
			match(IDENT);
			n = new ASTAssociation(t, (MyToken) name);
			match(LITERAL_between);
			ae=associationEnd();
			n.addEnd(ae);
			{
			int _cnt39=0;
			_loop39:
			do {
				if ((LA(1)==IDENT)) {
					ae=associationEnd();
					n.addEnd(ae);
				}
				else {
					if ( _cnt39>=1 ) { break _loop39; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt39++;
			} while (true);
			}
			match(LITERAL_end);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return n;
	}
	
	public final ASTConstraintDefinition  invariant() throws RecognitionException, TokenStreamException {
		ASTConstraintDefinition n;
		
		Token  v = null;
		n = null; ASTType t = null; ASTInvariantClause inv = null;
		
		try {      // for error handling
			n = new ASTConstraintDefinition();
			match(LITERAL_context);
			{
			if ((LA(1)==IDENT) && (LA(2)==COLON)) {
				v = LT(1);
				match(IDENT);
				match(COLON);
				n.setVarName((MyToken) v);
			}
			else if ((LA(1)==IDENT) && (LA(2)==LITERAL_inv)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			t=simpleType();
			n.setType(t);
			{
			int _cnt53=0;
			_loop53:
			do {
				if ((LA(1)==LITERAL_inv)) {
					inv=invariantClause();
					n.addInvariantClause(inv);
				}
				else {
					if ( _cnt53>=1 ) { break _loop53; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt53++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		return n;
	}
	
	public final ASTPrePost  prePost() throws RecognitionException, TokenStreamException {
		ASTPrePost n;
		
		Token  classname = null;
		Token  opname = null;
		n = null; List pl = null; ASTType rt = null; ASTPrePostClause ppc = null;
		
		try {      // for error handling
			match(LITERAL_context);
			classname = LT(1);
			match(IDENT);
			match(COLON_COLON);
			opname = LT(1);
			match(IDENT);
			pl=paramList();
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				rt=type();
				break;
			}
			case LITERAL_pre:
			case LITERAL_post:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n = new ASTPrePost((MyToken) classname, (MyToken) opname, pl, rt);
			{
			int _cnt59=0;
			_loop59:
			do {
				if ((LA(1)==LITERAL_pre||LA(1)==LITERAL_post)) {
					ppc=prePostClause();
					n.addPrePostClause(ppc);
				}
				else {
					if ( _cnt59>=1 ) { break _loop59; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt59++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		return n;
	}
	
	public final List  idList() throws RecognitionException, TokenStreamException {
		List idList;
		
		Token  id0 = null;
		Token  idn = null;
		idList = new ArrayList();
		
		try {      // for error handling
			id0 = LT(1);
			match(IDENT);
			idList.add((MyToken) id0);
			{
			_loop27:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					idn = LT(1);
					match(IDENT);
					idList.add((MyToken) idn);
				}
				else {
					break _loop27;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_4);
		}
		return idList;
	}
	
	public final ASTAttribute  attributeDefinition() throws RecognitionException, TokenStreamException {
		ASTAttribute n;
		
		Token  name = null;
		ASTType t; n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			match(COLON);
			t=type();
			{
			switch ( LA(1)) {
			case SEMI:
			{
				match(SEMI);
				break;
			}
			case IDENT:
			case LITERAL_constraints:
			case LITERAL_operations:
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n = new ASTAttribute((MyToken) name, t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_5);
		}
		return n;
	}
	
	public final ASTOperation  operationDefinition() throws RecognitionException, TokenStreamException {
		ASTOperation n;
		
		Token  name = null;
		List pl; ASTType t = null; ASTExpression e = null; 
		ASTPrePostClause ppc = null; n = null; 
		
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			pl=paramList();
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				t=type();
				{
				switch ( LA(1)) {
				case EQUAL:
				{
					match(EQUAL);
					e=expression();
					break;
				}
				case IDENT:
				case LITERAL_constraints:
				case SEMI:
				case LITERAL_end:
				case LITERAL_pre:
				case LITERAL_post:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case IDENT:
			case LITERAL_constraints:
			case SEMI:
			case LITERAL_end:
			case LITERAL_pre:
			case LITERAL_post:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n = new ASTOperation((MyToken) name, pl, t, e);
			{
			_loop34:
			do {
				if ((LA(1)==LITERAL_pre||LA(1)==LITERAL_post)) {
					ppc=prePostClause();
					n.addPrePostClause(ppc);
				}
				else {
					break _loop34;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case SEMI:
			{
				match(SEMI);
				break;
			}
			case IDENT:
			case LITERAL_constraints:
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_6);
		}
		return n;
	}
	
	public final ASTInvariantClause  invariantClause() throws RecognitionException, TokenStreamException {
		ASTInvariantClause n;
		
		Token  name = null;
		ASTExpression e; n = null;
		
		try {      // for error handling
			match(LITERAL_inv);
			{
			switch ( LA(1)) {
			case IDENT:
			{
				name = LT(1);
				match(IDENT);
				break;
			}
			case COLON:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(COLON);
			e=expression();
			n = new ASTInvariantClause((MyToken) name, e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_7);
		}
		return n;
	}
	
	public final ASTType  type() throws RecognitionException, TokenStreamException {
		ASTType n;
		
		n = null;
		
		try {      // for error handling
			MyToken tok = (MyToken) LT(1); /* remember start of type */
			{
			switch ( LA(1)) {
			case IDENT:
			{
				n=simpleType();
				break;
			}
			case LITERAL_Set:
			case LITERAL_Sequence:
			case LITERAL_Bag:
			case LITERAL_Collection:
			{
				n=collectionType();
				break;
			}
			case LITERAL_Tuple:
			{
				n=tupleType();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n.setStartToken(tok);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_8);
		}
		return n;
	}
	
	public final List  paramList() throws RecognitionException, TokenStreamException {
		List paramList;
		
		ASTVariableDeclaration v; paramList = new ArrayList();
		
		try {      // for error handling
			match(LPAREN);
			{
			switch ( LA(1)) {
			case IDENT:
			{
				v=variableDeclaration();
				paramList.add(v);
				{
				_loop66:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						v=variableDeclaration();
						paramList.add(v);
					}
					else {
						break _loop66;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_9);
		}
		return paramList;
	}
	
	public final ASTExpression  expression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  name = null;
		ASTLetExpression prevLet = null, firstLet = null; ASTType t = null; 
		ASTExpression e1, e2; n = null; 
		
		
		try {      // for error handling
			MyToken tok = (MyToken) LT(1); /* remember start of expression */
			{
			_loop72:
			do {
				if ((LA(1)==LITERAL_let)) {
					match(LITERAL_let);
					name = LT(1);
					match(IDENT);
					{
					switch ( LA(1)) {
					case COLON:
					{
						match(COLON);
						t=type();
						break;
					}
					case EQUAL:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(EQUAL);
					e1=expression();
					match(LITERAL_in);
					ASTLetExpression nextLet = new ASTLetExpression((MyToken) name, t, e1);
					if ( firstLet == null ) 
					firstLet = nextLet;
					if ( prevLet != null ) 
					prevLet.setInExpr(nextLet);
					prevLet = nextLet;
					
				}
				else {
					break _loop72;
				}
				
			} while (true);
			}
			n=conditionalImpliesExpression();
			if ( n != null ) 
			n.setStartToken(tok);
			if ( prevLet != null ) { 
			prevLet.setInExpr(n);
			n = firstLet;
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_10);
		}
		return n;
	}
	
	public final ASTPrePostClause  prePostClause() throws RecognitionException, TokenStreamException {
		ASTPrePostClause n;
		
		Token  name = null;
		ASTExpression e; n = null;
		
		try {      // for error handling
			MyToken t = (MyToken) LT(1);
			{
			switch ( LA(1)) {
			case LITERAL_pre:
			{
				match(LITERAL_pre);
				break;
			}
			case LITERAL_post:
			{
				match(LITERAL_post);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case IDENT:
			{
				name = LT(1);
				match(IDENT);
				break;
			}
			case COLON:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(COLON);
			e=expression();
			n = new ASTPrePostClause(t, (MyToken) name, e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_11);
		}
		return n;
	}
	
	public final ASTAssociationEnd  associationEnd() throws RecognitionException, TokenStreamException {
		ASTAssociationEnd n;
		
		Token  name = null;
		Token  rn = null;
		ASTMultiplicity m; n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			match(LBRACK);
			m=multiplicity();
			match(RBRACK);
			n = new ASTAssociationEnd((MyToken) name, m);
			{
			switch ( LA(1)) {
			case LITERAL_role:
			{
				match(LITERAL_role);
				rn = LT(1);
				match(IDENT);
				n.setRolename((MyToken) rn);
				break;
			}
			case IDENT:
			case SEMI:
			case LITERAL_end:
			case LITERAL_ordered:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LITERAL_ordered:
			{
				match(LITERAL_ordered);
				n.setOrdered();
				break;
			}
			case IDENT:
			case SEMI:
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case SEMI:
			{
				match(SEMI);
				break;
			}
			case IDENT:
			case LITERAL_end:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_12);
		}
		return n;
	}
	
	public final ASTMultiplicity  multiplicity() throws RecognitionException, TokenStreamException {
		ASTMultiplicity n;
		
		ASTMultiplicityRange mr; n = null;
		
		try {      // for error handling
			
				MyToken t = (MyToken) LT(1); // remember start position of expression
				n = new ASTMultiplicity(t); 
			
			mr=multiplicityRange();
			n.addRange(mr);
			{
			_loop46:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					mr=multiplicityRange();
					n.addRange(mr);
				}
				else {
					break _loop46;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_13);
		}
		return n;
	}
	
	public final ASTMultiplicityRange  multiplicityRange() throws RecognitionException, TokenStreamException {
		ASTMultiplicityRange n;
		
		int ms1, ms2; n = null;
		
		try {      // for error handling
			ms1=multiplicitySpec();
			n = new ASTMultiplicityRange(ms1);
			{
			switch ( LA(1)) {
			case DOTDOT:
			{
				match(DOTDOT);
				ms2=multiplicitySpec();
				n.setHigh(ms2);
				break;
			}
			case COMMA:
			case RBRACK:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_14);
		}
		return n;
	}
	
	public final int  multiplicitySpec() throws RecognitionException, TokenStreamException {
		int m;
		
		Token  i = null;
		m = -1;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INT:
			{
				i = LT(1);
				match(INT);
				m = Integer.parseInt(i.getText());
				break;
			}
			case STAR:
			{
				match(STAR);
				m = -1;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_15);
		}
		return m;
	}
	
	public final ASTSimpleType  simpleType() throws RecognitionException, TokenStreamException {
		ASTSimpleType n;
		
		Token  name = null;
		n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			n = new ASTSimpleType((MyToken) name);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_16);
		}
		return n;
	}
	
	public final ASTVariableDeclaration  variableDeclaration() throws RecognitionException, TokenStreamException {
		ASTVariableDeclaration n;
		
		Token  name = null;
		ASTType t; n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			match(COLON);
			t=type();
			n = new ASTVariableDeclaration((MyToken) name, t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_17);
		}
		return n;
	}
	
	public final ASTExpression  expressionOnly() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		n = null;
		
		try {      // for error handling
			n=expression();
			match(Token.EOF_TYPE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return n;
	}
	
	public final ASTExpression  conditionalImpliesExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  op = null;
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=conditionalOrExpression();
			{
			_loop75:
			do {
				if ((LA(1)==LITERAL_implies)) {
					op = LT(1);
					match(LITERAL_implies);
					n1=conditionalOrExpression();
					n = new ASTBinaryExpression((MyToken) op, n, n1);
				}
				else {
					break _loop75;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_10);
		}
		return n;
	}
	
	public final ASTExpression  conditionalOrExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  op = null;
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=conditionalXOrExpression();
			{
			_loop78:
			do {
				if ((LA(1)==LITERAL_or)) {
					op = LT(1);
					match(LITERAL_or);
					n1=conditionalXOrExpression();
					n = new ASTBinaryExpression((MyToken) op, n, n1);
				}
				else {
					break _loop78;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_18);
		}
		return n;
	}
	
	public final ASTExpression  conditionalXOrExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  op = null;
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=conditionalAndExpression();
			{
			_loop81:
			do {
				if ((LA(1)==LITERAL_xor)) {
					op = LT(1);
					match(LITERAL_xor);
					n1=conditionalAndExpression();
					n = new ASTBinaryExpression((MyToken) op, n, n1);
				}
				else {
					break _loop81;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_19);
		}
		return n;
	}
	
	public final ASTExpression  conditionalAndExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  op = null;
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=equalityExpression();
			{
			_loop84:
			do {
				if ((LA(1)==LITERAL_and)) {
					op = LT(1);
					match(LITERAL_and);
					n1=equalityExpression();
					n = new ASTBinaryExpression((MyToken) op, n, n1);
				}
				else {
					break _loop84;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_20);
		}
		return n;
	}
	
	public final ASTExpression  equalityExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=relationalExpression();
			{
			_loop88:
			do {
				if ((LA(1)==EQUAL||LA(1)==NOT_EQUAL)) {
					MyToken op = (MyToken) LT(1);
					{
					switch ( LA(1)) {
					case EQUAL:
					{
						match(EQUAL);
						break;
					}
					case NOT_EQUAL:
					{
						match(NOT_EQUAL);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					n1=relationalExpression();
					n = new ASTBinaryExpression(op, n, n1);
				}
				else {
					break _loop88;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_21);
		}
		return n;
	}
	
	public final ASTExpression  relationalExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=additiveExpression();
			{
			_loop92:
			do {
				if ((_tokenSet_22.member(LA(1)))) {
					MyToken op = (MyToken) LT(1);
					{
					switch ( LA(1)) {
					case LESS:
					{
						match(LESS);
						break;
					}
					case GREATER:
					{
						match(GREATER);
						break;
					}
					case LESS_EQUAL:
					{
						match(LESS_EQUAL);
						break;
					}
					case GREATER_EQUAL:
					{
						match(GREATER_EQUAL);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					n1=additiveExpression();
					n = new ASTBinaryExpression(op, n, n1);
				}
				else {
					break _loop92;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_23);
		}
		return n;
	}
	
	public final ASTExpression  additiveExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=multiplicativeExpression();
			{
			_loop96:
			do {
				if ((LA(1)==PLUS||LA(1)==MINUS)) {
					MyToken op = (MyToken) LT(1);
					{
					switch ( LA(1)) {
					case PLUS:
					{
						match(PLUS);
						break;
					}
					case MINUS:
					{
						match(MINUS);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					n1=multiplicativeExpression();
					n = new ASTBinaryExpression(op, n, n1);
				}
				else {
					break _loop96;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_24);
		}
		return n;
	}
	
	public final ASTExpression  multiplicativeExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		ASTExpression n1; n = null;
		
		try {      // for error handling
			n=unaryExpression();
			{
			_loop100:
			do {
				if ((_tokenSet_25.member(LA(1)))) {
					MyToken op = (MyToken) LT(1);
					{
					switch ( LA(1)) {
					case STAR:
					{
						match(STAR);
						break;
					}
					case SLASH:
					{
						match(SLASH);
						break;
					}
					case LITERAL_div:
					{
						match(LITERAL_div);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					n1=unaryExpression();
					n = new ASTBinaryExpression(op, n, n1);
				}
				else {
					break _loop100;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_26);
		}
		return n;
	}
	
	public final ASTExpression  unaryExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		n = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case PLUS:
			case MINUS:
			case LITERAL_not:
			{
				{
				MyToken op = (MyToken) LT(1);
				{
				switch ( LA(1)) {
				case LITERAL_not:
				{
					match(LITERAL_not);
					break;
				}
				case MINUS:
				{
					match(MINUS);
					break;
				}
				case PLUS:
				{
					match(PLUS);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				n=unaryExpression();
				n = new ASTUnaryExpression((MyToken) op, n);
				}
				break;
			}
			case IDENT:
			case INT:
			case LPAREN:
			case LITERAL_iterate:
			case LITERAL_oclAsType:
			case LITERAL_oclIsKindOf:
			case LITERAL_oclIsTypeOf:
			case LITERAL_if:
			case LITERAL_true:
			case LITERAL_false:
			case REAL:
			case STRING:
			case HASH:
			case LITERAL_Set:
			case LITERAL_Sequence:
			case LITERAL_Bag:
			case LITERAL_oclEmpty:
			case LITERAL_oclUndefined:
			case LITERAL_Tuple:
			{
				n=postfixExpression();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_27);
		}
		return n;
	}
	
	public final ASTExpression  postfixExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		boolean arrow; n = null;
		
		try {      // for error handling
			n=primaryExpression();
			{
			_loop107:
			do {
				if ((LA(1)==ARROW||LA(1)==DOT)) {
					{
					switch ( LA(1)) {
					case ARROW:
					{
						match(ARROW);
						arrow = true;
						break;
					}
					case DOT:
					{
						match(DOT);
						arrow = false;
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					n=propertyCall(n, arrow);
				}
				else {
					break _loop107;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_27);
		}
		return n;
	}
	
	public final ASTExpression  primaryExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  id1 = null;
		n = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INT:
			case LITERAL_true:
			case LITERAL_false:
			case REAL:
			case STRING:
			case HASH:
			case LITERAL_Set:
			case LITERAL_Sequence:
			case LITERAL_Bag:
			case LITERAL_oclEmpty:
			case LITERAL_oclUndefined:
			case LITERAL_Tuple:
			{
				n=literal();
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				n=expression();
				match(RPAREN);
				break;
			}
			case LITERAL_if:
			{
				n=ifExpression();
				break;
			}
			default:
				if ((_tokenSet_28.member(LA(1))) && (_tokenSet_29.member(LA(2))) && (_tokenSet_30.member(LA(3)))) {
					n=propertyCall(null, false);
				}
				else if ((LA(1)==IDENT) && (LA(2)==DOT) && (LA(3)==LITERAL_allInstances)) {
					id1 = LT(1);
					match(IDENT);
					match(DOT);
					match(LITERAL_allInstances);
					n = new ASTAllInstancesExpression((MyToken) id1);
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTExpression  propertyCall(
		ASTExpression source, boolean followsArrow
	) throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		n = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_iterate:
			{
				n=iterateExpression(source);
				break;
			}
			case LITERAL_oclAsType:
			case LITERAL_oclIsKindOf:
			case LITERAL_oclIsTypeOf:
			{
				n=typeExpression(source, followsArrow);
				break;
			}
			default:
				if (((LA(1)==IDENT) && (LA(2)==LPAREN) && (_tokenSet_32.member(LA(3))))&&( isQueryIdent(LT(1)) )) {
					n=queryExpression(source);
				}
				else if ((LA(1)==IDENT) && (_tokenSet_29.member(LA(2))) && (_tokenSet_33.member(LA(3)))) {
					n=operationExpression(source, followsArrow);
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTExpression  literal() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  t = null;
		Token  f = null;
		Token  i = null;
		Token  r = null;
		Token  s = null;
		Token  enumLit = null;
		n = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_true:
			{
				t = LT(1);
				match(LITERAL_true);
				n = new ASTBooleanLiteral(true);
				break;
			}
			case LITERAL_false:
			{
				f = LT(1);
				match(LITERAL_false);
				n = new ASTBooleanLiteral(false);
				break;
			}
			case INT:
			{
				i = LT(1);
				match(INT);
				n = new ASTIntegerLiteral((MyToken) i);
				break;
			}
			case REAL:
			{
				r = LT(1);
				match(REAL);
				n = new ASTRealLiteral((MyToken) r);
				break;
			}
			case STRING:
			{
				s = LT(1);
				match(STRING);
				n = new ASTStringLiteral((MyToken) s);
				break;
			}
			case HASH:
			{
				match(HASH);
				enumLit = LT(1);
				match(IDENT);
				n = new ASTEnumLiteral((MyToken) enumLit);
				break;
			}
			case LITERAL_Set:
			case LITERAL_Sequence:
			case LITERAL_Bag:
			{
				n=collectionLiteral();
				break;
			}
			case LITERAL_oclEmpty:
			{
				n=emptyCollectionLiteral();
				break;
			}
			case LITERAL_oclUndefined:
			{
				n=undefinedLiteral();
				break;
			}
			case LITERAL_Tuple:
			{
				n=tupleLiteral();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTExpression  ifExpression() throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  i = null;
		ASTExpression cond, t, e; n = null;
		
		try {      // for error handling
			i = LT(1);
			match(LITERAL_if);
			cond=expression();
			match(LITERAL_then);
			t=expression();
			match(LITERAL_else);
			e=expression();
			match(LITERAL_endif);
			n = new ASTIfExpression((MyToken) i, cond, t, e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTExpression  queryExpression(
		ASTExpression range
	) throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  op = null;
		
		ASTElemVarsDeclaration decls = new ASTElemVarsDeclaration(); 
		n = null; 
		
		
		try {      // for error handling
			op = LT(1);
			match(IDENT);
			match(LPAREN);
			{
			if ((LA(1)==IDENT) && (_tokenSet_34.member(LA(2)))) {
				decls=elemVarsDeclaration();
				match(BAR);
			}
			else if ((_tokenSet_32.member(LA(1))) && (_tokenSet_35.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			n=expression();
			match(RPAREN);
			n = new ASTQueryExpression((MyToken) op, range, decls, n);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTExpression  iterateExpression(
		ASTExpression range
	) throws RecognitionException, TokenStreamException {
		ASTExpression n;
		
		Token  i = null;
		
		ASTElemVarsDeclaration decls = null; 
		ASTVariableInitialization init = null; 
		n = null;
		
		
		try {      // for error handling
			i = LT(1);
			match(LITERAL_iterate);
			match(LPAREN);
			decls=elemVarsDeclaration();
			match(SEMI);
			init=variableInitialization();
			match(BAR);
			n=expression();
			match(RPAREN);
			n = new ASTIterateExpression((MyToken) i, range, decls, init, n);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTOperationExpression  operationExpression(
		ASTExpression source, boolean followsArrow
	) throws RecognitionException, TokenStreamException {
		ASTOperationExpression n;
		
		Token  name = null;
		ASTExpression e; n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			n = new ASTOperationExpression((MyToken) name, source, followsArrow);
			{
			switch ( LA(1)) {
			case AT:
			{
				match(AT);
				match(LITERAL_pre);
				n.setIsPre();
				break;
			}
			case EOF:
			case IDENT:
			case LITERAL_constraints:
			case RBRACE:
			case SEMI:
			case LITERAL_abstract:
			case LITERAL_class:
			case LESS:
			case LITERAL_end:
			case COMMA:
			case EQUAL:
			case LITERAL_association:
			case LITERAL_aggregation:
			case LITERAL_composition:
			case DOTDOT:
			case STAR:
			case LITERAL_context:
			case LITERAL_inv:
			case LITERAL_pre:
			case LITERAL_post:
			case LPAREN:
			case RPAREN:
			case LITERAL_let:
			case LITERAL_in:
			case LITERAL_implies:
			case LITERAL_or:
			case LITERAL_xor:
			case LITERAL_and:
			case NOT_EQUAL:
			case GREATER:
			case LESS_EQUAL:
			case GREATER_EQUAL:
			case PLUS:
			case MINUS:
			case SLASH:
			case LITERAL_div:
			case ARROW:
			case DOT:
			case BAR:
			case LITERAL_then:
			case LITERAL_else:
			case LITERAL_endif:
			case LITERAL_create:
			case LITERAL_destroy:
			case LITERAL_insert:
			case LITERAL_delete:
			case LITERAL_set:
			case COLON_EQUAL:
			case LITERAL_openter:
			case LITERAL_opexit:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LPAREN:
			{
				match(LPAREN);
				n.hasParentheses();
				{
				switch ( LA(1)) {
				case IDENT:
				case INT:
				case LPAREN:
				case LITERAL_let:
				case PLUS:
				case MINUS:
				case LITERAL_not:
				case LITERAL_iterate:
				case LITERAL_oclAsType:
				case LITERAL_oclIsKindOf:
				case LITERAL_oclIsTypeOf:
				case LITERAL_if:
				case LITERAL_true:
				case LITERAL_false:
				case REAL:
				case STRING:
				case HASH:
				case LITERAL_Set:
				case LITERAL_Sequence:
				case LITERAL_Bag:
				case LITERAL_oclEmpty:
				case LITERAL_oclUndefined:
				case LITERAL_Tuple:
				{
					e=expression();
					n.addArg(e);
					{
					_loop118:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							e=expression();
							n.addArg(e);
						}
						else {
							break _loop118;
						}
						
					} while (true);
					}
					break;
				}
				case RPAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RPAREN);
				break;
			}
			case EOF:
			case IDENT:
			case LITERAL_constraints:
			case RBRACE:
			case SEMI:
			case LITERAL_abstract:
			case LITERAL_class:
			case LESS:
			case LITERAL_end:
			case COMMA:
			case EQUAL:
			case LITERAL_association:
			case LITERAL_aggregation:
			case LITERAL_composition:
			case DOTDOT:
			case STAR:
			case LITERAL_context:
			case LITERAL_inv:
			case LITERAL_pre:
			case LITERAL_post:
			case RPAREN:
			case LITERAL_let:
			case LITERAL_in:
			case LITERAL_implies:
			case LITERAL_or:
			case LITERAL_xor:
			case LITERAL_and:
			case NOT_EQUAL:
			case GREATER:
			case LESS_EQUAL:
			case GREATER_EQUAL:
			case PLUS:
			case MINUS:
			case SLASH:
			case LITERAL_div:
			case ARROW:
			case DOT:
			case BAR:
			case LITERAL_then:
			case LITERAL_else:
			case LITERAL_endif:
			case LITERAL_create:
			case LITERAL_destroy:
			case LITERAL_insert:
			case LITERAL_delete:
			case LITERAL_set:
			case COLON_EQUAL:
			case LITERAL_openter:
			case LITERAL_opexit:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTTypeArgExpression  typeExpression(
		ASTExpression source, boolean followsArrow
	) throws RecognitionException, TokenStreamException {
		ASTTypeArgExpression n;
		
		ASTType t = null; n = null;
		
		try {      // for error handling
			MyToken opToken = (MyToken) LT(1);
			{
			switch ( LA(1)) {
			case LITERAL_oclAsType:
			{
				match(LITERAL_oclAsType);
				break;
			}
			case LITERAL_oclIsKindOf:
			{
				match(LITERAL_oclIsKindOf);
				break;
			}
			case LITERAL_oclIsTypeOf:
			{
				match(LITERAL_oclIsTypeOf);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LPAREN);
			t=type();
			match(RPAREN);
			n = new ASTTypeArgExpression(opToken, source, t, followsArrow);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTElemVarsDeclaration  elemVarsDeclaration() throws RecognitionException, TokenStreamException {
		ASTElemVarsDeclaration n;
		
		List idList; ASTType t = null; n = null;
		
		try {      // for error handling
			idList=idList();
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				t=type();
				break;
			}
			case SEMI:
			case BAR:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n = new ASTElemVarsDeclaration(idList, t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_36);
		}
		return n;
	}
	
	public final ASTVariableInitialization  variableInitialization() throws RecognitionException, TokenStreamException {
		ASTVariableInitialization n;
		
		Token  name = null;
		ASTType t; ASTExpression e; n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			match(COLON);
			t=type();
			match(EQUAL);
			e=expression();
			n = new ASTVariableInitialization((MyToken) name, t, e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_37);
		}
		return n;
	}
	
	public final ASTCollectionLiteral  collectionLiteral() throws RecognitionException, TokenStreamException {
		ASTCollectionLiteral n;
		
		ASTCollectionItem ci; n = null;
		
		try {      // for error handling
			MyToken op = (MyToken) LT(1);
			{
			switch ( LA(1)) {
			case LITERAL_Set:
			{
				match(LITERAL_Set);
				break;
			}
			case LITERAL_Sequence:
			{
				match(LITERAL_Sequence);
				break;
			}
			case LITERAL_Bag:
			{
				match(LITERAL_Bag);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n = new ASTCollectionLiteral(op);
			match(LBRACE);
			ci=collectionItem();
			n.addItem(ci);
			{
			_loop129:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					ci=collectionItem();
					n.addItem(ci);
				}
				else {
					break _loop129;
				}
				
			} while (true);
			}
			match(RBRACE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTEmptyCollectionLiteral  emptyCollectionLiteral() throws RecognitionException, TokenStreamException {
		ASTEmptyCollectionLiteral n;
		
		ASTType t = null; n = null;
		
		try {      // for error handling
			match(LITERAL_oclEmpty);
			match(LPAREN);
			t=collectionType();
			match(RPAREN);
			n = new ASTEmptyCollectionLiteral(t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTUndefinedLiteral  undefinedLiteral() throws RecognitionException, TokenStreamException {
		ASTUndefinedLiteral n;
		
		ASTType t = null; n = null;
		
		try {      // for error handling
			match(LITERAL_oclUndefined);
			match(LPAREN);
			t=type();
			match(RPAREN);
			n = new ASTUndefinedLiteral(t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTTupleLiteral  tupleLiteral() throws RecognitionException, TokenStreamException {
		ASTTupleLiteral n;
		
		ASTTupleItem ti; n = null; List tiList = new ArrayList();
		
		try {      // for error handling
			match(LITERAL_Tuple);
			match(LBRACE);
			ti=tupleItem();
			tiList.add(ti);
			{
			_loop136:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					ti=tupleItem();
					tiList.add(ti);
				}
				else {
					break _loop136;
				}
				
			} while (true);
			}
			match(RBRACE);
			n = new ASTTupleLiteral(tiList);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_31);
		}
		return n;
	}
	
	public final ASTCollectionItem  collectionItem() throws RecognitionException, TokenStreamException {
		ASTCollectionItem n;
		
		ASTExpression e; n = new ASTCollectionItem();
		
		try {      // for error handling
			e=expression();
			n.setFirst(e);
			{
			switch ( LA(1)) {
			case DOTDOT:
			{
				match(DOTDOT);
				e=expression();
				n.setSecond(e);
				break;
			}
			case RBRACE:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_38);
		}
		return n;
	}
	
	public final ASTCollectionType  collectionType() throws RecognitionException, TokenStreamException {
		ASTCollectionType n;
		
		ASTType elemType = null; n = null;
		
		try {      // for error handling
			MyToken op = (MyToken) LT(1);
			{
			switch ( LA(1)) {
			case LITERAL_Collection:
			{
				match(LITERAL_Collection);
				break;
			}
			case LITERAL_Set:
			{
				match(LITERAL_Set);
				break;
			}
			case LITERAL_Sequence:
			{
				match(LITERAL_Sequence);
				break;
			}
			case LITERAL_Bag:
			{
				match(LITERAL_Bag);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LPAREN);
			elemType=type();
			match(RPAREN);
			n = new ASTCollectionType(op, elemType);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_8);
		}
		return n;
	}
	
	public final ASTTupleItem  tupleItem() throws RecognitionException, TokenStreamException {
		ASTTupleItem n;
		
		Token  name = null;
		ASTExpression e; n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			match(COLON);
			e=expression();
			n = new ASTTupleItem((MyToken) name, e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_38);
		}
		return n;
	}
	
	public final ASTTupleType  tupleType() throws RecognitionException, TokenStreamException {
		ASTTupleType n;
		
		ASTTuplePart tp; n = null; List tpList = new ArrayList();
		
		try {      // for error handling
			match(LITERAL_Tuple);
			match(LPAREN);
			tp=tuplePart();
			tpList.add(tp);
			{
			_loop145:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					tp=tuplePart();
					tpList.add(tp);
				}
				else {
					break _loop145;
				}
				
			} while (true);
			}
			match(RPAREN);
			n = new ASTTupleType(tpList);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_8);
		}
		return n;
	}
	
	public final ASTTuplePart  tuplePart() throws RecognitionException, TokenStreamException {
		ASTTuplePart n;
		
		Token  name = null;
		ASTType t; n = null;
		
		try {      // for error handling
			name = LT(1);
			match(IDENT);
			match(COLON);
			t=type();
			n = new ASTTuplePart((MyToken) name, t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_17);
		}
		return n;
	}
	
	public final ASTCmdList  cmdList() throws RecognitionException, TokenStreamException {
		ASTCmdList cmdList;
		
		cmdList = new ASTCmdList(); ASTCmd c;
		
		try {      // for error handling
			c=cmd();
			cmdList.add(c);
			{
			_loop149:
			do {
				if ((_tokenSet_39.member(LA(1)))) {
					c=cmd();
					cmdList.add(c);
				}
				else {
					break _loop149;
				}
				
			} while (true);
			}
			match(Token.EOF_TYPE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return cmdList;
	}
	
	public final ASTCmd  cmd() throws RecognitionException, TokenStreamException {
		ASTCmd n;
		
		n = null;
		
		try {      // for error handling
			n=cmdStmt();
			{
			switch ( LA(1)) {
			case SEMI:
			{
				match(SEMI);
				break;
			}
			case EOF:
			case LITERAL_let:
			case LITERAL_create:
			case LITERAL_destroy:
			case LITERAL_insert:
			case LITERAL_delete:
			case LITERAL_set:
			case LITERAL_openter:
			case LITERAL_opexit:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_40);
		}
		return n;
	}
	
	public final ASTCmd  cmdStmt() throws RecognitionException, TokenStreamException {
		ASTCmd n;
		
		n = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_create:
			{
				n=createCmd();
				break;
			}
			case LITERAL_destroy:
			{
				n=destroyCmd();
				break;
			}
			case LITERAL_insert:
			{
				n=insertCmd();
				break;
			}
			case LITERAL_delete:
			{
				n=deleteCmd();
				break;
			}
			case LITERAL_set:
			{
				n=setCmd();
				break;
			}
			case LITERAL_openter:
			{
				n=opEnterCmd();
				break;
			}
			case LITERAL_opexit:
			{
				n=opExitCmd();
				break;
			}
			case LITERAL_let:
			{
				n=letCmd();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTCreateCmd  createCmd() throws RecognitionException, TokenStreamException {
		ASTCreateCmd n;
		
		List idList; ASTType t; n = null;
		
		try {      // for error handling
			match(LITERAL_create);
			idList=idList();
			match(COLON);
			t=simpleType();
			n = new ASTCreateCmd(idList, t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTDestroyCmd  destroyCmd() throws RecognitionException, TokenStreamException {
		ASTDestroyCmd n;
		
		ASTExpression e = null; n = null;
		
		try {      // for error handling
			match(LITERAL_destroy);
			e=expression();
			n = new ASTDestroyCmd(e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTInsertCmd  insertCmd() throws RecognitionException, TokenStreamException {
		ASTInsertCmd n;
		
		Token  id = null;
		ASTExpression e; List exprList = new ArrayList(); n = null;
		
		try {      // for error handling
			match(LITERAL_insert);
			match(LPAREN);
			e=expression();
			exprList.add(e);
			match(COMMA);
			e=expression();
			exprList.add(e);
			{
			_loop157:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					e=expression();
					exprList.add(e);
				}
				else {
					break _loop157;
				}
				
			} while (true);
			}
			match(RPAREN);
			match(LITERAL_into);
			id = LT(1);
			match(IDENT);
			n = new ASTInsertCmd(exprList, (MyToken) id);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTDeleteCmd  deleteCmd() throws RecognitionException, TokenStreamException {
		ASTDeleteCmd n;
		
		Token  id = null;
		ASTExpression e; List exprList = new ArrayList(); n = null;
		
		try {      // for error handling
			match(LITERAL_delete);
			match(LPAREN);
			e=expression();
			exprList.add(e);
			match(COMMA);
			e=expression();
			exprList.add(e);
			{
			_loop160:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					e=expression();
					exprList.add(e);
				}
				else {
					break _loop160;
				}
				
			} while (true);
			}
			match(RPAREN);
			match(LITERAL_from);
			id = LT(1);
			match(IDENT);
			n = new ASTDeleteCmd(exprList, (MyToken) id);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTSetCmd  setCmd() throws RecognitionException, TokenStreamException {
		ASTSetCmd n;
		
		ASTExpression e1, e2; n = null;
		
		try {      // for error handling
			match(LITERAL_set);
			e1=expression();
			match(COLON_EQUAL);
			e2=expression();
			n = new ASTSetCmd(e1, e2);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTOpEnterCmd  opEnterCmd() throws RecognitionException, TokenStreamException {
		ASTOpEnterCmd n;
		
		Token  id = null;
		ASTExpression e; n = null;
		
		try {      // for error handling
			match(LITERAL_openter);
			e=expression();
			id = LT(1);
			match(IDENT);
			n = new ASTOpEnterCmd(e, (MyToken) id);
			match(LPAREN);
			{
			switch ( LA(1)) {
			case IDENT:
			case INT:
			case LPAREN:
			case LITERAL_let:
			case PLUS:
			case MINUS:
			case LITERAL_not:
			case LITERAL_iterate:
			case LITERAL_oclAsType:
			case LITERAL_oclIsKindOf:
			case LITERAL_oclIsTypeOf:
			case LITERAL_if:
			case LITERAL_true:
			case LITERAL_false:
			case REAL:
			case STRING:
			case HASH:
			case LITERAL_Set:
			case LITERAL_Sequence:
			case LITERAL_Bag:
			case LITERAL_oclEmpty:
			case LITERAL_oclUndefined:
			case LITERAL_Tuple:
			{
				e=expression();
				n.addArg(e);
				{
				_loop165:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						e=expression();
						n.addArg(e);
					}
					else {
						break _loop165;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTOpExitCmd  opExitCmd() throws RecognitionException, TokenStreamException {
		ASTOpExitCmd n;
		
		ASTExpression e = null; n = null;
		
		try {      // for error handling
			match(LITERAL_opexit);
			{
			if ((_tokenSet_32.member(LA(1))) && (_tokenSet_42.member(LA(2))) && (_tokenSet_43.member(LA(3)))) {
				e=expression();
			}
			else if ((_tokenSet_41.member(LA(1))) && (_tokenSet_44.member(LA(2))) && (_tokenSet_45.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			n = new ASTOpExitCmd(e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	public final ASTLetCmd  letCmd() throws RecognitionException, TokenStreamException {
		ASTLetCmd n;
		
		Token  name = null;
		ASTExpression e = null; ASTType t = null; n = null;
		
		try {      // for error handling
			match(LITERAL_let);
			name = LT(1);
			match(IDENT);
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				t=type();
				break;
			}
			case EQUAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EQUAL);
			e=expression();
			n = new ASTLetCmd((MyToken) name, t, e);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_41);
		}
		return n;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"model\"",
		"an identifier",
		"\"constraints\"",
		"\"enum\"",
		"'{'",
		"'}'",
		"';'",
		"\"abstract\"",
		"\"class\"",
		"'<'",
		"\"attributes\"",
		"\"operations\"",
		"\"end\"",
		"','",
		"':'",
		"'='",
		"\"association\"",
		"\"aggregation\"",
		"\"composition\"",
		"\"between\"",
		"'['",
		"']'",
		"\"role\"",
		"\"ordered\"",
		"'..'",
		"INT",
		"'*'",
		"\"context\"",
		"\"inv\"",
		"'::'",
		"\"pre\"",
		"\"post\"",
		"'('",
		"')'",
		"\"let\"",
		"\"in\"",
		"\"implies\"",
		"\"or\"",
		"\"xor\"",
		"\"and\"",
		"'<>'",
		"'>'",
		"'<='",
		"'>='",
		"'+'",
		"'-'",
		"'/'",
		"\"div\"",
		"\"not\"",
		"'->'",
		"'.'",
		"\"allInstances\"",
		"'|'",
		"\"iterate\"",
		"'@'",
		"\"oclAsType\"",
		"\"oclIsKindOf\"",
		"\"oclIsTypeOf\"",
		"\"if\"",
		"\"then\"",
		"\"else\"",
		"\"endif\"",
		"\"true\"",
		"\"false\"",
		"REAL",
		"STRING",
		"'#'",
		"\"Set\"",
		"\"Sequence\"",
		"\"Bag\"",
		"\"oclEmpty\"",
		"\"oclUndefined\"",
		"\"Tuple\"",
		"\"Collection\"",
		"\"create\"",
		"\"destroy\"",
		"\"insert\"",
		"\"into\"",
		"\"delete\"",
		"\"from\"",
		"\"set\"",
		"':='",
		"\"openter\"",
		"\"opexit\"",
		"WS",
		"SL_COMMENT",
		"ML_COMMENT",
		"RANGE_OR_INT",
		"ESC",
		"HEX_DIGIT",
		"VOCAB"
	};
	
	private static final long _tokenSet_0_data_[] = { 2L, 0L };
	public static final BitSet _tokenSet_0 = new BitSet(_tokenSet_0_data_);
	private static final long _tokenSet_1_data_[] = { 7346370L, 0L };
	public static final BitSet _tokenSet_1 = new BitSet(_tokenSet_1_data_);
	private static final long _tokenSet_2_data_[] = { 7346242L, 0L };
	public static final BitSet _tokenSet_2 = new BitSet(_tokenSet_2_data_);
	private static final long _tokenSet_3_data_[] = { 2154829890L, 0L };
	public static final BitSet _tokenSet_3 = new BitSet(_tokenSet_3_data_);
	private static final long _tokenSet_4_data_[] = { 72057594038306368L, 0L };
	public static final BitSet _tokenSet_4 = new BitSet(_tokenSet_4_data_);
	private static final long _tokenSet_5_data_[] = { 98400L, 0L };
	public static final BitSet _tokenSet_5 = new BitSet(_tokenSet_5_data_);
	private static final long _tokenSet_6_data_[] = { 65632L, 0L };
	public static final BitSet _tokenSet_6 = new BitSet(_tokenSet_6_data_);
	private static final long _tokenSet_7_data_[] = { 6449862722L, 0L };
	public static final BitSet _tokenSet_7 = new BitSet(_tokenSet_7_data_);
	private static final long _tokenSet_8_data_[] = { 72057783017243744L, 0L };
	public static final BitSet _tokenSet_8 = new BitSet(_tokenSet_8_data_);
	private static final long _tokenSet_9_data_[] = { 51539936352L, 0L };
	public static final BitSet _tokenSet_9 = new BitSet(_tokenSet_9_data_);
	private static final long _tokenSet_10_data_[] = { -9151313422486135198L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_10 = new BitSet(_tokenSet_10_data_);
	private static final long _tokenSet_11_data_[] = { 53694504034L, 0L };
	public static final BitSet _tokenSet_11 = new BitSet(_tokenSet_11_data_);
	private static final long _tokenSet_12_data_[] = { 65568L, 0L };
	public static final BitSet _tokenSet_12 = new BitSet(_tokenSet_12_data_);
	private static final long _tokenSet_13_data_[] = { 33554432L, 0L };
	public static final BitSet _tokenSet_13 = new BitSet(_tokenSet_13_data_);
	private static final long _tokenSet_14_data_[] = { 33685504L, 0L };
	public static final BitSet _tokenSet_14 = new BitSet(_tokenSet_14_data_);
	private static final long _tokenSet_15_data_[] = { 302120960L, 0L };
	public static final BitSet _tokenSet_15 = new BitSet(_tokenSet_15_data_);
	private static final long _tokenSet_16_data_[] = { 72058062190117986L, 14008320L, 0L, 0L };
	public static final BitSet _tokenSet_16 = new BitSet(_tokenSet_16_data_);
	private static final long _tokenSet_17_data_[] = { 137439084544L, 0L };
	public static final BitSet _tokenSet_17 = new BitSet(_tokenSet_17_data_);
	private static final long _tokenSet_18_data_[] = { -9151312322974507422L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_18 = new BitSet(_tokenSet_18_data_);
	private static final long _tokenSet_19_data_[] = { -9151310123951251870L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_19 = new BitSet(_tokenSet_19_data_);
	private static final long _tokenSet_20_data_[] = { -9151305725904740766L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_20 = new BitSet(_tokenSet_20_data_);
	private static final long _tokenSet_21_data_[] = { -9151296929811718558L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_21 = new BitSet(_tokenSet_21_data_);
	private static final long _tokenSet_22_data_[] = { 246290604630016L, 0L };
	public static final BitSet _tokenSet_22 = new BitSet(_tokenSet_22_data_);
	private static final long _tokenSet_23_data_[] = { -9151279337625149854L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_23 = new BitSet(_tokenSet_23_data_);
	private static final long _tokenSet_24_data_[] = { -9151033047020519838L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_24 = new BitSet(_tokenSet_24_data_);
	private static final long _tokenSet_25_data_[] = { 3377700794269696L, 0L };
	public static final BitSet _tokenSet_25 = new BitSet(_tokenSet_25_data_);
	private static final long _tokenSet_26_data_[] = { -9150188622090387870L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_26 = new BitSet(_tokenSet_26_data_);
	private static final long _tokenSet_27_data_[] = { -9146810921296118174L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_27 = new BitSet(_tokenSet_27_data_);
	private static final long _tokenSet_28_data_[] = { 4179340454199820320L, 0L };
	public static final BitSet _tokenSet_28 = new BitSet(_tokenSet_28_data_);
	private static final long _tokenSet_29_data_[] = { -8831558878660706718L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_29 = new BitSet(_tokenSet_29_data_);
	private static final long _tokenSet_30_data_[] = { -324259182020706718L, 16777215L, 0L, 0L };
	public static final BitSet _tokenSet_30 = new BitSet(_tokenSet_30_data_);
	private static final long _tokenSet_31_data_[] = { -9119789323531895198L, 16105475L, 0L, 0L };
	public static final BitSet _tokenSet_31 = new BitSet(_tokenSet_31_data_);
	private static final long _tokenSet_32_data_[] = { 8796374841318965280L, 8188L, 0L, 0L };
	public static final BitSet _tokenSet_32 = new BitSet(_tokenSet_32_data_);
	private static final long _tokenSet_33_data_[] = { -324259182020706718L, 16769023L, 0L, 0L };
	public static final BitSet _tokenSet_33 = new BitSet(_tokenSet_33_data_);
	private static final long _tokenSet_34_data_[] = { 72057594038321152L, 0L };
	public static final BitSet _tokenSet_34 = new BitSet(_tokenSet_34_data_);
	private static final long _tokenSet_35_data_[] = { 9115285028933738784L, 8188L, 0L, 0L };
	public static final BitSet _tokenSet_35 = new BitSet(_tokenSet_35_data_);
	private static final long _tokenSet_36_data_[] = { 72057594037928960L, 0L };
	public static final BitSet _tokenSet_36 = new BitSet(_tokenSet_36_data_);
	private static final long _tokenSet_37_data_[] = { 72057594037927936L, 0L };
	public static final BitSet _tokenSet_37 = new BitSet(_tokenSet_37_data_);
	private static final long _tokenSet_38_data_[] = { 131584L, 0L };
	public static final BitSet _tokenSet_38 = new BitSet(_tokenSet_38_data_);
	private static final long _tokenSet_39_data_[] = { 274877906944L, 14008320L, 0L, 0L };
	public static final BitSet _tokenSet_39 = new BitSet(_tokenSet_39_data_);
	private static final long _tokenSet_40_data_[] = { 274877906946L, 14008320L, 0L, 0L };
	public static final BitSet _tokenSet_40 = new BitSet(_tokenSet_40_data_);
	private static final long _tokenSet_41_data_[] = { 274877907970L, 14008320L, 0L, 0L };
	public static final BitSet _tokenSet_41 = new BitSet(_tokenSet_41_data_);
	private static final long _tokenSet_42_data_[] = { 9115284891494786338L, 14016508L, 0L, 0L };
	public static final BitSet _tokenSet_42 = new BitSet(_tokenSet_42_data_);
	private static final long _tokenSet_43_data_[] = { -72058193721940702L, 14024700L, 0L, 0L };
	public static final BitSet _tokenSet_43 = new BitSet(_tokenSet_43_data_);
	private static final long _tokenSet_44_data_[] = { 8796374841318966306L, 14016508L, 0L, 0L };
	public static final BitSet _tokenSet_44 = new BitSet(_tokenSet_44_data_);
	private static final long _tokenSet_45_data_[] = { 9115284891495179554L, 16113660L, 0L, 0L };
	public static final BitSet _tokenSet_45 = new BitSet(_tokenSet_45_data_);
	
	}
