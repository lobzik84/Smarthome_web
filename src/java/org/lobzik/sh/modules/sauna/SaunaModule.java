package org.lobzik.sh.modules.sauna;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.lobzik.sh.CommonData;
import org.lobzik.sh.Parameter;
import org.lobzik.sh.modules.ModuleAbstract;
import org.lobzik.tools.CommonTools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

@SuppressWarnings("rawtypes")
public class SaunaModule extends ModuleAbstract

	{
		private static SaunaConnector connector = null;
		private static  Logger log = null;
		private static final HashMap<String, String> saunaState = new HashMap<String, String>();
		private static SaunaCommandHandler handler = null;
		int logItemsOnPage = 25;
		
		private static final HashMap configMap = new HashMap();

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
			//int  = CommonTools.parseInt(configMap.get(""), 1);
			 	
			connector = SaunaConnector.getInstance(deviceIp, port, connectAfterLostPeriod, connectAttemptsPeriod, tucTucPeriod, commandTimeout);
			connector.setLogger(log);
			handler = SaunaCommandHandler.getInstance(connector, saunaState, log);
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
		
		public void shutdown() throws Exception
		{
			connector.disconnect();
		}
		
		public void service(HttpServletRequest request, HttpServletResponse response)
				throws Exception {

			String templateJspName = (String)request.getAttribute("TemplateJspName");
			String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
			int index = request.getRequestURL().lastIndexOf(moduleUrl) + moduleUrl.length();
			String mode = request.getRequestURL().substring(index);
			
			if (request.getMethod().equals("POST"))
			{
				String command = request.getParameter("command");
				int value = CommonTools.parseInt(request.getParameter("value"), 0);
				if (command != null && command.length() > 0)
				{
					if (value > 0)
						handler.handle(command + "=" + value);
					else
						handler.handle(command);
				}
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
				}
			}

			HashMap<String, Object> jspData = new HashMap<String, Object>();

			jspData.put("SaunaNavigationJspName", "/modules/sauna/sauna_navigation.jsp");
			jspData.put("Connected", connector.isConnected());
			jspData.put("ErrorMessage", connector.getErrorMessage());
			
			if (mode.startsWith("commands"))
			{
				jspData.put("SaunaJspName","/modules/sauna/sauna_commands.jsp");
			}
			else if (mode.startsWith("settings"))
			{
				handler.handle("pns");
				jspData.put("SaunaJspName","/modules/sauna/sauna_settings.jsp");
				jspData.put("SaunaState", saunaState);
			}
			else if (mode.startsWith("terminal"))
			{
				jspData.put("SaunaJspName","/modules/sauna/sauna_terminal.jsp");
				jspData.put("SaunaLog", connector.getSaunaReplies()); 
			}
			else if (mode.startsWith("log"))
			{
				int pageNo = 0;
				if (mode.contains("/")) pageNo = CommonTools.parseInt(mode.substring(mode.indexOf("/") + 1), 0);
				ArrayList<String> logLines = new ArrayList<String>();
				String grep = request.getParameter("grep");
				String level = request.getParameter("level");
				int pages = getDBLog(logLines, pageNo, logItemsOnPage, level, grep);//;getSaunaLogReplies(logLines, pageNo, 40, level, grep);
				jspData.put("SaunaJspName","/modules/sauna/sauna_log.jsp");
				jspData.put("SaunaLog", logLines); 
				jspData.put("SaunaLogPageNo", pageNo); 
				jspData.put("SaunaLogPages", pages); 
			}
			else
			{
				if (request.getMethod().equals("POST")) handler.handle("ps");
				jspData.put("SaunaJspName","/modules/sauna/sauna_state.jsp");
				jspData.put("SaunaState", saunaState);
			}

			jspData.put("ModuleJspName", "/modules/sauna/sauna.jsp");

			
			
			RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher(templateJspName);
		    request.setAttribute("JspData", jspData);
		    disp.include(request, response);

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
		
	}


