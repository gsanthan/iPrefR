package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import model.DominanceTestPair;
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
import translate.PreferenceInputTranslator;
import translate.PreferenceInputTranslatorFactory;
import translate.PreferenceLanguage;
import util.Constants;
import util.Constants.MODEL_CHECKER;
import util.ExceptionUtil;
import util.FileUtil;
import util.OutputUtil;

import com.google.common.base.Splitter;
import com.thoughtworks.xstream.XStream;

import exception.PreferenceReasonerException;
import generate.CPTheoryGenerator;
import generate.SpecGenerator;

public class CPTheoryDominanceExperimentDriver {

	
	public enum REASONING_TASK {DOMINANCE,CONSISTENCY,ORDERING};
	
	public static void main(String[] args) throws Exception {
		/*OutputUtil.println(PropertiesManager.class.getClass().getResourceAsStream("/runtime.properties"));
		InputStream is = PropertiesManager.class.getClass().getResourceAsStream(Constants.CONFIG_RUNTIME_PROPERTIES);
		PropertiesManager.runtimeProperties.load(is);*/
		CPTheoryDominanceExperimentDriver d = new CPTheoryDominanceExperimentDriver();
//		d.testDominance();
//		d.dominanceTester();
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
//		Scanner sc = new Scanner(System.in);
//		return sc.nextLine();
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

	public void executeDominanceTest(PreferenceReasoner reasoner, Query query) {
		Outcome betterOutcome = null;
		Outcome worseOutcome = null;

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
			QueryResult result = reasoner.dominates(betterOutcome, worseOutcome);
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
		
		SpecGenerator specGen = new SpecGenerator();
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
		List<DominanceTestPair> instances = specGen.createRandomDominanceTestSpecsSize(prefSpec.getVariables(), numSpecs);
		int specIndex = 0;
		for(DominanceTestPair instance : instances) {
			long timer = System.currentTimeMillis();
//			OutputUtil.println(instance.getFirst() +""+instance.getSecond());
			Constants.BATCH_QUERY_INDEX = specIndex + 1;
			QueryResult result = reasoner.dominates(instance.getFirst(), instance.getSecond());
			long fullTime = (System.currentTimeMillis() - timer);
			totalTime += fullTime;
			String[] words = new String[]{"SPEC ", (specIndex+1)+"   ", result.getResult()+"", fullTime+"   ", "ms"};
			int[] padLengths = new int[]{7, 7, 10, 10, 3};
			String resultString = FileUtil.appendPaddedWordsAsLineToFile(reasoner.smvFile+"-"+numSpecs+"specs.txt", words, padLengths);
			OutputUtil.println(resultString);
			specIndex++;
		}	
		Constants.BATCH_QUERY_INDEX = 1;
		OutputUtil.println();
		OutputUtil.println("Average time per dominance test: " + (totalTime/numSpecs) + " ms");
	}
	
	public void testDominance() throws Exception {
		
//		String path = "D:\\Ganesh\\Research\\WIP\\JAIR2013\\example\\cptheory-gen\\";
//		Constants.FOLDER = path;
//		String path = Constants.FOLDER;
		
		ExperimentConfigurator.configureExperiment();
		PreferenceLanguage l = PreferenceLanguage.CPTheory;
		Date d = new Date();
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd'_'HH-mm-ss").format(new java.util.Date());
		String resultFile = Constants.FOLDER+File.separator+"result-"+(Constants.CURRENT_MODEL_CHECKER==Constants.MODEL_CHECKER.NuSMV?"n":"c")+"_"+date+".txt";
		Constants.RESULT_FILE = resultFile;
//		String logFile=path+"cinet-generation-testing-log.txt";
		int domainSize = 2;
		List<String> parameters = new ArrayList<String>();
		parameters.add("Model Checker : " + Constants.CURRENT_MODEL_CHECKER);
		parameters.add("Model Checker Command : " + Constants.SMV_EXEC_COMMAND);
		parameters.add(Constants.CURRENT_MODEL_CHECKER.toString());
		parameters.add("Preference Language : " + l);
		parameters.add("#Variables : " + Constants.MIN_VAR_SIZE + " to " + Constants.MAX_VAR_SIZE + " increment by " + Constants.VAR_SIZE_INCREMENT);
		parameters.add("#Statments : " + Constants.MIN_CPT_SIZE + " to " + Constants.MAX_CPT_SIZE + " increment by " + Constants.CPT_SIZE_INCREMENT);
		parameters.add("Are # Statements specified per variable ? " + Constants.IS_CPT_PER_VAR);
		parameters.add("Preference Spec File Location : " + Constants.FOLDER);
		parameters.add("#Pref Spec Files : " + Constants.NUM_PREF_FILES);
		parameters.add("#Pref Specs : " + Constants.NUM_SPECS);
		parameters.add("Intravariable Preference Total Order ? " + Constants.INTRAVAR_TOTALORDER);
		parameters.add("Obtain proof of dominance by default ? " + Constants.OBTAIN_PROOF_OF_DOMINANCE_BY_DEFAULT);
		parameters.add("Result File Name : " + Constants.RESULT_FILE);
		
		FileUtil.appendLineToFile(resultFile, "--------------------Parameters----------------------");
		for(String p : parameters) {
			FileUtil.appendLineToFile(resultFile, p);
			OutputUtil.println(p);
		}
		FileUtil.appendLineToFile(resultFile, "----------------------------------------------------");
		
		for(int numVariables=Constants.MIN_VAR_SIZE; numVariables<=Constants.MAX_VAR_SIZE; numVariables+=Constants.VAR_SIZE_INCREMENT) {
//			String resultFile = Constants.RESULT_FILE + ".vs" + vs;
			for(int numStatements=Constants.MIN_CPT_SIZE; numStatements<=Constants.MAX_CPT_SIZE; numStatements+=Constants.CPT_SIZE_INCREMENT) {
				for (int numIndex = 0; numIndex < Constants.NUM_PREF_FILES; numIndex++) {
		//			int numVariables = MathUtil.getRandomInteger(7,10,Constants.random);
		//			int numStatements = MathUtil.getRandomInteger(8,14,Constants.random);
//					OutputUtil.println(numVariables+","+numStatements);
//					FileUtil.appendLineToFile(logFile,numVariables+","+numStatements);
					
					String cptheoryFile = Constants.FOLDER+l.toString().toLowerCase()+"--vs"+numVariables+"-ss"+numStatements+"-"+(numIndex+1)+".txt";
					String xmlFile = Constants.FOLDER+l.toString().toLowerCase()+"--vs"+numVariables+"-ss"+numStatements+"-"+(numIndex+1)+".xml";
					
					CPTheoryGenerator cptGen = new CPTheoryGenerator();
				
					PreferenceSpecification prefSpec = cptGen.generateCPTheory(numVariables, domainSize, numStatements, Constants.IS_CPT_PER_VAR, l);
					writePrefSpecToFile(xmlFile, prefSpec);
					
					PreferenceInputTranslator translator = PreferenceInputTranslatorFactory.createTranslator(PreferenceLanguage.CPTheory);
					String smvFile = translator.convertToSMV(xmlFile, REASONING_TASK.DOMINANCE, 0);
					
					PreferenceReasoner p = new CyclicPreferenceReasoner(smvFile);
					
					SpecGenerator specGen = new SpecGenerator();
					for(int specIndex=0; specIndex<Constants.NUM_SPECS; specIndex++) {
						
						List<Outcome> outcomePair = specGen.createRandomDominanceTestInstance(prefSpec.getVariables());
						
						long timer = System.currentTimeMillis();
						QueryResult result = p.dominates(outcomePair.get(0), outcomePair.get(1));
						String fullTime = (System.currentTimeMillis() - timer)+"";
						
						String[] words = new String[]{"Vars " , numVariables+"","Stmts " , numStatements+"   ", "File ", (numIndex+1)+"   ", "SPEC ", (specIndex+1)+"   ", result.getResult()+"", fullTime+"   ", "ms", "  ",  (specIndex==0?xmlFile:"-")};
						int[] padLengths = new int[]{7, 7, 7, 7, 7, 7, 7, 7, 10, 10, 3, 4, 75};
						String resultString = FileUtil.appendPaddedWordsAsLineToFile(resultFile, words, padLengths);
						OutputUtil.println(resultString);
						
					}	
					
						/*long timer = System.currentTimeMillis();
						
						boolean result = p.dominates(outcome1, outcome2);
						
						String fullTime = (System.currentTimeMillis() - timer)+"";
						String bddCount = StringUtil.padWithSpace(ModelCheckingDelegate.findBDDUsageStats(pmd),13);
						String userTime = StringUtil.padWithSpace(ModelCheckingDelegate.findUserRuntimeStats(pmd),13);
						String systemTime = StringUtil.padWithSpace(ModelCheckingDelegate.findSystemtimeUsageStats(pmd),13);
						DecimalFormat decim = new DecimalFormat("0.000");
						String totalTime = StringUtil.padWithSpace((decim.format(Double.parseDouble(userTime) + Double.parseDouble(systemTime)))+"",13);
						String memory = StringUtil.padWithSpace(ModelCheckingDelegate.findMemoryUsageStats(pmd),13);
						
						String[] words = new String[]{"vs " , vs+"","cs " , cs+"", "ms " ,""+ maxDegree, "File ", ""+ (j+1)+"", "SPEC ", c+"", result+"", fullTime, "ms", userTime, systemTime, totalTime, memory, bddCount, "  "+pmd.getSmvFile()+"  ", dominanceInstance };
						int[] padLengths = new int[]{3, 4, 3, 4, 3, 4, 4, 4, 5, 5, 10, 12, 3, 7, 7, 7, 10, 10, 25, 225};
						
						String resultString = FileUtil.appendPaddedWordsAsLineToFile(resultFile, words, padLengths);
						OutputUtil.println(resultString);
//						ModelCheckingDelegate.deleteFiles(pmd);
*/				}
//				deleteFilesExceptXML(Constants.FOLDER, resultFile);
			}
//			deleteFiles(Constants.FOLDER, resultFile);
		}
	}
	

	public static void writePrefSpecToFile(String xmlFile, PreferenceSpecification prefSpec) throws IOException {
		XStream xStream = new XStream();
		xStream.autodetectAnnotations(true);
		String xml = xStream.toXML(prefSpec);
		Iterable<String> xmlLines = Splitter.onPattern("\r?\n")
			       .trimResults()
			       .omitEmptyStrings()
			       .split(xml);
		FileUtil.deleteFileIfExists(xmlFile);
		for(String line : xmlLines) {
			FileUtil.appendLineToFile(xmlFile, line);						
		}
	}
	
/*	private static void deleteFiles(String folder, String resultFile) {
		File dir = new File(folder);
//		OutputUtil.println(FOLDER);
		File[] listOfFiles = dir.listFiles();
		OutputUtil.println("Deleting "+listOfFiles.length+" files in "+folder);
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (!(resultFile.equalsIgnoreCase(listOfFiles[i].getAbsolutePath())) && !(listOfFiles[i].getName().contains(Constants.RESULT_PREFIX))) {
//					  if(!(listOfFiles[i].getName().endsWith(".xml")) && !(listOfFiles[i].getName().endsWith(".jar"))) {
					if(!(listOfFiles[i].getName().endsWith(".jar"))) {
						  listOfFiles[i].delete();
					  }
				}
			}
		}
	}*/
	
	/*private static void deleteFilesExceptXML(String folder, String resultFile) {
		File dir = new File(folder);
//		OutputUtil.println(FOLDER);
		File[] listOfFiles = dir.listFiles();
		OutputUtil.println("Deleting "+listOfFiles.length+" files in "+folder);
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (!(resultFile.equalsIgnoreCase(listOfFiles[i].getAbsolutePath())) && !(listOfFiles[i].getName().contains("results"))) {
//					  if(!(listOfFiles[i].getName().endsWith(".xml")) && !(listOfFiles[i].getName().endsWith(".jar"))) {
					if(!(listOfFiles[i].getName().endsWith(".jar")) && !(listOfFiles[i].getName().endsWith(".xml"))) {
						  listOfFiles[i].delete();
					  }
				}
			}
		}
	}*/
}
