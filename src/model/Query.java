package model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import model.PreferenceQuery.QueryType;
import reasoner.CyclicPreferenceReasoner;
import reasoner.PreferenceReasoner;
import test.CPTheoryDominanceExperimentDriver.REASONING_TASK;
import translate.CPTheoryToSMVTranslator;
import util.Constants;
import util.OutputUtil;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import exception.PreferenceReasonerException;

@XStreamAlias("PREFERENCE-QUERY")
public class Query {

	QueryType queryType;

	@XStreamAlias("PREFERENCE-SPECIFICATION-FILENAME")
	String preferenceSpecificationFileName;
	
	public String getPreferenceSpecificationFileName() {
		return preferenceSpecificationFileName;
	}

	public void setPreferenceSpecificationFileName(
			String preferenceSpecificationFileName) {
		this.preferenceSpecificationFileName = preferenceSpecificationFileName;
	}

	@XStreamAlias("QUERY-TYPE")
	String parsedQueryType;

	@XStreamImplicit(itemFieldName = "OUTCOME")
	Set<XParsedOutcome> outcomes;

	public Query() {
		outcomes = new HashSet<XParsedOutcome>();
	}

	public QueryType getQueryType() {
		return parsedQueryType.equalsIgnoreCase("DOMINANCE") ? PreferenceQuery.QueryType.DOMINANCE
				: PreferenceQuery.QueryType.CONSISTENCY;
	}

	public Set<XParsedOutcome> getOutcomes() {
		return outcomes;
	}

	public void setOutcomes(Set<XParsedOutcome> outcomes) {
		this.outcomes = outcomes;
	}
	
	public void print() {
		System.out.println(parsedQueryType);
		for(XParsedOutcome o : outcomes) {
			o.print();
		}
		System.out.println();
	}

	public void executeQuery() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, PreferenceReasonerException {
		PreferenceSpecification ps = CPTheoryToSMVTranslator.parsePreferenceSpecification(preferenceSpecificationFileName);
		if(getQueryType().equals(PreferenceQuery.QueryType.DOMINANCE)) {
			Outcome better = null;
			Outcome worse = null;
			String smvFile = new CPTheoryToSMVTranslator().convertToSMV(preferenceSpecificationFileName, REASONING_TASK.DOMINANCE, 0);
			PreferenceReasoner reasoner = new CyclicPreferenceReasoner(smvFile);
			for(XParsedOutcome o : outcomes) {
				if(o.getLabel().equalsIgnoreCase("BETTER")) {
					better = o.getAsOutcome();
				} else if(o.getLabel().equalsIgnoreCase("WORSE")) {
					worse = o.getAsOutcome();
				} else {
					throw new PreferenceReasonerException("Invalid label for outcome provided with dominance query");
				}
			}
			try {
				Constants.LOG_VERIFICATION_SPECS = true;
				Constants.OBTAIN_PROOF_OF_DOMINANCE_BY_DEFAULT = true;
				boolean result = reasoner.dominates(better, worse);
				if(result) {
					OutputUtil.println("Yes.");
				} else {
					OutputUtil.println("No.");
				}
			} catch (PreferenceReasonerException pe) {
				OutputUtil.println("Error evaluating dominance: ");
				pe.printStackTrace();
			} catch (Exception e) {
				OutputUtil.println("Error evaluating dominance: ");
				e.printStackTrace();
			}
		} else if (getQueryType().equals(PreferenceQuery.QueryType.CONSISTENCY)) {
			String smvFile = new CPTheoryToSMVTranslator().convertToSMV(preferenceSpecificationFileName, REASONING_TASK.CONSISTENCY, 0);
			PreferenceReasoner reasoner = new CyclicPreferenceReasoner(smvFile);
			try {
				Constants.LOG_VERIFICATION_SPECS = true;
				boolean result = reasoner.isConsistent();
				if(result) {
					OutputUtil.println("Consistent.");
				} else {
					OutputUtil.println("Inconsistent.");
				}
			} catch (Exception e) {
				OutputUtil.println("Error evaluating consistency: ");
				e.printStackTrace();
			}
		}
	}
}
