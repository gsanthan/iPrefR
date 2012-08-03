package verify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import model.PreferenceMetaData;
import model.WorkingPreferenceModel;

import util.Constants;
import util.FileUtil;

/**
 * Verifies SMV model using the given constraints against the given specifications, and parses the verification result
 *  
 * @author gsanthan
 *
 */
public class ModelCheckingDelegate {
	
	/**
	 * Interfaces with the ModelChecker to verify properties on the SMV model encoding the induced preference graph
	 * 
	 * @param prefMetaData 
	 * @param appendix List of constraints and specifications to be appended to the model before verification
	 * @param property Name of the property verified   
	 * @return Name of the output file
	 * @throws IOException
	 */
	public static String verify(PreferenceMetaData prefMetaData, List<String> appendix, String property) throws IOException {
		String smvFile = WorkingPreferenceModel.getPrefMetaData().getSmvFile();
		String workingFile = WorkingPreferenceModel.getPrefMetaData().getWorkingFile();
		
		//Make a copy of the original file before appending the constraints and property specs
		File working = new File(workingFile);
		working.delete();
		working = new File(workingFile);
		FileUtil.copyFile(new File(smvFile), working);
		
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			//Cadence SMV files end with "}" -- so we need to append the constraints and specs before that line 
			BufferedReader r = new BufferedReader(new FileReader(smvFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(workingFile));
			try{
				String nextLine = "";
				while((nextLine = r.readLine()) != null) {
					if(nextLine.trim().equals("}")) {
						//Here is where we need to append the constraints and property specs
						w.newLine();
						for(String a : appendix) {
							w.newLine();
							w.write(a);
						}
						w.newLine();
					}
					w.newLine();
					w.write(nextLine);
				}
			} finally {
				r.close();			
				w.close();	
			}
		} else {
			//For NuSMV files, we can simply append constraints and specs at the end of the file 
			for (String a : appendix) {
				FileUtil.appendLineToFile(workingFile, "");
				FileUtil.appendLineToFile(workingFile, a);
			}
		}

		String outputFile = new String();
		try {
			ModelChecker invoke = new ModelChecker();
			outputFile = invoke.invokeModelChecker(Constants.SMV_EXEC_COMMAND,	workingFile, null);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println();
		}
//		PerformanceAnalyzer.addLatestPerformanceRecord(null);
		return outputFile;
	}
	
	/**
	 * Returns verification result (true/false) by parsing the model checker's trace  
	 * 
	 * @param prefMetaData PreferenceMetaData object that provides the file name for the model checker's trace
	 * @return Verification result
	 * @throws IOException
	 */
	public static boolean findVerificationResult(PreferenceMetaData prefMetaData) throws IOException {
		String outputFile = WorkingPreferenceModel.getPrefMetaData().getOutputFile();
		BufferedReader r = FileUtil.openFileForRead(outputFile);
		try{
			if(r == null) {
				throw new RuntimeException("Output file "+outputFile+" cannot be opened.");
			}
			//Parse the result -- return "true" or "false" as soon as you find one of these in any line of the file  
			String result = "";
			while((result = r.readLine()) != null) {
				if(result.contains("true")) {
					return true;
				} else if(result.contains("false")) {
					return false;
				}
			}
		} finally { 
			r.close();
		}
		//There was no "true" or "false" in any line -- assumed to be an error 
		throw new RuntimeException("Error in parsing output! " + outputFile);
	}
	
}
