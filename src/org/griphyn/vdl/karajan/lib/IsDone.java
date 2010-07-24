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
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Path;

public class IsDone extends VDLFunction {
    public static final Arg STAGEOUT = new Arg.Positional("stageout");
    
    static {
        setArguments(IsDone.class, new Arg[] { STAGEOUT });
    }

    @Override
    protected Object function(VariableStack stack) throws ExecutionException,
            HandleOpenException {
        List files = TypeUtil.toList(STAGEOUT.getValue(stack));
        for (Object f : files) { 
            List pv = TypeUtil.toList(f);
            Path p = Path.parse(TypeUtil.toString(pv.get(0)));
            DSHandle handle = (DSHandle) pv.get(1);
            if (!IsLogged.isLogged(stack, handle, p)) {
                return Boolean.FALSE;
            }
        }
        if (files.isEmpty()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
