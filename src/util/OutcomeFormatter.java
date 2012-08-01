package util;

import java.util.Arrays;
import java.util.List;

import model.WorkingPreferenceModel;

/**
 * Formats outcomes into assignments that can be used to build the guard transitions and create verification specs for model checking
 * @author gsanthan
 *
 */
public class OutcomeFormatter {

	/**
	 * Returns a String that contains the assignment of preference variables to valuations corresponding to the input outcome.
	 * Note: The returned String is formatted as a conjunctive boolean formula assuming binary domain for the variables.
	 *    
	 * @param outcome An array names of preference variable that have valuation 1 in the outcome
	 * @return Formatted outcome string
	 */
	public static String formatOutcome(String[] outcome) {
		String formattedOutcome = new String();
		List<String> outcomeAsList = Arrays.asList(outcome);
		for (String var : WorkingPreferenceModel.getPrefMetaData().getVariables()) {
			if(formattedOutcome.trim().length()>0) {
				formattedOutcome = formattedOutcome + " & ";
			}
			if(outcomeAsList.contains(var)) {
				formattedOutcome = formattedOutcome + var + "=1"; 
			} else {
				formattedOutcome = formattedOutcome + var + "=0";
			}
			
		}
		return formattedOutcome;
	}
	
	/**
	 * Returns a String that contains the assignment of preference and change variables to valuations corresponding to the input outcome.
	 * Note: The returned String is formatted as a conjunctive boolean formula assuming binary domain for the variables.
	 *    
	 * @param outcome An array names of preference variables and change variables that have valuation 1 in the outcome
	 * @return Formatted outcome with change variables' valuations
	 */
	public static String formatOutcomeWithChangeVariables(String[] outcome) {
		String formattedOutcome = new String();
		List<String> outcomeAsList = Arrays.asList(outcome);
		for (String var : WorkingPreferenceModel.getPrefMetaData().getVariables()) {
			if(formattedOutcome.trim().length()>0) {
				formattedOutcome = formattedOutcome + " & ";
			}
			if(outcomeAsList.contains(var)) {
				formattedOutcome = formattedOutcome + var + "=1"; 
			} else {
				formattedOutcome = formattedOutcome + var + "=0";
			}
			
			if(formattedOutcome.trim().length()>0) {
				formattedOutcome = formattedOutcome + " & ";
			}
			
			if(outcomeAsList.contains("ch"+var)) {
				formattedOutcome = formattedOutcome + "ch"+var + "=1"; 
			} else {
				formattedOutcome = formattedOutcome + "ch"+var + "=0";
			}
		}
		return formattedOutcome;
	}
}
