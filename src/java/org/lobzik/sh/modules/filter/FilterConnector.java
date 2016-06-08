package org.lobzik.sh.modules.filter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.apache.log4j.*;


public class FilterConnector extends Thread 
{

	private static String deviceIP = "";
	private static int devicePort = 0;
	private static int connectAfterLostPeriod = 10; //seconds
	private static int connectAttemptsPeriod = 10;
	private static int tucTucPeriod = 10;
	private static int commandTimeout = 1000;
	private static String connectErrorMessage = "";
	private static long connectTries = 0;
	private static boolean run = true;
	private static boolean connected = false;
	private static FilterReader reader = null;
	private static FilterPoller poller = null;
	private static Logger log = null;
	private static FilterCommandHandler commandHandler = null;
	private static final FilterConnector instance = new FilterConnector();
		
	private FilterConnector() {}
	
	public static FilterConnector getInstance(String deviceIp, int port, int afterLostPeriod, int attemptsPeriod, int tucPeriod, int timeout)
	{
		instance.setName(instance.getClass().getSimpleName()+"-Thread");
		deviceIP = deviceIp;
		devicePort = port;
		connectAfterLostPeriod = afterLostPeriod;
		connectAttemptsPeriod = attemptsPeriod;
		tucTucPeriod = tucPeriod;
		commandTimeout = timeout;
		return instance;
	}
	
	public int getTucTucPeriod()
	{
		return tucTucPeriod;
	}
	
	public int getCommandTimeout()
	{
		return commandTimeout;
	}
	public void doCommand(String command) throws Exception 
	{
		if (poller != null)
			poller.doCommand(command);
	}
	
	public void setCommandHandler(FilterCommandHandler handler)
	{
		commandHandler = handler;
	}
	
	public void setLogger(Logger logger)
	{
		log = logger;
	}
	
	public ArrayList<String> getFilterReplies()
	{
		if (reader != null)
			return reader.getFilterReplies();
		else return new ArrayList<String>();
	}
	
	public String getErrorMessage()
	{
		return connectErrorMessage;
	}
	
	public boolean isConnected()
	{
		return connected;
	}
		
	public synchronized void run() 
	{
		while(run)
		{
			try 
			{
				connectTries++;
				Socket clientSocket = new Socket(deviceIP, devicePort);
				clientSocket.setKeepAlive(true);
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				log.info("Connected to " + deviceIP + " after " + connectTries + " attempts...");
				reader = new FilterReader(inFromServer, this, commandHandler, log);
				reader.start();
				poller = new FilterPoller(outToServer, this);
				poller.start();
				connectErrorMessage = "";
				connectTries = 0;
				connected = true;
				
				synchronized(this) 
				{
					try{wait();} catch (InterruptedException ie) {}
				}
				connected = false;
				reader.exit();
				poller.exit();
				try{clientSocket.close();} catch (Exception ee) {}
				if (run) 
				{
					log.error("Connection with " + deviceIP + " lost: " + connectErrorMessage + ". Restoring in "+ connectAfterLostPeriod + " secs...");
				}
				synchronized(this) 
				{
					try{wait(connectAfterLostPeriod * 1000);} catch (InterruptedException ie) {}
				}
			}
			catch (SocketException se)
			{
				if (!connectErrorMessage.contains(se.getMessage()))
				{
					connected = false;
					connectErrorMessage = se.getMessage();
					log.error("Cannot connect to " + deviceIP + ": " + connectErrorMessage + ". Retrying in " + connectAttemptsPeriod + " seconds. ");
				}
				synchronized(this) 
				{
					try{wait(connectAttemptsPeriod * 1000);} catch (InterruptedException ie) {}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	}

	public synchronized void disconnect()
	{
		log.info("Disconnecting...");
		run = false;
		synchronized(this) {notifyAll();}
		log.info("Disconnected.");
	}
	
	public synchronized void reconnect(String errorMessage)
	{
		connectErrorMessage = errorMessage;
		synchronized(this) {notifyAll();}
	}

}

