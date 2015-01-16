package generate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import model.Outcome;
import model.PreferenceSpecification;
import model.PreferenceVariable;
import model.Query;
import model.QueryType;
import util.Constants;
import util.FileUtil;
import util.OutputUtil;
import util.StringUtil;
import exception.PreferenceReasonerException;

/**
 * Generates random specifications for experimental testing of sample preference specification   
 * @author gsanthan
 *
 */
public class SpecGenerator {

	public static void main(String[] args) throws PreferenceReasonerException {
//		SpecGenerator sg = new SpecGenerator();
		Set<String> d1 = new HashSet<String>();
		d1.add("a");
		d1.add("b");
		d1.add("c");
		PreferenceVariable v1 = new PreferenceVariable("v1",d1);

		Set<String> d2 = new HashSet<String>();
		d2.add("a");
		d2.add("b");
		d2.add("c");
		PreferenceVariable v2 = new PreferenceVariable("v2", d2);

		Set<PreferenceVariable> pv = new HashSet<PreferenceVariable>();
		pv.add(v1);
		pv.add(v2);
	    /*OutputUtil.println(sg.createRandomDominanceTestSpecsSize(pv,3));
	    OutputUtil.println();
		for(int i=0; i<3; i++) {
			OutputUtil.println(sg.createRandomDominanceTestInstance(pv));
		}*/
	}
	
	/**
	 * Generates a total of sampleSize CTL specs for dominance testing with respect to outcomes specified by the input preferences namesOfVariables and domains
	 * @param namesOfVariables
	 * @param domains
	 * @param sampleSize
	 * @return An array of dominance test CTL specs (randomly generated)
	 */
	public String[] createRandomDominanceTestSpecs(String[] variables, String[][] domains, int sampleSize) {
		String[] specs = new String[sampleSize];
		Random random = new Random(Constants.RANDOM_SEED);
		
		for(int i = 0; i < sampleSize; i++) {
			String outcome1 = new String();
			String outcome2 = new String();
			String readableOutcome1 = new String();
			String readableOutcome2 = new String();
	        String ctr = StringUtil.padWithSpace(""+(i+1),7);
			for (int j = 0; j < variables.length; j++) {
				String randomValuation = domains[j][random.nextInt(domains[j].length)];
				if(outcome1.length()>0) {
					outcome1 = outcome1 + " & ";
					readableOutcome1 = readableOutcome1 + ",";
				}
				outcome1 = outcome1 + variables[j] + "=" + randomValuation;
				readableOutcome1 = readableOutcome1 + randomValuation;
				
				randomValuation = domains[j][random.nextInt(domains[j].length)];
				if(outcome2.length()>0) {
					outcome2 = outcome2 + " & ";
					readableOutcome2 = readableOutcome2 + ",";
				}
				outcome2 = outcome2 + variables[j] + "=" + randomValuation;
				readableOutcome2 = readableOutcome2 + randomValuation;
			}
			specs[i] = "SPEC ("+ outcome1 + " -> EX EF (" + outcome2 + ")) -- "+ ctr + ". (" + readableOutcome1 + ") > (" + readableOutcome2 + ")";
			OutputUtil.println(specs[i]);
		}
		
		return specs;
	}
	
