<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% 
HashMap<String, String> saunaState = (HashMap<String, String>)JspData.get("SaunaState");
ArrayList<String> saunaReplies = (ArrayList<String>)JspData.get("SaunaLog");
if (saunaReplies == null ) saunaReplies = new ArrayList<String>();

for (String reply: saunaReplies)
{
	%> <%=reply%> <br /><%
}
%>

<form action="" method="post">
Выполнить команду: <input id="tofocus" type="text" size="40" name="command"></input>
<input type="submit" value="OK"></input>
</form>
