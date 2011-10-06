
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;

public class TargetCanvasRenderer extends SwingCanvasRenderer {
	public TargetCanvasRenderer() {
		CanvasView sfv = new SerialFlowView();
		addSupportedView(sfv);
		setView(sfv);
	}
}