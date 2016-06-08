package org.lobzik.sh.servlet;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lobzik.sh.CommonData;
import org.lobzik.sh.modules.ModuleAbstract;
import org.lobzik.tools.CommonTools;

@SuppressWarnings("rawtypes")
/**
 * Servlet implementation class Main
 */

public class Main extends HttpServlet  
{ 
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Main() {
        super();
    }
    

	public void init(ServletConfig config) throws ServletException
    {
      super.init(config);
	      
	  	try
		{

		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}

    }

    
    @SuppressWarnings("unchecked")
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		if (request.getLocalPort() == 80 && ("localhost".equals(request.getLocalName()) || "127.0.0.1".equals(request.getLocalName())))
			CommonData.onConsole = true; //если открыт на локалхосте и порт 80, то это локальная консоль и ведём себя соответственно
		else 
			CommonData.onConsole = false;
		try
		{

			request.setAttribute("ModulesList", CommonData.modulesList);
			String url = request.getPathInfo();
			if (url == null) url = "";
			if (url.startsWith("/")) url = url.substring(1);
			int moduleId = 0;
			String className = null;
			String templateJspName = null;
			for (int i = CommonData.modulesList.size()-1; i >= 0; i--)
			{
				HashMap module = CommonData.modulesList.get(i);
				moduleId = CommonTools.parseInt(module.get("ID"), 0);
				className = (String)module.get("CLASS");
				templateJspName = (String)module.get("TEMPLATE");
				String moduleUrl = (String)module.get("URL");
				request.setAttribute("ModuleMap", module);
				if (url.startsWith(moduleUrl))
					break;

			}
			
			request.setAttribute("TemplateJspName", templateJspName);
		    Class cmClass = ModuleAbstract.class.getClassLoader().loadClass(className);
		    if (cmClass == null) throw new ClassNotFoundException(className + "- not found");
		    Constructor cons = cmClass.getConstructor(null);
		    ModuleAbstract module = (ModuleAbstract) cons.newInstance(null);
		    module.service(request, response);
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
    }


}
