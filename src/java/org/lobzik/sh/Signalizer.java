package org.lobzik.sh;

import java.util.ArrayList;
import java.util.Iterator;

import org.lobzik.sh.modules.Module;

public class Signalizer extends Thread
{
	private static final ArrayList<Module> modules = new ArrayList<Module>();
	private static final Signalizer instance = new Signalizer();
	private static boolean run = true;
	private ArrayList<Parameter> changed = new ArrayList<Parameter>();
	
	public static Signalizer getInstance()
	{
		instance.setName(instance.getClass().getSimpleName()+"-Thread");
		return instance;
	}
	
	public void run() 
	{
		while(run)
		{
			try 
			{
				synchronized(this) 
				{
					if (changed.size() == 0)
						try{wait();} catch (InterruptedException ie) {}
				}
				for (Parameter p: new ArrayList<Parameter>(changed))
				{
					for (Module m: modules)
					{
						try
						{
							m.signal(p);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					changed.remove(p);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void registerModule(Module m)
	{
		modules.add(m);
	}
	
	public void parameterUpdated(Parameter p)
	{
		changed.add(p);
		synchronized(this)
		{
			notify();
		}
	}

	public void unregisterModule(Module m)
	{
		modules.remove(m);
	}

	
	public void unregisterAllModules()
	{
		modules.clear();
	}
	
	public void exit()
	{
		unregisterAllModules();
		run = false;		
		synchronized (this) 
		{
			notifyAll();
		}
	}
}
