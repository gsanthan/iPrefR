package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import model.Outcome;
import model.PreferenceQuery;
import model.PreferenceQuery.QueryType;
import model.PreferenceSpecification;
import model.Query;
import model.QueryResult;

import org.xml.sax.SAXException;

import reasoner.CyclicPreferenceReasoner;
import reasoner.PreferenceQueryParser;
import reasoner.PreferenceReasoner;
import translate.CPTheoryToSMVTranslator;
import util.Constants;
import util.Constants.MODEL_CHECKER;
import util.ExceptionUtil;
import util.FileUtil;
import util.OutputUtil;
import exception.PreferenceReasonerException;
import generate.SpecGenerator;

public class CPTheoryDominanceExperimentDriver {

	
	public enum REASONING_TASK {DOMINANCE,CONSISTENCY,ORDERING};
	
	public static void main(String[] args) throws Exception {

		CPTheoryDominanceExperimentDriver d = new CPTheoryDominanceExperimentDriver();
		d.serveInitialMenu();
	}
	
	public String readFromConsole(String text) {
		System.out.print(text);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		try {
			line = in.readLine();
		} catch (IOException e) {
			OutputUtil.println("Error parsing input from console: ");
			e.printStackTrace();
		}
		return line;
	}
	
	
	public void serveInitialMenu() throws Exception {
		String workingDirectory = "";
		File f = null;
		
		do {
			workingDirectory = readFromConsole("Enter working directory: ");
			f = new File(workingDirectory);
			if(!f.exists()) {
				OutputUtil.println(workingDirectory + " does not exist.");
			}
		} while (!f.exists());
		f = null;
		Constants.FOLDER = workingDirectory;
		
		String xmlFile = "";
		String choice = "";
		do {
			choice = readFromConsole("Enter [S] to start with a preference specification file (XML) or [Q] to start with a query file (XML) : ");
		} while(!(choice.equalsIgnoreCase("S") || choice.equalsIgnoreCase("Q")));
		
		do {
			xmlFile = workingDirectory + File.separator + readFromConsole("Enter the location of the XML file: ");
			Constants.QUERY_FILE = xmlFile;
			f = new File(xmlFile);
			if(!f.exists()) {
				OutputUtil.println(xmlFile + " does not exist.");
			}
		} while (!f.exists());
		f=null;
		
		String modelCheckerCommand = readFromConsole("Enter model checker command: ");
		Constants.SMV_EXEC_COMMAND = modelCheckerCommand + " ";
		
		String modelChecker = readFromConsole("Enter model checker: ");
		if(modelChecker == null){
			throw new RuntimeException("Unsupported Model Checker");
		} else if (modelChecker.equalsIgnoreCase("cadenceSMV")) {
			Constants.CURRENT_MODEL_CHECKER = MODEL_CHECKER.CadenceSMV;
		} else if(modelChecker.equalsIgnoreCase("nuSMV")) {
			Constants.CURRENT_MODEL_CHECKER = MODEL_CHECKER.NuSMV;
		} else {
			throw new RuntimeException("Unsupported model checker");
		}
		
		try {
			if(choice.equalsIgnoreCase("S")) {
				serveReasoningMenu(xmlFile);
			} else {
				Query q = null;
				OutputUtil.println("Parsing query specification ... "+xmlFile);
				q = PreferenceQueryParser.parsePreferenceQuery(xmlFile);
				executeQuery(q);
				System.exit(0);
			}
			
		} catch (FileNotFoundException e) {
			OutputUtil.println("Unable to find/parse file " + xmlFile);
			e.printStackTrace();
			System.exit(-1);
		} catch (PreferenceReasonerException e) {
			OutputUtil.println("Unable to find/parse file " + xmlFile);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void serveReasoningMenu(String xmlFile)
			throws FileNotFoundException, Exception,
			ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		CPTheoryToSMVTranslator translator = new CPTheoryToSMVTranslator();
		OutputUtil.println("Parsing preference specification ... "+xmlFile);
		PreferenceSpecification ps =  CPTheoryToSMVTranslator.parsePreferenceSpecification(xmlFile);
		String option = "";
		do {
			OutputUtil.println();
			OutputUtil.println("Reasoning options:");
			OutputUtil.println("[1] Test Dominance");
			OutputUtil.println("[2] Test Dominance Performance");
//			OutputUtil.println("[3] Generate Ordering");
			OutputUtil.println("[3] Test Consistency");
			OutputUtil.println("[9] Exit");
			option = readFromConsole("Enter option: ");
			option = option.trim();
			if(option.equals("1")) {
				loopedDominanceTest(ps);
			} else if(option.equals("2")) {
				String smvFile = translator.convertToSMV(xmlFile, REASONING_TASK.DOMINANCE, 0);
				PreferenceReasoner reasoner = new CyclicPreferenceReasoner(smvFile);
				dominancePerformanceTester(reasoner, ps);
			}/*else if(option.equals("3")) {
				nextPreferredTester(reasoner, ps);
			}*/else if(option.equals("3")) {
				String smvFile = translator.convertToSMV(xmlFile, REASONING_TASK.CONSISTENCY, 0);
				PreferenceReasoner reasoner = new CyclicPreferenceReasoner(smvFile);
				consistencyTester(reasoner);
			} else if(option.equals("9")) {
				OutputUtil.println();
			} else {
				OutputUtil.println("Incorrect option.");
			}
		} while (!option.equals("9"));
	}

	public void executeQuery(Query q) throws PreferenceReasonerException {
		String smvFile = null;
		
		if(q.getQueryType().equals(PreferenceQuery.QueryType.DOMINANCE)) {
			try {
				smvFile = new CPTheoryToSMVTranslator().convertToSMV(q.getPreferenceSpecificationFileName(), REASONING_TASK.DOMINANCE, 0);
			} catch(Exception fe) {
				fe.printStackTrace();
				throw new PreferenceReasonerException(ExceptionUtil.getStackTraceAsString(fe));
			}
			PreferenceReasoner reasoner = new CyclicPreferenceReasoner(smvFile);
			executeDominanceTest(reasoner, q);
		} else if (q.getQueryType().equals(PreferenceQuery.QueryType.CONSISTENCY)) {
			try {
				smvFile = new CPTheoryToSMVTranslator().convertToSMV(q.getPreferenceSpecificationFileName(), REASONING_TASK.CONSISTENCY, 0);
			} catch(Exception fe) {
				throw new PreferenceReasonerException(ExceptionUtil.getStackTraceAsString(fe));
			}
			PreferenceReasoner reasoner = new CyclicPreferenceReasoner(smvFile);
			executeConsistencyTest(reasoner);
		}
	}

	public void executeConsistencyTest(PreferenceReasoner reasoner) {
		try {
			QueryResult result = reasoner.isConsistent();
			OutputUtil.println(result.getQueryResultAsText());
		} catch (Exception e) {
			OutputUtil.println("Error evaluating consistency: ");
			e.printStackTrace();
		}
	}
	
	public void consistencyTester(PreferenceReasoner reasoner) {
		executeConsistencyTest(reasoner);
	}
	
	public void loopedDominanceTest(PreferenceSpecification ps) throws Exception {

		String more = null;
		do {
		
			Map<String, String> morePreferredAlternative = new HashMap<String,String>();
			Map<String, String> lessPreferredAlternative = new HashMap<String,String>();
			
			OutputUtil.println("Dominating alternative");
			
			for(String var : ps.getVariableNames()) {
				String value = readFromConsole(var+" = ? ");
				morePreferredAlternative.put(var,value);
			}
			
			OutputUtil.println("Dominated alternative");
			
			for(String var : ps.getVariableNames()) {
				String value = readFromConsole(var+" = ? ");
				lessPreferredAlternative.put(var,value);
			}
			
			Outcome morePreferredOutcome = new Outcome(morePreferredAlternative);
			morePreferredOutcome.setLabel("BETTER");
			Outcome lessPreferredOutcome = new Outcome(lessPreferredAlternative);
			lessPreferredOutcome.setLabel("WORSE");
			
			Set<Outcome> outcomesForDominanceTest = new HashSet<Outcome>();
			outcomesForDominanceTest.add(morePreferredOutcome);
			outcomesForDominanceTest.add(lessPreferredOutcome);
			
			Query query = new Query(QueryType.DOMINANCE,ps.getPrefSpecFileName(),outcomesForDominanceTest);
			
			executeQuery(query);
			
			do {
				more = readFromConsole("Continue with another dominance test? [Y/N] ");
			} while(!more.equalsIgnoreCase("y") && !more.equalsIgnoreCase("n"));
		} while (more.equalsIgnoreCase("y"));
	}

	public QueryResult executeDominanceTest(PreferenceReasoner reasoner, Query query) {
		Outcome betterOutcome = null;
		Outcome worseOutcome = null;
		QueryResult result = null;
		try {
			Constants.OBTAIN_PROOF_OF_DOMINANCE_BY_DEFAULT = true;
			for(Outcome o : query.getOutcomes()) {
				if(o.getLabel().equalsIgnoreCase("BETTER")) {
					betterOutcome = o;
				}
				if(o.getLabel().equalsIgnoreCase("WORSE")) {
					worseOutcome = o;
				}
			}
			 result = reasoner.dominates(betterOutcome, worseOutcome);
			OutputUtil.println(result.getQueryResultAsText());
		} catch (PreferenceReasonerException pe) {
			OutputUtil.println("Error evaluating dominance: ");
			pe.printStackTrace();
			System.out.println(ExceptionUtil.getStackTraceAsString(pe));
		} catch (Exception e) {
			OutputUtil.println("Error evaluating dominance: ");
			e.printStackTrace();
			System.out.println(ExceptionUtil.getStackTraceAsString(e));
		}
		
		return result;
	}
	
	public void dominancePerformanceTester(PreferenceReasoner reasoner, PreferenceSpecification prefSpec) throws Exception {
		String dominanceProof = readFromConsole("Compute proof of dominance? (Y/N) ");
		Constants.OBTAIN_PROOF_OF_DOMINANCE_BY_DEFAULT = dominanceProof.trim().equalsIgnoreCase("Y")?true:false;
		
		int numSpecs = 0;
		do {
			numSpecs = 0;
			String nSpecs = readFromConsole("Enter number of specifications to test: ");
			try {
				numSpecs = Integer.parseInt(nSpecs);
			} catch (NumberFormatException e) {
				OutputUtil.println("Incorrect number format. Enter an integer > 0.");
				numSpecs = 0;
			}
		} while (!(numSpecs > 0));
		
		SpecGenerator specGen = new SpecGenerator();
		Constants.NUM_SPECS = numSpecs;
		long totalTime = 0;
		List<Query> queries = specGen.createRandomDominanceTestSpecsSize(prefSpec, numSpecs);
		int specIndex = 0;
		for(Query query : queries) {
			long timer = System.currentTimeMillis();
			Constants.BATCH_QUERY_INDEX = specIndex + 1;
			QueryResult result = executeDominanceTest(reasoner, query);
			long fullTime = (System.currentTimeMillis() - timer);
			totalTime += fullTime;
			String[] words = new String[]{"QUERY ", (specIndex+1)+"   ", result.getResult()+"", fullTime+"   ", "ms"};
			int[] padLengths = new int[]{7, 7, 10, 10, 3};
			String resultString = FileUtil.appendPaddedWordsAsLineToFile(prefSpec.getPrefSpecFileName()+"-"+numSpecs+"-queryresults.txt", words, padLengths);
			OutputUtil.println(resultString);
			specIndex++;
		}	
		Constants.BATCH_QUERY_INDEX = 1;
		OutputUtil.println();
		OutputUtil.println("Average time per dominance test: " + (totalTime/numSpecs) + " ms");
	}
	
	public void nextPreferredTester(PreferenceReasoner reasoner, PreferenceSpecification prefSpec) throws Exception {
		
		int numOutcomes = 1;
		/*do {
			numOutcomes = 0;
			String nSpecs = readFromConsole("Enter number of alternatives to compute: ");
			try {
				numOutcomes = Integer.parseInt(nSpecs);
			} catch (NumberFormatException e) {
				OutputUtil.println("Incorrect number format. Enter an integer > 0.");
				numOutcomes = 0;
			}
		} while (!(numOutcomes > 0));*/
		
//		SpecGenerator specGen = new SpecGenerator();
		Constants.NUM_OUTCOMES = numOutcomes;
		
//		long totalTime = 0;
//		for(int specIndex=0; specIndex<numOutcomes; specIndex++) {
//			List<Map<String, String>> instance = specGen.createRandomDominanceTestInstance(prefSpec.getVariables());
			long timer = System.currentTimeMillis();
			reasoner.generateWeakOrderWithCycles();
			long fullTime = (System.currentTimeMillis() - timer);
//			totalTime += fullTime;
//			String[] words = new String[]{"Alternative ", (specIndex+1)+"   ", result+"", fullTime+"   ", "ms"};
//			int[] padLengths = new int[]{7, 7, 10, 10, 3};
//			String resultString = FileUtil.appendPaddedWordsAsLineToFile(reasoner.smvFile+"-"+numOutcomes+"alternatives.txt", words, padLengths);
//			OutputUtil.println(resultString);
			OutputUtil.println();
			OutputUtil.println("Total time: " + fullTime + " ms");
//		}	
		
	}

}
