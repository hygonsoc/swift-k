//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.griphyn.vdl.mapping.AbsFile;

public class InFileDirs extends AbstractSequentialWithArguments {
    public static final Arg STAGEINS = new Arg.Positional("stageins");
    
    static {
        setArguments(InFileDirs.class, new Arg[] { STAGEINS });
    }

    @Override
    protected void post(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(STAGEINS.getValue(stack));
        VariableArguments ret = ArgUtil.getVariableReturn(stack);
        for (Object f : files) { 
        	String path = (String) f;
            String dir = new AbsFile(path).getDir();
            if (dir.startsWith("/")) {
            	ret.append(dir.substring(1));
            }
            else {
                ret.append(dir);
            }
        }
        super.post(stack);
    }
}
