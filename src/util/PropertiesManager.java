package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import util.Constants.MODEL_CHECKER;

import exception.PreferenceReasonerException;

public class PropertiesManager {
	 
	/**
	 * Properties to store runtime parameters  
	 */
	public static Properties runtimeProperties;
	/**
	 * Properties to store exception codes and their respective error messages 
	 */
	public static Properties exceptionProperties;
	/**
	 * Properties to store configuration options for the tool. 
	 */
	public static Properties preferenceToolProperties;
	/**
	 * Stores the code corresponding to the properties files and the corresponding loaded properties object for easy lookup.  
	 */
	public static Map<String, Properties> propertiesMap;
	
	static {
		init();
	}
	
	public static void verifyPropertiesLoaded() throws PreferenceReasonerException {
		if(propertiesMap == null || propertiesMap.isEmpty()) {
			throw new PreferenceReasonerException("LOAD_PROPERTIES_ERROR");
		}
		for (Properties p : propertiesMap.values()) {
			if(p == null || p.isEmpty()) {
				throw new PreferenceReasonerException("LOAD_PROPERTIES_ERROR");
			}
		}
	}
		
	public static void init() {
		
		PropertiesManager.runtimeProperties = new Properties();
		try {
			InputStream is = PropertiesManager.class.getClass().getResourceAsStream(Constants.CONFIG_RUNTIME_PROPERTIES);
			PropertiesManager.runtimeProperties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		PropertiesManager.exceptionProperties = new Properties();
		try {
			InputStream is = PropertiesManager.class.getResourceAsStream(Constants.CONFIG_EXCEPTION_PROPERTIES);
			PropertiesManager.exceptionProperties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PropertiesManager.preferenceToolProperties = new Properties();
		try {
			InputStream is = PropertiesManager.class.getClass().getResourceAsStream(Constants.CONFIG_REASONER_PROPERTIES);
			PropertiesManager.preferenceToolProperties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PropertiesManager.propertiesMap = new HashMap<String, Properties>();
		PropertiesManager.propertiesMap.put(Constants.CONFIG_EXCEPTION_PROPERTIES, PropertiesManager.exceptionProperties);
		PropertiesManager.propertiesMap.put(Constants.CONFIG_REASONER_PROPERTIES, PropertiesManager.preferenceToolProperties);
		PropertiesManager.propertiesMap.put(Constants.CONFIG_RUNTIME_PROPERTIES, PropertiesManager.runtimeProperties);
		
		
		String modelChecker = null;
		try {
			modelChecker = PropertiesManager.getProperty(Constants.CONFIG_REASONER_PROPERTIES,"model_checker_name", null);
		} catch (PreferenceReasonerException e) {
			e.printStackTrace();
		}
		if(modelChecker == null){
			throw new RuntimeException("Unsupported Model Checker");
		} else if (modelChecker.equals("cadenceSMV")) {
			Constants.CURRENT_MODEL_CHECKER = MODEL_CHECKER.CadenceSMV;
		} else if(modelChecker.equals("nuSMV")) {
			Constants.CURRENT_MODEL_CHECKER = MODEL_CHECKER.NuSMV;
		} else {
			throw new RuntimeException("Unsupported model checker");
		}
		 
		try {
			Constants.SMV_EXEC_COMMAND = PropertiesManager.getProperty(Constants.CONFIG_REASONER_PROPERTIES,"model_checker_command", null) + " ";
		} catch (PreferenceReasonerException e) {
			e.printStackTrace();
		}
		if(Constants.SMV_EXEC_COMMAND == null || Constants.SMV_EXEC_COMMAND.trim().length() == 0){
			throw new RuntimeException("Invalid Model Checker Command");
		}
	}
	

	public static String getProperty(String propertiesFileName, String key)  throws PreferenceReasonerException {
		verifyPropertiesLoaded();
		return (String)propertiesMap.get(propertiesFileName).getProperty(key);
	}
	
	public static String getProperty(String propertiesFileName, String key, String defaultValue) throws PreferenceReasonerException {
		verifyPropertiesLoaded();
		return (String)propertiesMap.get(propertiesFileName).getProperty(key, defaultValue);
	}
	
	public static Properties getPropertyObject(String propertiesFileName) throws PreferenceReasonerException {
		verifyPropertiesLoaded();
		return propertiesMap.get(propertiesFileName);
	}
	
	public static String[] getPropertiesAsParameterSet(Properties p) throws PreferenceReasonerException {
		Iterator<?> i = p.keySet().iterator();
		List<String> params = new ArrayList<String>();
		while (i.hasNext()) {
			String key = (String) i.next();
			String value = (String) p.getProperty(key, null);
			params.add("-"+key+"="+value);
		}
		return params.toArray(new String[]{});
	}
}
