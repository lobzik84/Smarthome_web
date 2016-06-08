<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%@ page import="org.lobzik.sh.CommonData"%>
<% HashMap<String, String> saunaState = (HashMap<String, String>)JspData.get("SaunaState");

%>
<form action="" method="post">
<input type="submit" value="Обновить!"></input>
</form>
<br />
<table border="1px">
<tr>
<td>Время сауны </td><td><%= saunaState.get("date")%></td>
</tr>
<tr>
<td>Заданная температура </td><td><%= saunaState.get("temperature setting")%></td>
</tr>
<tr>
<td>Температура в сауне </td><td><%= saunaState.get("measured temperature")%></td>
</tr>
<tr>
<td>Влажность в сауне </td><td><%= saunaState.get("measured humidity")%></td>
</tr>
<tr>
<td>Нагреватель </td><td><%= saunaState.get("heater")%></td>
</tr>
<tr>
<td>Мощность нагревателя (0..255) </td><td><%= saunaState.get("PIM setting")%></td>
</tr>
<tr>
<td>Частота ошибок датчика </td><td><%= saunaState.get("sensor error rate")%></td>
</tr>
<tr>
<td>Режим </td><td><%= saunaState.get("mode")%></td>
</tr>
<tr>
<td>Kp </td><td><%= saunaState.get("Kp")%></td>
</tr>
<tr>
<td>Ki </td><td><%= saunaState.get("Ki")%></td>
</tr>
<tr>
<td>Kd </td><td><%= saunaState.get("Kd")%></td>
</tr>
</table>



