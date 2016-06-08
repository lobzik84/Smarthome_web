<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<% HashMap<String, String> state = (HashMap<String, String>)JspData.get("FilterState");
state.put("state_desc", "неизвестно");
String stateS = state.get("state");
if (stateS == null) 
	{}
else if (stateS.equals("-3"))
	state.put("state_desc", "ОТКАЗ КОНЦЕВИКА");
else if (stateS.equals("-2"))
	state.put("state_desc", "ОТКАЗ ПРИВОДА");
else if (stateS.equals("-1"))
	state.put("state_desc", "НЕ ИНИЦИАЛИЗИРОВАН");
else if (stateS.equals("0"))
	state.put("state_desc", "ОК");
else if (stateS.equals("1"))
	state.put("state_desc", "ожидает промывки");
else if (stateS.equals("2"))
	state.put("state_desc", "идёт промывка");
else if (stateS.equals("3"))
	state.put("state_desc", "дежурный режим");

state.put("cycle_desc", "неизвестно");
String cycle = state.get("cycle");
if (cycle == null) 
	{}
else if (cycle.equals("0"))
	state.put("cycle_desc", "фильтрация");
else if (cycle.equals("1"))
	state.put("cycle_desc", "обратная промывка");
else if (cycle.equals("2"))
	state.put("cycle_desc", "обработка реагентом");
else if (cycle.equals("3"))
	state.put("cycle_desc", "прямая промывка");
else if (cycle.equals("4"))
	state.put("cycle_desc", "наполнение");

%>
<br />
<form action="" method="post">
<input type="submit" value="Обновить!"></input>
</form>
<br />
<table border="1px">
<tr>
<td>Счётчик:</td><td><%= state.get("volume")%> </td>
</tr>
<tr>
<td>Время фильтра:</td><td><%= state.get("date")%></td>
</tr>
<tr>
<td>С последней промывки:</td><td><%= state.get("counter")%> имп.</td>
</tr>
<tr>
<td>Последняя промывка:</td><td><%= state.get("last recharge")%></td>
</tr>
<tr>
<td>Состояние:</td><td><%= state.get("state_desc")%></td>
</tr>
<tr>
<td>Цикл:</td><td><%= state.get("cycle_desc")%></td>
</tr>
<tr>
<td>Привод:</td><td><%= state.get("motor")%></td>
</tr>
<tr>
<td>Аэратор:</td><td><%= state.get("air pump")%></td>
</tr>
<tr>
<td>Пульсатрон:</td><td><%= state.get("chem pump")%></td>
</tr>
<tr>
<td>Кран подачи:</td><td><%= state.get("out valve")%></td>
</tr>
<tr>
<td>Питание:</td><td><%= state.get("main power")%></td>
</tr>
<tr>
<td>Главный концевик:</td><td><%= state.get("main switch")%></td>
</tr>
<tr>
<td>Вспомогательный концевик:</td><td><%= state.get("aux switch")%></td>
</tr>
</table>



