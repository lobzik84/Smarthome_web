<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%
String returnUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL");

%>
<script type="text/javascript">
var url = '<%=returnUrl%>';

function resumeView()
{
	document.location.replace(url);
}

</script>
<div style="width:1920px; height:1080px" onmousemove = "resumeView();">

</div>