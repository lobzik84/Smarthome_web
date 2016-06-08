package org.lobzik.sh;

import org.lobzik.tools.CommonTools;

public class Formatter 
{
	public static String format(Parameter p)
	{
		return format(p, true);
	}
	
	public static String format(Parameter p, boolean useUnits)
	{
		if (p.get() == null)
			return "N/A";
		if ("formatter".equals(p.getPattern()))
		{
			if (p.getName().equals("WEEWX_WINDDIR"))
			{
				return windCompasRU((int)CommonTools.parseDouble(p.get(), 0));
			}
			else if (p.getName().equals("FILTER_STATE"))
			{
				switch (CommonTools.parseInt(p.get(), -5))
				{
					case -3:
						return "ОТКАЗ КОНЦЕВИКА";
					case -2:
						return "ОТКАЗ ПРИВОДА";	
					case -1:
						return "НЕ ИНИЦИАЛИЗИРОВАН";
					case 0:
						return "ОК";
					case 1:
						return "ожидает промывки";
					case 2:
						return "идёт промывка";
					case 3:
						return "дежурный режим";
					default:
						return "?";
				}
				 
			}
			else if (p.getName().equals("FILTER_POWER"))
			{
				String val = "?";
				if ("1".equals(p.get()))
					val = "ON";
				else if ("0".equals(p.get()))
					val = "OFF"; 
				return val;
			}
		}
		else 
		{
			if (p.getPattern() != null)
			{
				String val = String.format(p.getPattern(), CommonTools.parseDouble(p.get(), 0));
				if (useUnits && p.getUnit() != null)
					return val + " " + p.getUnit();
				else 
					return val;
			}
		}
		if (useUnits && p.getUnit() != null)
			return p.get() + " " + p.getUnit();
		else 
			return p.get();
	}
	
	  public static String windCompasRU(Integer degrees) 
	  {
		  if (degrees == null)
			  return "";
		  String[] dirs = {"С","ССВ","СВ","ВСВ","В","ВЮВ","ЮВ","ЮЮВ","Ю","ЮЮЗ","ЮЗ","ЗЮЗ","З","ЗСЗ","СЗ","ССЗ", "С"};
		  int index = (int)((double)(degrees+11.25) / 22.5);
		  if (index < 0  || index >= dirs.length)
			  return "";
		  else 
			  return dirs[index];
	  }
}
