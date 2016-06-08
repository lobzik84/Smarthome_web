<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%
HashMap<String, String> state = (HashMap<String, String>)JspData.get("SaunaState");
%>
<br />
<form action="" method="post">
<input type="hidden" name="command" value="pc"></input>
<input type="submit" value="Обновить!"></input>
</form>
<br />
<table border="1px">
<%if (state != null)
	for (String key:state.keySet())
	{%><tr>
<td><%=key %></td><td><%= state.get(key)%></td>
</tr>
<%} %>
</table>


