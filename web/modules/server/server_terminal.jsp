<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% 
HashMap<String, String> serverState = (HashMap<String, String>)JspData.get("ServerState");
ArrayList<String> serverReplies = (ArrayList<String>)JspData.get("ServerLog");
if (serverReplies == null ) serverReplies = new ArrayList<String>();

for (String reply: serverReplies)
{
	%> <%=reply%> <br /><%
}
%>

<form action="" method="post">
Выполнить команду: <input id="tofocus" type="text" size="40" name="command"></input>
<input type="submit" value="OK"></input>
</form>
