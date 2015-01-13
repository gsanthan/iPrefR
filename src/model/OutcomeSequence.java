package model;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import exception.PreferenceReasonerException;
import util.BinaryEncoding;
import util.OutputUtil;

/**
 * Stores an ordered sequence of outcomes using a LinkedHashSet of outcomes (Set of strings).  
 * Note: This implementation assumes that the preference namesOfVariables are binary, i.e., each has a 0/1 valuation.
 * It stores each outcome as a Set of Strings, 
 * such that the names of namesOfVariables with valuation 1 are included in the set, and those with valuation 0 are not included.  
 * This methods needs to be changed if there is a need to represent sets of outcomes when preference namesOfVariables are non-binary. 
 * @author gsanthan
 *
 */
@XStreamAlias("OUTCOME-SEQUENCE")
public class OutcomeSequence {
	
	/**
	 * An ordered sequence of outcomes   
	 */
	@XStreamImplicit(itemFieldName="OUTCOME")
	List<Outcome> outcomeSequence;

	/**
	 * Initialize the OutcomeSequence with no outcomes
	 */
	public OutcomeSequence() {
		outcomeSequence = new ArrayList<Outcome>();
	}

	/**
	 * Initialize the OutcomeSequence with one outcome
	 * @param outcome
	 */
	public OutcomeSequence(Outcome outcome) {
		outcomeSequence = new ArrayList<Outcome>();
		outcomeSequence.add(outcome);
	}
	
	
	/**
	 * Returns the sequence of outcomes stored in this OutcomeSequence as a List of String arrays 
	 * @return Sequence of outcomes stored in this OutcomeSequence object as a List of String arrays 
	 */
	public List<String[]> getOutcomeSequenceAsListOfStringArray() {
		List<String[]> list = new ArrayList<String[]>();
		for(Outcome outcome : outcomeSequence) {
			String[] array = new String[outcome.getOutcomeAsSetOfPositiveLiterals().size()];
			int i=0;
			for(String s : outcome.getOutcomeAsSetOfPositiveLiterals()) {
				array[i] = s;
				i++;
			}
			list.add(array);
		}
		return list;
	}
	
	/**
	 * Creates and returns a new OutcomeSequence object with the same set of outcomes as this 
	 * @return Copy of this OutcomeSequence object
	 * @throws PreferenceReasonerException 
	 */
	public OutcomeSequence getOutcomeSequenceCopy() throws PreferenceReasonerException {
		OutcomeSequence copy = new OutcomeSequence();
		for(Outcome outcome : outcomeSequence) {
			copy.addOutcome(outcome);
		}
		return copy;
	}
	
	public List<Outcome> getOutcomeSequence() {
		return outcomeSequence;
	}
	
	public void setOutcomeSequence(List<Outcome> outcomeSequence) {
		this.outcomeSequence = outcomeSequence;
	}
	
	public void addOutcome(Set<String> outcome) throws PreferenceReasonerException {
		Outcome o = new Outcome(WorkingPreferenceModel.getPrefMetaData().getVariables());
		o.makeOutcome(outcome);
		addOutcome(o);
	}
	
	public void addOutcome(Outcome outcome) throws PreferenceReasonerException {
		if(outcome == null) {
			throw new PreferenceReasonerException("Null outcome cannot be added to outcome sequence");
		}
		outcomeSequence.add(outcome);
	}
	
	public void addOutcomeSequenceAsArray(List<String[]> outcomeSequence) {
		for (String[] o : outcomeSequence) {
			Outcome outcome = new Outcome(WorkingPreferenceModel.getPrefMetaData().getVariables());
			outcome.makeOutcome(o);
			this.outcomeSequence.add(outcome);
		}
	}
	
	public void addOutcomeSequence(List<Set<String>> outcomeSequence) throws PreferenceReasonerException {
		for(Set<String> o : outcomeSequence) {
			Outcome outcome = new Outcome(WorkingPreferenceModel.getPrefMetaData().getVariables());
			outcome.makeOutcome(o);
			this.outcomeSequence.add(outcome);
		}
	}
	
	public void addOutcomeSequence(OutcomeSequence outcomeSequence) {
		this.outcomeSequence.addAll(outcomeSequence.getOutcomeSequence());
	}
	
	public String getOutcomeSequenceAsString() {
		String text = "Sequence: ";
		if(outcomeSequence == null || outcomeSequence.size()==0) {
			text += "Empty!";
		} else {
			boolean first = true;
			String s = new String();
			for (Outcome o : outcomeSequence) {
				s = s + (first?"":" -> ");
				s = s + o.getOutcomeAsSetOfStringAssignments();
				first = false;
			}
			text += s;
		}
		return text;
	}
	
	/**
	 * Prints the set of outcomes in this OutcomeSequence 
	 */
	public void printOutcomeSequence() {
		OutputUtil.println(getOutcomeSequenceAsString());
	}
	
	/**
	 * Given the variable names, this method returns the set of outcomes in this OutcomeSequence in a binary encoding (assuming the preference namesOfVariables are binary)
	 * @param namesOfVariables Names of preference namesOfVariables
	 * @return String containing binary encoded set of outcomes 
	 */
	public String getEncodedOutcomeSequence(String[] variables) {
		boolean first = true;
		String s = new String();
		for (Outcome o : outcomeSequence) {
			s = s + (first?"":" -> ");
			s = s + BinaryEncoding.getBinaryEncoding(variables, o);
			first = false;
		}
		return s;
	}
	
	/**
	 * Given the variable names, this method prints the set of outcomes in this OutcomeSequence in a binary encoding (assuming the preference namesOfVariables are binary)
	 * @param namesOfVariables Names of preference namesOfVariables
	 */
	public void printEncodedOutcomeSequence(String[] variables) {
		if(outcomeSequence == null || outcomeSequence.size()==0) {
			OutputUtil.println("Empty!");
		} else {
			String s = getEncodedOutcomeSequence(variables);
			
			try {
				PrintStream out = new PrintStream(System.out, true, "UTF-8");
				out.println(s);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void fillInCarriedOverValuations() {
		Outcome prev = null;
		
		for(Outcome o : outcomeSequence) {
			if(prev != null) {
				o.copyMissingValuationsFrom(prev);
			}
			prev = o;
		}
	}
	
	/**
	 * Overrides the equals method in java.lang.Object.
	 * Considers a non-null OutcomeSequence objects equal to this object, when it has the same set of outcomes (order does not matter)
	 * @param other Must be another OutcomeSequence object with same set of outcomes in order for the method to return true
	 */
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof OutcomeSequence)) return false;
	    
	    //Compare the set of outcomes contained in this object with the set of outcomes in the other
	    OutcomeSequence otherOutcomeSequence = (OutcomeSequence)other;
	    if(this.getOutcomeSequence().equals(otherOutcomeSequence.getOutcomeSequence())) {
	    	return true;
	    } 
	    return false;
	}
}