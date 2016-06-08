<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
%>

 &nbsp; <a href="<%=moduleUrl%>state">Состояние</a>  &nbsp; 
 &nbsp; <a href="<%=moduleUrl%>commands">Команды</a>  &nbsp; 
 &nbsp; <a href="<%=moduleUrl%>terminal">Терминал</a>  &nbsp;  
 &nbsp; <a href="<%=moduleUrl%>settings">Настройки</a>  &nbsp;
 &nbsp; <a href="<%=moduleUrl%>log">Лог</a>  &nbsp;
 <br />
<% 
Boolean isConnected = (Boolean)JspData.get("Connected");
if (isConnected)
{%><font color="green">Соединение установлено!</font> <%}
else {%><font color="red">Соединение отсутствует: <%=(String)JspData.get("ErrorMessage")%></font> <%} %>
<br />
