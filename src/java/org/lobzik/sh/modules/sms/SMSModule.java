package org.lobzik.sh.modules.sms;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class SMSModule extends ModuleAbstract

	{
		private static SMSConnector connector = null;
		private static Logger log = null;
		private static SMSCommandHandler handler = null;
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
		
		public void signal(Parameter p) throws Exception
		{
			//log.debug("Signal! " + p.getName() + ":" + p.get());
			if (p.getName().equals("NEW_SMS_TEXT"))
			{
				log.info("NEW Signal SMS to " + CommonData.parameters.get("NEW_SMS_RECIPIENT"));
				handler.sendMessage(CommonData.parameters.get("NEW_SMS_RECIPIENT"), p.get());
			}
		}
		
		public void start() throws Exception
		{
			String deviceIp = (String)configMap.get("DeviceIp");
			int port = CommonTools.parseInt(configMap.get("DevicePort"), 1);
			int connectAfterLostPeriod = CommonTools.parseInt(configMap.get("ConnectAfterLostPeriod"), 1);
			int commandTimeout = CommonTools.parseInt(configMap.get("CommandTimeout"), 1);
			int tucTucPeriod = CommonTools.parseInt(configMap.get("TucTucPeriod"), 1);
			int connectAttemptsPeriod = CommonTools.parseInt(configMap.get("ConnectAttemptsPeriod"), 1);
			 	
			connector = SMSConnector.getInstance(deviceIp, port, connectAfterLostPeriod, connectAttemptsPeriod, tucTucPeriod, commandTimeout);
			connector.setLogger(log);
			Connection conn = DBTools.openConnection(CommonData.dataSourceName);
			handler = SMSCommandHandler.getInstance(connector, log, conn, configMap);
			connector.setCommandHandler(handler);
			handler.start();
			connector.start();
		}
		
		public void shutdown() throws Exception
		{
			connector.disconnect();
			handler.exit();
		}
		
		public void service(HttpServletRequest request, HttpServletResponse response)	throws Exception 
		{

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
						handler.handle(command + "\r");
				}
				else
				{
					String message = request.getParameter("message");
					String number = request.getParameter("number");
					if (message != null && message.length() > 0 && number != null && number.length() > 0)
					{
						int msgId = handler.sendMessage(number, message);
						log.info("New outgoing SMS ID=" + msgId + " to " + number + " from " + request.getRemoteHost());
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
						if ("RequestSpeed".equals(command))
						{
							handler.doSpeedRequest();
						}
					}
				}
			}

			HashMap<String, Object> jspData = new HashMap<String, Object>();

			jspData.put("SMSNavigationJspName", "/modules/sms/sms_navigation.jsp");
			jspData.put("Connected", connector.isConnected());
			jspData.put("ErrorMessage", connector.getErrorMessage());
			
			if (mode.startsWith("outbox"))
			{
				jspData.put("Outbox",getOutbox());
				jspData.put("SMSJspName","/modules/sms/sms_outbox.jsp");
			}
			else if (mode.startsWith("settings"))
			{

				jspData.put("SMSJspName","/modules/sms/sms_settings.jsp");

			}
			else if (mode.startsWith("terminal"))
			{
				jspData.put("SMSJspName","/modules/sms/sms_terminal.jsp");
				jspData.put("SMSLog", connector.getSMSReplies()); 
			}
			else if (mode.startsWith("log"))
			{
				int pageNo = 0;
				if (mode.contains("/")) pageNo = CommonTools.parseInt(mode.substring(mode.indexOf("/") + 1), 0);
				ArrayList<String> logLines = new ArrayList<String>();
				String grep = request.getParameter("grep");
				String level = request.getParameter("level");
				int pages = getDBLog(logLines, pageNo, logItemsOnPage, level, grep);//;getSMSLogReplies(logLines, pageNo, 40, level, grep);
				jspData.put("SMSJspName","/modules/sms/sms_log.jsp");
				jspData.put("SMSLog", logLines); 
				jspData.put("SMSLogPageNo", pageNo); 
				jspData.put("SMSLogPages", pages); 
			}
			else
			{
				jspData.put("Inbox",getInbox());
				jspData.put("SMSJspName","/modules/sms/sms_inbox.jsp");

			}

			jspData.put("ModuleJspName", "/modules/sms/sms.jsp");

			
			
			RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher(templateJspName);
		    request.setAttribute("JspData", jspData);
		    disp.include(request, response);

		}
		
		private List<HashMap> getInbox() throws Exception
		{
			String sSQL = "SELECT * FROM SMS_INBOX ORDER BY DATE DESC ";
			Connection conn = DBTools.openConnection(CommonData.dataSourceName);
			List<HashMap> result = null;
			try
			{
				result = DBSelect.getRows(sSQL, conn);
				DBTools.closeConnection(conn);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				throw e;
			}
			return result;

		}
		
		private List<HashMap> getOutbox() throws Exception
		{
			String sSQL = "SELECT * FROM SMS_OUTBOX ORDER BY DATE DESC ";
			Connection conn = DBTools.openConnection(CommonData.dataSourceName);
			List<HashMap> result = null;
			try
			{
				result = DBSelect.getRows(sSQL, conn);
				DBTools.closeConnection(conn);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				throw e;
			}
			return result;

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
				String sSQL = "SELECT * FROM LOGS " + whereClause + " ORDER BY DATED LIMIT " + (pageNo - 1) * linesOnPage + "," + linesOnPage + ";";
					
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