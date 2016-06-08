package org.lobzik.sh;

import java.util.Date;

public class Parameter 
{
	private int id = 0;
	private String name = null;
	private String alias = null;
	private String value = null;
	private String prevValue = null;
	private String description = null;
	private String pattern = null;
	private String unit = null;
	private Date updateTime = null;
	private Object data = null;
	
	public Parameter(int idP, String nameP, String aliasP, String valueP, String descriptionP, String patternP, String unitP)
	{
		id = idP;
		name = nameP;
		alias = aliasP;
		value = valueP;
		description = descriptionP;
		pattern = patternP;
		updateTime = new Date();
		unit = unitP;
	}
	
	public void setData (Object dataP)
	{
		data = dataP;
		updateTime = new Date();
	}
	
	public void set(String valueP)
	{
		prevValue = value;
		value = valueP;
		updateTime = new Date();
	}
	
	public String get()
	{
		return value;
	}
	
	public String getF()
	{
		return Formatter.format(this, false);
	}
	
	public String getFU()
	{
		return Formatter.format(this, true);
	}
	
	public String getPrevious()
	{
		return prevValue;
	}

	public String getName()
	{
		return name;
	}
	
	public String getAlias()
	{
		return alias;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getPattern()
	{
		return pattern;
	}
	
	public String getUnit()
	{
		return unit;
	}
	
	public int getId()
	{
		return id;
	}
	
	public Date getUpdateTime()
	{
		return updateTime;
	}
	
	public void setUpdateTime(Date updateTimeP)
	{
		updateTime = updateTimeP;
	}
	
	public Object getData()
	{
		return data;
	}
}
