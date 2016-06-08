package org.lobzik.sh;

import java.util.*;

import org.lobzik.tools.CommonTools;


public class ParametersStorage {

	private final static HashMap<Integer, Parameter> storage = new HashMap<Integer, Parameter>();
	
	private final static ParametersStorage instance = new ParametersStorage();
	
	private static Signalizer signalizer = null;
	
	private ParametersStorage() {}
	
	public static ParametersStorage getInstance()
	{
		return instance;
	}
	
	@SuppressWarnings("rawtypes")
	public void configure(List<HashMap> dbParamsList)
	{
		for (HashMap h:dbParamsList)
		{
			int id = CommonTools.parseInt(h.get("ID"), 0);
			Parameter p = new Parameter(id, (String)h.get("NAME"), (String)h.get("ALIAS"), null, (String)h.get("DESCRIPTION"), (String)h.get("FORMAT_PATTERN"), (String)h.get("UNIT"));
			storage.put(id, p);
		}
	}
	
	public void setSignalizer(Signalizer signalizerP)
	{
		signalizer = signalizerP;
	}
	
	public int resolve(String name)
	{
		for(Integer id: storage.keySet())
		{
			if (storage.get(id).getName().equals(name))
				return id;
		}
		return -1;
	}

	public int resolveAlias(String alias)
	{
		for(Integer id: storage.keySet())
		{
			if (alias.equals(storage.get(id).getAlias()))
				return id;
		}
		return -1;
	}

	public String resolve(int id)
	{
		Parameter p = storage.get(id);
		if (p == null)
			return null;
		else return p.getName();
	}
	
	public void set(int id, String value)
	{
		Parameter p = storage.get(id);
		if (p == null)	return;
		String prevValue = p.get(); 
		p.set(value);
		if (signalizer != null && id > 0 && value.length() > 0 && !value.equals(prevValue))
			signalizer.parameterUpdated(p);
	}
	
	public void setData(int id, Object data)
	{
		Parameter p = storage.get(id);
		if (p == null)	return;
		//String prevValue = p.get(); 
		p.setData(data);
		//if (signalizer != null && id > 0 && value.length() > 0 && !value.equals(prevValue))
			//signalizer.parameterUpdated(p);
	}
	
	public void set(String name, String value)
	{
		set(resolve(name), value);
	}
	
	public String get(int id)
	{
		Parameter p = storage.get(id);
		if (p == null)
			return null;
		else return p.get();
	}
	
	public String get(String name)
	{
		return get(resolve(name));
	}
	
	public String getF(int id)
	{
		Parameter p = storage.get(id);
		if (p == null)
			return null;
		else return p.getF();
	}
	
	public String getF(String name)
	{
		return getF(resolve(name));
	}
	
	public String getFU(int id)
	{
		Parameter p = storage.get(id);
		if (p == null)
			return null;
		else return p.getFU();
	}
	
	public String getFU(String name)
	{
		return getFU(resolve(name));
	}
	
	public Date getUpdateTime(int id)
	{
		Parameter p = storage.get(id);
		if (p == null)
			return null;
		else return p.getUpdateTime();
	}
	
	public Date getUpdateTime(String name)
	{
		return getUpdateTime(resolve(name));
	}
	
	public String getDescription(int id)
	{
		Parameter p = storage.get(id);
		if (p == null)
			return null;
		else return p.getDescription();
	}

	public String getDescription(String name)
	{
		return getDescription(resolve(name));
	}

	public Parameter getParameter(int id)
	{
		return storage.get(id);
	}
	
	public Parameter getParameter(String name)
	{
		return getParameter(resolve(name));
	}
	
	public void sendSignal(String name, String value, Object data)
	{
		Parameter p = new Parameter(0, name, null, value, null, null, null);
		p.setData(data);
		if (signalizer != null)
			signalizer.parameterUpdated(p);
	}
}
