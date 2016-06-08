package org.lobzik.sh.modules.console;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
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
@SuppressWarnings({ "rawtypes", "unchecked" })

public class ConsoleModule extends ModuleAbstract{
	private static  Logger log = null;
	private static final HashMap configMap = new HashMap();
	private static int moduleId = 0;
	private static boolean modectOnByTimer = false;
	
	public void start() throws Exception
	{
		
	}

	public void signal(Parameter p) throws Exception
	{
		if (p.getName().equals("SMS_RECIEVED_TEXT"))
		{
			if (p.get().equalsIgnoreCase("modect.on"))
				enableModect(CommonData.parameters.get("SMS_RECIEVED_SENDER"), new HashMap());
			else if (p.get().equalsIgnoreCase("modect.off"))
				disableModect(CommonData.parameters.get("SMS_RECIEVED_SENDER"), new HashMap());
		}
		else if (p.getName().equals("TIMER_EVENT") && CommonTools.parseInt(configMap.get("handleTimer"), 0) == 1)
		{//TODO переосмыслить логику - работает неправильно! могут снять-поставить во время таймера, тогда что? вроде исправил
			if (p.get().equalsIgnoreCase("modect.on"))
			{
				if ("on".equalsIgnoreCase(CommonData.parameters.get("FILTER_POWER")) || CommonTools.parseInt(configMap.get("handlePower"), 0) == 0)
					 enableModect("timer", new HashMap());
				modectOnByTimer = true;
			}
			else if (p.get().equalsIgnoreCase("modect.off"))
			{
				if ("on".equalsIgnoreCase(CommonData.parameters.get("FILTER_POWER")) || CommonTools.parseInt(configMap.get("handlePower"), 0) == 0)
					disableModect("timer", new HashMap());
				modectOnByTimer = false;
			}
		}
		else if ("FILTER_POWER".equals(p.getName()) && CommonTools.parseInt(configMap.get("handlePower"), 0) == 1 
				&& (!modectOnByTimer || CommonTools.parseInt(configMap.get("handleTimer"), 0) ==  0))
		{
			if ("off".equalsIgnoreCase(p.get()) && "on".equalsIgnoreCase(p.getPrevious()))
				enableModect("power off", new HashMap());
			else if ("on".equalsIgnoreCase(p.get()) && "off".equalsIgnoreCase(p.getPrevious()))
				disableModect("power on", new HashMap());
		}
	}
	
	public void init(HashMap config) throws Exception
	{
		configMap.putAll(config);
		Logger initLog = (Logger)config.get("Logger");
		if (initLog != null) log = initLog; 
	}
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		String templateJspName = (String)request.getAttribute("TemplateJspName");
		String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
		moduleId = CommonTools.parseInt(((HashMap)request.getAttribute("ModuleMap")).get("ID"), 0);
		int index = request.getRequestURL().lastIndexOf(moduleUrl) + moduleUrl.length();
		String mode = request.getRequestURL().substring(index);
		HashMap<String, Object> jspData = new HashMap<String, Object>();
		if (request.getMethod().equals("POST"))
		{
			String command = null;
			for (Object key: request.getParameterMap().keySet())
			{
				String skey = key instanceof String?(String)key:"";
				if (skey != null && skey.endsWith("_command"))
				{
					command = skey.substring(0, skey.indexOf("_command"));
				}
			}
			if ("ModectOn".equals(command))
			{
				enableModect(request.getRemoteHost(), jspData);
			}
			if ("ModectOff".equals(command))
			{
				disableModect(request.getRemoteHost(), jspData);
			}
			if (command == null) command = request.getParameter("command");
			if ("do".equals(command))
			{
				if (CommonTools.parseInt(request.getParameter("handleTimer"), 0) == 1 && CommonTools.parseInt(configMap.get("handleTimer"), 0) == 0 )
				{
					configMap.put("handleTimer", "1");
					updateDBConfig();
				}
				else if (CommonTools.parseInt(request.getParameter("handleTimer"), 0) == 0 && CommonTools.parseInt(configMap.get("handleTimer"), 0) == 1 )
				{
					configMap.put("handleTimer", "0");
					updateDBConfig();
				}
				if (CommonTools.parseInt(request.getParameter("handlePower"), 0) == 1 && CommonTools.parseInt(configMap.get("handlePower"), 0) == 0 )
				{
					configMap.put("handlePower", "1");
					updateDBConfig();
				}
				else if (CommonTools.parseInt(request.getParameter("handlePower"), 0) == 0 && CommonTools.parseInt(configMap.get("handlePower"), 0) == 1 )
				{
					configMap.put("handlePower", "0");
					updateDBConfig();
				}
			}
			String event = request.getParameter("event");
			if (event != null && event.length() > 0)
				log.info("Event: " + event);
		}
		
