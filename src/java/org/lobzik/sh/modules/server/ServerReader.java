package org.lobzik.sh.modules.server;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;


public class ServerReader extends Thread {
	BufferedReader inFromServer;
	ServerConnector connector;
	ServerCommandHandler handler;
	volatile boolean  run = true;
	private Logger log = null;//Logger.getLogger(ServerReader.class);
	private static final ArrayList<String> serverReplies = new ArrayList<String>();
	private static final int repliesBufferSize = 100;
	
	
	public ServerReader (BufferedReader inFromServer, ServerConnector connector, ServerCommandHandler commandHandler, Logger log) 
	{
		setName(this.getClass().getSimpleName() + "-Thread");
		this.log = log;
		this.handler = commandHandler;
		this.connector = connector;
		this.inFromServer = inFromServer;
		run = true;
	}
	
	public ArrayList<String> getServerReplies()
	{
		return serverReplies;
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
				serverReplies.add(response);
				if (handler != null) handler.lineRecieved(serverReplies);
				if (serverReplies.size() > repliesBufferSize) serverReplies.remove(0);
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