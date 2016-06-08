package org.lobzik.sh.modules.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

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
public class MainModule  extends ModuleAbstract
{
	//private static final String[] statParams = {"",""};
	private static final ArrayList<String> statParamsList = new ArrayList<String>();
	private static List<HashMap> paramsList = null;
	static int logItemsOnPage = 35;
	private static  Logger log = null;
	private static final HashMap configMap = new HashMap();
	private static HashMap<Integer, Timer> timers = new HashMap<Integer, Timer>();
	private final static String datePattern = "yyyy.MM.dd HH:mm:ss";
	private static GraphBuilder graphBuilder = null;
	
	public void init(HashMap config) throws Exception 
	{
		logItemsOnPage = CommonTools.parseInt(config.get("LogItemsOnPage"), logItemsOnPage);

		configMap.putAll(config);
		Logger initLog = (Logger)config.get("Logger");
		if (initLog != null) log = initLog; 
		graphBuilder = new GraphBuilder(configMap);
		Connection conn = null;
		try
		{
			String sSQL =	" SELECT DISTINCT P.ID, P.NAME, G.TYPE FROM PARAMETERS P " + 
							"INNER JOIN GRAPHS G ON G.ID = P.GRAPH_ID ";
			conn = DBTools.openConnection(CommonData.dataSourceName);
			paramsList = DBSelect.getRows(sSQL, conn);
			DBTools.closeConnection(conn);
			for (HashMap h: paramsList)
				statParamsList.add((String)h.get("NAME"));
		}
		catch (Exception e)
		{
			DBTools.closeConnection(conn);
			log.error("Error reading parameters config: " + e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	public void start() throws Exception
	{
		if (!CommonData.debug)
			startTimers();
	}
	
	public void signal(Parameter p) throws Exception
	{
		if (statParamsList.contains(p.getName()))
		{
			Connection conn = null;
			try
			{
				String svalue = p.get().trim();
				int index = svalue.indexOf(" ");
				if (index > 0)
					svalue = svalue.substring(0, index);
				double value = CommonTools.parseDouble(svalue, 0); 
				conn = DBTools.openConnection(CommonData.dataSourceName);	
				HashMap db = new HashMap();
				db.put("PARAMETER_ID", p.getId());
				db.put("VALUE", value); 
				db.put("DATE", p.getUpdateTime());
				//TODO может вынести эту трататень в отдельный тред, чтоб не тормозить обработку сигналов - обдумать
				if (!CommonData.debug) 
					DBTools.insertRow("STATS", db, conn);
				DBTools.closeConnection(conn);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				log.error("Error writing statistics " + e.getClass().getName() + ": " + e.getMessage());
			}
		}
		if (p.getName().equals("TIMER_EVENT") && p.get().equals("UPDATE_DATA"))
		{//сбор данных погоды из БД WeeWx - вообще место эитому в отдельном модуле погоды, но пока не буду заводить 
			fetchWeatherDataFromDB();
		}
		if (p.getName().equals("TIMER_EVENT") && p.get().equals("DbCleanUp"))
		{			
			Connection conn = null;
			int daysDebug = CommonTools.parseInt(configMap.get("DaysToStoreDebugMessages"), 5);
			try
			{
				conn = DBTools.openConnection(CommonData.dataSourceName);
				DBSelect.executeStatement("DELETE FROM LOGS WHERE LEVEL = 'DEBUG' AND DATEDIFF(CURDATE(), DATED) >= "+daysDebug, null, conn);
				for (HashMap h: paramsList)
				{
					int paramId = CommonTools.parseInt(h.get("ID"), 0);
					int paramType = CommonTools.parseInt(h.get("TYPE"), 0);
					if (paramType == 1)
					{
						DBSelect.executeStatement(	"INSERT INTO STATS " +
													" (SELECT PARAMETER_ID, AVG(VALUE) AS VALUE, DATE(DATE) AS DATE, 1 AS GROUPED " +
													" FROM STATS WHERE GROUPED=0 AND PARAMETER_ID=" + paramId +
													" AND DATEDIFF(CURDATE(), DATE) > "+ daysDebug +
													" GROUP BY (DATE(DATE)));", null, conn);
					}
					else if (paramType == 2)
					{
						DBSelect.executeStatement(	"INSERT INTO STATS " +
													" (SELECT PARAMETER_ID, MIN(VALUE) AS VALUE, DATE(DATE) AS DATE, 1 AS GROUPED " +
													" FROM STATS WHERE GROUPED=0 AND PARAMETER_ID=" + paramId +
													" AND DATEDIFF(CURDATE(), DATE) > "+ daysDebug +
													" GROUP BY (DATE(DATE)));", null, conn);
					}
				}
				DBSelect.executeStatement("DELETE FROM STATS WHERE GROUPED = 0 AND DATEDIFF(CURDATE(), DATE) > "+daysDebug, null, conn);
				DBTools.closeConnection(conn);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				log.error("Error cleaning DB: " + e.getClass().getName() + ": " + e.getMessage());
			}
			log.debug("DB log and stats cleaning done");
		}
		
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		String templateJspName = (String)request.getAttribute("TemplateJspName");
		String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
		int index = request.getRequestURL().lastIndexOf(moduleUrl) + moduleUrl.length();
		String mode = request.getRequestURL().substring(index);
		HashMap<String, Object> jspData = new HashMap<String, Object>();
		jspData.put("NavigationJspName", "/modules/main/main_navigation.jsp");
		if (mode.startsWith("timer"))
		{	
			Connection conn = null;
			try
			{
				conn = DBTools.openConnection(CommonData.dataSourceName);
				List<HashMap> resList = getDBTimers(conn);
				if (request.getMethod().equals("POST") && request.getParameter("save") != null)
				{
					Map reqParams = request.getParameterMap();
					ArrayList<HashMap> changed = new ArrayList<HashMap>();
					for (HashMap db: resList)
					{
						int id = CommonTools.parseInt(db.get("ID"), 0);
						HashMap changedMap = new HashMap();
						changedMap.put("ID", id);
						int enabled = 0;
						if (reqParams.containsKey(id+ "_ENABLED")) enabled = 1;
						if (CommonTools.parseInt(db.get("ENABLED"), 0) != enabled) changedMap.put("ENABLED", enabled);
						String name = request.getParameter(id + "_NAME");
						if (!((String)db.get("NAME")).equals(name)) changedMap.put("NAME", name);
						String startDateStr = request.getParameter(id + "_START_DATE_STR");
						Date startDate = CommonTools.parseDate(startDateStr, datePattern);
						if (!((String)db.get("START_DATE_STR")).equals(startDateStr) && startDate != null) changedMap.put("START_DATE", startDate);
						int periodU = CommonTools.parseInt(request.getParameter(id + "_PERIOD_U"), 0);
						int periodUnits = CommonTools.parseInt(request.getParameter(id + "_PERIOD_UNITS"), 0);
						int period = periodU * periodUnits;
						if (CommonTools.parseInt(db.get("PERIOD"), 0) != period) changedMap.put("PERIOD", period);
						if (changedMap.size() > 1) changed.add(changedMap);
					}
					jspData.put("Changed", changed);
					for (HashMap h: changed)
					{
						DBTools.updateRow("TIMER", h, conn);
					}
					if (changed.size() > 0)
					{
						resList = getDBTimers(conn);
						for (HashMap h: changed)
						{
							int id = CommonTools.parseInt(h.get("ID"), 0);
							for (HashMap timerMap: resList)
							{
								if (id == CommonTools.parseInt(timerMap.get("ID"), 0))
								{
									Timer timer = timers.get(id);
									if (timer != null)
									{
										timer.cancel();
										timers.remove(id);
									}
									startTimer(timerMap);
								}
							}
						}
					}
				}
				jspData.put("Timers", resList);
				DBTools.closeConnection(conn);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				throw e;
			}
			jspData.put("MainJspName","/modules/main/main_timer.jsp");
		}
		else if (mode.startsWith("stats"))
		{
			String sSQL = " SELECT * FROM GRAPHS G ORDER BY G.ORDER; ";
			Connection conn = DBTools.openConnection(CommonData.dataSourceName);
			try
			{
				List<HashMap> result = DBSelect.getRows(sSQL, conn);
				jspData.put("Graphs", result);
				DBTools.closeConnection(conn);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				throw e;
			}
		
			jspData.put("MainJspName","/modules/main/main_stats.jsp");
		}		

		else
		{
			Connection conn = DBTools.openConnection(CommonData.dataSourceName);
			jspData.put("MainJspName","/modules/main/main_state.jsp");
			try
			{
				String whereClause = " WHERE LEVEL != 'DEBUG' ";
				String countSQL = "SELECT COUNT(*) AS CNT FROM LOGS " + whereClause + ";";
				int lines = CommonTools.parseInt(DBSelect.getRows(countSQL, conn).get(0).get("CNT"), 1);
				String sSQL = "SELECT L.DATED, M.NAME, L.LEVEL, L.MESSAGE FROM LOGS L INNER JOIN MODULES M ON M.ID = L.MODULE_ID  "
							+ whereClause + " ORDER BY L.DATED, L.ID LIMIT " + (lines - logItemsOnPage) + "," + logItemsOnPage + ";";
				List<HashMap> result = DBSelect.getRows(sSQL, conn);
				jspData.put("LogLines", result);
				DBTools.closeConnection(conn);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				throw e;
			}		
		}
		jspData.put("ModuleJspName", "/modules/main/main.jsp");
		fetchWeatherDataFromDB();
		
		RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher(templateJspName);
	    request.setAttribute("JspData", jspData);
		
	    if (mode.startsWith("graph"))
		{
	    	graphBuilder.service(request, response);

		}
		else
			disp.include(request, response);
		

	}
	
	public void shutdown() throws Exception
	{		
		for (int id: timers.keySet())
		{
			Timer timer = timers.get(id);
			timer.cancel();
		}
	}

	private void startTimers() throws Exception
	{
		Connection conn = DBTools.openConnection(CommonData.dataSourceName);
		List<HashMap> timerDBList = getDBTimers(conn);
		conn.close();
		for (HashMap timerMap: timerDBList)
		{
			startTimer(timerMap);
		}
	}
	
	private void startTimer(HashMap timerMap) throws Exception
	{
		Date startDate = (Date)timerMap.get("START_DATE");
		int period = CommonTools.parseInt(timerMap.get("PERIOD"), 0);
		int id = CommonTools.parseInt(timerMap.get("ID"), 0);
		int enabled = CommonTools.parseInt(timerMap.get("ENABLED"), 0);
		if (enabled == 0) return;
		if (startDate == null && period == 0) return;
		if (startDate == null) startDate = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		if (cal.getTimeInMillis() <= System.currentTimeMillis() && period > 0)
		{
			while (cal.getTimeInMillis() <= System.currentTimeMillis())
			{
				cal.add(Calendar.SECOND, period);
			}
		}
		
		String name = (String)timerMap.get("NAME");
		Timer timer = new Timer(name + "-Timer", true);
		SignalTask signalTask = new SignalTask("TIMER_EVENT", name, null);
		if (period > 0)
			timer.scheduleAtFixedRate(signalTask, cal.getTime(), period*1000);
		else
			timer.schedule(signalTask, cal.getTime());
		timers.put(id, timer);
	}

	private List<HashMap> getDBTimers(Connection conn) throws Exception
	{
		String sSQL = " SELECT * FROM TIMER ";
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		List<HashMap> result = DBSelect.getRows(sSQL, conn);
		for (HashMap timer: result)
		{
			if (timer.get("START_DATE") != null)
				timer.put("START_DATE_STR", sdf.format(timer.get("START_DATE")));
			else
				timer.put("START_DATE_STR", "Сразу");
			int period = CommonTools.parseInt(timer.get("PERIOD"), -1);
			int periodUnits = 1;
			if (period % 60 == 0)
			{
				periodUnits = 60;
				period = period / 60;
				if (period % 60 == 0)
				{
					periodUnits = 3600;
					period = period / 60;
					
				}
			}
			timer.put("PERIOD_U", period);
			timer.put("PERIOD_UNITS", periodUnits);
		}
		return result;
	}

	private static void fetchWeatherDataFromDB() throws Exception 
	{
		Connection conn = null;
		try
		{
			conn = DBTools.openConnection(CommonData.dataSourceName);
			long unixSecs = (System.currentTimeMillis() / 1000) - 300;
			String sSQL = " select av.*, if (windDirAvg < 0, windDirAvg+360, windDirAvg) as windDir  from " +
					" (select" +
					" max(a.windGust) as windGustMS," +
					" avg(a.windSpeed) as windSpeedMS," +
					" avg(a.barometer*0.7495) as barommHg," +
					" avg(a.outTemp) as outTemp," +
					" avg(a.outHumidity) as outHumidity," +
					" degrees(atan2(avg(windSpeed*sin(radians(windDir))), avg(windSpeed*cos(radians(windDir))))) as windDirAvg " +
					//" avg(windDir) as windDirWrong" +
					" from weewx.archive a "+ 
					" where a.dateTime > "+ unixSecs + ") av";
			List<HashMap> result = DBSelect.getRows(sSQL, conn);
			for (HashMap h: result) 
			{
				Date weeDate = new Date((long)CommonTools.parseInt(h.get("dateTime"), 0) * 1000l);
				for (Object key: h.keySet()) 
				{
					if (h.get(key) != null) 
					{
						int paramId = CommonData.parameters.resolveAlias(key + "");
						if (paramId > 0) 
								CommonData.parameters.set(paramId, h.get(key)+"");
					}
				}				
			}
			DBTools.closeConnection(conn);
		}
		catch (Exception e)
		{
			DBTools.closeConnection(conn);
			log.error("Error getting weather data " + e.getClass().getName() + ": " + e.getMessage());
		}
	}
}



class SignalTask extends TimerTask
{
	private String name = null;
	private String value = null;
	private Object data = null;
	
	public SignalTask(String nameP, String valueP, Object dataP)
	{
		super();
		name = nameP;
		value = valueP;
		data = dataP;
	}
	@Override
	public void run() 
	{
		System.out.println("Timer signal: " + name + "=" + value);
		CommonData.parameters.sendSignal(name, value, data);
	}
}