package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import exception.PreferenceReasonerException;

@XStreamAlias("OUTCOME")
public class XParsedOutcome {
	
	@XStreamAlias("LABEL")
	String label;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set<Assignment> getAssignments() {
		return assignments;
	}

	public void setAssignments(Set<Assignment> assignments) {
		this.assignments = assignments;
	}

	@XStreamImplicit(itemFieldName="ASSIGNMENT")
	Set<Assignment> assignments;

	public void print() {
		System.out.println(label);
		for(Assignment a : assignments) {
			a.print();
		}
	}
	
	public Outcome getAsOutcome() throws PreferenceReasonerException {
		Map<String, String> varAssignments = new HashMap<String, String>(); 
		for(Assignment a : assignments) {
			varAssignments.put(a.getVariableName(), a.getVariableValuation());
		}
		return new Outcome(varAssignments);
	}
}
