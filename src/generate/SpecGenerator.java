package generate;

import java.util.Random;

import util.Constants;
import util.StringUtil;

/**
 * Generates random specifications for experimental testing of sample preference specification   
 * @author gsanthan
 *
 */
public class SpecGenerator {

	/**
	 * Generates a total of sampleSize CTL specs for dominance testing with respect to outcomes specified by the input preferences variables and domains
	 * @param variables
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
			specs[i] = "SPEC ("+ outcome1 + " -> EX EF (" + outcome2 + ")) -- "+ ctr + ". (" + readableOutcome1 + ") -> (" + readableOutcome2 + ")";
			System.out.println(specs[i]);
		}
		
		return specs;
	}
}	