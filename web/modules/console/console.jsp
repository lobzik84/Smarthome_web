<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="org.lobzik.sh.CommonData"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="java.util.*"%>
<%
String currentUrl = request.getRequestURI();
String stop = request.getParameter("stop");
String zmServerIp = CommonData.rootConfigMap.get("ZMServerIp");
String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
String errorMessage = (String)JspData.get("ErrorMessage");
HashMap configMap = (HashMap)JspData.get("configMap");
int handleTimer = CommonTools.parseInt(configMap.get("handleTimer"), 0);
int handlePower = CommonTools.parseInt(configMap.get("handlePower"), 0);
int scale = CommonTools.parseInt(request.getParameter("scale"), 100);
float q = scale/100;
String baseUrl = request.getContextPath() + request.getServletPath();
ArrayList<HashMap> modulesList = (ArrayList<HashMap>)request.getAttribute("ModulesList");
int currentModuleId = CommonTools.parseInt(((HashMap)request.getAttribute("ModuleMap")).get("ID"), 0);
%>

<script type="text/javascript">
function submitForm()
{
	document.getElementById('modectForm').submit();	
}
</script>

<table border="0" cellpadding="0" cellspacing="0"  style="width:<%=(int)(640*3*q)%>px; margin:0px;">
<tr>
<td><img id="liveStream5" src="http://<%=zmServerIp %>/cgi-bin/nph-zms?mode=jpeg&monitor=5&scale=<%=scale %>&maxfps=5" alt="Ulica" /><img style="text-align: bottom-left;" align="bottom" id="liveStream3" src="http://<%=zmServerIp %>/cgi-bin/nph-zms?mode=jpeg&monitor=3&scale=<%=scale %>&maxfps=5" alt="Fasad" /><img id="liveStream6" src="http://<%=zmServerIp %>/cgi-bin/nph-zms?mode=jpeg&monitor=7&scale=<%=scale %>&maxfps=5" alt="Sever" /></td>

<td width="<%=(int)((640*3 - 704 - 432 - 432)*q) %>" style="text-align:center;">
<h3>Погода:</h3> <font style="font-size: 22pt;">
<b><%=CommonData.parameters.getFU("WEEWX_OUTTEMP") %> </b> &nbsp; <b><%=CommonData.parameters.getFU("WEEWX_OUTHUMID") %></b> &nbsp; <br>
<b><%=CommonData.parameters.getF("WEEWX_WIND") %> (<font color="red"><%=CommonData.parameters.getF("WEEWX_WINDGUST")%></font>) </b> <%=CommonData.parameters.getParameter("WEEWX_WIND").getUnit() %>
<b><%=CommonData.parameters.getFU("WEEWX_WINDDIR") %></b></font>
<br>
<font style="font-size: 18pt;">
<%for (HashMap module: modulesList)
{
	int moduleId = CommonTools.parseInt(module.get("ID"), 0);
	String moduleName = (String)module.get("NAME");
	String moduleLink = (String)module.get("URL");
	%><br><%=(currentModuleId == moduleId?"<b>":"") %> <a href="<%=baseUrl + "/" + moduleLink%>"><%=moduleName %></a> &nbsp; <%=(currentModuleId == moduleId?"</b>":"") %><%
}
%>
</font>
<br />
<br/>
<form id="modectForm" onsubmit="return confirm('Вы уверены?');" action="<%= moduleUrl%>" method="post">
<input type="submit" name="ModectOn_command" value="Включить запись по событию"></input> <br /><br />
<input type="submit" name="ModectOff_command" value="Выключить запись по событию"></input><br />
<input type="hidden" name="command" value="do"/>
<br/><% 
if (errorMessage != null && errorMessage.length() > 0)
{
	%><font color="red""><%=errorMessage %></font><%
}
else
{
	%> &nbsp;&nbsp;<%=JspData.get("modectStateMessage")%><%
}
%><br />
<input type="checkbox" onchange="submitForm();" name="handleTimer" <%=handleTimer==1?"checked":"" %> value="1"/> Включать запись по таймеру &nbsp; 
<br/>
<input type="checkbox" onchange="submitForm();" name="handlePower" <%=handlePower==1?"checked":"" %> value="1"/> Включать запись при полной охране
</form>

</td>
</tr>
<tr>
<td colspan="2"><img id="liveStream4" src="http://<%=zmServerIp %>/cgi-bin/nph-zms?mode=jpeg&monitor=4&scale=<%=scale %>&maxfps=5" alt="Hozblok" /><img id="liveStream2" src="http://<%=zmServerIp %>/cgi-bin/nph-zms?mode=jpeg&monitor=2&scale=<%=scale %>&maxfps=5" alt="Dvor" /><img id="liveStream1" src="http://<%=zmServerIp %>/cgi-bin/nph-zms?mode=jpeg&monitor=1&scale=<%=scale %>&maxfps=5" alt="Okna" /></td>
</tr>
</table>