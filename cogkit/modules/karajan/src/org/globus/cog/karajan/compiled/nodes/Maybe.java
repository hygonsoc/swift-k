/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 26, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;


public class Maybe extends Try {
	
	public void run(LWThread thr) {
		int ec = childCount();
        int i = thr.checkSliceAndPopState(ec + 1);
        int fc = thr.popIntState();
        Stack stack = thr.getStack();
        try {
        	switch (i) {
        		case 0:
        			fc = stack.frameCount();
        			i++;
        		default:
        			addBuffers(stack);
        			try {
        				for (; i <= ec; i++) {
        					runChild(i - 1, thr);
        				}
        				commitBuffers(stack);
        			}
        			catch (ExecutionException e) {
        				stack.dropToFrame(fc);
        			}
        	}
        }
        catch (Yield y) {
            y.getState().push(fc);
            y.getState().push(i, ec + 1);
            throw y;
        }
    }
}
