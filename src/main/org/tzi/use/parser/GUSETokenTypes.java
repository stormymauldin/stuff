// $ANTLR 2.7.1: "use.g" -> "GLexer.java"$
 
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

public interface GUSETokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LITERAL_model = 4;
	int IDENT = 5;
	int LITERAL_constraints = 6;
	int LITERAL_enum = 7;
	int LBRACE = 8;
	int RBRACE = 9;
	int SEMI = 10;
	int LITERAL_abstract = 11;
	int LITERAL_class = 12;
	int LESS = 13;
	int LITERAL_attributes = 14;
	int LITERAL_operations = 15;
	int LITERAL_end = 16;
	int COMMA = 17;
	int COLON = 18;
	int EQUAL = 19;
	int LITERAL_association = 20;
	int LITERAL_aggregation = 21;
	int LITERAL_composition = 22;
	int LITERAL_between = 23;
	int LBRACK = 24;
	int RBRACK = 25;
	int LITERAL_role = 26;
	int LITERAL_ordered = 27;
	int DOTDOT = 28;
	int INT = 29;
	int STAR = 30;
	int LITERAL_context = 31;
	int LITERAL_inv = 32;
	int COLON_COLON = 33;
	int LITERAL_pre = 34;
	int LITERAL_post = 35;
	int LPAREN = 36;
	int RPAREN = 37;
	int LITERAL_let = 38;
	int LITERAL_in = 39;
	int LITERAL_implies = 40;
	int LITERAL_or = 41;
	int LITERAL_xor = 42;
	int LITERAL_and = 43;
	int NOT_EQUAL = 44;
	int GREATER = 45;
	int LESS_EQUAL = 46;
	int GREATER_EQUAL = 47;
	int PLUS = 48;
	int MINUS = 49;
	int SLASH = 50;
	int LITERAL_div = 51;
	int LITERAL_not = 52;
	int ARROW = 53;
	int DOT = 54;
	int LITERAL_allInstances = 55;
	int BAR = 56;
	int LITERAL_iterate = 57;
	int AT = 58;
	int LITERAL_oclAsType = 59;
	int LITERAL_oclIsKindOf = 60;
	int LITERAL_oclIsTypeOf = 61;
	int LITERAL_if = 62;
	int LITERAL_then = 63;
	int LITERAL_else = 64;
	int LITERAL_endif = 65;
	int LITERAL_true = 66;
	int LITERAL_false = 67;
	int REAL = 68;
	int STRING = 69;
	int HASH = 70;
	int LITERAL_Set = 71;
	int LITERAL_Sequence = 72;
	int LITERAL_Bag = 73;
	int LITERAL_oclEmpty = 74;
	int LITERAL_oclUndefined = 75;
	int LITERAL_Tuple = 76;
	int LITERAL_Collection = 77;
	int LITERAL_create = 78;
	int LITERAL_destroy = 79;
	int LITERAL_insert = 80;
	int LITERAL_into = 81;
	int LITERAL_delete = 82;
	int LITERAL_from = 83;
	int LITERAL_set = 84;
	int COLON_EQUAL = 85;
	int LITERAL_openter = 86;
	int LITERAL_opexit = 87;
	int WS = 88;
	int SL_COMMENT = 89;
	int ML_COMMENT = 90;
	int RANGE_OR_INT = 91;
	int ESC = 92;
	int HEX_DIGIT = 93;
	int VOCAB = 94;
}
