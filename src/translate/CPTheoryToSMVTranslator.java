package translate;

import generate.SpecGenerator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import model.PreferenceSpecification;
import model.PreferenceStatement;
import model.PreferenceVariable;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import test.CPTheoryDominanceExperimentDriver.REASONING_TASK;
import util.Constants;
import util.FileUtil;
import util.OutputUtil;
import util.StringUtil;
import util.XPathUtil;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.thoughtworks.xstream.XStream;

/**
 * Translates TCP-nets specified in XML format into SMV model suitable as input for model checkers Cadence SMV and NuSMV.
 * Based on the chosen model checker (according to value set in the constant Constants.CURRENT_MODEL_CHECKER), the output file format differs.
 * Translation is based on the theory from the paper "Dominance Testing via Model Checking" Santhanam et al. AAAI 2010 which contains the precise translation rules.
 * 
 * @see translate.PreferenceInputTranslator#convertToSMV(java.lang.String, int, boolean)
 * @author gsanthan
 *
 */
public class CPTheoryToSMVTranslator implements PreferenceInputTranslator {
	
	public enum VAR_TYPE {CHANGE, COPY};
	
	public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		CPTheoryToSMVTranslator t = new CPTheoryToSMVTranslator();
		String file = "D:\\Ganesh\\Research\\WIP\\JAIR2013\\example\\cyberdefense-cptheory.xml";
		OutputUtil.println(t.convertToSMV(file, REASONING_TASK.CONSISTENCY, 2));
	}
	
	/**
	 * Parses XML file and saves a SMV file fit for model checking by NuSMV or Cadence SMV.
	 * Also generates random 'sampleSize' specs, i.e., dominance test cases and saves them in a separate '.spec' file. 
	 * Note: Output file syntax is switched according to value set in the constant Constants.CURRENT_MODEL_CHECKER
	 *   
	 * @param xmlFile Name of file specifying the input TCP-net
	 * @param sampleSize Number of sample dominance test cases to be generated
	 * @return Name of saved SMV file that can be model checked by NuSMV or Cadence SMV
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public String convertToSMV(String xmlFile, REASONING_TASK reasoningTask, int sampleSize) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		
		PreferenceSpecification ps = parsePreferenceSpecification(xmlFile);
		//Open XML file, load up Document object
		Document doc = XPathUtil.makeDocument(xmlFile);

//		loadVariablesAndDomains(doc);
		
		// Create/open the smv file where we are going to save the translated code
		String smvFile = xmlFile.substring(0, xmlFile.length()-4).concat(".smv");
		BufferedWriter w = FileUtil.openFile(smvFile);
		
		String changeVarList = StringUtil.commaSeparatedPrefixed(ps.getVariableNames(), "ch");
		String copyVarList = StringUtil.commaSeparatedSuffixed(ps.getVariableNames(), "_0");
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			String formalParameterList = ""; 
			if(reasoningTask == REASONING_TASK.CONSISTENCY) {
				formalParameterList = changeVarList + "," + copyVarList;
			} else {
				formalParameterList = changeVarList;
			}
			FileUtil.writeLineToFile(w, "module main("+formalParameterList+"){");
		} else {
			FileUtil.writeLineToFile(w, "MODULE main");
		}
		
		FileUtil.writeLineToFile(w, "");
		
		/**
		 * SMV syntax for a variable "a" with domain ("a1","a2") is as follows:
		 * VAR  
		 * 	a : {a1, a2};
		 */
		// Write the lines declaring the namesOfVariables and their domains to smv file
		FileUtil.writeLineToFile(w, "VAR");

		//Conditional preference -- for multiple statements -- translate each of them
		ListMultimap<String,String> preferenceStatementGuards = translateConditionalPreferences(ps, doc);
		
		//Write variable declarations
		declareVariablesAndConstraints(ps, reasoningTask, w);
		
		// Conditional Preferences - Each CPT entry is a transition specification in the model
		FileUtil.writeLineToFile(w, "ASSIGN");
		
		encodeGuards(ps, w, preferenceStatementGuards, reasoningTask);
		
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			FileUtil.writeLineToFile(w, "}");
		}
		
		FileUtil.closeFile(w);
		
