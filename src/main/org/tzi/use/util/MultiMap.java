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
import java.util.List;
import java.util.Set;

/** 
 * A MultiMap is a Map allowing multiple occurrences of keys.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 * @see         java.util.Map
 */


public interface MultiMap {
    // Query Operations

    /**
     * Returns the number of values in this multimap.
     */
    int size();

    /**
     * Returns <tt>true</tt> if this multimap contains no mappings.  
     */
    boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this multimap contains a mapping for
     * the specified key.  
     */
    boolean containsKey(Object key);

    /**
     * Returns <tt>true</tt> if this multimap maps one or more keys to
     * the specified value.  
     */
    boolean containsValue(Object value);

    /**
     * Returns a list of values to which this multimap maps the specified
     * key.  
     *
     * @return the list of values to which this map maps the specified
     *         key, the list may be empty if the multimap contains no
     *         mapping for this key. 
     */
    List get(Object key);

    // Modification Operations

    /**
     * Adds the specified value with the specified key to this multimap.
     */
    void put(Object key, Object value);

    /**
     * Copies all entries from the specified multimap to this
     * multimap.  
     */
    void putAll(MultiMap t);

    /**
     * Removes all mappings for this key from this multimap if present.
     */
    void remove(Object key);

    /**
     * Removes the specified key/value mapping from this multimap if present.
     */
    void remove(Object key, Object value);


    // Bulk Operations

    /**
     * Removes all mappings from this map (optional operation).
     */
    void clear();


    // Views

    /**
     * Returns a set view of the keys contained in this multimap.
     */
    Set keySet();

    /**
     * Returns a collection view of the values contained in this map.
     */
    //Collection values();

    // Comparison and hashing

    /**
     * Compares the specified object with this multimap for equality.
     */
    boolean equals(Object o);

    /**
     * Returns the hash code value for this map.
     */
    int hashCode();
}
