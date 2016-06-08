package org.lobzik.sh.modules;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lobzik.sh.Parameter;

public interface Module {
	public void start() throws Exception;
	public void init(HashMap config) throws Exception;
	public void signal(Parameter parameter) throws Exception;
	public void service(HttpServletRequest request, HttpServletResponse response) throws Exception;
	public void shutdown() throws Exception;
}