//		generateRandomSpec(ps, xmlFile, sampleSize);
		
		return smvFile;
	}

	public static PreferenceSpecification parsePreferenceSpecification(String xmlFile)
			throws FileNotFoundException {
		XStream xStream = new XStream();
		xStream.autodetectAnnotations(true);
		FileReader reader = new FileReader(xmlFile);
		xStream.toXML(new PreferenceSpecification()); // Don't know why, but fromXML throws Exception if we don't do toXML first!
		PreferenceSpecification ps = (PreferenceSpecification) xStream.fromXML(reader);
		ps.makeValid();
		return ps;
	}

	public void encodeGuards(PreferenceSpecification ps, BufferedWriter w,
			ListMultimap<String, String> preferenceStatementGuards, REASONING_TASK reasoningTask)
			throws IOException {
		// Write the assignments in terms of next(var) : = case ... esac; statements
		for (PreferenceVariable prefVar : ps.getVariables()) {
			
			String variableName = prefVar.getVariableName();
			//Write 'next(var):= case'
			FileUtil.writeLineToFile(w, "  next("+variableName+") :=");
			
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				FileUtil.writeLineToFile(w, "    case{");
			} else {
				FileUtil.writeLineToFile(w, "    case");	
			}
			
			// Dump the translated lines for current variable into the smv file
			for (String line : preferenceStatementGuards.get(variableName)) {
				
				//Add to global change list of lines to write
//				globalChange.add(linesToWrite[i]);
				FileUtil.writeLineToFile(w, line);
			}
			
			// Specify the default value in the next state of the transition - unchanged
			FileUtil.writeLineToFile(w, "      TRUE : " + variableName + ";");
			
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				FileUtil.writeLineToFile(w, "    };");
			} else {
				FileUtil.writeLineToFile(w, "    esac;");
			}
		}
		
		if(reasoningTask == REASONING_TASK.CONSISTENCY || reasoningTask == REASONING_TASK.ORDERING) {

			//Write 'next(gch):= case'
			FileUtil.writeLineToFile(w, "  next(gch) :=");
			
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				FileUtil.writeLineToFile(w, "    case{");
			} else {
				FileUtil.writeLineToFile(w, "    case");	
			}
			
			//Note: There is a difference depending on whether the following line is included as part of next(gch)
			//FileUtil.writeLineToFile(w, "      gch=1: 0;");
			for (PreferenceVariable prefVar : ps.getVariables()) {
				String variableName = prefVar.getVariableName();
				for (String line : preferenceStatementGuards.get(variableName)) {
					if(!line.trim().startsWith("--")) {
						String g = line.split(":")[0];
						g = g + ": 1;";
						FileUtil.writeLineToFile(w, g);
					}
				}
			}
			FileUtil.writeLineToFile(w, "      TRUE: 0;");
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				FileUtil.writeLineToFile(w, "    };");
			} else {
				FileUtil.writeLineToFile(w, "    esac;");	
			}
		}
	}

	public void declareVariablesAndConstraints(PreferenceSpecification ps, REASONING_TASK reasoningTask, BufferedWriter w) throws IOException {
		for (PreferenceVariable prefVar : ps.getVariables()) {
			String variableName = prefVar.getVariableName();
			String varLine = "  " + variableName + " : " ;
			//Extract domain of the current variable
			Set<String> variableDomain = prefVar.getDomainValues();

			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				//Cadence SMV -- use "boolean" instead of {0,1} for binary domain; otherwise, use enumeration
				if(variableDomain.equals(new HashSet<String>(Arrays.asList(new String[]{"0","1"})))) {
					varLine = varLine + "boolean";	
				} else {
					varLine = varLine + "{";
					for (Iterator<String> varDomainIterator = variableDomain.iterator(); varDomainIterator.hasNext();) {
						String value = (String) varDomainIterator.next();
						varLine = varLine + value;
						if(varDomainIterator.hasNext()) {
							varLine = varLine + ",";
						}
					}
					varLine = varLine + "}";
				}
			} else {
				//NuSMV -- use enumeration {0,1} even for binary domain
				varLine = varLine + "{";
				for (Iterator<String> varDomainIterator = variableDomain.iterator(); varDomainIterator.hasNext();) {
					String value = (String) varDomainIterator.next();
					varLine = varLine + value;
					if(varDomainIterator.hasNext()) {
						varLine = varLine + ",";
					}
				}
				varLine = varLine + "}";
			}
			
			varLine = varLine + ";";
			
			FileUtil.writeLineToFile(w, varLine);
		}
		
		

		
		
		if(reasoningTask == REASONING_TASK.CONSISTENCY || reasoningTask == REASONING_TASK.ORDERING) {
			//Declare global change variable
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				FileUtil.writeLineToFile(w, "  gch : boolean;");
			} else {
				FileUtil.writeLineToFile(w, "  gch : {0,1};");
			}
			
			FileUtil.writeLineToFile(w, "");
			
			//Aux. namesOfVariables to compare the current state with the start state (i.e., outcome where verification begun)
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.NuSMV) {
				FileUtil.writeLineToFile(w, "FROZENVAR");
			}
			writeAuxiliaryVariableDeclarations(w, new ArrayList<String>(ps.getVariableNames()), "", "_0", VAR_TYPE.COPY);
			
			//Aux. namesOfVariables indicating change of corresponding preference namesOfVariables in next state
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.NuSMV) {
				FileUtil.writeLineToFile(w, "IVAR");
			}
			writeAuxiliaryVariableDeclarations(w, new ArrayList<String>(ps.getVariableNames()), "ch", "", VAR_TYPE.CHANGE);
			
			FileUtil.writeLineToFile(w, "");
			
			//Define a variable that is true whenever at least one of the change namesOfVariables is true -- used in defining next(gch)
			FileUtil.writeLineToFile(w, "DEFINE");
			int varIndex=0;
			String start = /*"";
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				start += "DEFINE ";
			}
			start +=*/ "start := ";
			for (Iterator<String> iterator = ps.getVariableNames().iterator(); iterator.hasNext(); varIndex++) {
				String variableName = (String) iterator.next();
				if(varIndex > 0) { 
					start = start + " & ";
				}
				start = start + variableName + "=" + variableName + "_0";
			}
			start = start + ";";
			FileUtil.writeLineToFile(w, start);
			FileUtil.writeLineToFile(w, "");
		
			FileUtil.writeLineToFile(w, "INIT start=TRUE;");
			String transConstraintCopyVar = "";
			varIndex=0;
			for (Iterator<String> iterator = ps.getVariableNames().iterator(); iterator.hasNext(); varIndex++) {
				String variableName = (String) iterator.next();
				if(varIndex > 0) { 
					transConstraintCopyVar = transConstraintCopyVar + " & ";
				}
				transConstraintCopyVar = transConstraintCopyVar + variableName + "_0=next(" + variableName + "_0)";
			}
			FileUtil.writeLineToFile(w, "TRANS "+transConstraintCopyVar+";");
	
			FileUtil.writeLineToFile(w, "");
		} else {
		
			//Aux. namesOfVariables indicating change of corresponding preference namesOfVariables in next state
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.NuSMV) {
				FileUtil.writeLineToFile(w, "IVAR");
			}
			writeAuxiliaryVariableDeclarations(w, new ArrayList<String>(ps.getVariableNames()), "ch", "", VAR_TYPE.CHANGE);
			
			FileUtil.writeLineToFile(w, "");
		}
			
		
		// Write the assignments in terms of next(var) : = case ... esac; statements
		// If there are parents, then write one line (modeling a transition) for each CPT entry
		// 		specifying the current variable's preference for that parent assignment, 
		//		with all other namesOfVariables equal for all namesOfVariables other than current variable in the two states
		//		List<String> copy = new ArrayList<String>(ps.getVariableNames());
	}

	private void writeAuxiliaryVariableDeclarations(BufferedWriter w, List<String> variableNames, String prefix, String suffix, VAR_TYPE type) throws IOException {
		Iterator<String> iterator = variableNames.iterator();
		// Create namesOfVariables that indicate change of value in the corresponding preference variable 
		while (iterator.hasNext()) {
			String variableName = (String) iterator.next();
			String varLine = "";
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				if(type == VAR_TYPE.CHANGE || type == VAR_TYPE.COPY) {
					varLine = " input " + prefix + variableName + suffix + " : " + "boolean;";
				} else {
					varLine = "  " + prefix + variableName + suffix + " : " + "boolean;";
				}
			} else {
				varLine = "  " + prefix + variableName + suffix + " : " + "{0,1};";
			}
			FileUtil.writeLineToFile(w, varLine);
		}
	}

	public void generateRandomSpec(PreferenceSpecification ps, String xmlFile, int sampleSize)
			throws IOException {
		if(sampleSize > 0) {
			// Generate and write randomly generated 'sampleSize' specs to the spec file
			String specFile = xmlFile.substring(0, xmlFile.length()-4).concat(".spec");
			BufferedWriter wSpec = FileUtil.openFile(specFile);
			SpecGenerator specGen = new SpecGenerator();
			
			String[] specs = specGen.createRandomDominanceTestSpecs(ps.getVariables(), sampleSize);
			for (int i = 0; i < specs.length; i++) {
				FileUtil.writeLineToFile(wSpec, specs[i]);
			}
			
			FileUtil.closeFile(wSpec);
		}
	}
	
	/*Set<PreferenceStatement> getPreferenceStatements(Document doc) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		Set<PreferenceStatement> prefStatements = new HashSet<PreferenceStatement>();
		
		List<ListMultimap<String, String>> listOfPrefStatementNodes = XPathUtil.nodeListOfNodeListsAsListOfMultimaps(XPathUtil.eval(doc, "//PREFERENCE-STATEMENT"));
		for(ListMultimap<String, String> node : listOfPrefStatementNodes) {
			PreferenceStatement prefStmt = new PreferenceStatement();
			String var = node.get("PREFERENCE-VARIABLE").get(0);
			prefStmt.setVariableName(var);
			String id = node.get("ID").get(0);
			prefStmt.setStatementId(id);
			List<String> conditions = node.get("CONDITION");
			prefStmt.setParentAssignments(conditions);
			*//**
			List<String> parents = new ArrayList<String>();
			String parentVar;
			for(String c : conditions) {
				try {
					Iterable<String> result = Splitter.on('=')
						       .trimResults()
						       .split(c);
					parentVar = result.iterator().next();
					parents.add(parentVar);
				} catch(Exception e) {
					e.printStackTrace();
					OutputUtil.println("Error parsing CONDITION for preference statement on variable "+var);
				}
			}
			prefStmt.setParentVariables(parents);
			*//*
			
			List<String> preferences = node.get("PREFERENCE");
			prefStmt.setIntravarPreferences(preferences);
			
			List<String> lessImpVars = node.get("REGARDLESS-OF");
			prefStmt.setLessImpVariables(lessImpVars);
		
			prefStatements.add(prefStmt);
		}
		
		return prefStatements;
	}*/

	/**
	 * Parses CP-theory in xml format and translates into smv
	 * See "Dominance Testing via Model Checking" Santhanam et al. AAAI 2010 for precise translation rules.
	 * Returns a ListMultimap with corresponding guarded transitions for each variable in the SMV model 
	 * @param prefStatements 
	 * 
	 * @param variableName Input variable 
	 * @param cptKeys Conditions under which intra-variable preferences are specified in the CPT of variableName
	 * @param cptValues Intra-variable preferences corresponding to conditions in the CPT of variableName
	 * @param variableNames List of all variableNames
	 * @param doc XML file containing the input preference specification 
	 * @return Array of strings that have to be included as guarded transitions to the next(variableName) in the SMV model 
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private ListMultimap<String, String> translateConditionalPreferences(PreferenceSpecification ps, Document doc) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		
		Set<PreferenceStatement> prefStatements = ps.getStatements();
		ListMultimap<String, String> lines = ArrayListMultimap.create();
		
		for(PreferenceStatement p : prefStatements) {
		
			// Extract parents of current node -- Not absolutely needed for translation or reasoning.
			String variableName = p.getVariableName();
			
			// For each preference statement, define a transition:
			// preference condition: variable=better value; 
			// 		then for parents get the CPT key and replace ',' with '&' to model smv syntax;
			//		then for ALL namesOfVariables including parents excluding the current variable, 
			//					ensure equality in next state
			// RHS: worse value for the variable
				
				//These are the pairs of binary relations specifying the order on the domain of the current variable 
				String[] preferences = p.getIntravarPreferences().toArray(new String[0]);
				
				for (int j = 0; j < preferences.length; j++) {
					//Each pair is specified as a binary relation, i.e., a>b for a is preferred to b
					String[] orderedValues = preferences[j].split(Constants.PREFERENCE_SYMBOL_IN_XML);
									
					// Model the transition from the worse to the better value of current variable
					String currentLine = "      " + variableName + "=" + orderedValues[1];
					
					// For all PARENT namesOfVariables of the current one, 
					// enforce the assignment specified in preference condition of preference statement
					if(p.getParentAssignments() != null) {
						for (String pa : p.getParentAssignments()) {
							// If it is unconditional, then there is exactly one preference statement with empty preference condition
							if(pa.trim().length()>0) {
								currentLine = currentLine + " & " + pa;
							}
						}
					}
					
					// For all namesOfVariables OTHER than the current one, EXCEPT THOSE LESS IMPORTANT THAN CURRENT ONE (Relative importance)
					// enforce equality in the current and next state of the transition
					Set<String> variableNames = ps.getVariableNames();
					for (String var : variableNames) {
						if(!p.getLessImpVariables().contains(var)) {
							currentLine = currentLine + " & " + "ch" + var + (var.equals(variableName)? "=1" : "=0");
						} else { // Is this needed? (Will work without this)
							currentLine = currentLine + " & " + "ch" + var + "=1";
						}
					}
					
					// Specify the better value in the next state of the transition
					lines.put(variableName, currentLine + " : " + orderedValues[0] + "; -- #"+p.getStatementId()+" : " + p.getParentAssignments() +" => " + p.getVariableName()+"=" + p.getIntravarPreferences() + "  >> " + p.getLessImpVariables() );
					
					for(String lessImpVar : p.getLessImpVariables()) {
						Set<String> variableDomain = ps.getPreferenceVariable(lessImpVar).getDomainValues();
						lines.put(lessImpVar, currentLine + " : {" + StringUtil.commaSeparated(variableDomain) + "}; -- #"+p.getStatementId()+" : " + p.getParentAssignments() +" => " + p.getVariableName()+"=" + p.getIntravarPreferences() + "  >> " + p.getLessImpVariables() );
					}
				}
			}
		
		return lines;
	}
}
