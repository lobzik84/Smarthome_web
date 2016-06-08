package org.lobzik.sh.servlet;


import javax.servlet.*;

import org.lobzik.sh.Initializer;

public class Init implements ServletContextListener 
{
	Initializer initializer = Initializer.getInstance();
    public void contextInitialized(ServletContextEvent event) 
    {
	  	try
		{
	  		initializer.init();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    public void contextDestroyed(ServletContextEvent event) 
    {
    	try 
    	{
			initializer.destroy();
		} 
    	catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
}
