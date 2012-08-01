package reasoner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.OutcomeSequence;
import model.PreferenceMetaData;
import model.WorkingPreferenceModel;

import util.PerformanceAnalyzer;

/**
 * The abstract class that defines the model checking based preference reasoning tasks 
 *   
 * @author gsanthan
 *
 */
public abstract class PreferenceReasoner {
	/**
	 * The SMV model file
	 */
	public String smvFile;
	/**
	 * Preference variables in the model
	 */
	public String[] variables;
	/**
	 * Consistency of the preference specification (cycle-freeness of the induced preference graph)
	 */
	public boolean consistent;
	/**
	 * List of non-dominated outcomes computed until now
	 */
	public static List<String[]> currentMaximalOutcomes;
	/**
	 * List of specs to be added when computing next-preferred outcomes at levels lower than the most preferred outcomes
	 */
	public static List<String> invariants = new ArrayList<String>();
	/**
	 * Sequence of outcomes in a total order consistent with the induced preference graph 
	 */
	public static OutcomeSequence outcomesInOrder = new OutcomeSequence();
	
	/**
	 * Initializes the reasoner with SMV model, and retrieves and stores the preference variables used
	 * @param smvFile
	 */
	PreferenceReasoner(String smvFile) {
		if(smvFile == null || smvFile.trim().length()==0) {
			throw new RuntimeException("The translated preference specification SMV file name is empty!");
		}
		this.smvFile = smvFile;
		WorkingPreferenceModel.setPrefMetaData(new PreferenceMetaData(smvFile));
		this.variables = WorkingPreferenceModel.getPrefMetaData().getVariables();
		currentMaximalOutcomes = new ArrayList<String[]>();
		invariants = new ArrayList<String>();
		resetGeneratedOutcomes();
	}
	
	/**
	 * Dominance Testing: Does morePreferredOutcome dominate lessPreferredOutcome? Returns true or false.
	 * 
	 * @param morePreferredOutcome
	 * @param lessPreferredOutcome
	 * @return Result of dominance testing: true or false
	 * @throws Exception
	 */
	public abstract boolean dominates(Set<String> morePreferredOutcome,
			Set<String> lessPreferredOutcome) throws Exception;
	
	/**
	 * Consistency Testing: Is the induced preference graph cycle-free? Returns true or false.
	 * 
	 * @return Result of consistency testing: true or false
	 * @throws Exception
	 */
	public abstract boolean isConsistent() throws Exception;
	
	/**
	 * Returns the next preferred outcome in a total ordering consistent with the induced preference graph.
	 * Note: This method has to be called within a session.
	 *    
	 * @return next preferred outcome 
	 * @throws IOException
	 */
	public abstract Set<String> nextPreferred() throws IOException;
	
	/**
	 * Resets the session and makes the reasoner ready for computing the next preferred outcomes in sequence from the top most level.
	 */
	public abstract void resetReasoner();

	/**
	 * Computes the set of outcomes at the top-most level when the ignoredOutcomes are removed from the induced preference graph
	 *   
	 * @param ignoredOutcomes
	 * @return The non-dominated set of elements in the current model when ignoredOutcomes are removed   
	 * @throws IOException
	 */
	public abstract OutcomeSequence computeNextPreferredSetIgnoring(OutcomeSequence ignoredOutcomes) throws IOException;
	
	/**
	 * Computes the set of outcomes currently at the top-most level 
	 * 
	 * @return The set of non-dominated elements in the current model used 
	 * @throws IOException
	 */
	public abstract OutcomeSequence computeCurrentPreferredSet() throws IOException; 
	
	/**
	 * Computes a sequence of set of outcomes at subsequent levels in an optimistic minimal weak order extension consistent with the induced preference graph.
	 *  
	 * @return List of set of outcomes (corresponding to each level in the weak order) 
	 * @throws IOException
	 */
	public abstract List<OutcomeSequence> generateWeakOrder() throws IOException;
	
	/**
	 * Removes specified outcomes in the induced preference graph from the model
	 *  
	 * @param outcomes
	 */
	public abstract void removeOutcomes(OutcomeSequence outcomes);
	
	/**
	 * Returns the number of outcomes generated in session so far
	 * @return Number of outcomes generated in session so far
	 */
	public int getOutcomeCount() {
		return outcomesInOrder.getOutcomeSequence().size();
	}
	
	/**
	 * Resets the session of the reasoner
	 */
	public void resetGeneratedOutcomes() {
		outcomesInOrder.getOutcomeSequence().clear();
	}
	
	/**
	 * Keeps track of all outcomes generated in the session - includes the outcomeSequence as part of the generated set
	 * 
	 * @param outcomeSequence
	 * @return Total number of outcomes generated so far
	 * @throws IOException
	 */
	public static int addOutcomeSequenceToGeneratedSequence(OutcomeSequence outcomeSequence) throws IOException {
		for(Set<String> outcome : outcomeSequence.getOutcomeSequence()) {
			outcomesInOrder.addOutcome(outcome);
//			PerformanceAnalyzer.addLatestPerformanceRecord(outcome);
		}
		return outcomesInOrder.getOutcomeSequence().size();
	}
	
	/**
	 * Keeps track of all outcomes generated in the session - includes the outcome as a part of the generated set 
	 * 
	 * @param outcome
	 * @return Total number of outcomes generated so far
	 * @throws IOException
	 */
	public static int addOutcomeToGeneratedSequence(Set<String> outcome) throws IOException {
		outcomesInOrder.addOutcome(outcome);
		PerformanceAnalyzer.addLatestPerformanceRecord(outcome);
		return outcomesInOrder.getOutcomeSequence().size();
	}
	
	/**
	 * Returns a list of specs used to remove outcomes as needed to simulate the in-session reasoning tasks 
	 * @return A List of invariants as formulas corresponding to negation of the outcomes excluded from the model  
	 */
	public static List<String> getInvariants() {
		return invariants;
	}
}