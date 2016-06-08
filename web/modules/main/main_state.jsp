<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="org.lobzik.sh.CommonData"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%
List<HashMap> logLines = (List<HashMap>)JspData.get("LogLines");
%>
<table>
<tbody>
<tr>
<td style="width:430px;">
<h3>Погода:</h3>
<b><%=CommonData.parameters.getFU("WEEWX_OUTTEMP") %> </b> &nbsp; <b><%=CommonData.parameters.getFU("WEEWX_OUTHUMID") %></b> &nbsp; 
<b><%=CommonData.parameters.getF("WEEWX_WIND") %> (<font color="red"><%=CommonData.parameters.getF("WEEWX_WINDGUST")%></font>) </b> <%=CommonData.parameters.getParameter("WEEWX_WIND").getUnit() %>
<b><%=CommonData.parameters.getFU("WEEWX_WINDDIR") %></b> <!--  (<%=CommonData.parameters.get("WEEWX_WINDDIR") %>) --> 
<br><br><br>
<%=CommonData.parameters.getDescription("PARK_TEMP") %> : <b><%=CommonData.parameters.getFU("PARK_TEMP") %></b><br />
<%=CommonData.parameters.getDescription("FILTER_STATE") %> : <b><%=CommonData.parameters.getFU("FILTER_STATE") %> </b><br />
<%=CommonData.parameters.getDescription("SAUNA_POWER") %> : <b><%=CommonData.parameters.getFU("SAUNA_POWER") %> </b><br />
<%=CommonData.parameters.getDescription("SAUNA_TEMP") %> : <b><%=CommonData.parameters.getFU("SAUNA_TEMP") %></b><br />

<%if (!CommonData.onConsole) 
{%>
<br><br>
<h3>Ссылки:</h3>
<a href="http://<%=CommonData.rootConfigMap.get("ZMServerIp") %>/public/" target="_blank">Public</a><br />
<br/>
<a href="http://<%=CommonData.rootConfigMap.get("ZMServerIp") %>/zm/" target="_blank">ZoneMinder Console</a><br />
<a href="http://<%=CommonData.rootConfigMap.get("ZMServerIp") %>/lightsquid/" target="_blank">LightSquid</a><br />
<a href="http://<%=CommonData.rootConfigMap.get("MikrotikIp") %>/graphs/" target="_blank">Routeros Graphs</a><br />
<a href="https://<%=CommonData.rootConfigMap.get("ZMServerIp") %>:8444/manage" target="_blank">UniFi</a><br />
<a href="http://<%=CommonData.rootConfigMap.get("ZMServerIp") %>:8080/promise/" target="_blank">Promise WebPAM</a><br />
<a href="http://<%=CommonData.rootConfigMap.get("ZMServerIp") %>/weewx/" target="_blank">WeeWx</a><br />
<a href="http://192.168.8.1/html/deviceinformation.html" target="_blank">E3372 diags</a><br />
<%} %>
</td>
<td>
Лог:<br/>
<%
SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
for(HashMap line: logLines)
{
	%><%=sdf.format(line.get("DATED"))%>&nbsp;<%=line.get("NAME") %>&nbsp;<%=line.get("LEVEL") %>: &nbsp;<%=line.get("MESSAGE")%><br />
	<%	
}
%>
</td>
</tr>
</tbody>
</table>