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
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * Skeleton for implementations of the Bag interface.
 *
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public abstract  class AbstractBag extends AbstractCollection implements Bag {
    // Comparison and hashing

    /**
     * Compares the specified Object with this Bag for equality.
     */
    public boolean equals(Object o) {
	if (o == this)
	    return true;

	if (!(o instanceof Bag))
	    return false;
	Collection c = (Collection) o;
	if (c.size() != size())
	    return false;
	return containsAll(c);
    }

    /**
     * Returns the hash code value for this Bag.
     */
    public int hashCode() {
	int h = 0;
	Iterator i = iterator();
	while (i.hasNext()) {
	    Object obj = i.next();
            if (obj != null)
                h += obj.hashCode();
        }
	return h;
    }
}

