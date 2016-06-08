<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
%>

<br />
Хорошо подумайте, прежде чем нажимать то, что ниже!<br />
<form onsubmit="return confirm('Вы уверены?');" action="" method="post">
<input type="submit" name="GET_command" value="Перезагрузить Микротик немедленно"></input>
<input type="submit" name="dw4=hi_command" value="Выключить UniFi"></input>
<input type="submit" name="dw4=lo_command" value="Включить UniFi"></input><br />
<br/>
</form>	
