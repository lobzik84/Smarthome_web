<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<% String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
%>
<form action="<%= moduleUrl%>" method="post">
<table>
<tr><td><input type="submit" name="ap1_command" value="Включить аэратор"></input> </td><td><input type="submit" name="ap0_command" value="Выключить аэратор"></input> </td></tr>
<tr><td><input type="submit" name="cp1_command" value="Включить пульсатрон"></input></td><td><input type="submit" name="сp0_command" value="Выключить пульсатрон"></input></td></tr>
<tr><td><input type="submit" name="oo_command" value="Открыть кран"></input></td><td><input type="submit" name="co_command" value="Закрыть кран"></input></td></tr>
<tr><td><input type="submit" name="wr_command" value="Запланировать промывку"></input></td><td><input type="submit" name="cr_command" value="Отменить промывку"></input></td></tr>
</table>
<input type="submit" name="sync_time" value="Установить время сервера"></input>
</form>
<br />
Хорошо подумайте, прежде чем нажимать то, что ниже!<br />
<form onsubmit="return confirm('Вы уверены?');" action="<%= moduleUrl%>" method="post">
<input type="submit" name="sr_command" value="Начать промывку немедленно"></input>
<input type="submit" name="rc_command" value="Сбросить счётчик"></input>
<input type="submit" name="im_command" value="Инициализировать привод"></input>
</form>	
