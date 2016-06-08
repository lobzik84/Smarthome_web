package org.lobzik.sh;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.lobzik.sh.servlet.Main;

public class CommonData 
{

	public static ArrayList<HashMap> modulesList = new ArrayList<HashMap>();
	public static final String dataSourceName = "jdbc/sh";
	public static final ParametersStorage parameters = ParametersStorage.getInstance();
	public static final HashMap<String,String> rootConfigMap = new HashMap<String,String>();
	public static boolean debug = false;
	public static boolean onConsole = false;
}

