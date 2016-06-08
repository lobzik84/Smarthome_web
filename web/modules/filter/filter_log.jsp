<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% 
HashMap<String, String> state = (HashMap<String, String>)JspData.get("state");
int pages = CommonTools.parseInt(JspData.get("FilterLogPages"), 1);
int pageNo = CommonTools.parseInt(JspData.get("FilterLogPageNo"), 0);
if (pageNo == 0) pageNo = pages;
String grep = request.getParameter("grep");
String level = request.getParameter("level");
String modeUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/log/";
%>
Стр. <%=pageNo%> из <%=pages %><br />
<%
boolean skip = false;
for (int logPage = 1; logPage <= pages; logPage ++)
{
	if ((logPage > 5 && pages - logPage > 5) && (pageNo - logPage > 2 || logPage - pageNo > 2))
	{
		if (!skip) 
		{
			%> ... <%
			skip = true;
		}
		continue;
	}
	skip = false;
	String pageUrl = modeUrl + logPage;
	if (grep != null && grep.length() > 0) pageUrl += "?grep=" + grep;
	if (level != null)
	{
		if (pageUrl.contains("?"))
			pageUrl += "&";
		else pageUrl += "?";
		pageUrl +=  "level=" + level;
	}
%> <%=logPage==pageNo?"<b>":""%><a href = "<%=pageUrl%>"><%=logPage%></a> <%=logPage==pageNo?"</b>":""%><%	
} %>
 <br />
<form action="<%=modeUrl%>" method="get">
grep: <input type="text" size="40" name="grep" value="<%=grep!=null?grep:""%>"></input> 
Log level: <select name="level"> 
<%for (String logLevel: CommonTools.loglevels)
	{%><option value="<%=logLevel%>" <%=logLevel.equals(level)?"selected":""%> ><%=logLevel%></option> <%}%>

 </select>
<input type="submit" value="OK"></input>
</form>
Лог: <br />
<% ArrayList<String> filterReplies = (ArrayList<String>)JspData.get("FilterLog");
if (filterReplies == null ) filterReplies = new ArrayList<String>();

for (String logline: filterReplies)
{
	%> <%=logline%> <br /><%
}
%>
