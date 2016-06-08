<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%
ArrayList<HashMap> modulesList = (ArrayList<HashMap>)request.getAttribute("ModulesList");
int currentModuleId = CommonTools.parseInt(((HashMap)request.getAttribute("ModuleMap")).get("ID"), 0);
for (HashMap module: modulesList)
{
	int moduleId = CommonTools.parseInt(module.get("ID"), 0);
	String moduleName = (String)module.get("NAME");
	String moduleUrl = (String)module.get("URL");
	String baseUrl = request.getContextPath() + request.getServletPath();
	%>&nbsp;<%=(currentModuleId == moduleId?"<b>":"") %> <a href="<%=baseUrl + "/" + moduleUrl%>"><%=moduleName %></a> &nbsp; <%=(currentModuleId == moduleId?"</b>":"") %><%
}
%>
