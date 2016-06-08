<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.sh.CommonData"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="org.lobzik.tools.db.mysql.DBTools"%>
<%
String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL");
List<HashMap> graphs = (List<HashMap>)JspData.get("Graphs");
%> <h2 style="text-align:center;">За сутки</h2><%
for (HashMap h: graphs)
{ if (CommonTools.parseInt(h.get("DAY"), 0) != 1) continue;
 %> <img src="<%=moduleUrl%>/graph.png?graphId=<%=h.get("ID")%>&period=day"/>  
<%} %>
<h2 style="text-align:center;">За месяц</h2><%
for (HashMap h: graphs)
{ if (CommonTools.parseInt(h.get("MONTH"), 0) != 1) continue;
%> <img src="<%=moduleUrl%>/graph.png?graphId=<%=h.get("ID")%>&period=month"/>  
<%}%>
<h2 style="text-align:center;">За год</h2><%
for (HashMap h: graphs)
{ if (CommonTools.parseInt(h.get("YEAR"), 0) != 1) continue;
%> <img src="<%=moduleUrl%>/graph.png?graphId=<%=h.get("ID")%>&period=year"/>  
<%} %>


