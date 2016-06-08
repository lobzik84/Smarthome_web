<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
%>

 &nbsp; <a href="<%=moduleUrl%>state">Сводка</a>  &nbsp; 
 &nbsp; <a href="<%=moduleUrl%>timer">Таймеры</a>  &nbsp; 
 &nbsp; <a href="<%=moduleUrl%>stats">Статистика</a>  &nbsp;  

 <br />
<% %>