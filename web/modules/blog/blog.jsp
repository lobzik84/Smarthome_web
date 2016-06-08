<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="java.text.SimpleDateFormat" %>
<% 
ArrayList<HashMap> posts = (ArrayList<HashMap>)JspData.get("Posts");
int pages = CommonTools.parseInt(JspData.get("Pages"), 1);
int pageNo = CommonTools.parseInt(JspData.get("PageNo"), 0);
if (pageNo == 0) pageNo = pages;
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
%> <%=logPage==pageNo?"<b>":""%><a href = "<%=pageUrl%>"><%=logPage%></a> <%=logPage==pageNo?"</b>":""%><%	
} %>
 <br />
 
 <% 
 SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
 for (HashMap post:posts)
 {%>

 <h2><%=post.get("SUBJECT")==null?"(Без темы)":post.get("SUBJECT") %></h2> <i><%=sdf.format((Date)post.get("DATE"))%></i> 
<br/>

 <%=post.get("POST") %><br /> <br />
 <% } %>
 
 <br />
 <br />
<i> Новая запись:</i>
<form action="<%=modeUrl%>" method="post">
Тема: <input type="text" size="110" name="subject" /> <br />
<textarea rows="10" cols="110" name="post"></textarea> <br />
<input type="submit" name="submit" value="Отправить" /> 
</form>