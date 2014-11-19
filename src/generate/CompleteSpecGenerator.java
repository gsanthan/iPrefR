package generate;

import util.OutputUtil;

public class CompleteSpecGenerator {

	public static void main(String[] args) throws Exception {

		CompleteSpecGenerator specGen = new CompleteSpecGenerator();
//		String[] namesOfVariables = {"V0", "V1", "V2", "V3", "V4", "V5"};
//		String[][] domains = {{"0","1"}, {"0","1"}, {"0","1"}, {"0","1"}, {"0","1"}, {"0","1"}};		
		String[] variables = {"V0", "V1", "V2", "V3"};
		String[][] domains = {{"0","1"}, {"0","1"}, {"0","1"}, {"0","1"}};
//		int sampleSize = 5;
//		String[] randomSpecs = specGen.createRandomSpecs(namesOfVariables, domains, sampleSize);
//		OutputUtil.println(randomSpecs);

		String[] binarySequence = specGen.getBinarySequence(variables.length);
		String[] outcomes = specGen.createOutcomes(binarySequence, variables);
		String[] formattedOutcomes = specGen.createReadableOutcomes(binarySequence, variables);
		String[] specs = specGen.createSpecs(outcomes, formattedOutcomes);
		OutputUtil.println(specs);
//		invoke.invokeSMV("I:\\Ganesh\\Research\\Dominance-ModelChecking\\dominance-testing-3vars.smv");
		
//		invoke.formatSMVOutputFile("I:\\Ganesh\\Research\\Dominance-ModelChecking\\A-RI-3vars-Brafman.txt");
//		invoke.formatSMVOutputFile("I:\\Ganesh\\Research\\Dominance-ModelChecking\\A-RI-3vars-Wilson.txt");
	}
	
	public String[] preprocessAndCreateSpecs(String[] variables, String[][] domains) {
		CompleteSpecGenerator specGen = new CompleteSpecGenerator();
		String[] binarySequence = specGen.getBinarySequence(variables.length);
		String[] outcomes = specGen.createOutcomes(binarySequence, variables);
		String[] formattedOutcomes = specGen.createReadableOutcomes(binarySequence, variables);
		String[] specs = specGen.createSpecs(outcomes, formattedOutcomes);
		return specs;
	}
	
	public String[] createReadableOutcomes(String[] binarySequence, String[] variables) {
		String[] outcomes = new String[binarySequence.length];
		
		for (int i = 0; i < binarySequence.length; i++) {
			outcomes[i] = new String("(");
			
			for(int j = 0; j < binarySequence[i].length(); j++) {
				if(binarySequence[i].charAt(j) == '0') {
					outcomes[i] = outcomes[i] + "0";
				} else if (binarySequence[i].charAt(j) == '1') {
					outcomes[i] = outcomes[i] + "1";
				} else {
					OutputUtil.println("Digit should be 0 or 1");
				}
				if(j < binarySequence[i].length() - 1) {
					outcomes[i] = outcomes[i] + ",";
				}
			}
			outcomes[i] = outcomes[i] + ")";
//			OutputUtil.println(outcomes[i]);
		}
		
		return outcomes;
	}
	
	public String[] createOutcomes(String[] binarySequence, String[] variables) {
		String[] outcomes = new String[binarySequence.length];
		
		for (int i = 0; i < binarySequence.length; i++) {
			outcomes[i] = new String();
			
			for(int j = 0; j < binarySequence[i].length(); j++) {
				if(binarySequence[i].charAt(j) == '0') {
					outcomes[i] = outcomes[i] + variables[j] + "=" + "0";
				} else if (binarySequence[i].charAt(j) == '1') {
					outcomes[i] = outcomes[i] + variables[j] + "=" + "1";
				} else {
					OutputUtil.println("Digit should be 0 or 1");
				}
				if(j < binarySequence[i].length() - 1) {
					outcomes[i] = outcomes[i] + " & ";
				}
			}
//			OutputUtil.println(outcomes[i]);
		}
		
		return outcomes;
	}
	
	public String[] getBinarySequence(int numDigits) {
		int maxNumber = (int)Math.pow(2,numDigits);
		String[] binarySequence = new String[maxNumber];
		for(int k = 0; k<maxNumber; k++) {
			byte number = (byte)k;
			binarySequence[k] = new String();
			int temp = maxNumber;
			while( (temp >>= 1) > 0) {
				binarySequence[k] = binarySequence[k] + ((number & temp) != 0 ? "1" : "0");
			}
//			OutputUtil.println(binarySequence[k]);
		}
		return binarySequence;
	}

	public String[] createSpecs(String[] outcomes, String[] readableOutcomes) {
		String[] specs = new String[outcomes.length*outcomes.length];
		int k = 0;
		String space="";
		for (int i = 0; i < outcomes.length; i++) {
			for (int j = 0; j < outcomes.length; j++) {
				space=(k<10?"00":(k<100?"0":""));
//				if(!outcomes[i].equals(outcomes[j])) {
					specs[k] = "SPEC (" + outcomes[i] + " -> EX EF (" + outcomes[j] + "))" + "; -- ("+ space + k + ")" + readableOutcomes[i] + " -> " + readableOutcomes[j];
//					OutputUtil.println(specs[k]);
//				}
				k++;
			}
		}

		return specs;
	}
}