		if (mode.startsWith("pause"))
		{
			jspData.put("ModuleJspName","/modules/console/pause.jsp");
		}
		else
		{
			try
			{
	            URL url = new URL("http://" + CommonData.rootConfigMap.get("ZMServerIp") + "/zm/index.php?view=monitors_status");
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
	            {
	            	jspData.put("ErrorMessage", "ZM Connection Error:" + connection.getResponseCode());
	            	log.error("ZM Connection Error:" + connection.getResponseCode());
	            }
	            else
	            {
	            	InputStream is = connection.getInputStream();
	            	java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	            	String resp = s.hasNext() ? s.next() : "";
	            	is.close();
	            	connection.disconnect();
	            	ArrayList<Boolean> states = new ArrayList<Boolean>();
	            	ArrayList<String> monitors = new ArrayList<String>();
	            	for (String line:resp.split(";"))
	            	{
	            		int ind = line.indexOf(":");
	            		if (ind <=0 )
	            			continue;
	            		String monitor = line.substring(0, ind).trim();
	            		int state = CommonTools.parseInt(line.substring(ind+1, ind+2), -1);
	            		monitors.add(monitor);
	            		states.add(state==1);
	            	}
	            	String enabledMonitors = ""; 
	            	int enabledCnt = 0;
	            	for (int i = 0; i < monitors.size(); i++)
	            	{
	            		if (!states.get(i)) continue;
	            		if (enabledMonitors.length() > 0) enabledMonitors += ", ";
	            		enabledMonitors += monitors.get(i);
	            		enabledCnt++;
	            	}
	            	String modectStateMessage = "";
	            	if (enabledCnt == 0)
	            		modectStateMessage = "Запись выключена";
	            	else 
	            	{
	            		if (enabledCnt == monitors.size())
	            			modectStateMessage = "Запись включена на всех " + enabledCnt + " камерах.";
	            		else
	            			modectStateMessage = "Запись включена на камерах " + enabledMonitors;
	            	}
	            	jspData.put("configMap", configMap);	
	            	jspData.put("modectStateMessage", modectStateMessage);	
	            	
	            }
	            
			}
			catch (Exception e)
			{
				jspData.put("ErrorMessage",  "ZM Connection Error:" + e.getMessage());
				log.error("ZM Connection Error:" + e.getMessage());
			}
			jspData.put("ModuleJspName","/modules/console/console.jsp");
		}
			
		RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher(templateJspName);
	    request.setAttribute("JspData", jspData);
	    disp.include(request, response);

	}

	
	public static void enableModect(String who, HashMap jspData)
	{
		try
		{
            URL url = new URL("http://" + CommonData.rootConfigMap.get("ZMServerIp") + "/zm/index.php?action=enableall");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
            	jspData.put("ErrorMessage", "ZM Connection Error:" + connection.getResponseCode());
            	log.error("ZM Connection Error:" + connection.getResponseCode());
            }
            else
	            log.info("Motion Detection enabled by " + who);//request.getRemoteHost());
            connection.disconnect();

		}
		catch (Exception e)
		{
			jspData.put("ErrorMessage",  "ZM Connection Error:" + e.getMessage());
			log.error("ZM Connection Error:" + e.getMessage());
		}
	}
	
	public static void disableModect(String who, HashMap jspData)
	{
		try
		{
            URL url = new URL("http://" + CommonData.rootConfigMap.get("ZMServerIp") + "/zm/index.php?action=disableall");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
            	jspData.put("ErrorMessage", "ZM Connection Error:" + connection.getResponseCode());
            	log.error("ZM Connection Error:" + connection.getResponseCode());
            }
            else
	            log.info("Motion Detection disabled by " + who);//request.getRemoteHost());
            connection.disconnect();

		}
		catch (Exception e)
		{
			jspData.put("ErrorMessage",  "ZM Connection Error:" + e.getMessage());
			log.error("ZM Connection Error:" + e.getMessage());
		}
	}

	private void updateDBConfig() throws Exception
	{
		Connection conn = null;
		try
		{
			conn = DBTools.openConnection(CommonData.dataSourceName);
			List<HashMap> configList = DBSelect.getRows(" SELECT * FROM MODULES_CONFIG WHERE MODULE_ID=" + moduleId, conn);
			for(HashMap db: configList)
			{
				String name = (String)db.get("NAME");
				String value = (String)db.get("VALUE");
				if (!value.equals(configMap.get(name)))
				{
					HashMap dbMap = new HashMap();
					dbMap.put("ID", db.get("ID"));
					dbMap.put("VALUE", configMap.get(name));
					DBTools.updateRow("MODULES_CONFIG", dbMap, conn);

					//System.out.println(dbMap);
				}
			}
			DBTools.closeConnection(conn);
		}
		catch (Exception e)
		{
			DBTools.closeConnection(conn);
			throw e;
		}
	}
}