package org.lobzik.sh.modules.filter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.lobzik.sh.CommonData;
import org.lobzik.sh.Parameter;
import org.lobzik.sh.modules.ModuleAbstract;
import org.lobzik.tools.CommonTools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

@SuppressWarnings("rawtypes")
public class FilterModule  extends ModuleAbstract
{
	private static FilterConnector connector = null;
	private static Logger log = null;
	private static final HashMap<String, String> state = new HashMap<String, String>();
	private static FilterCommandHandler handler = null;
	private static final HashMap configMap = new HashMap();
	private int logItemsOnPage = 25;
	
	@SuppressWarnings("unchecked")
	public void init(HashMap config) throws Exception 
	{
		configMap.putAll(config);
		Logger initLog = (Logger)config.get("Logger");
		if (initLog != null) log = initLog; 
		logItemsOnPage = CommonTools.parseInt(configMap.get("LogItemsOnPage"), logItemsOnPage);
	}

	public void start() throws Exception
	{
		String deviceIp = (String)configMap.get("DeviceIp");
		int port = CommonTools.parseInt(configMap.get("DevicePort"), 1);
		int connectAfterLostPeriod = CommonTools.parseInt(configMap.get("ConnectAfterLostPeriod"), 1);
		int commandTimeout = CommonTools.parseInt(configMap.get("CommandTimeout"), 1);
		int tucTucPeriod = CommonTools.parseInt(configMap.get("TucTucPeriod"), 1);
		int connectAttemptsPeriod = CommonTools.parseInt(configMap.get("ConnectAttemptsPeriod"), 1);
			
		connector = FilterConnector.getInstance(deviceIp, port, connectAfterLostPeriod, connectAttemptsPeriod, tucTucPeriod, commandTimeout);
		connector.setLogger(log);

		handler = FilterCommandHandler.getInstance(connector, state, log);
		connector.setCommandHandler(handler);
		connector.start();
	}
	

