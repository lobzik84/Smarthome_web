<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%

String navigationJspName = (String)JspData.get("SaunaNavigationJspName");
String saunaJspName =  (String)JspData.get("SaunaJspName");

%>
<jsp:include page="<%=navigationJspName %>" />
   ********************************************************************************************************************<br />
<jsp:include page="<%=saunaJspName %>" />