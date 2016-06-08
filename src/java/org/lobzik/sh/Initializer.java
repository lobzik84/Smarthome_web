package org.lobzik.sh;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.jdbc.JDBCAppender;
import org.lobzik.sh.modules.ModuleAbstract;
import org.lobzik.tools.CommonTools;
import org.lobzik.tools.db.mysql.DBSelect;


@SuppressWarnings("rawtypes")
public class Initializer 
{
	private static final Initializer instance = new Initializer();
	private static boolean initDone = false;
	private static boolean initInProgress = false;
	private static Logger log = Logger.getRootLogger();
	private static final Signalizer signalizer = Signalizer.getInstance();
	
	private Initializer()
	{
		
	}
	
	public static Initializer getInstance()
	{
		return instance;
	}
	

	@SuppressWarnings("unchecked")
	public void init() throws Exception
	{
		if (initDone || initInProgress) throw new Exception("Init already done or in progress!");

		initInProgress = true;
		
		CommonData.debug = "lobzik".equals(System.getProperty("user.name"));
		
		PatternLayout layout = new PatternLayout("%d{yyyy.MM.dd HH:mm:ss} %c{1} %-5p: %m%n");
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		BasicConfigurator.configure(consoleAppender);
		log.info("Root Log init ok");
				
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		BasicDataSource ds = (BasicDataSource)envCtx.lookup(CommonData.dataSourceName);
		String sSQL = 	"SELECT * FROM MODULES M " + 
						" WHERE M.STATUS=1 "+
						" ORDER BY M.ORDER; ";
		Connection conn = null;
		String configSQL = "SELECT MODULE_ID, NAME, VALUE FROM MODULES_CONFIG; ";
		List<HashMap> configList = null;
		String paramsSQL = "SELECT * FROM PARAMETERS";
		List<HashMap> parametersList = null;
		try
		{
			conn = ds.getConnection();
			CommonData.modulesList.addAll(DBSelect.getRows(sSQL, conn));
			configList = DBSelect.getRows(configSQL, conn);
			parametersList = DBSelect.getRows(paramsSQL, conn);
		    conn.close();
		}
		catch (Exception e)
		{
			try{conn.close();} catch (Exception ee) {}
			throw e;
		}
		
		CommonData.parameters.configure(parametersList);
		
		log.info("Modules list ready, config ready, parameters storage initialized. Starting modules...");

		String dsUrl = ds.getUrl();
		if (dsUrl.contains("?"))
			dsUrl += "&";
		else
			dsUrl += "?";
		dsUrl += "autoReconnect=true";
      for (HashMap moduleInit: CommonData.modulesList)
      {
    	
    	int moduleId = CommonTools.parseInt(moduleInit.get("ID"), 0);
    	String moduleName = (String)moduleInit.get("NAME");
    	String className = (String)moduleInit.get("CLASS");
	    
  		Logger log = Logger.getLogger(moduleName);

  		if (!CommonData.debug)
  		{
  			Appender appender = ConnJDBCAppender.getAppenderInstance(ds, moduleId);
  			log.addAppender(appender);
  		}
    	moduleInit.put("Logger", log);
    	
    	for (HashMap h:configList)
    	{
    		if (CommonTools.parseInt(h.get("MODULE_ID"), 0) == 0) 
    			CommonData.rootConfigMap.put((String)h.get("NAME"), (String)h.get("VALUE"));
    		if (CommonTools.parseInt(h.get("MODULE_ID"), 0) != moduleId) 
    			continue;
    		moduleInit.put(h.get("NAME"), h.get("VALUE"));
    	}
    	
    	Class cmClass = ModuleAbstract.class.getClassLoader().loadClass(className);
	    if (cmClass == null) throw new ClassNotFoundException(className + "- not found");
	    Constructor cons = cmClass.getConstructor( null);
	    ModuleAbstract moduleA = (ModuleAbstract) cons.newInstance( null);
	    moduleA.init(moduleInit);
	    moduleA.start();
	    signalizer.registerModule(moduleA);
      }
      log.info("Modules started. Starting ParameterSender.");
      signalizer.start();
      CommonData.parameters.setSignalizer(signalizer);
      initInProgress = false;
      initDone = true;
	}
	
	public void reInit() throws Exception
	{
		if (!initDone || initInProgress) throw new Exception("Init not done or in progress!");
		initDone = false;
		destroy();
		init();
	}
	
	public void destroy() throws Exception
	{
	      for (HashMap moduleInit: CommonData.modulesList)
	      {
	    	try
	    	{
				int moduleId = CommonTools.parseInt(moduleInit.get("ID"), 0);
				String className = (String)moduleInit.get("CLASS");
			    Class cmClass = ModuleAbstract.class.getClassLoader().loadClass(className);
			    if (cmClass == null) throw new ClassNotFoundException(className + "- not found");
			    Constructor cons = cmClass.getConstructor( null);
			    ModuleAbstract moduleA = (ModuleAbstract) cons.newInstance( null);
			    moduleA.shutdown();
	    	}
	    	catch (Exception e)
	    	{
	    		log.error(e.getMessage());
	    	}

	      }
	      log.info("Modules stopped. Closing appenders...");
	      for (HashMap moduleInit: CommonData.modulesList)
	      {
	    	  Logger log = (Logger)moduleInit.get("Logger");
	    	  Enumeration en = log.getAllAppenders();
	    	  if (!en.hasMoreElements())
	    		  en = Logger.getRootLogger().getAllAppenders();
	    	  for (; en.hasMoreElements() ;) 
	    	  {
	    		  try
	    		  {
	  				Appender a = (Appender)en.nextElement();
	  				a.close();
	    		  }
	    		  catch (Exception e) {}
	    	  }
	      }
	      signalizer.exit();
	      /*log.info("Deregistering JDBC drivers...");
	      Enumeration<Driver> drivers = DriverManager.getDrivers();
	      while (drivers.hasMoreElements()) 
	      {
	          Driver driver = drivers.nextElement();
	          try 
	          {
	              DriverManager.deregisterDriver(driver);
	          } 
	          catch (Exception e) {}
	      } // не работает, но ваще как-то надо делать
	      log.info("Killing logger"); // Автоэпитафия.*/
	      BasicConfigurator.resetConfiguration();
	}

}
