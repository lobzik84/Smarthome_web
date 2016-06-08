<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="org.lobzik.sh.CommonData"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%
List<HashMap> inbox = (ArrayList<HashMap>)JspData.get("Inbox");
String outboxUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/outbox";
%>
<form action="<%=outboxUrl %>" method="post">
<table>
<tr>
<td>
Отправить сообщение:</td><td> <input id="tofocus" type="text" size="70" name="message"></input></td></tr>
<tr><td>
На номер:</td><td> <input id="tofocus" type="text" size="12" name="number"></input></td></tr>
<tr><td colspan="2">
<input type="submit" value="Послать!"></input></td></tr>
</table>
</form>
<br/>
<b>Входящие:</b><br /><br/>
<table border="1px">
<%
SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
for (HashMap h: inbox)
{
	%><tr><td width="100"><%=sdf.format((Date)h.get("DATE"))%> </td> <td  width="120"><b><%=h.get("SENDER") %></b></td> <td  width="600"><%=h.get("MESSAGE") %></td></tr> <%
}
%>
</table>