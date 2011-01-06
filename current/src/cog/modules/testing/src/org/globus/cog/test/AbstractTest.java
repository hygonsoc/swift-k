
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;


public abstract class AbstractTest implements TestInterface{
    protected OutputWriter output;
    public MachineListParser machines;

    public void setOutputWriter(OutputWriter output) {
        this.output = output;
    }

    public void setMachines(MachineListParser machines){
        this.machines = machines;
    }
}
