//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.Stack;


public class Integral extends AbstractAtom {
	
	protected void setParams(String[] params) {
		assertEquals(params.length, 0, Integral.class);
	}

	public boolean parse(ParserContext context, Stack stack) {
		Long lng = new Long((String) stack.pop());
		stack.push(lng);
		return true;
	}
	
	public String toString() {
		return "INTEGRAL()";
	}
}