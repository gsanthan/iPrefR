package model;

import java.io.IOException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("QUERY-RESULT")
public class QueryResult {
	
	@XStreamAlias("QUERY-FILE")
	String queryFile;
	
	@XStreamAlias("RESULT")
	boolean result;
	
	@XStreamAlias("PROOF")
	OutcomeSequence proof;
	
	@XStreamAlias("ERROR")
	String errorMessage;
	
	public QueryResult(String queryFile, boolean result, OutcomeSequence proof, String errorMessage) {
		this.queryFile = queryFile;
		this.result = result;
		this.proof = proof;
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public OutcomeSequence getProof() {
		return proof;
	}

	public void setProof(OutcomeSequence proof) {
		this.proof = proof;
	}

	public String getQueryFileName() {
		return queryFile;
	}

	public void setQueryFile(String queryFile) {
		this.queryFile = queryFile;
	}



	public String getQueryResultAsXML()
			throws IOException {
		XStream xStream = new XStream();
		xStream.autodetectAnnotations(true);
		String xmlOutcomeSequence = xStream.toXML(this);
		return xmlOutcomeSequence;
	}

	public String getQueryResultAsText() {
		String text = "";
		text += "Result: "+(result+"").toUpperCase();
		if(proof != null) text += "; "+"Proof: "+proof.getOutcomeSequenceAsString();
		if(errorMessage != null) text += "; "+"Error: "+errorMessage;
		if(queryFile != null) text += "; "+"Query File: "+queryFile;
		return text;
	}
}
