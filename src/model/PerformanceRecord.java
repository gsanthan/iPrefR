package model;

import java.util.Set;

import util.BinaryEncoding;

/**
 * Stores the statistics related to one verification run of the model checker.
 * This is used to keep track of the performance of the preference reasoner as it does various reasoning tasks.
 * @author gsanthan
 *
 */
public class PerformanceRecord {
	/**
	 * An index to the PerformanceRecord
	 */
	private static long counter = 0;
	/**
	 * An index to the PerformanceRecord
	 */
	private long recordId = 0;
	/**
	 * User Time elapsed during verification  
	 */
	private double userTimeElapsed = 0;
	/**
	 * System Time elapsed during verification
	 */
	private double systemTimeElapsed = 0;
	/**
	 * Number of BDDs used during verification
	 */
	private long bddsUsed = 0;
	/**
	 * Name of property that was verified (for ease of identification of the reasoning task)
	 */
	private String property;
	/**
	 * Result of verification
	 */
	private boolean verified;
	/**
	 * Name of file containing the SMV model  
	 */
	private String smvFile;
	/**
	 * The outcome, if any, that was generated as a counterexample during the verification
	 */
	private Set<String> outcome;
	
	public Set<String> getOutcome() {
		return outcome;
	}

	public void setOutcome(Set<String> outcome) {
		this.outcome = outcome;
	}

	/**
	 * Initializes the PerformanceRecord with all the details; 
	 * automatically keeps track of the record ID using the counter.
	 * @param smvFile Name of file containing the SMV model  
	 * @param userTimeElapsed User Time elapsed during verification  
	 * @param systemTimeElapsed System Time elapsed during verification
	 * @param bddsUsed Number of BDDs used during verification
	 * @param property Name of property that was verified (for ease of identification of the reasoning task)
	 * @param verified Result of verification
	 */
	public PerformanceRecord(String smvFile, double userTimeElapsed, double systemTimeElapsed, long bddsUsed, String property, boolean verified) {
		counter++;
		this.recordId = counter;
		this.smvFile = smvFile;
		this.userTimeElapsed = userTimeElapsed;
		this.systemTimeElapsed = systemTimeElapsed;
		this.bddsUsed = bddsUsed;
		this.property = property;
		this.verified = verified;
	}
	
	public long getCounter() {
		return counter;
	}
	
	public long getRecordId() {
		return recordId;
	}
	
	public String getProperty() {
		return property;
	}

	public void setProperty(String query) {
		this.property = query;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean result) {
		this.verified = result;
	}

	public double getUserTimeElapsed() {
		return userTimeElapsed;
	}

	public void setUserTimeElapsed(long userTimeElapsed) {
		this.userTimeElapsed = userTimeElapsed;
	}

	public double getSystemTimeElapsed() {
		return systemTimeElapsed;
	}

	public void setSystemTimeElapsed(long systemTimeElapsed) {
		this.systemTimeElapsed = systemTimeElapsed;
	}

	public long getBddsUsed() {
		return bddsUsed;
	}

	public void setBddsUsed(long bddsUsed) {
		this.bddsUsed = bddsUsed;
	}

	public void addToTimeElapsed(long time) {
		userTimeElapsed += time;
	}
	
	public String getSmvFile() {
		return smvFile;
	}

	public void setSmvFile(String smvFile) {
		this.smvFile = smvFile;
	}

	public static void resetCounter() {
		counter = 0;
	}
	
	/**
	 * Formats the entire performance statistics into a single line string.
	 */
	public String toString() {
		String formatted = "";
		String encodedOutcome = "intermediate step";
		if(outcome != null) {
			encodedOutcome=BinaryEncoding.getBinaryEncoding(WorkingPreferenceModel.getPrefMetaData().getVariables(), outcome);
		}
		formatted += "["+recordId+","+encodedOutcome+","+smvFile+","+property+","+verified+","+userTimeElapsed+","+systemTimeElapsed+","+bddsUsed+"]";
		return formatted;
	}
}
