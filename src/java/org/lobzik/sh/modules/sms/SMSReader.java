package org.lobzik.sh.modules.sms;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;


public class SMSReader extends Thread {
	BufferedReader inFromServer;
	SMSConnector connector;
	SMSCommandHandler handler;
	volatile boolean  run = true;
	private Logger log = null;
	private static final ArrayList<String> smsReplies = new ArrayList<String>();
	private static final int repliesBufferSize = 100;
	private static final int maxLineLength = 1000; 
	
	public SMSReader (BufferedReader inFromServer, SMSConnector connector, SMSCommandHandler commandHandler, Logger log) 
	{
		setName(this.getClass().getSimpleName() + "-Thread");
		this.log = log;
		this.handler = commandHandler;
		this.connector = connector;
		this.inFromServer = inFromServer;
		run = true;
	}
	
	public ArrayList<String> getSMSReplies()
	{
		return  smsReplies;
	}
	
	public void exit() 
	{
		run = false;
		synchronized(this) 
		{
			notifyAll();
		}
	}
	
	public void run() 
	{
		try 
		{
			while (run) 
			{
				StringBuffer resp = new StringBuffer();
				while (run)
				{
					int b =  this.inFromServer.read();
					if (b == 13 || b == 10 || b == 62)	break;
					if (resp.length() >= maxLineLength) break;
					resp.append((char)b);
				}
				String response = resp.length() < maxLineLength?resp.toString().trim():"";
				if (response.length() == 0 || resp.length() >= maxLineLength) continue;
				smsReplies.add(response);
				if (handler != null) handler.lineRecieved(smsReplies);
				if (smsReplies.size() > repliesBufferSize) smsReplies.remove(0);
				if (response.contains("ERROR"))
					log.error(response);
				else
					log.debug(response);
				//this.inFromServer.
			}
		}
		catch (Exception e)
		{
			synchronized(connector) {connector.reconnect(e.getClass().getSimpleName() + ": " + e.getMessage());}
		}
		
	}
	
}