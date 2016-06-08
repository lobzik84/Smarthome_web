<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% 
HashMap<String, String> state = (HashMap<String, String>)JspData.get("state");
ArrayList<String> filterReplies = (ArrayList<String>)JspData.get("FilterLog");
if (filterReplies == null ) filterReplies = new ArrayList<String>();

for (String reply: filterReplies)
{
	%> <%=reply%> <br /><%
}
%>

<form action="" method="post">
Выполнить команду: <input id="tofocus" type="text" size="40" name="command"></input>
<input type="submit" value="OK"></input>
</form>
