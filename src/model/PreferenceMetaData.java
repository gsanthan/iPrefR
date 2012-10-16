package model;

import java.io.IOException;
import java.util.Arrays;

import translate.TCPNetToSMVTranslator;
import util.Constants;

/**
 * This class contains essential information that links an SMV model to a preference specification.
 * As soon as a preference reasoner is created, this class loads and 
 * initializes the preference reasoning session with information such as the SMV model file name, and 
 * associated file names such as model checker's trace output, counter example generated, and verification results.
 *  
 * @author gsanthan
 *
 */
public class PreferenceMetaData {
	
	/**
	 * Preference variables used in the SMV model 
	 */
	public String[] variables;
	/**
	 * Name of file containing SMV model specification
	 */
	public String smvFile;
	/**
	 * Name of file that stores the model checker's output trace
	 */
	public String outputFile;
	/**
	 * Name of file that stores the counter example generated by the model checker during verification
	 */
	public String counterExampleFile;
	/**
	 * Name of file that contains the original SMV model and the properties to be verified  
	 */
	public String workingFile;
	/**
	 * Name of file that stores the verification results of the model checker 
	 */
	public String resultFile;
	
	/**
	 * If using this constructor, all the meta data regarding the preference variables, 
	 * file names of the working files that will be used for verification of properties,
	 * storing the model checker trace output, counter example generated by the model checker, and verification results
	 * must be set manually.
	 */
	public PreferenceMetaData() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Loads preference variables used in the preference specification from the SMV model;
	 * sets the file names of the working files that will be used for verification of properties,
	 * storing the model checker trace output, counter example generated by the model checker, and verification results. 
	 * 
	 * @param smvFile
	 */
	public PreferenceMetaData(String smvFile) {
		try {
			//TODO change the following code to read variables using generic translator, not TCP-net specific
			this.variables = TCPNetToSMVTranslator.getVariablesFromSMVModel(smvFile);
			Arrays.sort(variables);
		} catch (IOException e) {
			System.out.println("Error in parsing list of variables from SMV file");
			RuntimeException r = new RuntimeException();
			r.setStackTrace(e.getStackTrace());
			throw r;
		}
		this.smvFile = smvFile;
		this.workingFile = smvFile + "-c.smv";
		this.outputFile = workingFile + "-output.txt";
		updateCounterExampleFile();
		this.resultFile = workingFile + "-c.smv.out";
	}	

	/**
	 * Sets the preference variables used to the parameter variables; and parses the SMV model file to 
	 * set the file names of the working files that will be used for verification of properties,
	 * storing the model checker trace output, counter example generated by the model checker, and verification results. 
	 * 
	 * @param variables
	 * @param smvFile
	 */
	public PreferenceMetaData(String[] variables, String smvFile) {
		this.variables = variables;
		this.smvFile = smvFile;
		this.workingFile = smvFile + "-c.smv";
		this.outputFile = workingFile + "-output.txt";
		updateCounterExampleFile();
		this.resultFile = workingFile + "-c.smv.out";
	}

	public String getCounterExampleFile() {
		return counterExampleFile;
	}
	
	public void setCounterExampleFile(String counterExampleFile) {
		this.counterExampleFile = counterExampleFile;
	}

	public String[] getVariables() {
		return variables;
	}

	public void setVariables(String[] variables) {
		this.variables = variables;
	}

	public String getSmvFile() {
		return smvFile;
	}
	
	/**
	 * Returns the file name alone without the preceding file path
	 * @return File name alone without the preceding file path
	 */
	public String getSmvFileWithoutPath() {
		String fileNameWithoutPath = smvFile.substring(smvFile
				.lastIndexOf(System.getProperty("file.separator")) + 1);
		return fileNameWithoutPath;
	}
	
	public void setSmvFile(String smvFile) {
		this.smvFile = smvFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
		updateCounterExampleFile();
	}

	public String getResultFile() {
		return resultFile;
	}

	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	
	public String getWorkingFile() {
		return workingFile;
	}

	public void setWorkingFile(String workingFile) {
		this.workingFile = workingFile;
	}
	
	/**
	 * Sets the name of the file that stores the counter example generated by the model checker;
	 * switches according to the model checker used. 
	 */
	private void updateCounterExampleFile() {
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			this.counterExampleFile = workingFile.substring(0,workingFile.lastIndexOf(".smv")) + ".out";
		} else {
			this.counterExampleFile = this.outputFile;
		}
	}
}
