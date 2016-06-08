<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="org.lobzik.sh.CommonData"%>
<% HashMap<String, String> serverState = (HashMap<String, String>)JspData.get("ServerState");

%>
<form action="" method="post">
<input type="submit" value="Обновить!"></input>
</form>
<br />
<table border="1px">
<tr>
<td>Гараж:&nbsp;</td><td><%for (String key:serverState.keySet())
	if (key.contains("28:ce:94:26:04:00:00:dd"))
	{
		{%>		<%= serverState.get(key)%> °C <% }  
		break;
	}

%></td>
</tr>
<tr>
<td>Шкаф:&nbsp;</td><td><%for (String key:serverState.keySet())
	if (key.contains("28:c9:da:5f:04:00:00:28"))
	{
		{%>		<%= serverState.get(key)%> °C <% }  
		break;
	}

%></td>
</tr>
<tr>
<td>Котёл:&nbsp;</td><td><%for (String key:serverState.keySet())
	if (key.contains("28:41:cc:26:04:00:00:b8"))
	{
		{%>		<%= serverState.get(key)%> °C <% }  
		break;
	}

%></td>
</tr>

</table>



