package util;

import java.util.HashSet;
import java.util.Set;

import model.Outcome;

/**
 * Provides methods to perform Binary Encoding and Decoding of an outcomes specified as a Set of variable names included in them 
 * @author gsanthan
 *
 */
public class BinaryEncoding {
	
	/**
	 * Returns a binary (0/1) encoded String corresponding to the namesOfVariables that are true in the given outcome
	 * @param variableNames Names of preference namesOfVariables
	 * @param outcome Outcome to be encoded
	 * @return Constructed Binary Encoding 
	 */
	public static String getBinaryEncoding(String[] variableNames, Outcome outcome) {
		String encoding = "";
		if(outcome == null) {
			for(int i=0; i<variableNames.length;i++) {
				encoding+="-";
			}
		} else {
			for (int i = 0; i < variableNames.length; i++) {
				if(outcome.containsPositiveLiteral(variableNames[i])) {
					encoding += "1";
				} else {
					encoding += "0";
				}
			}
		}
		return encoding;
	}
	
	/**
	 * Returns an outcome (as a set of variable names) corresponding to the given encoded outcome
	 * @param variableNames Names of preference namesOfVariables
	 * @param encoding Binary encoded outcome 
	 * @return Constructed outcome
	 */
	public static Set<String> getOutcome(String[] variableNames, String encoding) {
		Set<String> outcome = new HashSet<String>();
		for (int i = 0; i < encoding.length(); i++) {
			if(encoding.substring(i,i+1).equals("1")) {
				outcome.add(variableNames[i]);
			}
		}
		return outcome;
	}
}
