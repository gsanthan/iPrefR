package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * Stores all the constants used by the Preference Reasoner application
 * @author gsanthan
 *
 */
public class Constants {

	/**
	 * 	Minimum number of variables to start experiments with
	 */
	public static int MIN_VAR_SIZE = 4;
	/**
	 * Maximum number of variables to start experiments with
	 */
	public static int MAX_VAR_SIZE = 5;
	/**
	 * Step increment interval for the number of variables 
	 */
	public static int VAR_SIZE_INCREMENT = 1;
	
	/**
	 * Minimum number of rows in the conditional preference table 
	 */
	public static int MIN_CPT_SIZE = 5;
	/**
	 * Maximum number of rows in the conditional preference table
	 */
	public static int MAX_CPT_SIZE = 5;
	/**
	 * Step increment interval for the number of rows in the conditional preference table 
	 */
	public static int CPT_SIZE_INCREMENT = 5;

	/**
	 * Number of preference specification samples to be generated for each combination of experimental parameters
	 */
	public static int NUM_PREF_FILES = 10;
	/**
	 * Number of preference (dominance test) specifications that each sample is to be tested with
	 */
	public static int NUM_SPECS = 0;
	
	/**
	 * Starting value of the maximum degree allowed for each node in the conditional dependency graph
	 */
	public static int MIN_MAXDEGREE = 5;
	/**
	 * Maximum value of the maximum degree allowed for each node in the conditional dependency graph
	 */
	public static int MAX_MAXDEGREE = 5;
	/**
	 * Step increment interval for the maximum degree allowed for each node in the conditional dependency graph
	 */
	public static int MAXDEGREE_SIZE_INCREMENT = 5;
	/**
	 * Maximum degree allowed for each node in the conditional dependency graph
	 */
	public static int MAX_DEGREE = 5;
	
	/**
	 * Specifies whether the intra-variable preferences are totally ordered or not
	 */
	public static boolean INTRAVAR_TOTALORDER = true;
	
	/**
	 * Symbol used for representing the preference relation in the preference specification files  
	 */
	public static String PREFERENCE_SYMBOL_IN_XML = ">"; 
	/**
	 * Symbol used for delimiting the various preference relations specifying a strict partial order 
	 */
	public static String PREFERENCE_DELIMITER_IN_XML = ",";
	
	/**
	 * Seed used by Random Generator
	 */
	public static long RANDOM_SEED = 987324L;//118742L; -- for Set 1
	/**
	 * Random object used to generate random numbers
	 */
	public static Random random = new Random(RANDOM_SEED);

	/**
	 * Specifies whether cycles are allowed or not in the preference specifications
	 */
	public static boolean CYCLES_ALLOWED = false;
	/**
	 * Specifies whether each spec generated has to be verified by individual verification runs 
	 */
	public static boolean VERIFY_SPEC_INDIVIDUALLY = false; 
	
	/**
	 * Folder containing the preference specification files
	 */
	public static String FOLDER = "I:\\Ganesh\\Research\\CIKM 2012\\experiments\\tcpnet-cadence\\";
	/**
	 * Base file name format of the the preference specification files
	 */
	public static String BASE_FILE = Constants.FOLDER+"tcpnet--";
	/**
	 * Result file name format 
	 */
	public static String RESULT_FILE = BASE_FILE+new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());

	/**
	 * Model Checkers supported 
	 * @author gsanthan
	 *
	 */
	public enum MODEL_CHECKER {NuSMV, CadenceSMV};
	/**
	 * Model Checker currently used for preference reasoning
	 */
	public static MODEL_CHECKER CURRENT_MODEL_CHECKER;
	
	
	/**
	 * Command line used to invoke the model checker from the preference reasoner; depends on the currently used model checker
	 */

	public static String SMV_EXEC_COMMAND;
//	public static String SMV_EXEC_COMMAND = (CURRENT_MODEL_CHECKER==MODEL_CHECKER.CadenceSMV)?"C:\\Program Files (x86)\\SMV\\bin\\smv.exe  -f ":"NuSMV ";
//	public static String SMV_EXEC_COMMAND = (CURRENT_MODEL_CHECKER==MODEL_CHECKER.CadenceSMV)?"C:\\Program Files\\SMV\\bin\\smv.exe ":"nusmv ";
	
	static {
		String fs = System.getProperty("file.separator");
		
		File propertiesFile = new File("config"+fs+fs+"reasoner.properties");
		FileReader propertiesReader = null;
		try {
			 propertiesReader = new FileReader(propertiesFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Properties properties = new Properties();
		try {
			properties.load(propertiesReader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String modelChecker = properties.getProperty("model_checker_name", null);
		if(modelChecker == null){
			throw new RuntimeException("Unsupported Model Checker");
		} else if (modelChecker.equals("cadenceSMV")) {
			CURRENT_MODEL_CHECKER = MODEL_CHECKER.CadenceSMV;
		} else if(modelChecker.equals("nuSMV")) {
			CURRENT_MODEL_CHECKER = MODEL_CHECKER.NuSMV;
		} else {
			throw new RuntimeException("Unsupported model checker");
		}
		 
		SMV_EXEC_COMMAND = properties.getProperty("model_checker_command", null) + " ";
		if(SMV_EXEC_COMMAND == null || SMV_EXEC_COMMAND.trim().length() == 0){
			throw new RuntimeException("Invalid Model Checker Command");
		}
	}
	
	/**
	 * Total number of outcomes to be computed for the next preferred computation
	 */
	public static int NUM_OUTCOMES = 20;
	
	/**
	 * Specifies whether the properties constructed for verification and their results are to be logged or not 
	 */
	public static boolean LOG_VERIFICATION_SPECS = false;
}