	public void signal(Parameter p) throws Exception
	{
		if (p.getName().equals("TIMER_EVENT"))
		{
			if (p.get().equals("SYNC_TIME"))
			{
				Calendar cal = Calendar.getInstance();
				long unixsecs = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET) + System.currentTimeMillis()) / 1000;
				handler.handle("stunixsecs=" + unixsecs);
			}
			else if (p.get().equals("UPDATE_DATA"))
			{
				handler.handle("ps");
			}
		}
	}
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{

		String templateJspName = (String)request.getAttribute("TemplateJspName");
		String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
		int index = request.getRequestURL().lastIndexOf(moduleUrl) + moduleUrl.length();
		String mode = request.getRequestURL().substring(index);
		
		if (request.getMethod().equals("POST"))
		{
			String command = request.getParameter("command");
			if (command != null && command.length() > 0)
				handler.handle(command);
			else
			{
				for (Object key: request.getParameterMap().keySet())
				{
					String skey = key instanceof String?(String)key:"";
					if (skey != null && skey.endsWith("_command"))
					{
						command = skey.substring(0, skey.indexOf("_command"));
						handler.handle(command);
					}
				}
				command = request.getParameter("sync_time");
				if (command != null && command.length() > 0)
					handler.handle("stmillis=" + System.currentTimeMillis());
			}
		}

		HashMap<String, Object> jspData = new HashMap<String, Object>();

		jspData.put("FilterNavigationJspName", "/modules/filter/filter_navigation.jsp");
		jspData.put("Connected", connector.isConnected());
		jspData.put("ErrorMessage", connector.getErrorMessage());
		
		if (mode.startsWith("commands"))
		{
			jspData.put("FilterJspName","/modules/filter/filter_commands.jsp");
		}
		else if (mode.startsWith("settings"))
		{
			handler.handle("pvs");
			jspData.put("FilterJspName","/modules/filter/filter_settings.jsp");
			jspData.put("state", state);
		}
		else if (mode.startsWith("terminal"))
		{
			jspData.put("FilterJspName","/modules/filter/filter_terminal.jsp");
			jspData.put("FilterLog", connector.getFilterReplies()); 
		}
		else if (mode.startsWith("log"))
		{
			int pageNo = 0;
			if (mode.contains("/")) pageNo = CommonTools.parseInt(mode.substring(mode.indexOf("/") + 1), 0);
			ArrayList<String> logLines = new ArrayList<String>();
			String grep = request.getParameter("grep");
			String level = request.getParameter("level");
			int pages = getDBLog(logLines, pageNo, logItemsOnPage, level, grep);//getFilterLogReplies(logLines, pageNo, 40, level, grep);
			jspData.put("FilterJspName","/modules/filter/filter_log.jsp");
			jspData.put("FilterLog", logLines); 
			jspData.put("FilterLogPageNo", pageNo); 
			jspData.put("FilterLogPages", pages); 
		}
		else
		{
			if (request.getMethod().equals("POST")) handler.handle("ps");
			jspData.put("FilterJspName","/modules/filter/filter_state.jsp");
			jspData.put("FilterState", state);
		}

		jspData.put("ModuleJspName", "/modules/filter/filter.jsp");

		
		
		RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher(templateJspName);
	    request.setAttribute("JspData", jspData);
	    disp.include(request, response);

	}

	public void shutdown() throws Exception
	{
		connector.disconnect();
	}
	
	private int getDBLog(ArrayList<String> logLines, int pageNo, int linesOnPage, String logLevel, String contains) throws Exception
	{
		int pages = 1;
		String whereClause = "WHERE MODULE_ID = " + configMap.get("ID");
		if (logLevel != null && logLevel.length() > 0)
		{
			whereClause += " AND LEVEL IN (";
			boolean levelFound = false;
			for (String level: CommonTools.loglevels)
			{
				if (level.equals(logLevel)) levelFound = true;
				if (levelFound) whereClause += "\"" + level + "\", ";
			}
			whereClause += "\"\") "; 
		}
		ArrayList<String> argsList = null;
		if (contains != null && contains.length() > 0)
		{
			argsList = new ArrayList<String>();
			argsList.add("%" + contains + "%");
			whereClause += " AND MESSAGE LIKE ? ";
		}
		String countSQL = "SELECT COUNT(*) AS CNT FROM LOGS " + whereClause + ";";
		Connection conn = DBTools.openConnection(CommonData.dataSourceName);
		try 
		{
			int lines = CommonTools.parseInt(DBSelect.getRows(countSQL, argsList, conn).get(0).get("CNT"), 1);
			pages = (lines / linesOnPage) + 1;
			if (pageNo == 0)
				pageNo = pages;
			String sSQL = "SELECT * FROM LOGS " + whereClause + " ORDER BY DATED,ID LIMIT " + (pageNo - 1) * linesOnPage + "," + linesOnPage + ";";
				
			List<HashMap> result = DBSelect.getRows(sSQL, argsList, conn);
			DBTools.closeConnection(conn);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			
			for (HashMap h:result)
			{
				String logLine = sdf.format((Date)h.get("DATED"));
				logLine += " " + (String)h.get("LEVEL");
				logLine += " : " + (String)h.get("MESSAGE");
				logLines.add(logLine);
			}
		}
		catch (Exception e)
		{
			DBTools.closeConnection(conn);
			throw e;
		}
		return pages;
	}
	
	@SuppressWarnings({ "unused" })
	private int getFilterLogReplies(ArrayList<String> logLines, int pageNo, int linesOnPage, String logLevel, String contains)
	{
		int pages = 1;
		FileAppender appender = null;
		Enumeration en = log.getAllAppenders();
		if (!en.hasMoreElements())
			en = Logger.getRootLogger().getAllAppenders(); //в поисках лога
		for (; en.hasMoreElements() ;) 
		{
			Object o = en.nextElement();
			if (o instanceof FileAppender)
			{
				appender = (FileAppender)o;
				break;
			}
		}
		if (appender == null)
			return pages;
	    try
	    {
	        BufferedReader reader = new BufferedReader(new FileReader(appender.getFile()));
	        String line = null;
	        int lines = 0;
	        while ((line = reader.readLine()) != null)
	        { 
	        	if ((contains == null || line.contains(contains)) && (logLevel == null || levelMatches(line, logLevel)))
	        	{
	        		if (pageNo == 0 || lines < pageNo * linesOnPage) logLines.add(line);
	        		lines++;
	        	}
	        	if (logLines.size() > linesOnPage)	logLines.remove(0);
	        }
	        pages = (lines / linesOnPage) + 1;
	      }
	      catch (Exception e)
	      {
	        log.error(e);
	      }
		return pages;
	}

	private static boolean levelMatches(String line, String logLevel)
	{
		boolean levelFound = false;
		for (String level: CommonTools.loglevels)
		{
			if (level.equals(logLevel)) levelFound = true;
			if (levelFound && line.contains(level)) return true;
		}
		return false;
	}
}
