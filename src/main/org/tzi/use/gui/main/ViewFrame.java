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

package org.tzi.use.gui.main;
import org.tzi.use.gui.views.View;
import org.tzi.use.gui.views.PrintableView;
import org.tzi.use.config.Options;
import javax.swing.JInternalFrame;
import javax.swing.ImageIcon;
import java.awt.print.PageFormat;

/** 
 * An internal frame holding a view of a system state.
 * 
 * @version 	$ProjectVersion: 2-1-0-release.1 $
 * @author 	Mark Richters
 */

public class ViewFrame extends JInternalFrame {
    private View fView;

    ViewFrame(String title, View view, String iconFilename) {
	super(title, true, true, true, true);
	fView = view;
	setFrameIcon(new ImageIcon(Options.iconDir + iconFilename));
    }

    void close() {
	fView.detachModel();
    }

    boolean isPrintable() {
	return fView instanceof PrintableView;
    }
    
    void print(PageFormat pf) {
	if ( fView instanceof PrintableView )
	    ((PrintableView) fView).printView(pf);
    }
}
