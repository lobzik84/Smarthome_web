<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="java.text.SimpleDateFormat" %>
<% 
List<HashMap> timers = (List<HashMap>)JspData.get("Timers");
List<HashMap> changed = (List<HashMap>)JspData.get("Changed");
%> <form action="" method="post">
<table>
<thead>
<tr>
	<th>Активен</th><th>Имя</th><th>Дата начала</th><th>Периодичность</th>
	</tr>
</thead>
<tbody>
<%

for (HashMap timer: timers)
{
	Integer id = (Integer)timer.get("ID");
	Integer enabled = (Integer)timer.get("ENABLED");
	Integer per = (Integer)timer.get("PERIOD_UNITS");
	%><tr>
	<td><input type="checkbox" name="<%=id %>_ENABLED" <%=enabled!=null&&enabled==1?"checked":""%> />
	<td><input type="text" name="<%=id %>_NAME" value="<%=timer.get("NAME")%>" /></td>
	<td><input type="text" name="<%=id %>_START_DATE_STR" value="<%=timer.get("START_DATE_STR")%>"/></td>
	<td><input type="text" size="5" name="<%=id %>_PERIOD_U" value="<%=timer.get("PERIOD_U")%>"/> 
		<select name="<%=id %>_PERIOD_UNITS">
			<option <%=per!=null&&per==1?"selected":""%>  value="1">сек</option>
			<option <%=per!=null&&per==60?"selected":""%> value="60">мин</option>
			<option <%=per!=null&&per==3600?"selected":""%> value="3600">час</option>
		</select>  </td>
	
</tr><%
}
%>
</tbody>
</table>
	<input type="submit" name="save" value="Сохранить">
</form>
<!-- 
<%
if (changed != null)
	for (HashMap chgd: changed)
	{
		%><%=chgd%><br><%
	}
%>-->