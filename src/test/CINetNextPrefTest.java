package test;

import exception.PreferenceReasonerException;
import reasoner.CyclicPreferenceReasoner;
import translate.CINetToSMVTranslator;
import util.Constants;
import util.Constants.MODEL_CHECKER;

/**
 * Test driver used for tool submission accompanying TACAS 2013 paper 
 * @author gsanthan
 *
 */
public class CINetNextPrefTest {

	public static void main(String[] args) throws Exception {
		
		boolean cinetFileSet = false;
		boolean modelCheckerSet = false;
		boolean modelCheckerCommandSet = false;
		String cinetFile = new String();
		for (String string : args) {
			if(string != null && string.length()>0) {
				if (string.startsWith("-cinetFile=")) {
					cinetFile = string.substring(new String("-cinetFile=").length());
					cinetFileSet = true;
				} else if (string.startsWith("-modelchecker=")) {
					if (string.substring(new String("-modelchecker=").length()).equalsIgnoreCase("cadenceSMV")) {
						Constants.CURRENT_MODEL_CHECKER = MODEL_CHECKER.CadenceSMV;
					} else {
						Constants.CURRENT_MODEL_CHECKER = MODEL_CHECKER.NuSMV;
					}
					modelCheckerSet = true;
				} else if (string.startsWith("-modelcheckercommand=")) {
					Constants.SMV_EXEC_COMMAND = string.substring(new String("-modelcheckercommand=").length()) + " ";
					modelCheckerCommandSet = true;
				}
			}
		}
		
		if(!cinetFileSet) {
			cinetFile = "cinet-tacas2013.txt";
		}
	
		if((modelCheckerSet & !modelCheckerCommandSet) | (!modelCheckerSet & modelCheckerCommandSet) ) {
			System.out.println("Must specify either both or none: modelchecker and modelcheckercommand parameters");
			System.exit(1);
		}
		
		System.out.println("Model Checker: " + Constants.CURRENT_MODEL_CHECKER);
		System.out.println("CI-net file: " + cinetFile);
		
		CINetToSMVTranslator translator = new CINetToSMVTranslator();
		String smvFileReverse = translator.convertToSMV(cinetFile, 0, false);
		
		CyclicPreferenceReasoner cpr = new CyclicPreferenceReasoner(smvFileReverse);
		cpr.generateWeakOrderWithCycles();
	}


}
