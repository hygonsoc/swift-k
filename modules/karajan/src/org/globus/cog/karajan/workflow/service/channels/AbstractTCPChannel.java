//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.commands.ChannelConfigurationCommand;

public abstract class AbstractTCPChannel extends AbstractStreamKarajanChannel implements Runnable {
	private Socket socket;
	private boolean started;
	private Exception startException;
	private boolean closing;

	public AbstractTCPChannel(RequestManager requestManager, ChannelContext channelContext,
			boolean client) {
		super(requestManager, channelContext, client);
	}

	protected void setSocket(Socket socket) throws IOException {
		this.socket = socket;
		setInputStream(socket.getInputStream());
		setOutputStream(socket.getOutputStream());
	}

	public synchronized void start() throws ChannelException {
		if (isClient()) {
			setName("C(" + socket.getLocalAddress() + ")");
		}
		else {
			setName("S(" + socket.getLocalAddress() + ")");
		}
		new Thread(this).start();
		while (!isStarted() && !isClosed() && startException == null) {
			try {
				wait();
			}
			catch (InterruptedException e) {
			}
		}
		if (startException != null) {
			logger.debug("Exception while starting channel", startException);
			throw new ChannelException(startException);
		}
		logger.info(getContact() + "Channel started");
		if (isClient()) {
			try {
				configure();
			}
			catch (Exception e) {
				throw new ChannelException("Failed to configure channel", e);
			}
		}
	}

	public void run() {
		ChannelContext context = getChannelContext();
		try {
			try {
				started = true;
			}
			catch (Exception e) {
				startException = e;
				e.printStackTrace();
				return;
			}
			finally {
				synchronized (this) {
					notifyAll();
				}
			}
			initializeConnection();
			register();
		}
		catch (Exception e) {
			if (!closing) {
				logger.warn("Exception in channel", e);
				context.notifyRegisteredListeners(e);
			}
		}
	}

	protected void initializeConnection() {

	}


	public void shutdown() {
		if (isLocalShutdown()) {
			return;
		}
		try {
			setLocalShutdown();
			ChannelManager.getManager().shutdownChannel(this);
		}
		catch (ShuttingDownException e) {
			logger.debug("Channel already shutting down");
		}
		catch (Exception e) {
			logger.warn(getContact() + ": Could not shutdown channel", e);
		}
		super.close();
		synchronized (this) {
			notify();
		}
		logger.info(getContact() + ": Channel terminated");
	}

	public void close() {
		closing = true;
		try {
			if (!socket.isClosed()) {
				socket.close();
				logger.info(getContact() + ": Channel shut down");
			}
		}
		catch (Exception e) {
			logger.warn(getContact() + ": Failed to close socket", e);
		}
		super.close();
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isOffline() {
		return isClosed();
	}
}
