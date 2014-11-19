package verify;

import java.util.Map;
import java.util.Set;

import model.WorkingPreferenceModel;
import util.Constants;

/**
 * Generates SMV model code for initializing a model to the set of states corresponding to an outcome.
 * Formats CTL/LTL specifications for Cadence SMV or NuSMV.
 * @author gsanthan
 *
 */
public class SpecHelper {
	
	/**
	 * Generates SMV model code in assignment style for initializing a model to the set of states corresponding to an outcome. 
	 * @param outcome
	 * @return Code to initialize model to outcome using SMV syntax 
	 */
	public static String getInitOutcomeSpec(Set<String> outcome) {
		String spec = new String();
		String[] variables = WorkingPreferenceModel.getPrefMetaData().getNamesOfVariables();
		for (int j = 0; j < variables.length; j++) {
			String variable = variables[j];
			if(outcome.contains(variable)) {
				spec = spec + " init("+variable+"):=1;";
			} else {
				spec = spec + " init("+variable+"):=0;";
			}
			
			if(outcome.contains("ch"+variable)) {
				spec = spec + " init(ch"+variable+"):=1;";
			} else {
				spec = spec + " init(ch"+variable+"):=0;";
			}
		}
			
		return spec;
	}
	
	public static String getInitOutcomeSpec(Map<String, String> assignment) {
		String spec = new String();
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.NuSMV) {
			for (String variable : assignment.keySet()) {
				spec += " init("+variable+"):="+assignment.get(variable)+"; ";			
			} 
		} else {
			spec = "INIT ";
			int counter = 0;
			for (String variable : assignment.keySet()) {
				if(counter > 0) {
					spec += " & ";
				}
				counter ++;
				spec += variable+" = "+assignment.get(variable);			
			} 
			spec += ";";
		}
			
		return spec;
	}
	
	public static String getInitChangeVariablesCondition() {
		String spec = new String("(");
		String[] variables = WorkingPreferenceModel.getPrefMetaData().getNamesOfVariables();
		for (String variable : variables) {
			if(spec.trim().length()>1) {
				spec = spec + " & ";
			}
			spec = spec + "ch" + variable+"=0";
		}
		spec = spec + ")";
		return spec;
	}
	
	/**
	 * Generates SMV model code in constraint style for initializing a model to the set of states corresponding to an outcome.  
	 * @param outcome
	 * @return Constraint on the SMV model
	 */
	public static String getInitOutcomeSpecConstraintStyle(Set<String> outcome) {
		String spec = new String();
		String[] variables = WorkingPreferenceModel.getPrefMetaData().getNamesOfVariables();
		for (int j = 0; j < variables.length; j++) {
			String variable = variables[j];
			if(spec.trim().length()>0) {
				spec += " & ";
			}
			if(outcome.contains(variable)) {
				spec += variable;
			} else {
				spec += "!" + variable;
			}
			
			if(spec.trim().length()>0) {
				spec += " & ";
			}
			if(outcome.contains("ch"+variable)) {
				spec += "ch"+variable;
			} else {
				spec += "!ch" + variable;
			}
		}
			
		return spec;
	}
	
	/**
	 * Returns the CTL query (labeled with the propertyName) formatted to work with Cadence SMV or NuSMV model checker 
	 * @param ctlSpec
	 * @param propertyName
	 * @param comment
	 * @return CTL specficiation
	 */ 
	public static String getCTLSpec(String ctlSpec, String propertyName, String comment) {
		String spec = "SPEC " + ctlSpec;
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			spec = propertyName + ": SPEC ( " + ctlSpec + " ); ";// + "/* " + comment + " */";
		}
		return spec;
	}
	
	/**
	 * Returns the LTL query (labeled with the propertyName) formatted to work with Cadence SMV or NuSMV model checker
	 * @param ltlSpec
	 * @param propertyName
	 * @param comment
	 * @return LTL specficiation
	 */
	public static String getLTLSpec(String ltlSpec, String propertyName, String comment) {
		String spec = "LTLSPEC " + ltlSpec;
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			spec = propertyName + ": assert ( " + ltlSpec + " ); ";// + "/* " + comment + " */";
		}
		return spec;
	}
	
	public static String getDefinitionSpec(String definitionName, String definition, String comment) {
		String spec = "DEFINE " + definitionName + ":=" + definition + ";";
		return spec;
	}
}
