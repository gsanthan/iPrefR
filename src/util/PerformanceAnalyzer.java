package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.Outcome;
import model.PerformanceRecord;
import model.WorkingPreferenceModel;
import verify.TraceFormatter;
import verify.TraceFormatterFactory;

/**
 * Manages and analyzes the performance of various preference reasoning (verification) tasks
 * @author gsanthan
 *
 */
public class PerformanceAnalyzer {
	
	/**
	 * Stores a list of performance records of all the verifications 
	 */
	private static List<PerformanceRecord> records = new ArrayList<PerformanceRecord>();

	public static List<PerformanceRecord> getRecords() {
		return records;
	}

	public static void setRecords(List<PerformanceRecord> records) {
		PerformanceAnalyzer.records = records;
	}  
	
	/**
	 * Clears all stored performance records
	 */
	public static void clearRecords() {
		records.clear();
		PerformanceRecord.resetCounter();
	}
	
	/**
	 * Adds a performance record for a verification task along with an associated outcome that was generated as a counterexample during verification 
	 * @param record
	 * @param outcome
	 */
	public static void addPerformanceRecord(PerformanceRecord record, Outcome outcome) {
		record.setOutcome(outcome);
		records.add(record);
	}
	
	/**
	 * Prints the stored performance records 
	 */
	public static void printRecords() {
		OutputUtil.println("Performance Records: ");
		for (PerformanceRecord record : records) {
			OutputUtil.println("@@## "+record);
		}
	}
	
	/**
	 * Add the performance corresponding to the latest verification task by parsing the model checker's trace output file and stores it along with an associated outcome   
	 * @param outcome
	 * @throws IOException
	 */
	public static void addLatestPerformanceRecord(Outcome outcome) throws IOException {
		addPerformanceRecord(getPerformanceRecordForLastQuery(), outcome);
	}
	
	/**
	 * Parses the latest performance record from the model checker's trace output file
	 * @return PerformanceRecord for the latest query run by the model checker
	 * @throws IOException
	 */
	public static PerformanceRecord getPerformanceRecordForLastQuery() throws IOException {
		TraceFormatter f = TraceFormatterFactory.createTraceFormatter();
		PerformanceRecord pr = f.getPerformanceRecord(WorkingPreferenceModel.getPrefMetaData().getOutputFile());
		return pr;
	}
}
