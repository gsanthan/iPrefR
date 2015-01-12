package model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.OutputUtil;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import exception.PreferenceReasonerException;

@XStreamAlias("OUTCOME")
public class Outcome {
	
	@XStreamAlias("LABEL")
	String label;

	@XStreamImplicit(itemFieldName="ASSIGNMENT")
	Set<Assignment> assignments;

	public Outcome(Collection<PreferenceVariable> variables) {
		assignments = new HashSet<Assignment>();
		for(PreferenceVariable variable : variables) {
			assignments.add(new Assignment(variable.getVariableName(), null));
		}
	}
	
	public Outcome(Map<String, String> val) throws PreferenceReasonerException {
		if(val != null) {
			assignments = new HashSet<Assignment>();
			for(String varName : val.keySet()) {
				assignments.add(new Assignment(varName, val.get(varName)));
			}
		} else {
			throw new PreferenceReasonerException("Invalid null argument to construct an outcome.");
		}
	}
	
	public Outcome(Set<String> variableNames) {
		for(String vn : variableNames) {
			for(Assignment a : assignments) { 
				if(a.getVariableName().equals(vn)) {
					a.setVariableValuation("1");
				} else {
					a.setVariableValuation("0");
				}
			}
		}
	}
	
	public String getValuationOfVariable(String varName) {
		return getOutcomeAsValuationMap().get(varName);
	}
	
	public void makeOutcome(String[] variableNames) {
		for(String vn : variableNames) {
			for(Assignment a : assignments) { 
				if(a.getVariableName().equals(vn)) {
					a.setVariableValuation("1");
				} else {
					a.setVariableValuation("0");
				}
			}
		}
	}
	
	public void makeOutcome(Set<String> stringVariableAssignments) throws PreferenceReasonerException {
		for(String svAssignment : stringVariableAssignments) {
			String varName = svAssignment.trim().substring(0,svAssignment.indexOf("=")-1);
			
			if(!isVariableAssigned(varName)) {
				throw new PreferenceReasonerException("Error in making outcome from " + stringVariableAssignments + " - variable "+varName+ " not in the outcome's list of variables");
			}
			String varValuation = svAssignment.substring(svAssignment.indexOf("=") + 2);
			assignVarToVal(varName, varValuation);
		}
	}
	
	public void assignVarToVal(String varName, String varValuation) {
		for(Assignment a : assignments) {
			if(a.getVariableName().equalsIgnoreCase(varName)) {
				a.setVariableValuation(varValuation);
			}
		}
	}

	public void copyMissingValuationsFrom(Outcome o) {
		for(Assignment ao : o.getAssignments()) {
			for(Assignment a : assignments) {
				if(a.getVariableName().equalsIgnoreCase(ao.variableName)) {
					if(a.getVariableValuation() == null) {
						a.setVariableValuation(ao.variableValuation);	
					}
				}
			}
		}
	}

	
	private boolean isVariableAssigned(String varName) {
		for(Assignment a : assignments) {
			if(a.getVariableName().equalsIgnoreCase(varName)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, String> getOutcomeAsValuationMap() {
		Map<String, String> variableValuations = new HashMap<String, String>();
		for(Assignment a : assignments) {
			variableValuations.put(a.getVariableName(), a.getVariableValuation());
		}
		return variableValuations;
	}
	
	public Set<String> getOutcomeAsSetOfStringAssignments() {
		Set<String> stringAssignments = new HashSet<String>();
		for(Assignment a : assignments) {
			String val = a.getVariableValuation();
			if(val != null) {
				stringAssignments.add(a.getVariableName() + " = " + val);
			}
		}
		return stringAssignments;
	}
	
	public Set<String> getOutcomeAsSetOfPositiveLiterals() {
		Set<String> positiveLiterals = new HashSet<String>();
		for(Assignment a : assignments) {
			String val = a.getVariableValuation();
			if(val != null && !val.equalsIgnoreCase("0") && !val.equalsIgnoreCase("false")) {
				positiveLiterals.add(a.getVariableName());
			}
		}
		return positiveLiterals;
	}
	
	public boolean containsPositiveLiteral(String variable) {
		
		Map<String, String> variableValuations = getOutcomeAsValuationMap();
		if(variableValuations.get(variable) != null && !variableValuations.get(variable).equalsIgnoreCase("0") && !variableValuations.get(variable).equalsIgnoreCase("false")) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Outcome) { 
			if(this.getOutcomeAsValuationMap().equals(((Outcome) obj).getOutcomeAsValuationMap())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean validateOutcome() throws PreferenceReasonerException {
		Map<String, String> variableValuations = getOutcomeAsValuationMap();
		Set<String> variableNames = new HashSet<String>(Arrays.asList(WorkingPreferenceModel.getPrefMetaData().getNamesOfVariables()));
		boolean valid = false;
		if(variableNames.equals(variableValuations.keySet())) {
			valid = true;
		} else {
			OutputUtil.println("Variable names differ from those in preference specification, or not all variable valuations have been specified");
			throw new PreferenceReasonerException("Variable names differ from those in preference specification, or not all variable valuations have been specified");
		}
		Set<PreferenceVariable> variables = WorkingPreferenceModel.getPrefMetaData().getVariables();
		for(PreferenceVariable var : variables) {
			String valForVar = variableValuations.get(var.getVariableName());
			if(!var.getDomainValues().contains(valForVar)) {
				valid = false;
				OutputUtil.println("Variable valuation provided is outside domain of variable - "+var.getVariableName()+":"+valForVar+" (Domain: "+var.getDomainValues()+")");
				throw new PreferenceReasonerException("Variable valuation provided is outside domain of variable - "+var.getVariableName()+":"+valForVar+" (Domain: "+var.getDomainValues()+")");
			}
		}
		return valid;
	}
	
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
