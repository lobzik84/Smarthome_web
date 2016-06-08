<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="org.lobzik.sh.CommonData"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%

String moduleJspName = (String)JspData.get("ModuleJspName");
String pauseUrl = request.getContextPath() + request.getServletPath() + "/console/pause";
boolean paused = request.getRequestURI().startsWith(pauseUrl);
boolean onConsole = CommonData.onConsole;
boolean doPause = onConsole && !paused;
if (doPause)
{
	%>
<script type="text/javascript">
var timeCnt = 0;
var t = setTimeout(function(){tickTack(); }, 1000);
var consoleTimeout = <%=CommonTools.parseInt(CommonData.rootConfigMap.get("ConsolePauseTimeout"), 30)%>;

function moveDetected()
{
	timeCnt = 0;
}

function tickTack()
{
	timeCnt++;
	t = setTimeout(function(){tickTack(); }, 1000);
	if (timeCnt > consoleTimeout)
	{
		document.location.replace('<%=pauseUrl %>');
	}
}

</script>
 
	<%
}
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Smarthome</title>
</head>
<style>
img
{
    vertical-align: text-top;
}
</style>
<script>
function setFocus()
{
	if(document.getElementById("tofocus") != null)
		document.getElementById("tofocus").focus();
}
</script>
<body onload="setFocus()">
<div <%=doPause?"onmousemove=\"moveDetected();\"":""%>>
   <jsp:include page="module_navigation.jsp" /><br />
   ********************************************************************************************************************<br />
 <%if (moduleJspName !=null && moduleJspName.length()> 0) 
 {%>  <jsp:include page="<%=moduleJspName %>" /> <%} %>

</div>
</body>
</html>