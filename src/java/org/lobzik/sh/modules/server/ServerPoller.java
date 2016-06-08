package org.lobzik.sh.modules.server;

import java.io.DataOutputStream;

public class ServerPoller extends Thread {
	DataOutputStream outToServer;
	ServerConnector connector;
	static String command = "";
	volatile boolean  run = true;
	public ServerPoller(DataOutputStream outToServer, ServerConnector connector) 
	{
		setName(this.getClass().getSimpleName() + "-Thread");
		this.connector = connector;
		this.outToServer = outToServer;
		run = true;
	}

	public void exit() 
	{
		run = false;
		synchronized(this) 
		{
			notifyAll();
		}
	}
	
	public void doCommand(String comm)
	{
		command = comm;
		synchronized(this) 
		{
			notify();
		}
	}
	
	public synchronized void run() 
	{
		try 
		{
		    while (run) 
			{
		    	this.outToServer.writeBytes(command + "\r");
				outToServer.flush();
				command = "";
				synchronized(this) 
				{
					try{wait(connector.getTucTucPeriod() * 1000);} catch (InterruptedException ie) {}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			synchronized(connector) {connector.notify();}
		}
		
	}
}
