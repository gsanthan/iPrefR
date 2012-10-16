package translate;

import generate.SpecGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import util.Constants;
import util.FileUtil;

/**
 * Translates CI-nets specified in text format into SMV model suitable as input for model checkers Cadence SMV and NuSMV.
 * Based on the chosen model checker (according to value set in the constant Constants.CURRENT_MODEL_CHECKER), the output file format differs.
 * Translation is based on the theory from the paper "Dominance Testing via Model Checking" Santhanam et al. AAAI 2010 which contains the precise translation rules.
 * 
 * @see translate.PreferenceInputTranslator#convertToSMV(java.lang.String, int, boolean)
 * @author gsanthan
 *
 */
public class CINetToSMVTranslator implements PreferenceInputTranslator {

	
	public String convertToSMV(String cinetFile, int sampleSize) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		//By default, assume forward model, i.e., improving flip
		return convertToSMV(cinetFile, sampleSize, true);
	}
	/**
	 * Translates CI-nets specified in text format into SMV model suitable as input for model checkers Cadence SMV and NuSMV.
 	 * Also generates random 'sampleSize' specs, i.e., dominance test cases and saves them in a separate '.spec' file. 
 	 * Note: Output file syntax is switched according to value set in the constant Constants.CURRENT_MODEL_CHECKER
 	 * 
	 * @see translate.PreferenceInputTranslator#convertToSMV(java.lang.String, int, boolean)
	 * @author gsanthan
	 * 
	 */
	public String convertToSMV(String cinetFile, int sampleSize, boolean improvingFlip) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		
		String smvFile = new String();
		if(improvingFlip) {
			smvFile = cinetFile.substring(0, cinetFile.length()-4).concat(".smv");
		} else {
			smvFile = cinetFile.substring(0, cinetFile.length()-4).concat("-reverse").concat(".smv");
		}
		BufferedReader r = new BufferedReader(new FileReader(cinetFile));
		
		// Create/open the smv file where we are going to save the translated code 
		BufferedWriter w = FileUtil.openFile(smvFile);
		
		//FileUtil.writeLineToFile(w, "MODULE main");
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			FileUtil.writeLineToFile(w, "module main(){");
		} else {
			FileUtil.writeLineToFile(w, "MODULE main");
		}
		
		FileUtil.writeLineToFile(w, "");
		
		String[] variables=null;
		String test = r.readLine();
		if(test !=null && test.equals("VARIABLES")) {
			// Extract preference variables
			variables = r.readLine().split(",");
		}
		
		//Binary variables
		String[][] variableDomains = new String[variables.length][];
		for (int i=0; i<variableDomains.length; i++) {
			variableDomains[i] = new String[]{"0","1"};
		}
		
		//Extract the conditional importance statements
		List<String> preferenceLines = new ArrayList<String>();
		String line = r.readLine(); // This is just the title "PREFERENCES"
		while((line = r.readLine())!=null) {
			preferenceLines.add(line);
		}
		//This map contains the statements to be added to each variable's "next" assignment
		Map<String, List<String>> variableMap = processConditionalImportancePreferences(preferenceLines, variables, improvingFlip);
		List<String> globalChange = new ArrayList<String>();
		
		// Start writing the SMV file
		
		// Write the lines declaring the variables and their domains to smv file
		FileUtil.writeLineToFile(w, "VAR");
		int varIndex = 0;
		for (varIndex=0; varIndex<variables.length; varIndex++) {
			String varLine;
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				varLine = "  " + variables[varIndex] + " : boolean;";
			} else {
				varLine = "  " + variables[varIndex] + " : {0,1};";
			}
			FileUtil.writeLineToFile(w, varLine);
		}
		
		// Create variables that indicate change of value in the corresponding preference variable 
		for (varIndex=0; varIndex<variables.length; varIndex++) {
			String varLine;
			
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				varLine = "  " + "ch" + variables[varIndex] + " : boolean;";
			} else {
				varLine = "  " + "ch" + variables[varIndex] + " : {0,1};";
			}
			
			FileUtil.writeLineToFile(w, varLine);
		}
		
		//Declare global change variable
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			FileUtil.writeLineToFile(w, "  gch : boolean;");
		} else {
			FileUtil.writeLineToFile(w, "  gch : {0,1};");
		}
		
		FileUtil.writeLineToFile(w, "");
		
		// Conditional Preferences - Each CPT entry is a transition specification in the model
		FileUtil.writeLineToFile(w, "ASSIGN");
		

		//TODO For next preferred with cycles, better have this in here!
		// Write the assignments in terms of next(var) : = case ... esac; statements
		for (varIndex=0; varIndex<variables.length; varIndex++) {
			String var = variables[varIndex];
			//Initialize the change variables to 0 (in the start state, chVi=0)
			FileUtil.writeLineToFile(w, "  init(ch"+var+") := 0;");
		}
		
		for (varIndex=0; varIndex<variables.length; varIndex++) {
			String var = variables[varIndex];
			//Write 'next(var):= case'
			FileUtil.writeLineToFile(w, "  next("+var+") :=");
			
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				FileUtil.writeLineToFile(w, "    case{");
			} else {
				FileUtil.writeLineToFile(w, "    case");	
			}
			
			String lineToWrite = new String();
			if(improvingFlip) {
				//Monotonicity Flip - Reversed Direction 
				lineToWrite = "      " + var + "=0" + " & " + "ch"+var+"=1" ;
				for (String otherVar : variables) {
					if(!otherVar.equals(variables[varIndex])) {
						lineToWrite = lineToWrite + " & " + "(("+otherVar+"=1 & ch"+otherVar+"=1) | ("+otherVar+"=0 & ch"+otherVar+"=0))";
					}
				}
				lineToWrite = lineToWrite + ": 1;";
			} else {
				//Monotonicity Flip
				lineToWrite = "      " + var + "=1" + " & " + "ch"+var+"=1" ;
				for (String otherVar : variables) {
					if(!otherVar.equals(variables[varIndex])) {
						lineToWrite = lineToWrite + " & " + "(("+otherVar+"=1 & ch"+otherVar+"=0) | ("+otherVar+"=0 & ch"+otherVar+"=1))";
					}
				}
				lineToWrite = lineToWrite + ": 0;";
			}
			
			//Add this transition to keep track of global change variable
			globalChange.add(lineToWrite);
			FileUtil.writeLineToFile(w, lineToWrite);
		
			//Conditional Importance Flip
			List<String> linesToWrite = variableMap.get(variables[varIndex]);
			//Add these transitions to keep track of global change variable
			globalChange.addAll(linesToWrite);
			for (String l : linesToWrite) {
				FileUtil.writeLineToFile(w, l);
			}
			FileUtil.writeLineToFile(w, "      TRUE: "+variables[varIndex]+";");
			
			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
				FileUtil.writeLineToFile(w, "    };");
			} else {
				FileUtil.writeLineToFile(w, "    esac;");
			}
		}
		
		
		//Process global change variable
		FileUtil.writeLineToFile(w, "  init(gch) := 0;");
		//Write 'next(gch):= case'
		FileUtil.writeLineToFile(w, "  next(gch) :=");
		
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			FileUtil.writeLineToFile(w, "    case{");
		} else {
			FileUtil.writeLineToFile(w, "    case");	
		}		
		
		//TODO For next preferred with cycles, better have this in
		FileUtil.writeLineToFile(w, "      --gch=1: 0;");
		
		for (String g : globalChange) {
			g = g.substring(0,g.lastIndexOf(":"));
			g = g + ": 1;";
			FileUtil.writeLineToFile(w, g);
		}
		FileUtil.writeLineToFile(w, "      TRUE: 0;");
		
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			FileUtil.writeLineToFile(w, "    };");
		} else {
			FileUtil.writeLineToFile(w, "    esac;");	
		}

		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			FileUtil.writeLineToFile(w, "}");
		}
		
		FileUtil.closeFile(w);
		
		if(sampleSize > 0) {
			// Generate and write randomly generated 'sampleSize' specs to the spec file
			String specFile = cinetFile.substring(0, cinetFile.length()-4).concat(".spec");
			BufferedWriter wSpec = FileUtil.openFile(specFile);
			SpecGenerator specGen = new SpecGenerator();
			String[] specs = specGen.createRandomDominanceTestSpecs(variables, variableDomains, sampleSize);
			for (int i = 0; i < specs.length; i++) {
				FileUtil.writeLineToFile(wSpec, specs[i]);
			}
			
			FileUtil.closeFile(wSpec);
		} 
		
		return smvFile;
	}

	private Map<String, List<String>> processConditionalImportancePreferences(List<String> preferenceLines, String[] variables, boolean improvingFlip) {
		
		Map<String,List<String>> variableMap = new HashMap<String,List<String>>();
		for(String variable : variables) {
			variableMap.put(variable, new ArrayList<String>());
		}
		
		for (String preferenceLine : preferenceLines) {
			String condition = preferenceLine.split(":")[0];
			String preference = preferenceLine.split(":")[1];
			
			String positiveSet = condition.split(";")[0];positiveSet=positiveSet.substring(1,positiveSet.length()-1); if(positiveSet.equals("{}")) positiveSet=""; 
			String negativeSet = condition.split(";")[1];negativeSet=negativeSet.substring(1,negativeSet.length()-1); if(negativeSet.equals("{}")) negativeSet="";
			
			String betterSet;
			String worseSet;
			if(improvingFlip) {
				//Reversed Direction of Transition
				betterSet = preference.split(";")[1];betterSet=betterSet.substring(1,betterSet.length()-1); if(betterSet.equals("{}")) betterSet="";
				worseSet = preference.split(";")[0];worseSet=worseSet.substring(1,worseSet.length()-1); if(worseSet.equals("{}")) worseSet="";
			} else {
				//Worsening Flip
				betterSet = preference.split(";")[0];betterSet=betterSet.substring(1,betterSet.length()-1); if(betterSet.equals("{}")) betterSet="";
				worseSet = preference.split(";")[1];worseSet=worseSet.substring(1,worseSet.length()-1); if(worseSet.equals("{}")) worseSet="";
			}
			
			String[] positive = null, negative = null, better = null, worse = null ;
			if (positiveSet.trim().length()>0) {
				positive = positiveSet.split(",");				
			} else {
				positive = new String[0];
			}
			if (negativeSet.trim().length()>0) {
				negative = negativeSet.split(",");
			} else {
				negative = new String[0];
			}
			if (betterSet.trim().length()>0) {
				better = betterSet.split(",");				
			} else {
				better = new String[0];
			}
			if (worseSet.trim().length()>0) {
				worse = worseSet.split(",");
			} else {
				worse = new String[0];
			}
			List<String> betterList = Arrays.asList(better);
			List<String> worseList = Arrays.asList(worse);
			
			String linePrefix = "      ";
			for (String p : positive) {
				if(linePrefix.trim().length()>0) {
					linePrefix += " & ";
				}
				linePrefix += p + "=1";
			}
			for (String n : negative) {
				if(linePrefix.trim().length()>0) {
					linePrefix += " & ";
				}
				linePrefix += n + "=0";
			}
	
			for (String b : better) {
				if(linePrefix.trim().length()>0) {
					linePrefix += " & ";
				}
				if(improvingFlip) {
					linePrefix += b + "=1" + " & " + "ch" + b + "=1";
				} else {
					linePrefix += b + "=0" + " & " + "ch" + b + "=1";
				}
				
			}
			
			for (String w : worse) {
				if(linePrefix.trim().length()>0) {
					linePrefix += " & ";
				}
				if(improvingFlip) {
					linePrefix += w + "=0" + " & " + "ch" + w + "=1";
				} else {
					linePrefix += w + "=1" + " & " + "ch" + w + "=1";
				}
			}
			
			for (String v : variables) {
				if(linePrefix.trim().length()>0) {
					linePrefix += " & ";
				}
				if(betterList.contains(v) || worseList.contains(v)) {
					linePrefix += "ch" + v + "=1";
				} else {
					linePrefix += "ch" + v + "=0";
				}
			}
			
			for (String b : better) {
				if(improvingFlip) {
					variableMap.get(b).add(linePrefix + ": 0;");
				} else {
					variableMap.get(b).add(linePrefix + ": 1;");
				}
			}
			for (String w : worse) {
				if(improvingFlip) {
					variableMap.get(w).add(linePrefix + ": 1;");
				} else {
					variableMap.get(w).add(linePrefix + ": 0;");
				}
			}
		}
		return variableMap;
	} 
}