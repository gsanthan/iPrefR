package model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.OutputUtil;
import exception.PreferenceReasonerException;

public class Outcome {

	Map<String, String> variableValuations;
	
	public Outcome(Collection<PreferenceVariable> variables) {
		variableValuations = new HashMap<String, String>();
		for(PreferenceVariable variable : variables) {
			variableValuations.put(variable.getVariableName(), null);
		}
	}
	
	public Outcome(Map<String, String> val) throws PreferenceReasonerException {
		if(val != null) {
			this.variableValuations = val;
		} else {
			throw new PreferenceReasonerException("Invalid null argument to construct an outcome.");
		}
	}
	
	public Outcome(Set<String> variableNames) {
		for(String vn : variableNames) {
			for(String variable : variableValuations.keySet())
			if(variable.equals(vn)) {
				variableValuations.put(variable, "1");
			} else {
				variableValuations.put(variable, "0");
			}
		}
	}
	
	
	public Map<String, String> getOutcomeAsValuationMap() {
		return variableValuations;
	}
	
	public void makeOutcome(String[] variableNames) {
		for(String vn : variableNames) {
			for(String variable : variableValuations.keySet())
			if(variable.equals(vn)) {
				variableValuations.put(variable, "1");
			} else {
				variableValuations.put(variable, "0");
			}
		}
	}
	
	public void makeOutcome(Set<String> variableAssignments) {
		for(String assignment : variableAssignments) {
			String varName = assignment.trim().substring(0,assignment.indexOf("=")-1);
			if(!variableValuations.containsKey(varName)) {
				throw new RuntimeException("Error in making outcome from " + variableAssignments + " - variable "+varName+ " not in the outcome's list of variables");
			}
			String varValuation = assignment.substring(assignment.indexOf("=") + 2);
			variableValuations.put(varName, varValuation);
		}
	}
	
	public Set<String> getOutcomeAsSetOfStringAssignments() {
		Set<String> stringAssignments = new HashSet<String>();
		for(String key : variableValuations.keySet()) {
			String val = variableValuations.get(key);
			if(val != null) {
				stringAssignments.add(key + " = " + val);
			}
		}
		return stringAssignments;
	}
	
	public Set<String> getOutcomeAsSetOfPositiveLiterals() {
		Set<String> positiveLiterals = new HashSet<String>();
		for(String key : variableValuations.keySet()) {
			if(variableValuations.get(key) != null && !variableValuations.get(key).equalsIgnoreCase("0") && !variableValuations.get(key).equalsIgnoreCase("false")) {
				positiveLiterals.add(key);
			}
		}
		return positiveLiterals;
	}
	
	public boolean containsPositiveLiteral(String variable) {
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
}
