package org.lobzik.sh.modules.sms;

import java.io.DataOutputStream;

public class SMSPoller extends Thread {
	DataOutputStream outToServer;
	SMSConnector connector;
	static String command = "";
	volatile boolean  run = true;
	public SMSPoller(DataOutputStream outToServer, SMSConnector connector) 
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
			synchronized(connector) {connector.reconnect(e.getClass().getSimpleName() + ": " + e.getMessage());}
		}
		
	}
}