package generate;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import model.PrefEdge;
import model.PrefNode;

import org.jgrapht.DirectedGraph;

import util.Constants;
import util.OutputUtil;
import util.PrefGraphUtil;


public class PreferenceGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PreferenceGenerator gen = new PreferenceGenerator();
		String[] parentNames = new String[]{"B","C"};
		String[][] parentDomains = new String[][]{{"b1","b2"},{"c1","c2"}};
//		String[] possibleParentValues = gen.generatePossibleParentValues(parentDomain);
		String[] cpt = gen.generateFormattedCPTRows("A", new String[]{"a0","a1","a2","a3","a4"}, parentNames, parentDomains, 0, false);
		for (int i = 0; i < cpt.length; i++) {
			OutputUtil.println(cpt[i]);
		}
	}

	/**
	 * Generates formatted CPT rows like b1,c1:a2>a3>a0>a4>a1 
	 * where b1,c1 is the parent assignment and a2>a3>a0>a4>a1 is a random total order on the domain 
	 * 
	 * @param var
	 * @param domain
	 * @param possibleParentValues
	 * @return
	 */
	private String[] generateFormattedCPTRows(String var, String[] domain, String[] possibleParentValues, int maxCptSize, boolean onlyTotalOrder) {

		String[] cpt = generateCPTRows(var, domain, possibleParentValues, maxCptSize, onlyTotalOrder);
		for (int i = 0; i < cpt.length; i++) {
			cpt[i] = possibleParentValues[i] + ":" + cpt[i];
		}
		return cpt;
	}
	
	public String[] generateFormattedCPTRows(String var, String[] domain, String[] parentNames, String[][] parentDomains, int maxCptSize, boolean onlyTotalOrder) {

		String[] possibleParentValues = generatePossibleParentValues(parentNames,parentDomains,maxCptSize);
		String[] cpt = generateFormattedCPTRows(var, domain, possibleParentValues, maxCptSize, onlyTotalOrder);
		return cpt;
	}
	
	/**
	 * Generates the Conditional Preference Table for the combinations of values in parentDomain array
	 * and returns an String array containing 2 arrays - possibleParentValues and condPref
	 * 
	 * @param var
	 * @param domain
	 * @param parentDomain
	 * @return
	 */
	public String[][] generateCPTRows(String var, String[] domain, String[] parentNames, String[][] parentDomain, int maxCptSize, boolean onlyTotalOrder) {

		String[] possibleParentValues = generatePossibleParentValues(parentNames, parentDomain, maxCptSize);
		String condPref[] = generateCPTRows(var, domain, possibleParentValues, maxCptSize, onlyTotalOrder);
		return new String[][] {possibleParentValues, condPref};
	}
	
	/**
	 * Generates the Conditional Preference Table for each value in possibleParentValues
	 * and returns an String array condPref containing random total orders for each of the possibleParentValues  
	 * 
	 * @param var
	 * @param domain
	 * @param possibleParentValues
	 * @return
	 */
	public String[] generateCPTRows(String var, String[] domain, String[] possibleParentValues, int maxCptSize, boolean onlyTotalOrder) {
		String condPref[] = null; 
		if(possibleParentValues.length == 0) {
			//Unconditional Preference
			condPref = new String[1];
			condPref[0] = getRandomTotalOrder(domain);
		} else {
			//Conditional Preference
			//Incomplete CPT specification - shuffle cpt rows and then pick a subset of random size  
			for (int i = 0; i < possibleParentValues.length; i++) { 
			   int r = i + (int) (Constants.random.nextDouble() * (possibleParentValues.length-i)); 
			   String t = possibleParentValues[r]; 
			   possibleParentValues[r] = possibleParentValues[i]; 
			   possibleParentValues[i] = t; 
			} 
			int cptSize;
/*			
 * Commented temporarily to take results with fixed cpt sizes of either user specified maxCptSize or the total number of parent assignments
 * 			if(possibleParentValues.length == 0 || possibleParentValues.length == 1) {
				//No parent valuations - unconditional case
				cptSize = 1;
			} else {
				//multiple rows for each parent valuation
				cptSize = (int) new Random(Constants.RANDOM_SEED).nextInt(possibleParentValues.length);
				while(cptSize == 0) {
					cptSize = (int) new Random(Constants.RANDOM_SEED).nextInt(possibleParentValues.length);
				}
				if(maxCptSize != 0 && cptSize > maxCptSize) {
					//Limit the number of CPT entries - humans can't provide so many inputs
					cptSize = maxCptSize;
				}
			}*/

			cptSize = possibleParentValues.length;
			
			condPref = new String[cptSize];
			for (int i = 0; i < cptSize; i++) {
				//Generate Random Ordering of the domain for this cpt row
				if(onlyTotalOrder) {
					condPref[i]  = getRandomTotalOrder(domain);
				} else {
					condPref[i]  = getRandomPartialOrder(domain);
				}
			}
		}
		return condPref;
	}
	
	private String getRandomTotalOrder(String[] domain) {
		String[] ordering = (String[])domain.clone();
		for (int k = ordering.length - 1; k > 0; k--) {
		    int index = (int)Math.floor(Constants.random.nextDouble() * (k+1));
		    String temp = ordering[index];
		    ordering[index] = ordering[k];
		    ordering[k] = temp;
		}
		
		String preference = new String();
		for (int i = 0; i < ordering.length-1; i++) {
			preference = preference + ordering[i] + ">" + ordering[i+1] + ",";
		}
		return preference.substring(0,preference.length()-1);
	}

	private String getRandomPartialOrder(String[] domain) {
		String[] pref = (String[])domain.clone();
		Set<PrefNode> vertices = new HashSet<PrefNode>();
		for (int i = 0; i < domain.length; i++) {
			vertices.add(new PrefNode(domain[i]));
		}
		int edgeCount = 0;
		if(domain.length > 2) {
			while(edgeCount == 0) {
				edgeCount = (int)Math.floor(Constants.random.nextDouble() * (domain.length*(domain.length-1)/2));
			}
		} else {
			edgeCount = 1;//If there are only 2 values then have 1 edge
		}
		DirectedGraph<PrefNode, PrefEdge> dag = PrefGraphUtil.makeRandomDAG(vertices, edgeCount);
		Set<PrefEdge>edges = dag.edgeSet();
		String preference = new String();
		for (PrefEdge prefEdge : edges) {
			preference = preference + prefEdge.getSource() + ">" + prefEdge.getTarget() + ",";
		}
		return preference.substring(0,preference.length()-1);
	}
	
	/**
	 * Finds combination of possible values, i.e., left hand side of CPT, given domains of parents
	 * Output example - b1,c1;b1,c2;b2,c1;b2,c2; containing 4 rows for 2 binary namesOfVariables 
	 * @param parentNames 
	 * 
	 * @param parentDomains
	 * @return
	 */
	public String[] generatePossibleParentValues(String[] parentNames, String[][] parentDomains, int maxCptSize) {
		long timer = System.currentTimeMillis();
//        int[][] combMatrix = getCombMatrix(parentDomains);
//        System.out.print("["+(System.currentTimeMillis()-timer)+"]");
        String possibleParentValues[] = null;
        String temp = new String();
        timer = System.currentTimeMillis();
        Random random = new Random(Constants.RANDOM_SEED);
        Set<String> cptRows = new LinkedHashSet<String>();
        int cptCount = 0;
        int cptSize = (getNumComb(parentDomains)>maxCptSize) ? maxCptSize : getNumComb(parentDomains);
        while(cptCount < cptSize) {
        	String valueComb = new String();
        	for(int j = 0; j<parentDomains.length; j++) {
        		String randomValuation = parentDomains[j][random.nextInt(parentDomains[j].length)];
        		valueComb = valueComb + parentNames[j] + "=" + randomValuation+",";
        	}
            int trimLength = (valueComb.length() > 0 ? valueComb.length()-1 : 0);
            valueComb = valueComb.substring(0, trimLength);
//            temp = temp + valueComb.substring(0, trimLength) + ";";
            if(!cptRows.contains(valueComb)) {
            	cptRows.add(valueComb);
            	cptCount++;
            }
		}
//        System.out.print("<"+(System.currentTimeMillis()-timer)+",");
        timer = System.currentTimeMillis();
//        OutputUtil.println(temp);
//        possibleParentValues = temp.split(";");
        possibleParentValues = cptRows.toArray(new String[0]);
//        System.out.print((System.currentTimeMillis()-timer)+">");
        //For unconditional preference, there is no parent assignment
        if(possibleParentValues.length == 0) {
        	possibleParentValues = new String[]{" "};
        }
        return possibleParentValues;
    }
	
	/*public String[] generatePossibleParentValues(String[] parentNames, String[][] parentDomains, int maxCptSize) {
		long timer = System.currentTimeMillis();
        int[][] combMatrix = getCombMatrix(parentDomains);
//        System.out.print("["+(System.currentTimeMillis()-timer)+"]");
        String possibleParentValues[] = null;
        String temp = new String();
        timer = System.currentTimeMillis();
        
        for (int i = 0; i < combMatrix.length; i++) { 
		   int r = i + (int) (Math.Random(Constants.RANDOM_SEED) * (combMatrix.length - i)); 
		   int[] t = combMatrix[r]; 
		   combMatrix[r] = combMatrix[i]; 
		   combMatrix[i] = t; 
		}
        int cptCount = 0;
        int cptSize = (combMatrix.length>maxCptSize) ? maxCptSize : combMatrix.length;
        int[][] pickedRows = new int[cptSize][parentDomains.length];
        while(cptCount < cptSize) {
        	int[] row = combMatrix[cptCount];
        	cptCount++;
            int rowIndex = 0;
            int colIndex = 0;
            String valueComb = new String();
            while(colIndex < parentDomains.length){
                valueComb = valueComb + parentNames[rowIndex] + "=" + parentDomains[rowIndex][row[colIndex]]+",";
                rowIndex++;
                colIndex++;
            }
            int trimLength = (valueComb.length() > 0 ? valueComb.length()-1 : 0);
            temp = temp + valueComb.substring(0, trimLength) + ";";
        }
        System.out.print("<"+(System.currentTimeMillis()-timer)+",");
        timer = System.currentTimeMillis();
//        OutputUtil.println(temp);
        possibleParentValues = temp.split(";");
        System.out.print((System.currentTimeMillis()-timer)+">");
        //For unconditional preference, there is no parent assignment
        if(possibleParentValues.length == 0) {
        	possibleParentValues = new String[]{" "};
        }
        return possibleParentValues;
    }*/
    
    private String getRandomValuation(String[] domain) {
    	int index = (int) (Constants.random.nextDouble() * domain.length);
		return null;
	}

	private int[][] getCombMatrix(String[][] arrays) {
        final int NUM_COMB = getNumComb(arrays);
        int[][] combMatrix = new int[NUM_COMB][arrays.length];
        int repetition = 1;
        for(int col = arrays.length-1; col >= 0; col--) {
            int max = arrays[col].length;
            int value = 0;
            for(int row = 0; row < NUM_COMB;) {
                for(int i = 0; i < repetition; i++, row++) {
                    combMatrix[row][col] = value;
                }
                value = value+1 >= max ? 0 : value+1;
            }
            repetition *= max;
        }
        return combMatrix;
    }
 
    private int getNumComb(String[][] arrays) {
        int n = 1;
        for(String[] s : arrays) n *= s.length;
        return n;
    }
 
}
