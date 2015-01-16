package reasoner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Outcome;
import model.OutcomeSequence;
import model.PreferenceMetaData;
import model.QueryResult;
import model.WorkingPreferenceModel;
import util.Constants;
import util.OutcomeFormatter;
import util.OutputUtil;
import verify.ModelCheckingDelegate;
import verify.SpecHelper;
import verify.TraceFormatterFactory;
import exception.PreferenceReasonerException;

/**
 * A Preference Reasoner for (T)CP-nets and CI-nets with support for the following reasoning tasks:
 * <ol>
 * 	<li>Consistency: Is the given preference specification consistent (induced preference graph is cycle-free)?</li>
 * 	<li>Dominance Testing: Does one outcome dominate another with respect to the given preference specification?</li>
 * 	<li>Next Preferred: Compute a sequence of outcomes in order such that an outcome that comes later in the sequence does not dominate a preceding outcome.</li>
 * </ol>
 * Note: Dominance Testing and Next Preferred reasoning tasks work only for consistent (T)CP-nets and CI-nets.
 * See "Dominance Testing via Model Checking", Santhanam et al. (AAAI 2010) for more details. 
 * @author gsanthan
 *
 */
public class CyclicPreferenceReasoner extends PreferenceReasoner {
	
	/**
	 * Initializes the SMV file, parses the model and retrieves the namesOfVariables, and makes the reasoner ready for reasoning tasks 
	 * @param smvFile
	 */
	public CyclicPreferenceReasoner(String smvFile) {
		super(smvFile);
	}
	
	public CyclicPreferenceReasoner(String smvFile, String smvFileReverse) {
		super(smvFile, smvFileReverse);
	}
	
	/**
	 * This method is used for AAAI 2014 tutorial demo
	 */
	
