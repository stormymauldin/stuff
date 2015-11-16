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

package org.tzi.use.util;

import junit.framework.TestCase;

/**
 * Test HashMultiMap class.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class HashMultiMapTest extends TestCase {

    public HashMultiMapTest(String name) {
        super(name);
    }

    protected HashMultiMap m0;
    protected HashMultiMap m1;
    protected HashMultiMap m2;

    protected void setUp() {
        m0 = new HashMultiMap();
        m1 = new HashMultiMap();
        m2 = new HashMultiMap();
        m1.put("ab", "a");
        m1.put("ab", "b");
        m1.put("c", "c");
    }

    public void test1() {
        assertEquals(0, m0.size());
        assertEquals(3, m1.size());
        assertEquals(true, m0.isEmpty());
        assertEquals(false, m1.isEmpty());
        assertEquals(true, m1.containsKey("c"));
        assertEquals(false, m1.containsKey("d"));
        assertEquals(false, m0.containsValue("b"));
        assertEquals(true, m1.containsValue("b"));
        assertEquals(false, m1.containsValue("e"));
        assertEquals(2, m1.get("ab").size());
    }

    public void test2() {
        m2.putAll(m1);
        assertEquals(true, m1.equals(m1));
        assertEquals(true, m1.equals(m2));
    }
}
