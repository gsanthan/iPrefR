package test;
import java.util.Set;

import model.OutcomeSequence;
import reasoner.CyclicPreferenceReasoner;
import translate.CINetToSMVTranslator;

/**
 * A Test driver for the entire IPrefR preference reasoner.
 * 
 * @author gsanthan
 *
 */
public class IPrefRCycleTest {
	
	public static void main(String[] args) throws Exception {
		
		String cinetFile = "examples\\cycle-cinet-3.txt";
		CINetToSMVTranslator translator = new CINetToSMVTranslator();
		String smvFile = translator.convertToSMV(cinetFile, 0);
		String smvFileReverse = translator.convertToSMV(cinetFile, 0, false);

		CyclicPreferenceReasoner cpr = new CyclicPreferenceReasoner(smvFileReverse);
//		cpr.generateWeakOrderWithCycles();
		
		OutcomeSequence currentLevel = null;
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		extractAlternatives(currentLevel);
		currentLevel = cpr.nextPreferredWithCycles(); 
		
	}

	private static void extractAlternatives(OutcomeSequence currentLevel) {
		if(currentLevel != null && currentLevel.getOutcomeSequence().size()>0) {
			Set<Set<String>> alternatives = currentLevel.getOutcomeSequence();
			for (Set<String> alternative : alternatives) {
				System.out.println(alternative);
			}
		} else {
			System.out.println("Empty!");
		}
	}
}
