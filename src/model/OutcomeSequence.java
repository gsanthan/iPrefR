package model;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import util.BinaryEncoding;

/**
 * Stores an ordered sequence of outcomes using a LinkedHashSet of outcomes (Set of strings).  
 * Note: This implementation assumes that the preference variables are binary, i.e., each has a 0/1 valuation.
 * It stores each outcome as a Set of Strings, 
 * such that the names of variables with valuation 1 are included in the set, and those with valuation 0 are not included.  
 * This methods needs to be changed if there is a need to represent sets of outcomes when preference variables are non-binary. 
 * @author gsanthan
 *
 */
public class OutcomeSequence {
	
	/**
	 * An ordered sequence of outcomes   
	 */
	Set<Set<String>> outcomeSequence;

	/**
	 * Initialize the OutcomeSequence with no outcomes
	 */
	public OutcomeSequence() {
		outcomeSequence = new LinkedHashSet<Set<String>>();
	}

	/**
	 * Initialize the OutcomeSequence with one outcome
	 * @param outcome
	 */
	public OutcomeSequence(Set<String> outcome) {
		outcomeSequence = new LinkedHashSet<Set<String>>();
		outcomeSequence.add(outcome);
	}
	
	/**
	 * Returns the sequence of outcomes stored in this OutcomeSequence as a List of String arrays 
	 * @return Sequence of outcomes stored in this OutcomeSequence object as a List of String arrays 
	 */
	public List<String[]> getOutcomeSequenceAsListOfStringArray() {
		List<String[]> list = new ArrayList<String[]>();
		for(Set<String> outcome : outcomeSequence) {
			String[] array = new String[outcome.size()];
			int i=0;
			for(String s : outcome) {
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
	 */
	public OutcomeSequence getOutcomeSequenceCopy() {
		OutcomeSequence copy = new OutcomeSequence();
		for(Set<String> outcome : outcomeSequence) {
			copy.addOutcome(outcome);
		}
		return copy;
	}
	
	public Set<Set<String>> getOutcomeSequence() {
		return outcomeSequence;
	}
	
	public void setOutcomeSequence(Set<Set<String>> outcomeSequence) {
		this.outcomeSequence = outcomeSequence;
	}
	
	/**
	 * Decodes the outcomes in the encodedOutcomeSequence parameter and set them as the current set of outcomes (assumes binary variables)
	 * @param variables Names of variables
	 * @param encodedOutcomeSequence Array of binary encoded outcomes
	 */
	public void setOutcomeSequence(String[] variables, String[] encodedOutcomeSequence) {
		this.outcomeSequence = new LinkedHashSet<Set<String>>();
		for (int i = 0; i < encodedOutcomeSequence.length; i++) {
			addOutcome(variables,encodedOutcomeSequence[i]);
		}
	}
	
	public void addOutcome(Set<String> outcome) {
		outcomeSequence.add(outcome);
	}
	
	public void addOutcome(String[] variables, String encodedOutcome) {
		outcomeSequence.add(BinaryEncoding.getOutcome(variables, encodedOutcome));
	}
	
	public void addOutcomeSequenceAsArray(List<String[]> outcomeSequence) {
		for (String[] o : outcomeSequence) {
			this.outcomeSequence.add(new HashSet<String>(Arrays.asList(o)));
		}
	}
	
	public void addOutcomeSequence(List<Set<String>> outcomeSequence) {
		this.outcomeSequence.addAll(outcomeSequence);
	}
	
	public void addOutcomeSequence(OutcomeSequence outcomeSequence) {
		this.outcomeSequence.addAll(outcomeSequence.getOutcomeSequence());
	}
	
	/**
	 * Prints the set of outcomes in this OutcomeSequence 
	 */
	public void printOutcomeSequence() {
		System.out.print("Outcome Sequence: ");
		if(outcomeSequence == null || outcomeSequence.size()==0) {
			System.out.println("Empty!");
		} else {
			boolean first = true;
			String s = new String();
			for (Set<String> o : outcomeSequence) {
				s = s + (first?"":" -> ");
				s = s + o;
				first = false;
			}
			System.out.println(s);
		}
	}
	
	/**
	 * Given the variable names, this method prints the set of outcomes in this OutcomeSequence in a binary encoding (assuming the preference variables are binary)
	 * @param variables Names of preference variables
	 * @return String containing binary encoded set of outcomes 
	 */
	public String getEncodedOutcomeSequence(String[] variables) {
		boolean first = true;
		String s = new String();
		for (Set<String> o : outcomeSequence) {
			s = s + (first?"":" -> ");
			s = s + BinaryEncoding.getBinaryEncoding(variables, o);
			first = false;
		}
		return s;
	}
	
	/**
	 * Given the variable names, this method prints the set of outcomes in this OutcomeSequence in a binary encoding (assuming the preference variables are binary)
	 * @param variables Names of preference variables
	 */
	public void printEncodedOutcomeSequence(String[] variables) {
		if(outcomeSequence == null || outcomeSequence.size()==0) {
			System.out.println("Empty!");
		} else {
			String s = getEncodedOutcomeSequence(variables);
			
			try {
				PrintStream out = new PrintStream(System.out, true, "UTF-8");
				out.println(s);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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