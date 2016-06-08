<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%

String navigationJspName = (String)JspData.get("SMSNavigationJspName");
String smsJspName =  (String)JspData.get("SMSJspName");

%>
<jsp:include page="<%=navigationJspName %>" />
   ********************************************************************************************************************<br />
<jsp:include page="<%=smsJspName %>" />