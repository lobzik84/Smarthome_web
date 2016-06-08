<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.tools.CommonTools"%>
<%
HashMap<String, String> state = (HashMap<String, String>)JspData.get("state");
state.put("mode_desc", "неизвестно");
String mode = state.get("mode");
if (mode == null) 
	{}
else if (mode.equals("0"))
	state.put("mode_desc", "промывка вручную");
else if (mode.equals("1"))
	state.put("mode_desc", "по объёму");
else if (mode.equals("2"))
	state.put("mode_desc", "по таймеру");
else if (mode.equals("3"))
	state.put("mode_desc", "по таймеру или объёму");

String pulse = state.get("onepulse");
if (pulse != null && pulse.indexOf(" ") > 0) 
{
	pulse = pulse.substring(0, pulse.indexOf(" "));
	state.put("pulse_weight", pulse);
}

%>
<br />
<form action="" method="post">
<input type="hidden" name="command" value="ps"></input>
<input type="submit" value="Обновить!"></input>
</form>
<br />
<table border="1px">
<tr>
<td>Режим промывки:</td><td><%= state.get("mode_desc")%> </td>
</tr>
<tr>
<td>Время начала промывки:</td><td><%= state.get("time_h")%> ч <%= state.get("time_m")%> мин</td>
</tr>
<tr>
<td>Ресурс засыпки:</td><td><%= state.get("vol")%> тыс. имп. </td>
</tr>
<tr>
<td>Периодичность промывки:</td><td><%= state.get("days")%> дн. </td>
</tr>
<tr>
<td>Длительность обратной промывки:</td><td><%= state.get("backwash")%> мин </td>
</tr>
<tr>
<td>Длительность обработки реагентом:</td><td><%= state.get("brine")%> мин</td>
</tr>
<tr>
<td>Длительность прямой промывки:</td><td><%= state.get("rinse")%> мин</td>
</tr>
<tr>
<td>Длительность цикла наполнения:</td><td><%= state.get("refill")%> мин</td>
</tr>
<tr>
<td>Пульсатрон при промывке:</td><td><%= state.get("chem_brine")%> </td>
</tr>
<tr>
<td>Длительность импульса пульсатрона:</td><td><%= state.get("pulse")%> мс</td>
</tr>
<tr>
<td>Импульс пульсатрона один раз за </td><td><%= state.get("counts")%> импульсов счётчика</td>
</tr>
<tr>
<td>Цена импульса счётчика:</td><td><%= state.get("pulse_weight")%> л</td>
</tr>
<tr>
<td>Время работы аэратора:</td><td><%= state.get("aero")%> с</td>
</tr>
<tr>
<td>Закрывать кран при промывке:</td><td><%= state.get("close_recharge")%></td>
</tr>
<tr>
<td>Закрывать кран в дежурном режиме:</td><td><%= state.get("close_standby")%></td>
</tr>
<tr>
<td>Закрывать кран при сбое:</td><td><%= state.get("close_on_err")%></td>
</tr>
</table>


