package test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import util.Constants;
import util.OutputUtil;
import util.PropertiesManager;
import exception.PreferenceReasonerException;

public class ExperimentConfigurator {

	public static void configureExperiment() throws PreferenceReasonerException {
		String[] args = PropertiesManager.getPropertiesAsParameterSet(PropertiesManager.getPropertyObject(Constants.CONFIG_RUNTIME_PROPERTIES));
		
		boolean resultFileSet = false;
		boolean baseFileSet = false;
		boolean modelCheckerSet = false;
		String modelChecker = null;
		for (String string : args) {
			if(string.startsWith("-FOLDER=")) {
				Constants.FOLDER = string.substring("-FOLDER=".length());
				if(!baseFileSet) {
					Constants.BASE_FILE = Constants.FOLDER+Constants.BASE_FILE;
				}
				OutputUtil.println("Folder: "+Constants.FOLDER);
			} else if(string.startsWith("-MODEL_CHECKER=")) {
				modelChecker = string.substring("-MODEL_CHECKER=".length());
				modelCheckerSet = true;
			} else if(string.startsWith("-BASE_FILE=")) {
				Constants.BASE_FILE = Constants.FOLDER+string.substring("-BASE_FILE=".length());
				OutputUtil.println("Base File: "+Constants.BASE_FILE);
			} else if(string.startsWith("-RESULT_FILE=")) {
				Constants.RESULT_FILE = string.substring("-RESULT_FILE=".length());
				OutputUtil.println("Result File: "+Constants.RESULT_FILE);
				resultFileSet = true;
			} else if(string.startsWith("-RESULT_PREFIX=")) {
				Constants.RESULT_PREFIX = string.substring("-RESULT_PREFIX=".length());
				OutputUtil.println("Result Prefix: "+Constants.RESULT_PREFIX);
			} else if(string.startsWith("-SMV_EXEC_COMMAND=")) {
				Constants.SMV_EXEC_COMMAND = string.substring("-SMV_EXEC_COMMAND=".length());
				OutputUtil.println("SMV Command: "+Constants.SMV_EXEC_COMMAND);
			} else if(string.startsWith("-MIN_CPT_SIZE=")) {
				Constants.MIN_CPT_SIZE = Integer.parseInt(string.substring("-MIN_CPT_SIZE=".length()));
				OutputUtil.println("MIN_CPT_SIZE: "+Constants.MIN_CPT_SIZE);
			} else if(string.startsWith("-MAX_CPT_SIZE=")) {
				Constants.MAX_CPT_SIZE = Integer.parseInt(string.substring("-MAX_CPT_SIZE=".length()));
				OutputUtil.println("MAX_CPT_SIZE: "+Constants.MAX_CPT_SIZE);
			} else if(string.startsWith("-CPT_SIZE_INCREMENT=")) {
				Constants.CPT_SIZE_INCREMENT = Integer.parseInt(string.substring("-CPT_SIZE_INCREMENT=".length()));
				OutputUtil.println("CPT_SIZE_INCREMENT: "+Constants.CPT_SIZE_INCREMENT);
			} else if(string.startsWith("-MIN_VAR_SIZE=")) {
				Constants.MIN_VAR_SIZE = Integer.parseInt(string.substring("-MIN_VAR_SIZE=".length()));
				OutputUtil.println("MIN_VAR_SIZE: "+Constants.MIN_VAR_SIZE);
			} else if(string.startsWith("-MAX_VAR_SIZE=")) {
				Constants.MAX_VAR_SIZE = Integer.parseInt(string.substring("-MAX_VAR_SIZE=".length()));
				OutputUtil.println("MAX_VAR_SIZE: "+Constants.MAX_VAR_SIZE);
			} else if(string.startsWith("-VAR_SIZE_INCREMENT=")) {
				Constants.VAR_SIZE_INCREMENT = Integer.parseInt(string.substring("-VAR_SIZE_INCREMENT=".length()));
				OutputUtil.println("VAR_SIZE_INCREMENT: "+Constants.VAR_SIZE_INCREMENT);
			} else if(string.startsWith("-MIN_MAXDEGREE=")) {
				Constants.MIN_MAXDEGREE = Integer.parseInt(string.substring("-MIN_MAXDEGREE=".length()));
				OutputUtil.println("MIN_MAXDEGREE: "+Constants.MIN_MAXDEGREE);
			} else if(string.startsWith("-MAX_MAXDEGREE=")) {
				Constants.MAX_MAXDEGREE = Integer.parseInt(string.substring("-MAX_MAXDEGREE=".length()));
				OutputUtil.println("MAX_MAXDEGREE: "+Constants.MAX_MAXDEGREE);
			} else if(string.startsWith("-MAXDEGREE_SIZE_INCREMENT=")) {
				Constants.MAXDEGREE_SIZE_INCREMENT = Integer.parseInt(string.substring("-MAXDEGREE_SIZE_INCREMENT=".length()));
				OutputUtil.println("MAXDEGREE_SIZE_INCREMENT: "+Constants.MAXDEGREE_SIZE_INCREMENT);
			} else if(string.startsWith("-NUM_PREF_FILES=")) {
				Constants.NUM_PREF_FILES = Integer.parseInt(string.substring("-NUM_PREF_FILES=".length()));
				OutputUtil.println("NUM_PREF_FILES: "+Constants.NUM_PREF_FILES);
			} else if(string.startsWith("-NUM_SPECS=")) {
				Constants.NUM_SPECS = Integer.parseInt(string.substring("-NUM_SPECS=".length()));
				OutputUtil.println("NUM_SPECS: "+Constants.NUM_SPECS);
			} else if(string.startsWith("-MAX_DEGREE=")) {
				Constants.MAX_DEGREE = Integer.parseInt(string.substring("-MAX_DEGREE=".length()));
				OutputUtil.println("MAX_DEGREE: "+Constants.MAX_DEGREE);
			} else if(string.startsWith("-VERIFY_SPEC_INDIVIDUALLY=")) {
				Constants.VERIFY_SPEC_INDIVIDUALLY = Boolean.parseBoolean(string.substring("-VERIFY_SPEC_INDIVIDUALLY=".length()));
				OutputUtil.println("VERIFY_SPEC_INDIVIDUALLY: "+Constants.VERIFY_SPEC_INDIVIDUALLY);
			} else if(string.startsWith("-NUM_OUTCOMES=")) {
				Constants.NUM_OUTCOMES = Integer.parseInt(string.substring("-NUM_OUTCOMES=".length()));
				OutputUtil.println("NUM_OUTCOMES: "+Constants.NUM_OUTCOMES);
			}  else if(string.startsWith("-INTRAVAR_TOTALORDER")) {
				Constants.INTRAVAR_TOTALORDER = true;
				OutputUtil.println("INTRAVAR_TOTALORDER: "+Constants.INTRAVAR_TOTALORDER);
			}     
		}
		
		if(modelCheckerSet) {
			if (modelChecker.equalsIgnoreCase("cadencesmv")) {
				Constants.CURRENT_MODEL_CHECKER = Constants.MODEL_CHECKER.CadenceSMV;
			} else if (modelChecker.equalsIgnoreCase("nusmv")) {
				Constants.CURRENT_MODEL_CHECKER = Constants.MODEL_CHECKER.NuSMV;
			} else {
				OutputUtil.println("Invalid Model Checker");
				System.exit(1);
			}
		}
		if(!resultFileSet) {
			Constants.RESULT_FILE = Constants.BASE_FILE+new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
		}
		File fileTemp = new File(Constants.RESULT_FILE);
		if (fileTemp.exists()){
		    fileTemp.delete();
		}
	}

}
