package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import exception.PreferenceReasonerException;

public class PropertiesManager {
	 
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
		//Load all the configuration options of the tool and the exceptions with corresponding error messages from properties files.
		File propertiesFile = new File(Constants.CONFIG_REASONER_PROPERTIES);
		FileReader propertiesReader = null;
		try {
			 propertiesReader = new FileReader(propertiesFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		preferenceToolProperties = new Properties();
		try {
			preferenceToolProperties.load(propertiesReader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/**
		 * Initialization code for loading the exception codes and their descriptions.
		 */
		propertiesFile = new File(Constants.CONFIG_EXCEPTION_PROPERTIES);
		propertiesReader = null;
		try {
			 propertiesReader = new FileReader(propertiesFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		exceptionProperties = new Properties();
		try {
			exceptionProperties.load(propertiesReader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		propertiesMap = new HashMap<String, Properties>();
		propertiesMap.put(Constants.CONFIG_EXCEPTION_PROPERTIES, exceptionProperties);
		propertiesMap.put(Constants.CONFIG_REASONER_PROPERTIES, preferenceToolProperties);
	}
	
	public static void verifyPropertiesLoaded() throws PreferenceReasonerException {
		for (Properties p : propertiesMap.values()) {
			if(p == null || p.isEmpty()) {
				throw new PreferenceReasonerException("LOAD_PROPERTIES_ERROR");
			}
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
}
