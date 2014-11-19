package generate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import model.PreferenceSpecification;
import model.PreferenceStatement;
import model.PreferenceVariable;
import reasoner.CyclicPreferenceReasoner;
import reasoner.PreferenceReasoner;
import test.CPTheoryDominanceExperimentDriver.REASONING_TASK;
import translate.PreferenceInputTranslator;
import translate.PreferenceInputTranslatorFactory;
import translate.PreferenceLanguage;
import util.Constants;
import util.FileUtil;
import util.OutputUtil;

import com.google.common.base.Splitter;
import com.google.common.collect.ListMultimap;
import com.thoughtworks.xstream.XStream;

public class CPTheoryGenerator {

	public static void main(String[] args) throws Exception {
		String path = "D:\\Ganesh\\Research\\WIP\\JAIR2013\\example\\cptheory-gen\\";
//		String logFile=path+"cinet-generation-testing-log.txt";
		int domainSize = 3;
		int sampleSize = 5;
		for(int numVariables=10; numVariables<15; numVariables++) {
			for(int numStatements=numVariables/2; numStatements<(2*numVariables+1); numStatements*=2) {
				for(int i=0;i<5;i++) {
		//			int numVariables = MathUtil.getRandomInteger(7,10,Constants.random);
		//			int numStatements = MathUtil.getRandomInteger(8,14,Constants.random);
					OutputUtil.println(numVariables+","+numStatements);
//					FileUtil.appendLineToFile(logFile,numVariables+","+numStatements);
					
//					String cptheoryFile = path+"cptheory--vs"+numVariables+"-cs"+numStatements+"-"+(i+1)+".txt";
					String xmlFile = path+"cptheory--vs"+numVariables+"-cs"+numStatements+"-"+(i+1)+".xml";
					
					CPTheoryGenerator cptGen = new CPTheoryGenerator();
				
					PreferenceSpecification prefSpec = cptGen.generateCPTheory(numVariables, domainSize, numStatements, true, PreferenceLanguage.CPTheory);
					writePrefSpecToFile(xmlFile, prefSpec);
					
					PreferenceInputTranslator translator = PreferenceInputTranslatorFactory.createTranslator(PreferenceLanguage.CPTheory);
					String smvFile = translator.convertToSMV(xmlFile, REASONING_TASK.DOMINANCE, sampleSize);
					
					PreferenceReasoner p = new CyclicPreferenceReasoner(smvFile);
					
					/*p.dominates(morePreferredOutcome, lessPreferredOutcome)
					
					for(int j=0;j<10;j++) {
						timer = System.currentTimeMillis();
						boolean dominates = p.dominates(ciGen.getRandomVariableSubset(namesOfVariables), ciGen.getRandomVariableSubset(namesOfVariables));
						OutputUtil.println((System.currentTimeMillis()-timer)+" ms            Dominance Test: " + dominates);
						FileUtil.appendLineToFile(logFile,(System.currentTimeMillis()-timer)+" ms            Dominance Test: " + dominates);
					}*/
				}
			}
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

	private Set<String> getRandomVariableSubset(List<String> variables) {
		Collections.shuffle(variables);
		List<String> subset = variables.subList(0, Constants.random.nextInt(variables.size()));
		return new HashSet<String>(subset);
	}
	
	public PreferenceSpecification generateCPTheory(int numVariables, int domainSize, int numStatements, boolean numStatementsIsPerVariable, PreferenceLanguage language)
			throws IOException {
		CPTheoryGenerator cptheoryGen = new CPTheoryGenerator();
		PreferenceSpecification prefSpec = new PreferenceSpecification();
		Set<PreferenceVariable> prefVariables = cptheoryGen.generateVariablesWithDomains(numVariables, domainSize);
		prefSpec.setVariables(prefVariables);
		
		if (numStatementsIsPerVariable) {
			List<String> variableNames = new ArrayList<String>(prefSpec.getVariableNames());
			for(int j=0; j<numVariables; j++) {
				for(int i=0;i<numStatements;i++) {
					String prefVar = variableNames.get(j);
					PreferenceStatement cptheoryStatement = cptheoryGen.generateCPTheoryStatement(prefSpec,"p-"+prefVar+"-"+(i+1), prefVar, language);
					prefSpec.getStatements().add(cptheoryStatement);
					/*String xml = xs.toXML(cptheoryStatement);
					Iterable<String> xmlLines = Splitter.onPattern("\r?\n")
				       .trimResults()
				       .omitEmptyStrings()
				       .split(xml);
					
					for(String l : xmlLines) {
						OutputUtil.println(l);
					}*/
				}
			}
		} else {
			//Each time, pick a random variable to create preference statement for.
			for(int i=0;i<numStatements;i++) {
				List<String> variableNames = new ArrayList<String>(prefSpec.getVariableNames());
				Collections.shuffle(variableNames, Constants.random);
				String prefVar = variableNames.get(0);
				PreferenceStatement cptheoryStatement = cptheoryGen.generateCPTheoryStatement(prefSpec,"p"+(i+1), prefVar, language);
				prefSpec.getStatements().add(cptheoryStatement);
				/*String xml = xs.toXML(cptheoryStatement);
				Iterable<String> xmlLines = Splitter.onPattern("\r?\n")
			       .trimResults()
			       .omitEmptyStrings()
			       .split(xml);
				
				for(String l : xmlLines) {
					OutputUtil.println(l);
				}*/
			}
		}
		return prefSpec;
	}
	
	public Set<PreferenceVariable> generateVariablesWithDomains(int numberOfVariables, int domainSize) {
		Set<PreferenceVariable> prefVariables = new HashSet<PreferenceVariable>();
		for(int i=0; i<numberOfVariables; i++) {
			Set<String> domain = new HashSet<String>();
			for(int j=0;j<domainSize;j++) {
				domain.add(j+"");
			}
			PreferenceVariable prefVar = new PreferenceVariable("V"+i, domain); 
			prefVar.setVariableName("V"+i);
			prefVariables.add(prefVar);
		}
		return prefVariables;
	}
	
	public PreferenceStatement generateCPTheoryStatement(PreferenceSpecification ps, String statementId, String prefVar, PreferenceLanguage language) {
		List<String> variableNames = new ArrayList<String>(ps.getVariableNames());
		List<String> fixedVariables = new ArrayList<String>();
		List<String> parentVariables = new ArrayList<String>();
		List<String> lessImpVariables = new ArrayList<String>();
		
		Random random = Constants.random;
		Collections.shuffle(variableNames, random);
//		String prefVar = variableNames.get(0);
		variableNames.remove(prefVar);
		
		int fixed=0,lessImp=0,parents=0;
		
		if(language == PreferenceLanguage.CPTheory) {
			for(String v : variableNames) {
				if(lessImp < 3) {
					lessImpVariables.add(v);
					lessImp++;
				} else {
				
					//Pick one of the 3 bins : parents, don't cares, or ceteris paribus; and deposit the current variable into the (randomly chosen) partition
					switch (random.nextInt(3)) {
						case 0: 
	//						if(random.nextBoolean()) {
								parentVariables.add(v);
	//						} else {
	//							lessImpVariables.add(v);
	//						}
							break;
						case 1: fixedVariables.add(v);						
							break;
						case 2: fixedVariables.add(v);					
							break;
	//					case 3: fixedVariables.add(v);					
	//						break;
					}
				}
			}
		} else if (language == PreferenceLanguage.CPnet) {
			for(String v : variableNames) {
					//Pick one of the 2 bins : parents or ceteris paribus; and deposit the current variable into the (randomly chosen) partition
				switch (random.nextInt(3)) {
					case 0: parentVariables.add(v);					
						break;
					case 1: fixedVariables.add(v);					
						break;
					case 2: fixedVariables.add(v);					
						break;
				}
			}
		} else if (language == PreferenceLanguage.TCPnet) {
			if(random.nextBoolean()) {
				//A TCP-net statement will have a relative importance clause 50% of the times
				String lessImpVar = variableNames.get(0);
				variableNames.remove(0);
				lessImpVariables.add(lessImpVar);
			} 
			for(String v : variableNames) {
					//Pick one of the 2 bins : parents or ceteris paribus; and deposit the current variable into the (randomly chosen) partition
				switch (random.nextInt(3)) {
					case 0: parentVariables.add(v);					
						break;
					case 1: fixedVariables.add(v);					
						break;
					case 2: fixedVariables.add(v);					
						break;
				}
			}
		}
		ListMultimap<String, String> variablesWithDomains = ps.getVariablesWithDomainsAsMultimap();
		List<String> prefConditions = getRandomAssignment(variablesWithDomains, parentVariables);
		String preference = getRandomPreference(variablesWithDomains, prefVar);
//		String cptheoryStatement = StringUtil.commaSeparated(prefConditions) + "  :  " + preference + " [" +  StringUtil.commaSeparated(lessImpVariables) + "]    Fixed[" + StringUtil.commaSeparated(fixedVariables) + "]";
//		OutputUtil.println(cptheoryStatement);
		List<String> preferenceAsList = new ArrayList<String>();
		preferenceAsList.add(preference);
		PreferenceStatement p = new PreferenceStatement(statementId, prefVar, prefConditions, preferenceAsList, lessImpVariables);
		return p;
	}
	
	public String getRandomPreference(ListMultimap<String,String> variables, String variableName) {
		List<String> domain = variables.get(variableName);
		Collections.shuffle(domain);
		List<String> prefValues = domain.subList(0, 2);
		
		String better = prefValues.get(0);
		String worse = prefValues.get(1);
		String preference = better + Constants.PREFERENCE_SYMBOL_IN_XML + worse; 
		return preference;
	}
	
	public String getRandomAssignment(ListMultimap<String,String> variables, String variableName) {
		int randomPosition = Constants.random.nextInt(variables.get(variableName).size());
		return variableName + "=" + variables.get(variableName).get(randomPosition);
	}
	
	public List<String> getRandomAssignment(ListMultimap<String,String> variables, List<String> variableNames) {
		List<String> assignments = new ArrayList<String>();
		for(String var : variableNames) {
			assignments.add(getRandomAssignment(variables, var));
		}
		return assignments;
	}
}
