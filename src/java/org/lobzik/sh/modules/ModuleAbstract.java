package org.lobzik.sh.modules;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lobzik.sh.Parameter;

public abstract class ModuleAbstract implements Module {
	
	public void start() throws Exception	{}
	public void shutdown() throws Exception	{}
	
	public void init(HashMap config) throws Exception {
		// TODO Auto-generated method stub

	}

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//response.getWriter().print("<html>");
		//response.getWriter().print("<body>");

	}
	
	public void signal(Parameter p) throws Exception
	{
		
	}

}
