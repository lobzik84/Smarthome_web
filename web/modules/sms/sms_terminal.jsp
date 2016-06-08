<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<form action="" method="post">
<input type="submit" name="RequestSpeed_command" value="Запросить текущую скорость"></input>
</form><br/>
<% 
ArrayList<String> smsReplies = (ArrayList<String>)JspData.get("SMSLog");
if (smsReplies == null ) smsReplies = new ArrayList<String>();

for (String reply: smsReplies)
{
	%> <%=reply%> <br /><%
}
%>
<form action="" method="post">
Выполнить команду: <input id="tofocus" type="text" size="40" name="command"></input>
<input type="submit" value="OK"></input>
</form>
