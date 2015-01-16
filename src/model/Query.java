package model;

import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import exception.PreferenceReasonerException;

@XStreamAlias("PREFERENCE-QUERY")
public class Query {

	@XStreamOmitField
	QueryType qt = null;
	
	@XStreamAlias("QUERY-FILE")
	String queryFileName = null;
	
	@XStreamAlias("PREFERENCE-SPECIFICATION-FILENAME")
	String preferenceSpecificationFileName;
	
	@XStreamAlias("QUERY-TYPE")
	String parsedQueryType;

	@XStreamImplicit(itemFieldName = "OUTCOME")
	Set<Outcome> outcomes;
	
	public String getPreferenceSpecificationFileName() {
		return preferenceSpecificationFileName;
	}

	public void setPreferenceSpecificationFileName(
			String preferenceSpecificationFileName) {
		this.preferenceSpecificationFileName = preferenceSpecificationFileName;
	}

	public Query(QueryType qt, String preferenceSpecFileName, Set<Outcome> outcomes) {
		this.qt = qt;
		this.preferenceSpecificationFileName = preferenceSpecFileName;
		this.outcomes = outcomes;
		this.parsedQueryType = qt.toString()+"";
	}
	
	public Query(String queryFileName, QueryType qt, String preferenceSpecFileName, Set<Outcome> outcomes) {
		this.qt = qt;
		this.queryFileName = queryFileName;
		this.preferenceSpecificationFileName = preferenceSpecFileName;
		this.outcomes = outcomes;
		this.parsedQueryType = qt.toString()+"";
	}
	
	public String getQueryFileName() {
		return queryFileName;
	}

	public void setQueryFileName(String queryFileName) {
		this.queryFileName = queryFileName;
	}

	public void setQueryType(QueryType queryType) {
		qt = queryType;
	}
	
	public QueryType getQueryType() throws PreferenceReasonerException {
		if(parsedQueryType.equalsIgnoreCase("DOMINANCE")) {
			qt = QueryType.DOMINANCE;
		} else if (parsedQueryType.equalsIgnoreCase("CONSISTENCY")) {
			qt = QueryType.CONSISTENCY;
		} else {
			throw new PreferenceReasonerException("Invalid query type: "+parsedQueryType);
		}
		return qt;
	}

	public String getParsedQueryType() {
		return parsedQueryType;
	}

	public void setParsedQueryType(String parsedQueryType) throws PreferenceReasonerException {
		this.parsedQueryType = parsedQueryType;
		if(parsedQueryType.equalsIgnoreCase("DOMINANCE")) {
			qt = QueryType.DOMINANCE;
		} else if (parsedQueryType.equalsIgnoreCase("CONSISTENCY")) {
			qt = QueryType.CONSISTENCY;
		} else {
			throw new PreferenceReasonerException("Invalid query type: "+parsedQueryType);
		}
	}

	public Set<Outcome> getOutcomes() {
		return outcomes;
	}

	public void setOutcomes(Set<Outcome> outcomes) {
		this.outcomes = outcomes;
	}
	
	public void print() {
		System.out.println(parsedQueryType);
		for(Outcome o : outcomes) {
			o.print();
		}
		System.out.println();
	}

	public void setDefaultQueryFileName() throws PreferenceReasonerException {
		if(getQueryType().equals(QueryType.CONSISTENCY)) {
			setQueryFileName(getPreferenceSpecificationFileName()+"-consistencyquery.xml");
		} else if(getQueryType().equals(QueryType.DOMINANCE)) {
			setQueryFileName(getPreferenceSpecificationFileName()+"-dominancequery.xml");
		} else {
			setQueryFileName(getPreferenceSpecificationFileName()+"-unknownquery.xml");
		}
	}

}