	/**
	 * Generates a total of sampleSize CTL specs for dominance testing with respect to outcomes specified by the input preferences namesOfVariables and domains
	 * @param namesOfVariables
	 * @param domains
	 * @param sampleSize
	 * @return An array of dominance test CTL specs (randomly generated)
	 */
	public String[] createRandomDominanceTestSpecs(Set<PreferenceVariable> prefVars, int sampleSize) {
		String[] specs = new String[sampleSize];
		Random random = new Random(Constants.RANDOM_SEED);
		
		for(int i = 0; i < sampleSize; i++) {
			String outcome1 = new String();
			String outcome2 = new String();
			String readableOutcome1 = new String();
			String readableOutcome2 = new String();
	        String ctr = StringUtil.padWithSpace(""+(i+1),7);
	        
			for(PreferenceVariable var : prefVars) {
				List<String> domain = new ArrayList<String>(var.getDomainValues());
				int r = random.nextInt(domain.size());
				String randomValuation = domain.get(r);
//				OutputUtil.println("Random val : "+randomValuation + " (" + r + ") of " + domain);
				if(outcome1.length()>0) {
					outcome1 = outcome1 + " & ";
					readableOutcome1 = readableOutcome1 + ",";
				}
				outcome1 = outcome1 + var.getVariableName() + "=" + randomValuation;
				readableOutcome1 = readableOutcome1 + randomValuation;
				
				r = random.nextInt(domain.size());
				randomValuation = domain.get(r);
//				OutputUtil.println("Random val : "+randomValuation + " (" + r + ") of " + domain);
				if(outcome2.length()>0) {
					outcome2 = outcome2 + " & ";
					readableOutcome2 = readableOutcome2 + ",";
				}
				outcome2 = outcome2 + var.getVariableName() + "=" + randomValuation;
				readableOutcome2 = readableOutcome2 + randomValuation;
			}
			specs[i] = "SPEC ("+ outcome1 + " -> EX EF (" + outcome2 + ")) -- "+ ctr + ". (" + readableOutcome1 + ") > (" + readableOutcome2 + ")";
			OutputUtil.println(specs[i]);
		}
		
		return specs;
	}
	
	
	public List<Query> createRandomDominanceTestSpecsSize(PreferenceSpecification prefSpec, int sampleSize) throws PreferenceReasonerException, IOException {
		String[] specs = new String[sampleSize];
		Random random = new Random(Constants.RANDOM_SEED);
		List<Query> queries = new ArrayList<Query>();
		for(int i = 0; i < sampleSize; i++) {
			String outcome1 = new String();
			String outcome2 = new String();
			String readableOutcome1 = new String();
			String readableOutcome2 = new String();
//	        String ctr = StringUtil.padWithSpace(""+(i+1),7);
	        
	        Map<String, String> first = new HashMap<String, String>();
			Map<String, String> second = new HashMap<String, String>();
			
			for(PreferenceVariable var : prefSpec.getVariables()) {
				List<String> domain = new ArrayList<String>(var.getDomainValues());
				int r = random.nextInt(domain.size());
				String randomValuation = domain.get(r);
//				OutputUtil.println("Random val : "+randomValuation + " (" + r + ") of " + domain);
				if(outcome1.length()>0) {
					outcome1 = outcome1 + " & ";
					readableOutcome1 = readableOutcome1 + ",";
				}
				outcome1 = outcome1 + var.getVariableName() + "=" + randomValuation;
				readableOutcome1 = readableOutcome1 + randomValuation;
				first.put(var.getVariableName(), randomValuation);
				
				r = random.nextInt(domain.size());
				randomValuation = domain.get(r);
//				OutputUtil.println("Random val : "+randomValuation + " (" + r + ") of " + domain);
				if(outcome2.length()>0) {
					outcome2 = outcome2 + " & ";
					readableOutcome2 = readableOutcome2 + ",";
				}
				outcome2 = outcome2 + var.getVariableName() + "=" + randomValuation;
				readableOutcome2 = readableOutcome2 + randomValuation;
				second.put(var.getVariableName(), randomValuation);
			}
			
			Set<Outcome> dominanceTestInstance = new HashSet<Outcome>();
			Outcome betterOutcome = new Outcome(first);
			betterOutcome.setLabel("BETTER");
			Outcome worseOutcome = new Outcome(second);
			worseOutcome.setLabel("WORSE");
			dominanceTestInstance.add(betterOutcome);
			dominanceTestInstance.add(worseOutcome);
			Query q = new Query(QueryType.DOMINANCE,prefSpec.getPrefSpecFileName(),dominanceTestInstance);
			queries.add(q);
			
			String[] words = new String[]{"QUERY", (i+1)+"   ", "("+outcome1+")", " -> EX EF ","("+outcome2+")" };
			int[] padLengths = new int[]{7, 7, 50, 10, 50};
//			specs[i] = "QUERY ("+ outcome1 + " -> EX EF (" + outcome2 + ")) -- "+ ctr + ". (" + readableOutcome1 + ") > (" + readableOutcome2 + ")";
//			OutputUtil.println(specs[i]);
			specs[i] = FileUtil.appendPaddedWordsAsLineToFile(prefSpec.getPrefSpecFileName()+"-"+sampleSize+"-queries.txt", words, padLengths);
			OutputUtil.println(specs[i]);
		}
		
		return queries;
	}
	
	
	public List<Outcome> createRandomDominanceTestInstance(Set<PreferenceVariable> prefVars) throws PreferenceReasonerException {
		List<Outcome> outcomePair = new ArrayList<Outcome>(); 
		Random random = new Random(Constants.RANDOM_SEED);
		
		Map<String, String> first = new HashMap<String, String>();
		Map<String, String> second = new HashMap<String, String>();
		
		
		for(PreferenceVariable var : prefVars) {
			List<String> domain = new ArrayList<String>(var.getDomainValues());
			int s = domain.size();	
			int r = random.nextInt(s);
			String randomValuation = domain.get(r);
			OutputUtil.println("Random val : "+randomValuation + " (" + r + "," + s +  ") of " + domain);
//			String randomValuation = domain.get(random.nextInt(domain.size()));
			first.put(var.getVariableName(), randomValuation);
			
			r = random.nextInt(s);
			randomValuation = domain.get(r);
			OutputUtil.println("Random val : "+randomValuation + " (" + r + "," + s + ") of " + domain);
//			randomValuation = domain.get(random.nextInt(domain.size()));
			second.put(var.getVariableName(), randomValuation);
		}
		
		outcomePair.add(new Outcome(first));
		outcomePair.add(new Outcome(second));
		return outcomePair;
		
	}
	
}	