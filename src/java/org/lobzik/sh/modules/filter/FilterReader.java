package org.lobzik.sh.modules.filter;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;


public class FilterReader extends Thread {
	BufferedReader inFromServer;
	FilterConnector connector;
	FilterCommandHandler handler;
	volatile boolean  run = true;
	private Logger log = null;
	private static final ArrayList<String> filterReplies = new ArrayList<String>();
	private static final int repliesBufferSize = 100;
	
	
	public FilterReader (BufferedReader inFromServer, FilterConnector connector, FilterCommandHandler commandHandler, Logger log) 
	{
		setName(this.getClass().getSimpleName() + "-Thread");
		this.handler = commandHandler;
		this.connector = connector;
		this.inFromServer = inFromServer;
		this.log = log;
		run = true;
	}
	
	public ArrayList<String> getFilterReplies()
	{
		return filterReplies;
	}
	
	public void exit() 
	{
		run = false;
	}
	
	public void run() 
	{
		try 
		{
			while (run) 
			{
				String response = this.inFromServer.readLine();
				response = response.trim();
				if (response.length() == 0 || response.equals(">"))
					continue; 
				filterReplies.add(response);
				if (handler != null) handler.lineRecieved(filterReplies);
				if (filterReplies.size() > repliesBufferSize) filterReplies.remove(0);
				if (response.startsWith("DEBUG:"))
					log.debug(response.substring(6));
				else if (response.startsWith("ERROR:"))
					log.error(response.substring(6));
				else if (response.startsWith("WARN:"))
					log.warn(response.substring(5));
				else if (response.startsWith("INFO:"))
					log.info(response.substring(5));
				else
					log.debug(response);
			}
		}
		catch (Exception e)
		{
			synchronized(connector) {connector.reconnect(e.getClass().getSimpleName() + ": " + e.getMessage());}
		}
		
	}
	
}