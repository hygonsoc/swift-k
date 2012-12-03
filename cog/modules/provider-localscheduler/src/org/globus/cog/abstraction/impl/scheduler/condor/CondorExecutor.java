//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.condor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessException;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gsi.gssapi.auth.AuthorizationException;

public class CondorExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(CondorExecutor.class);

	public CondorExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}

	protected void writeAttr(String attrName, String arg, Writer wr) throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			wr.write(arg + String.valueOf(value) + '\n');
		}
	}

    public void start()
    throws AuthorizationException, IOException, ProcessException {
    	File scriptdir = new File(".");
        script = File.createTempFile(getName(), ".submit", scriptdir);
        if (!logger.isDebugEnabled()) {
            script.deleteOnExit();
        }
        stdout = spec.getStdOutput() == null ? script.getAbsolutePath()
                + ".stdout" : spec.getStdOutput();
        stderr = spec.getStdError() == null ? script.getAbsolutePath()
                + ".stderr" : spec.getStdError();
        exitcode = script.getAbsolutePath() + ".exitcode";

        if (logger.isDebugEnabled()) {
            logger.debug("Writing " + getName() + " script to " + script);
        }

        String[] cmdline = buildCommandLine(scriptdir, script, exitcode,
            stdout, stderr);

        if (logger.isDebugEnabled()) {
            logCommandLine(cmdline);
        }
        Process process = Runtime.getRuntime().exec(cmdline, null, null);

        try {
            process.getOutputStream().close();
        }
        catch (IOException e) {
        }

        try {
            int code = process.waitFor();
            if (code != 0) {
                String errorText = getOutput(process.getInputStream())
                        + getOutput(process.getErrorStream());
                throw new ProcessException("Could not submit job ("
                        + getProperties().getSubmitCommandName()
                        + " reported an exit code of " + code + "). "
                        + errorText);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(getProperties().getSubmitCommandName()
                        + " done (exit code " + code + ")");
            }
        }
        catch (InterruptedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Interrupted exception while waiting for "
                        + getProperties().getSubmitCommandName(), e);
            }
            if (listener != null) {
                listener
                    .processFailed("The submission process was interrupted");
            }
        }

        String output = getOutput(process.getInputStream());
        jobid = parseSubmitCommandOutput(output);
        if (logger.isDebugEnabled()) {
            logger.debug("Submitted job with id '" + jobid + "'");
        }

        if (jobid.length() == 0) {
            String errorText = getOutput(process.getErrorStream());
            if (listener != null)
                listener.processFailed("Received empty jobid!\n" +
                                       output + "\n" + errorText);
        }

        process.getInputStream().close();

        getQueuePoller().addJob(
            job = createJob(jobid, stdout, spec.getStdOutputLocation(), stderr,
                spec.getStdErrorLocation(), exitcode, this));
    }
	
	protected void writeScript(Writer wr, String exitcodefile, String stdout, String stderr) throws IOException {
		boolean grid = false;
		JobSpecification spec = getSpec();

		// Handle some predefined jobTypes
		String type = (String) spec.getAttribute("jobType");
		if (logger.isDebugEnabled()) {
			logger.debug("Job type: " + type);
		}
		if ("MPI".equals(type)) {
			wr.write("universe = MPI\n");
		}
		else if("grid".equals(type)) {
			grid = true;
			String gridResource = (String) spec.getAttribute("gridResource");
			wr.write("universe = grid\n");
			wr.write("grid_resource = "+gridResource+"\n");
			// the below two lines are needed to cause the gridmonitor to be used
			// which is the point of all this...
			wr.write("stream_output = False\n");
			wr.write("stream_error  = False\n");
			wr.write("Transfer_Executable = false\n");
		}
		else {
			if(spec.getAttribute("condor.universe") == null) {
				wr.write("universe = vanilla\n");
			}
		}

		if ("true".equals(spec.getAttribute("holdIsFailure"))) {
			wr.write("periodic_remove = JobStatus == 5\n");
		}
		
		writeAttr("count", "machine_count = ", wr);
		wr.write("output = " + quote(stdout) + '\n');
		wr.write("error = " + quote(stderr) + '\n');

		if (spec.getStdInput() != null) {
			wr.write("input = " + quote(spec.getStdInput()) + "\n");
		}

		Iterator<String> i = spec.getEnvironmentVariableNames().iterator();
		if (i.hasNext()) {
			wr.write("environment = ");
		}
		while (i.hasNext()) {
			String name = (String) i.next();
			wr.write(name);
			wr.write('=');
			wr.write(quote(spec.getEnvironmentVariable(name)));
			wr.write(';');
		}
		wr.write("\n");

		if (spec.getDirectory() != null) {
			if(!grid) {
				wr.write("initialdir = " + quote(spec.getDirectory()) + "\n");
			} else {
				wr.write("remote_initialdir = " + quote(spec.getDirectory()) + "\n");
			}
		}
		
        String basename[] = spec.getExecutable().split("/");		
        FileChannel from = new FileInputStream(spec.getExecutable()).getChannel();
        FileChannel to = new FileOutputStream(basename[basename.length-1]).getChannel();
        to.transferFrom(from, 0, from.size());
        from.close();
        to.close();
        
		spec.getExecutable();
		wr.write("executable = " + quote(spec.getExecutable()) + "\n");
		List<String> args = spec.getArgumentsAsList();
		if (args != null && args.size() > 0) {
			wr.write("arguments = ");
			i = args.iterator();
			while (i.hasNext()) {
				wr.write(quote((String) i.next()));
				if (i.hasNext()) {
					wr.write(' ');
				}
			}
		}
 		wr.write('\n');

		// Handle all condor attributes specified by the user
	    for(String a : spec.getAttributeNames()) {
	    	if(a != null && a.startsWith("condor.")) {
	    		String attributeName[] = a.split("condor.");
	    		wr.write(attributeName[1] + " = " + spec.getAttribute(a) + '\n');
	    	}
	    }
	    
		wr.write("notification = Never\n");
		wr.write("leave_in_queue = TRUE\n");
		wr.write("queue\n");
		wr.close();
	}

	private static final boolean[] TRIGGERS;

    static {
        TRIGGERS = new boolean[128];
        TRIGGERS[' '] = true;
        TRIGGERS['\n'] = true;
        TRIGGERS['\t'] = true;
        TRIGGERS['\\'] = true;
        TRIGGERS['>'] = true;
        TRIGGERS['<'] = true;
        TRIGGERS['"'] = true;
    }

    protected String quote(String s) {
        if ("".equals(s)) {
            return "";
        }
        boolean quotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 128 && TRIGGERS[c]) {
                quotes = true;
                break;
            }
        }
        if (!quotes) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        if (quotes) {
            sb.append('\\');
            sb.append('"');
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        if (quotes) {
            sb.append('\\');
            sb.append('"');
        }
        return sb.toString();
    }
    
	protected String getName() {
		return "Condor";
	}

	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new Job(jobid, stdout, stdOutputLocation, stderr,
				stdErrorLocation, null, executor);
	}

	private static QueuePoller poller;

	protected AbstractQueuePoller getQueuePoller() {
		synchronized (CondorExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}

	protected String parseSubmitCommandOutput(String out) throws IOException {
	    out = out.trim();
		if (out.endsWith(".")) {
			out = out.substring(0, out.length() - 1);
		}
		int index = out.lastIndexOf(" ");
		return out.substring(index + 1);
	}

	public void cancel() throws TaskSubmissionException {
		String jobid = getJobid();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Marking job " + jobid
						+ " as removable from queue");
			}
			Process p = Runtime.getRuntime().exec(
					new String[] {
							getProperties()
									.getProperty(Properties.CONDOR_QEDIT),
							jobid, "LeaveJobInQueue", "FALSE" });
			int ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully marked " + jobid
							+ " as removable from queue");
				}
			}
			else {
				logger.warn("Failed to mark job " + jobid
						+ " as removable from queue: "
						+ getOutput(p.getInputStream()));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Cancelling job " + jobid);
			}

			p = Runtime.getRuntime().exec(
					new String[] { getProperties().getRemoveCommand(), jobid });
			ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully cancelled job " + jobid);
				}
			}
			else {
				logger.warn("Failed to cancel job " + jobid + ": "
						+ getOutput(p.getInputStream()));
			}
		}
		catch (Exception e) {
			logger.warn("Failed to cancel job " + jobid, e);
		}
	}
}
