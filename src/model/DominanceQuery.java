package model;

public class DominanceQuery extends PreferenceQuery {
	
	Outcome betterOutcome, worseOutcome;
	
	public DominanceQuery(PreferenceSpecification prefSpec, String xmlQueryFileName) {
		super(prefSpec, xmlQueryFileName);
	}
	
	@Override
	public void parseXMLQuery() {
		
	}
}
