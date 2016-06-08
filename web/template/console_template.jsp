<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="org.lobzik.sh.CommonData"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="java.util.*"%>
<%

String moduleJspName = (String)JspData.get("ModuleJspName");
String dinDonKeyCode = CommonData.rootConfigMap.get("DinDonKeyCode");
String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL");
String pauseUrl = moduleUrl + "/pause";

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
<script>
function setFocus()
{
	if(document.getElementById("tofocus") != null)
		document.getElementById("tofocus").focus();
}

function keyDetected(keyCode)
{
	if (keyCode == <%=dinDonKeyCode%>)
		document.getElementById('dindon').submit();	

}


</script>
<body <%=onConsole?"onkeydown=\"keyDetected(event.keyCode)\"":""%> onload="setFocus()" leftmargin="0" rightmargin="0" topmargin="0" bottommargin="0" style="margin:0px;">
<div <%=doPause?"onmousemove=\"moveDetected();\"":""%>>
 <%if (moduleJspName !=null && moduleJspName.length()> 0) 
 {%>  <jsp:include page="<%=moduleJspName %>" /> <%} %>
</div>
<form  id="dindon" action="<%=moduleUrl %>" method="post"><input type="hidden" name="event" value="dindon"></input></form>
</body>
</html>