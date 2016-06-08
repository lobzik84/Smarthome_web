<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%
List<HashMap> outbox = (ArrayList<HashMap>)JspData.get("Outbox");
%>
<b>Исходящие:</b><br/><br/>
<table border="1px">
<%
SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
for (HashMap h: outbox)
{
	int status = CommonTools.parseInt(h.get("STATUS"), -1);
%><tr>
	<td width="100"><%=sdf.format((Date)h.get("DATE"))%> </td>
	<td  width="120"><b><%=h.get("RECIPIENT") %></b></td> 
	<td  width="600"><%=h.get("MESSAGE") %></td>
	<td width="120"><%=status==1?"Отправлено":status==0?"Ещё не отправлено":status==-1?"Ошибка!":"" %></td>
</tr> <%

}
%>
</table>