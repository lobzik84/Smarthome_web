package org.lobzik.sh.modules.filter;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.lobzik.sh.CommonData;

public class FilterCommandHandler 
{
	private static final FilterCommandHandler instance = new FilterCommandHandler();
	private static FilterConnector connector = null;
	private static HashMap<String, String> state = null;
	private static int timeout = 1000;
	private static Logger log = null;
	private static boolean busy = false;
	private FilterCommandHandler() {}
	private static String lastRecieved = "";
	
	public static FilterCommandHandler getInstance(FilterConnector filterConnector, HashMap<String, String> stateMap, Logger logger)
	{
		log = logger;
		connector = filterConnector;
		state = stateMap;
		timeout = connector.getCommandTimeout();
		return instance;
	}
	
	public void lineRecieved(ArrayList<String> recievedLines)
	{
		if (recievedLines.get(recievedLines.size()-1).trim().length() > 0)
			lastRecieved = recievedLines.get(recievedLines.size()-1).trim();
		if (lastRecieved.equals("OK") || lastRecieved.contains("ERROR"))
		{
			synchronized (this) 
			{
				notify();				
			}
		}
		else
			parseReplyLine(lastRecieved);
	}
	
	public synchronized void handle(String command) throws Exception
	{
		if (connector == null || !connector.isConnected()) return;

		while (busy)
			synchronized(this) 
			{
				try{wait(timeout*10);} catch (InterruptedException ie) {}
			}
		busy = true;
		connector.getFilterReplies().clear();
		connector.doCommand(command);
		synchronized(this) 
		{
			try{wait(timeout);} catch (InterruptedException ie) {}
			busy = false;
		}
	}
	
	public void parseReplyLine(String replyLine)
	{
		int index = replyLine.indexOf(":");
		if (index <= 0) return;
		String key = replyLine.substring(0, index).trim();
		String value = replyLine.substring(index+1).trim();
		state.put(key, value);
		int paramId = CommonData.parameters.resolveAlias(key);
		CommonData.parameters.set(paramId, value);		
	}
}