	public QueryResult dominates(Outcome betterAssignment, Outcome worseAssignment) throws Exception {
		boolean dominates = false;
		OutcomeSequence proof = null;
		if(betterAssignment.validateOutcome() && worseAssignment.validateOutcome()) {
			//Don't need to compute anything if the outcomes are the same
			if(betterAssignment.equals(worseAssignment)) {
				if(Constants.LOG_OUTPUT) {
					OutputUtil.println("Dominance does not hold as the outcomes are equal");
				}
				return new QueryResult(null, dominates, null, null);
			}
			
			//Make a copy the original SMV file containing the model so that we can append specs for computing dominance 
			PreferenceMetaData pmd = new PreferenceMetaData(smvFile);
			pmd.setWorkingFile(smvFile+"-copy-dominance.smv");
			
			//Append the spec corresponding to the existence of a path from less preferred to more preferred outcome in the induced preference graph
			List<String> appendix = new ArrayList<String>();
			//Append the initial state contraints and the property to be verified to the model
	/*		String initSpec = SpecHelper.getInitOutcomeSpec(worseAssignment.getOutcomeAsSetOfPositiveLiterals());
			appendix.add(initSpec);
	*/		String spec = getDominanceSpec(worseAssignment,betterAssignment);
			appendix.add(spec);
			
			//Verify
			ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "dominates");
			dominates = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
			if(dominates ) {
				if(Constants.LOG_OUTPUT) {
					OutputUtil.println("Dominance holds.");
				}
				if(Constants.OBTAIN_PROOF_OF_DOMINANCE_BY_DEFAULT) {
					//Return the proof of dominance: a path from the less preferred to the more preferred outcome
					appendix.clear();
					//Negate the spec used to test dominance, so that the model checker can provide the proof of dominance
					spec = getNegatedDominanceSpec(worseAssignment,betterAssignment);
		/*			
					//Set the less preferred outcome as the initial state; 
					//the model checker need only search for a path to the more preferred outcome  
					String initSpec = SpecHelper.getInitOutcomeSpec(lessPreferredOutcome);
					
					//Append the initial state contraints and the property to be verified to the model 
					appendix.add(initSpec);*/
					
					appendix.add(spec);
					
					//Verify 
					ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "counterToDominates");
					//Model checker must return false, i.e., property is not verified 
					ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
					//Counter example provided by the model checker corresponds to the proof of dominance in the induced preference graph 
					proof = TraceFormatterFactory.createTraceFormatter().parsePathFromTrace(WorkingPreferenceModel.getPrefMetaData());
					proof.printOutcomeSequence();
					proof.fillInCarriedOverValuations();
					proof.printOutcomeSequence();
					if(proof.getOutcomeSequence().isEmpty()) {
						throw new RuntimeException("Error computing dominance: see " + WorkingPreferenceModel.getPrefMetaData().getWorkingFile());
					}
					if(Constants.LOG_OUTPUT) {
						OutputUtil.println("Proof of dominance: ");
						proof.printOutcomeSequence();
					}
				}
			} else if (!dominates) {
				if(Constants.LOG_OUTPUT) {
					OutputUtil.println("Dominance does not hold");
				}
			}
		}
		QueryResult q = new QueryResult(null,dominates, proof, null);
		
		return q;
	}

	/* (non-Javadoc)
	 * @see translate.PreferenceReasoner#isConsistent()
	 */
	public QueryResult isConsistent() throws Exception {

		//Append the spec corresponding to the existence of a cycle in the model (corresponds to a cycle in the induced preference graph)
		List<String> appendix = new ArrayList<String>();
		String spec = getConsistencySpec();
		appendix.add(spec);
		
		//Verify
		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "consistency");
		boolean consistent = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
		OutcomeSequence proof = null;
		if(!consistent) {
			//Parse and return the cycle 
			proof = TraceFormatterFactory.createTraceFormatter().parsePathFromTrace(WorkingPreferenceModel.getPrefMetaData());

			System.out.print("Cycle found. ");
			proof.fillInCarriedOverValuations();
			proof.printOutcomeSequence();
			OutputUtil.println();
		} else {
			OutputUtil.println("Consistent");
		}
		QueryResult q = new QueryResult(null,consistent, proof, null);
		return q;
	}

	/* (non-Javadoc)
	 * @see translate.PreferenceReasoner#resetReasoner()
	 */
	public void resetReasoner() {
		//Prepare for a new set of reasoning tasks; 
		//particularly forget the previously computed outcomes at the current level and model constraints   
		currentMaximalOutcomes = new ArrayList<String[]>();
		invariants = new ArrayList<String>();
		psi_i_minus_1 = new OutcomeSequence();
		psi_i_minus_2_to_psi_0 = new OutcomeSequence();
	}
	
	public OutcomeSequence findCycleContaining(Set<String> outcome, OutcomeSequence psi_i_minus_1_to_0) throws IOException, PreferenceReasonerException {
		Outcome o = new Outcome(WorkingPreferenceModel.getPrefMetaData().getVariables());
		o.makeOutcome(outcome);
		OutcomeSequence cycle = new OutcomeSequence();
		boolean result = true;
		
		do {
			List<String> appendix = new ArrayList<String>();
			String definitionOutcomesInCycle = getOutcomeSequenceAsDefinition(cycle, "psi_i_TypeII");
			String spec = getFindCycleSpec("psi_i_TypeII");
			List<String> modelRefinement = getRemoveOutcomesSpec(psi_i_minus_1_to_0);
			appendix.addAll(modelRefinement);
			appendix.add(definitionOutcomesInCycle);
			appendix.add(spec);
			
			if(Constants.LOG_VERIFICATION_SPECS) {
				OutputUtil.println("   "+" Def: "+definitionOutcomesInCycle);
				OutputUtil.println("   "+" CTL: "+spec);
			}
			
			//Verify 
			ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "findCycle");
			result = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
			
			if(Constants.LOG_VERIFICATION_SPECS) {
				OutputUtil.println(result);
			}
			
			if(result == false) {
				OutcomeSequence outcomesInCycle = TraceFormatterFactory.createTraceFormatter().parsePathFromTrace(WorkingPreferenceModel.getPrefMetaData());
				OutcomeSequence newOutcomes = outcomesInCycle.getOutcomeSequenceCopy();
				newOutcomes.getOutcomeSequence().removeAll(cycle.getOutcomeSequence());
				cycle.addOutcomeSequence(newOutcomes);
				
				if(Constants.LOG_VERIFICATION_SPECS) {
					for(String[] o1 : newOutcomes.getOutcomeSequenceAsListOfStringArray()) {
						OutputUtil.println("   "+"Added to cycle: "+OutcomeFormatter.formatOutcome(o1));
					}
				}
			}
		} while (result == false);
		return cycle;
	}
	
	private String getOutcomeSequenceAsDefinition(OutcomeSequence outcomes, String definitionName) {
		String formattedOutcomes = new String();
		for (String[] outcome : outcomes.getOutcomeSequenceAsListOfStringArray()) {
			String currentFormattedOutcome = OutcomeFormatter.formatOutcome(outcome);
			if(formattedOutcomes.trim().length()>0) {
				formattedOutcomes += " | ";
			}
			formattedOutcomes += "(" + currentFormattedOutcome + ")";
		}
		String definitionFormattedOutcomes = SpecHelper.getDefinitionSpec(definitionName, formattedOutcomes, "A Type II cycle is being extracted the current level including these outcomes");
		return definitionFormattedOutcomes;
	}
	
	private String getFindCycleSpec(String definitionOutcomesInCycle) {
		//CTL property that specifies that 
		String spec = SpecHelper.getCTLSpec(definitionOutcomesInCycle + " -> (AG ("+definitionOutcomesInCycle+"))", "findCycle","find outcomes not in psi_i that are part of a non-dominated cycle (Type II) at current level containing psi_i (in reversed model) ");
		return spec;
	}
	
	private String getTypeIOutcomeSpec(OutcomeSequence psi_i) {
		String formattedOutcomes = new String();
		for (String[] outcome : psi_i.getOutcomeSequenceAsListOfStringArray()) {
			String currentFormattedOutcome = OutcomeFormatter.formatOutcome(outcome);
			formattedOutcomes = formattedOutcomes + " | (" + currentFormattedOutcome + ")";
		}
		//CTL property that specifies that there is always an improving flip beginning from the current node
		String spec = SpecHelper.getCTLSpec("EF (gch=1)" + formattedOutcomes, "typeIOutcome","Type I element (non-dominated) at this level");
		return spec;
	}
	
	private Set<String> findTypeIOutcome(OutcomeSequence psi_i_minus_1_to_psi_0, OutcomeSequence psi_i_TypeIOutcomes) throws IOException, PreferenceReasonerException {
		
		//Append the spec corresponding to the property that there is no (maximal) outcome 
		//in the current (induced preference graph) model 
		//that has no (improving flip) outgoing transition in which a preference variable is changed  
		List<String> appendix = new ArrayList<String>();
		String spec = getTypeIOutcomeSpec(psi_i_TypeIOutcomes);
		List<String> modelRefinement = getRemoveOutcomesSpec(psi_i_minus_1_to_psi_0);
		appendix.addAll(modelRefinement);
		appendix.add(spec);
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			OutputUtil.println("   "+" CTL: "+spec);
		}
		
		//Verify
		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "nextPreferredTypeIOutcome");
		boolean result = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			OutputUtil.println(result);
		}
		
		Set<String> typeIOutcome;
		if(result == true) {
			//removeOutcomes(currentMaximal);
			//currentMaximalOutcomes.clear();
			//All Type I maximal outcomes at the current level have been computed
			typeIOutcome = null;
		} else {
			//Parse the found next preferred outcome from the model checker's output file
			String[] currentPreferred = TraceFormatterFactory.createTraceFormatter().parseCounterExampleFromTrace(WorkingPreferenceModel.getPrefMetaData(), true);
			
			//Keep track of the maximal outcomes at the current level 
			currentMaximalOutcomes.add(currentPreferred);
			
			//Return the found next preferred outcome at the current level
			typeIOutcome = new HashSet<String>(Arrays.asList(currentPreferred));
		}
		return typeIOutcome;
	}
	
	private String getTypeIIOutcomeCandidateSpec(OutcomeSequence psi_i_minus_1, OutcomeSequence psi_i, OutcomeSequence dropOutcomes) {
		String formattedOutcomes_psi_i_minus_1 = new String();
		for (String[] outcome : psi_i_minus_1.getOutcomeSequenceAsListOfStringArray()) {
			String currentFormattedOutcome = OutcomeFormatter.formatOutcome(outcome);
			if(formattedOutcomes_psi_i_minus_1.length()>0) {
				formattedOutcomes_psi_i_minus_1+=" | ";
			}
			formattedOutcomes_psi_i_minus_1 = formattedOutcomes_psi_i_minus_1 + "(" + currentFormattedOutcome + ")";
		}
		String formattedOutcomes_psi_i = new String();
		if(psi_i != null && psi_i.getOutcomeSequence().size()>0) {
			for (String[] outcome : psi_i.getOutcomeSequenceAsListOfStringArray()) {
				String currentFormattedOutcome = OutcomeFormatter.formatOutcome(outcome);
				if(formattedOutcomes_psi_i.length()>0) {
					formattedOutcomes_psi_i+=" | ";
				}
				formattedOutcomes_psi_i = formattedOutcomes_psi_i + "(" + currentFormattedOutcome + ")";
			}
		}
		String drop = new String();
		if(dropOutcomes != null | dropOutcomes.getOutcomeSequence().size()>0) {
			for (String[] outcome : dropOutcomes.getOutcomeSequenceAsListOfStringArray()) {
				String currentFormattedOutcome = OutcomeFormatter.formatOutcome(outcome);
				if(drop.length()>0) {
					drop+=" | ";
				}
				drop = drop + "(" + currentFormattedOutcome + ")";
			}
		}
		//CTL property that specifies that there is always an improving flip beginning from the current node
		String ctl = "(! EX EX (" + formattedOutcomes_psi_i_minus_1 + ")) | (" + formattedOutcomes_psi_i_minus_1 + ")";
		if(formattedOutcomes_psi_i.length()>0) {
			ctl += " | (" +formattedOutcomes_psi_i + ")";
			ctl += " | ( EF "+formattedOutcomes_psi_i+" )";
		}
		ctl += (drop.length()>0?" | "+drop:"");
		String spec = SpecHelper.getCTLSpec(ctl, "typeIIOutcomeCandidate","Type II outcome candidate at this level");
		return spec;
	}
	
	private String getVerifyCandidateCycleSpec(Set<String> outcome) throws PreferenceReasonerException {
		Outcome o = new Outcome(WorkingPreferenceModel.getPrefMetaData().getVariables());
		o.makeOutcome(outcome);
		OutcomeSequence temp = new OutcomeSequence(o);
		String formattedOutcome = OutcomeFormatter.formatOutcome(temp.getOutcomeSequenceAsListOfStringArray().get(0));
		//CTL property that specifies that there is always an improving flip beginning from the current node
		String spec = SpecHelper.getCTLSpec(formattedOutcome+" -> AG ( ("+SpecHelper.getInitChangeVariablesCondition()+") -> EF (" + formattedOutcome + "))", "verifyCandidateCycle","Verify if outcome is a possible Type II element (non-dominated cycle) at this level");
		return spec;
	}
	
	private Set<String> findTypeIIOutcomeCandidate(OutcomeSequence psi_i, OutcomeSequence psi_i_minus_1, OutcomeSequence psi_i_minus_2_to_psi_0, OutcomeSequence drop) throws IOException, PreferenceReasonerException {
		
		//Append the spec corresponding to the property that there is no (maximal) outcome 
		//in the current (induced preference graph) model 
		//that has no (improving flip) outgoing transition in which a preference variable is changed  
		List<String> appendix = new ArrayList<String>();
		String spec = getTypeIIOutcomeCandidateSpec(psi_i_minus_1, psi_i, drop);
		List<String> modelRefinement = getRemoveOutcomesSpec(psi_i_minus_2_to_psi_0);
		appendix.addAll(modelRefinement);
		appendix.add(spec);
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			OutputUtil.println("   "+" CTL: "+spec);
		}
		
		//Verify
		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "nextPreferredTypeIIOutcome_findCandidate");
		boolean result = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			OutputUtil.println(result);
		}
		
		Set<String> typeIIOutcomeCandidate;
		if(result == true) {
			//removeOutcomes(currentMaximal);
			//currentMaximalOutcomes.clear();
			//All Type I maximal outcomes at the current level have been computed
			typeIIOutcomeCandidate = null;
		} else {
			//Parse the found next preferred outcome from the model checker's output file
			String[] currentPreferred = TraceFormatterFactory.createTraceFormatter().parseCounterExampleFromTrace(WorkingPreferenceModel.getPrefMetaData(), true);
			
			//Keep track of the maximal outcomes at the current level 
			currentMaximalOutcomes.add(currentPreferred);
			
			//Return the found next preferred outcome at the current level
			typeIIOutcomeCandidate = new HashSet<String>(Arrays.asList(currentPreferred));
		}
		return typeIIOutcomeCandidate;
	}
	
	private boolean verifyTypeIIOutcomeCandidateCycle(OutcomeSequence psi_i_minus_1_to_psi_0, Set<String> typeIIOutcomeCandidate) throws IOException, PreferenceReasonerException {
		
		//Append the spec corresponding to the property that there is no (maximal) outcome 
		//in the current (induced preference graph) model 
		//that has no (improving flip) outgoing transition in which a preference variable is changed  
		List<String> appendix = new ArrayList<String>();
		String spec = getVerifyCandidateCycleSpec(typeIIOutcomeCandidate);
		List<String> modelRefinement = getRemoveOutcomesSpec(psi_i_minus_1_to_psi_0);
		appendix.addAll(modelRefinement);
		appendix.add(spec);
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			OutputUtil.println("   "+" CTL: "+spec);
		}
		
		//Verify
		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "nextPreferredTypeIIOutcome_verifyCandidate");
		boolean result = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			OutputUtil.println(result);
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see reasoner.PreferenceReasoner#nextPreferredWithCycles(model.OutcomeSequence, model.OutcomeSequence)
	 */
	public OutcomeSequence nextPreferredWithCycles(OutcomeSequence psi_i_minus_1, OutcomeSequence psi_i_minus_2_to_psi_0) throws IOException, PreferenceReasonerException {
		
		boolean topLevel = false;
		if(psi_i_minus_2_to_psi_0 == null) {
			psi_i_minus_2_to_psi_0 = new OutcomeSequence();
			if(psi_i_minus_1 == null) {
				psi_i_minus_1 = new OutcomeSequence();
			}
		}
		
		//For the top level, there is no previous levels
		if(psi_i_minus_2_to_psi_0.getOutcomeSequence().size()==0 && psi_i_minus_1.getOutcomeSequence().size()==0) {
			topLevel = true;
		}
		
		OutcomeSequence drop = new OutcomeSequence();
		OutcomeSequence psi_i = new OutcomeSequence();
		OutcomeSequence psi_i_TypeIOutcomes = new OutcomeSequence();
		OutcomeSequence psi_i_AllTypeIIOutcomes = new OutcomeSequence();
		Set<OutcomeSequence> psi_i_IndividualTypeIIOutcomes = new HashSet<OutcomeSequence>();
		
		//Compute the set of outcomes at levels 0 to i-1 
		OutcomeSequence psi_i_minus_1_to_psi_0 = psi_i_minus_2_to_psi_0.getOutcomeSequenceCopy();
		psi_i_minus_1_to_psi_0.addOutcomeSequence(psi_i_minus_1);
		
		//Find Type I outcomes first
		if(!topLevel) {
			//For the top level, it is assumed that there are no Type I outcomes
			//TODO: This assumption is valid only for CI-nets. Must change the logic to cater to other languages.
			Set<String> nextTypeIOutcome = null;
			do {
				nextTypeIOutcome = findTypeIOutcome(psi_i_minus_1_to_psi_0, psi_i_TypeIOutcomes);
				if(nextTypeIOutcome != null) {
					psi_i_TypeIOutcomes.addOutcome(nextTypeIOutcome);
				}
			} while (nextTypeIOutcome != null); 
			
			psi_i.addOutcomeSequence(psi_i_TypeIOutcomes);
			drop.addOutcomeSequence(psi_i_TypeIOutcomes);
		}
		
		//Find Type II outcomes next
		Set<String> typeIIOutcomeCandidate = null;
		do {
			if(topLevel) {
				//Assume empty set as a top level element 
				typeIIOutcomeCandidate = new HashSet<String>();
			} else {
				//Find an outcome that has an edge (improving flip) in the induced preference graph to an outcome in the previous level
				//Since all Type I outcomes have already been computed, such an outcome is a candidate Type II outcome for level i, 
				//provided it does not have a longer path (with two or more flips) to an outcome in the previous level (which would place this outcome at a level greater than i)   
				typeIIOutcomeCandidate = findTypeIIOutcomeCandidate(psi_i, psi_i_minus_1, psi_i_minus_2_to_psi_0, drop);
			}
			if(typeIIOutcomeCandidate != null) {
				//Verify if this candidate is indeed a cycle/SCC at level i,
				//by checking if it is a top-cycle / terminal-SCC in the induced preference graph with levels 0 to i-1 removed 
				boolean candidateInCycle = verifyTypeIIOutcomeCandidateCycle(psi_i_minus_1_to_psi_0, typeIIOutcomeCandidate);
				if(candidateInCycle) {
					//Obtain the entire SCC of outcomes: they belong to level i
					OutcomeSequence typeIICycleOutcomes = findCycleContaining(typeIIOutcomeCandidate, psi_i_minus_1_to_psi_0);
					psi_i_IndividualTypeIIOutcomes.add(typeIICycleOutcomes);
					psi_i_AllTypeIIOutcomes.addOutcomeSequence(typeIICycleOutcomes);
					psi_i.addOutcomeSequence(typeIICycleOutcomes);
					//This SCC of outcomes should not be considered as candidates for Type II level i outcomes in future 
					drop.addOutcomeSequence(typeIICycleOutcomes);
				} else {
					//This outcome should not be considered as candidate for Type II level i outcomes in future 
					drop.addOutcome(typeIIOutcomeCandidate);
				}
			}
			if(topLevel) {
				break;
			}
			//Repeat the above until there is no more Type II outcome candidates at level i 
		} while(typeIIOutcomeCandidate != null);
		
		if(psi_i_TypeIOutcomes != null && psi_i_TypeIOutcomes.getOutcomeSequence().size()>0) {
			psi_i_TypeIOutcomes.printEncodedOutcomeSequence(variables);
		}
		for(OutcomeSequence cycle : psi_i_IndividualTypeIIOutcomes) {
			cycle.printEncodedOutcomeSequence(variables);
		}
		OutputUtil.println("---");
		
		return psi_i;
	}

	OutcomeSequence psi_i_minus_1 = new OutcomeSequence();
	OutcomeSequence psi_i_minus_2_to_psi_0 = new OutcomeSequence();
	boolean allLevelsComputed = false;
	public OutcomeSequence nextPreferredWithCycles() throws IOException, PreferenceReasonerException {
		
		if(allLevelsComputed) {
			return null;
		}
		
		if(psi_i_minus_2_to_psi_0 == null) {
			psi_i_minus_2_to_psi_0 = new OutcomeSequence();
			if(psi_i_minus_1 == null) {
				psi_i_minus_1 = new OutcomeSequence();
			}
		}
		
		OutcomeSequence currentLevel = nextPreferredWithCycles(psi_i_minus_1, psi_i_minus_2_to_psi_0);
		if(currentLevel != null && currentLevel.getOutcomeSequence().size()>0) {
			psi_i_minus_2_to_psi_0.addOutcomeSequence(psi_i_minus_1);
			psi_i_minus_1 = currentLevel;
		} else {
			currentLevel = null;
			allLevelsComputed = true;
		}

		return currentLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see reasoner.PreferenceReasoner#generateWeakOrderWithCycles()
	 */
	public List<OutcomeSequence> generateWeakOrderWithCycles() throws IOException, PreferenceReasonerException {
		OutcomeSequence psi_i_minus_1 = new OutcomeSequence();
		OutcomeSequence psi_i_minus_2_to_psi_0 = new OutcomeSequence();
		
		List<OutcomeSequence> psi_i = new ArrayList<OutcomeSequence>();
		OutcomeSequence currentLevel = new OutcomeSequence();
		do {
			currentLevel = nextPreferredWithCycles(psi_i_minus_1, psi_i_minus_2_to_psi_0);
			psi_i_minus_2_to_psi_0.addOutcomeSequence(psi_i_minus_1);
			psi_i_minus_1 = currentLevel;
			if (currentLevel != null && currentLevel.getOutcomeSequence().size()>0) {
				psi_i.add(currentLevel);
			}
			currentLevel.printOutcomeSequence();
		} while(currentLevel != null && currentLevel.getOutcomeSequence().size()>0);
		
		return psi_i;
	}
	
	/* (non-Javadoc)
	 * @see translate.PreferenceReasoner#nextPreferred()
	 */
	public Outcome nextPreferred() throws IOException, PreferenceReasonerException {
		
		//Append the spec corresponding to the property that there is no (maximal) outcome 
		//in the current (induced preference graph) model 
		//that has no (improving flip) outgoing transition in which a preference variable is changed  
		List<String> appendix = new ArrayList<String>();
		String spec = getNextPreferredSpec();
		appendix.addAll(invariants);
		appendix.add(spec);
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			System.out.print("   "+" CTL: "+spec);
		}
		
		//Verify
		ModelCheckingDelegate.verify(WorkingPreferenceModel.getPrefMetaData(), appendix, "nextPreferred");
		boolean result = ModelCheckingDelegate.findVerificationResult(WorkingPreferenceModel.getPrefMetaData());
		
		if(Constants.LOG_VERIFICATION_SPECS) {
			OutputUtil.println(result);
		}
		
		Outcome pref;
		if(result == true) {
			OutcomeSequence currentMaximal = new OutcomeSequence();
			currentMaximal.addOutcomeSequenceAsArray(currentMaximalOutcomes);
			removeOutcomes(currentMaximal);
			currentMaximalOutcomes.clear();
			//All maximal outcomes at the current level have been computed
			pref = null;
		} else {
			//Parse the found next preferred outcome from the model checker's output file
			String[] currentPreferred = TraceFormatterFactory.createTraceFormatter().parseCounterExampleFromTrace(WorkingPreferenceModel.getPrefMetaData(), false);
			
			//Keep track of the maximal outcomes at the current level 
			currentMaximalOutcomes.add(currentPreferred);
			
			//Return the found next preferred outcome at the current level
			pref = new Outcome(new HashSet<String>(Arrays.asList(currentPreferred)));
		}
		return pref;
	}
	
	/*
	 * (non-Javadoc)
	 * @see translate.PreferenceReasoner#generateWeakOrder()
	 */
	public List<OutcomeSequence> generateWeakOrder() throws IOException, PreferenceReasonerException {
		//The to-be-computed weak order as a list of levels - each level has a set of outcome
		List<OutcomeSequence> weakOrder = new ArrayList<OutcomeSequence>();
		resetGeneratedOutcomes();
		OutcomeSequence nextPreferredSet = null;
		do {
			//Sequentially compute the outcomes at subsequent levels
			nextPreferredSet = computeCurrentPreferredSet();
			if(nextPreferredSet != null && nextPreferredSet.getOutcomeSequence().size()>0) {
				weakOrder.add(nextPreferredSet);
			}
			PreferenceReasoner.currentMaximalOutcomes = new ArrayList<String[]>();
			if(getOutcomeCount() >= Constants.NUM_OUTCOMES) {
				return weakOrder;
			}
			//After each level has been computed, remove outcomes just computed from the model so that the next level can be computed
			removeOutcomes(nextPreferredSet);
		} while (nextPreferredSet != null && !nextPreferredSet.getOutcomeSequence().isEmpty() && !(getOutcomeCount() >= Constants.NUM_OUTCOMES));
		return weakOrder;
	}

	/*
	 * (non-Javadoc)
	 * @see translate.PreferenceReasoner#computeCurrentPreferredSet()
	 */
	public OutcomeSequence computeCurrentPreferredSet() throws IOException, PreferenceReasonerException {
		
		Outcome next = null;
		OutcomeSequence visited = new OutcomeSequence();
		boolean computedEnoughOutcomes = false;
		do {
			//Iteratively generate all outcomes at the top-most level (non-dominated set)
			next = nextPreferred();
			
			if(next != null) {
				visited.addOutcome(next);
				
				//Stop if the user has set a maximum number of outcomes to be computed
				Outcome o = new Outcome(WorkingPreferenceModel.getPrefMetaData().getVariables());
				if(addOutcomeSequenceToGeneratedSequence(new OutcomeSequence(o)) >= Constants.NUM_OUTCOMES) {
					computedEnoughOutcomes = true;
				}
			}
		} while(next != null && !computedEnoughOutcomes);
		
		return visited;
	}

	/*
	 * (non-Javadoc)
	 * @see translate.PreferenceReasoner#computeNextPreferredSetIgnoring(test.OutcomeSequence)
	 */
	public OutcomeSequence computeNextPreferredSetIgnoring(OutcomeSequence ignoredOutcomes) throws IOException, PreferenceReasonerException {
		//Remove ignored outcomes from the model
		removeOutcomes(ignoredOutcomes);
		return computeCurrentPreferredSet();
	}

	public List<String> getRemoveOutcomesSpec(OutcomeSequence outcomes) {
		//Remove outcomes from the model by making the negation of each outcome invariant
		List<String> removedOutcomesSpec = new ArrayList<String>();
		for (String[] outcome : outcomes.getOutcomeSequenceAsListOfStringArray()) {
			
			if(Constants.CURRENT_MODEL_CHECKER==Constants.MODEL_CHECKER.CadenceSMV) {
				removedOutcomesSpec.add("INVAR !(" + OutcomeFormatter.formatOutcome(outcome) + ");");
			} else {
				removedOutcomesSpec.add("INVAR !(" + OutcomeFormatter.formatOutcome(outcome) + ")");
			}
		}
		return removedOutcomesSpec;
	}
	
	/*
	 * (non-Javadoc)
	 * @see translate.PreferenceReasoner#removeOutcomes(test.OutcomeSequence)
	 */
	public void removeOutcomes(OutcomeSequence outcomes) {
		//Remove outcomes from the model by making the negation of each outcome invariant 
		for (String[] outcome : outcomes.getOutcomeSequenceAsListOfStringArray()) {
			if(Constants.CURRENT_MODEL_CHECKER==Constants.MODEL_CHECKER.CadenceSMV) {
				invariants.add("INVAR !(" + OutcomeFormatter.formatOutcome(outcome) + ");");
			} else {
				invariants.add("INVAR !(" + OutcomeFormatter.formatOutcome(outcome) + ")");
			}
		}
	}

	/**
	 * Returns a CTL property that specifies that there is always an improving flip beginning from the current node 
	 * @return spec
	 */
	private String getNextPreferredSpec() {
		String formattedOutcomes = new String();
		for (String[] outcome : currentMaximalOutcomes) {
			String currentFormattedOutcome = OutcomeFormatter.formatOutcome(outcome);
			formattedOutcomes = formattedOutcomes + " | (" + currentFormattedOutcome + ")";
		}
		//CTL property that specifies that there is always an improving flip beginning from the current node
		String spec = SpecHelper.getCTLSpec("EF (gch=1)" + formattedOutcomes, "topElement","top element (non-dominated)");
		return spec;
	}

	/**
	 * Returns an LTL property that specifies that there is a cycle in the induced preference graph (sequence of improving flips leading from and to the same outcome)
	 * @return spec
	 */
	private String getConsistencySpec() {
		//LTL property that specifies that there is a cycle in the induced preference graph (sequence of improving flips leading from and to the same outcome)
//		String spec = SpecHelper.getLTLSpec("F G (gch=0)", "consistency", "consistency: in all paths there exists a future state where there is no more improvement/worsening (there is no cycle); counter example: a cyclic path");
		String spec = SpecHelper.getCTLSpec("start -> ! EX (gch=1 & EF start)", "consistency", "consistency: in all paths there exists a future state where there is no more improvement/worsening (there is no cycle); counter example: a cyclic path");
		return spec;
	}
	
	/**
	 * Returns a CTL property specifying that there is a path from outcome1 to outcome 2 (outcome2 is better than outcome1)
	 * @param worse Less preferred outcome
	 * @param better More preferred outcomes
	 * @return
	 */
	private String getDominanceSpec(Outcome worse, Outcome better) {
		String spec = new String();
		String outcome1 = new String();
		String outcome2 = new String();
		String readableOutcome1 = new String();
		String readableOutcome2 = new String();
		for (int j = 0; j < variables.length; j++) {
			String variable = variables[j];
			
			if(outcome1.length()>0) {
				outcome1 = outcome1 + " & ";
				readableOutcome1 = readableOutcome1 + ",";
			}
			
			if(outcome2.length()>0) {
				outcome2 = outcome2 + " & ";
				readableOutcome2 = readableOutcome2 + ",";
			}
			outcome1 = outcome1 + variable + "=" + worse.getValuationOfVariable(variable);
			outcome2 = outcome2 + variable + "=" + better.getValuationOfVariable(variable);
			
		}
		//CTL property specifying that there is a path from outcome1 to outcome 2 (outcome2 is better than outcome1) 	
		spec = SpecHelper.getCTLSpec("("+ outcome1 + " -> EX EF (" + outcome2 + ")) ","dominance","-- "+ " (" + readableOutcome1 + ") -> (" + readableOutcome2 + ")");
		
		return spec;
	}
	
	
	
	
	/**
	 * Returns a CTL property specifying that there is no path from outcome1 to outcome 2 (outcome2 is better than outcome1)
	 * @param worse Less preferred outcome
	 * @param better More preferred outcomes
	 * @return
	 */
	private String getNegatedDominanceSpec(Outcome worse, Outcome better) {
		String spec = new String();
		String outcome1 = new String();
		String outcome2 = new String();
		String readableOutcome1 = new String();
		String readableOutcome2 = new String();
		for (int j = 0; j < variables.length; j++) {
			String variable = variables[j];
			if(outcome1.length()>0) {
				outcome1 = outcome1 + " & ";
				readableOutcome1 = readableOutcome1 + ",";
			}
			
			if(outcome2.length()>0) {
				outcome2 = outcome2 + " & ";
				readableOutcome2 = readableOutcome2 + ",";
			}
			
			outcome1 = outcome1 + variable + "=" + worse.getValuationOfVariable(variable);
			outcome2 = outcome2 + variable + "=" + better.getValuationOfVariable(variable);
			
		}
		//CTL property specifying that there is no path from outcome1 to outcome 2 (outcome2 is better than outcome1) 	
		spec = SpecHelper.getCTLSpec("(("+ outcome1 + " -> !EX EF (" + outcome2 + "))) ","counterExampleForDominanceTest"," (" + readableOutcome1 + ") -> (" + readableOutcome2 + ")");
		
		return spec;
	}
	
	
}
