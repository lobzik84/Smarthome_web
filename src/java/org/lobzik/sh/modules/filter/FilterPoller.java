package org.lobzik.sh.modules.filter;

import java.io.DataOutputStream;

public class FilterPoller extends Thread {
	DataOutputStream outToServer;
	FilterConnector connector;
	static String command = "";
	volatile boolean  run = true;
	
	public FilterPoller(DataOutputStream outToServer, FilterConnector connector) 
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
