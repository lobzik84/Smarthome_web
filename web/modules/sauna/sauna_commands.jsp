<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<% String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
%>
<br />
<form action="<%= moduleUrl%>" method="post">
<table>
<tr><td><input type="submit" name="pwr1_command" value="Включить сауну"></input> </td><td><input type="submit" name="pwr0_command" value="Выключить сауну"></input> </td></tr>
</table>

</form>
<form action="<%= moduleUrl%>" method="post">
<input type="hidden" name="command" value="sts"></input><input type="text" name="value" /> <input type="submit" name="submit" value="Задать температуру"></input> 
</form>