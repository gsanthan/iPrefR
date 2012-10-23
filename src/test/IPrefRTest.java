package test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import model.OutcomeSequence;
import reasoner.AcyclicPreferenceReasoner;
import reasoner.CyclicPreferenceReasoner;
import reasoner.PreferenceReasoner;
import translate.CINetToSMVTranslator;
import translate.PreferenceInputTranslator;
import translate.PreferenceInputTranslatorFactory;
import translate.PreferenceInputType;
import util.Constants;

/**
 * A Test driver for the entire IPrefR preference reasoner.
 * 
 * @author gsanthan
 *
 */
public class IPrefRTest {
	
	public static void main(String[] args) throws Exception {
		
		String xmlFile = new String();
		String smvFile = new String();
		
		xmlFile = "examples\\nocycle-cpnet.xml";
		smvFile = testReasonerForTranslation(xmlFile, PreferenceInputType.TCPnet);
		testReasonerForConsistency(smvFile);
		testReasonerForDominanceTesting(smvFile);
		testReasonerForNextPreferred(smvFile);
		testReasonerForWeakOrder(smvFile);
		
		xmlFile = "examples\\cycle-cpnet.xml";
		smvFile = testReasonerForTranslation(xmlFile, PreferenceInputType.TCPnet);
		testReasonerForConsistency(smvFile);
		testReasonerForDominanceTesting(smvFile);
		testReasonerForNextPreferred(smvFile);
		testReasonerForWeakOrder(smvFile);
		
		xmlFile = "examples\\nocycle-tcpnet.xml";
		smvFile = testReasonerForTranslation(xmlFile, PreferenceInputType.TCPnet);
		testReasonerForConsistency(smvFile);
		testReasonerForDominanceTesting(smvFile);
		testReasonerForNextPreferred(smvFile);
		testReasonerForWeakOrder(smvFile);
		
		String cinetFile = "examples\\nocycle-cinet.txt";
		smvFile = testReasonerForTranslation(cinetFile, PreferenceInputType.CInet);
		testReasonerForConsistency(smvFile);
		testReasonerForDominanceTesting(smvFile);
		testReasonerForNextPreferred(smvFile);
		testReasonerForWeakOrder(smvFile);
		
		//Cyclic Preference Reasoner
		cinetFile = "examples\\cycle-cinet-3.txt";
		CINetToSMVTranslator translator = new CINetToSMVTranslator();
		smvFile = translator.convertToSMV(cinetFile, 0);
		String smvFileReverse = translator.convertToSMV(cinetFile, 0, false);
		CyclicPreferenceReasoner cpr = new CyclicPreferenceReasoner(smvFileReverse);
		cpr.generateWeakOrderWithCycles();
	}

	private static String testReasonerForTranslation(String xmlFile, PreferenceInputType type) throws Exception {
		System.out.println("Testing Translation...");
		
		PreferenceInputTranslator translator = PreferenceInputTranslatorFactory.createTranslator(type);
		return translator.convertToSMV(xmlFile, 0);
	}
	
	private static void testReasonerForConsistency(String smvFile) throws Exception {
		System.out.println("Testing Consistency...");
		
		PreferenceReasoner p1 = new AcyclicPreferenceReasoner(smvFile);
		p1.isConsistent();
	}
	
	private static void testReasonerForDominanceTesting(String smvFile) throws Exception {
		System.out.println("Testing Dominance...");
		
		PreferenceReasoner p1 = new AcyclicPreferenceReasoner(smvFile);
		p1.isConsistent();
		
		System.out.println(p1.dominates(new HashSet<String>(Arrays.asList(new String[]{})),new HashSet<String>(Arrays.asList(new String[]{}))));
		System.out.println(p1.dominates(new HashSet<String>(Arrays.asList(new String[]{})),new HashSet<String>(Arrays.asList(new String[]{"a"}))));
		System.out.println(p1.dominates(new HashSet<String>(Arrays.asList(new String[]{"a"})),new HashSet<String>(Arrays.asList(new String[]{}))));
		System.out.println(p1.dominates(new HashSet<String>(Arrays.asList(new String[]{"b"})),new HashSet<String>(Arrays.asList(new String[]{}))));
	}
	
	private static void testReasonerForNextPreferred(String smvFile) throws Exception {
		System.out.println("Testing Next Preferred...");
		
		PreferenceReasoner p1 = new AcyclicPreferenceReasoner(smvFile);
		p1.isConsistent();
		
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
		System.out.println(p1.nextPreferred());
	}
	
	private static void testReasonerForWeakOrder(String smvFile) throws Exception {
		System.out.println("Testing Weak Order...");
		
		PreferenceReasoner p1 = new AcyclicPreferenceReasoner(smvFile);
		Constants.NUM_OUTCOMES = 10;
		
		List<OutcomeSequence> weakOrder = p1.generateWeakOrder();
		for (OutcomeSequence level : weakOrder) {
			level.printOutcomeSequence();
		}
	}
}
