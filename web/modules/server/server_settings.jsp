<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%
HashMap<String, String> state = (HashMap<String, String>)JspData.get("ServerState");
HashMap configMap = (HashMap)JspData.get("configMap");
int disableUnifiOnPower = CommonTools.parseInt(configMap.get("DisableUnifiOnPower"), 0);
%>
<script type="text/javascript">
function submitForm()
{
	document.getElementById('configForm').submit();	
}
</script>
<br />
<form  id="configForm" action="" method="post">
<input type="checkbox" onchange="submitForm();" name="DisableUnifiOnPower" <%=disableUnifiOnPower==1?"checked":"" %> value="1"/> Выключать Unifi при полной охране 
<input type="hidden" name="config" value="do"/>
</form>
<br />
<form action="" method="post">
<input type="hidden" name="command" value="pc"></input>
<input type="submit" value="Обновить!"></input>
</form><br />
<table border="1px">
<%if (state != null)
	for (String key:state.keySet())
	{%><tr>
<td><%=key %></td><td><%= state.get(key)%></td>
</tr>
<%} %>
</table>


