package model;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("PREFERENCE-SPECIFICATION")
public class PreferenceSpecification {

	@XStreamAlias("FILE-NAME")
	String fileName;
	
	@XStreamImplicit(itemFieldName="PREFERENCE-VARIABLE")
	Set<PreferenceVariable> variables;
	
	@XStreamImplicit(itemFieldName="PREFERENCE-STATEMENT")
	Set<PreferenceStatement> statements;
	
	public PreferenceSpecification() {
		fileName = "";
		variables = new HashSet<PreferenceVariable>();
		statements = new HashSet<PreferenceStatement>();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Set<PreferenceVariable> getVariables() {
		return variables;
	}

	public void setVariables(Set<PreferenceVariable> variables) {
		this.variables = variables;
	}

	public Set<PreferenceStatement> getStatements() {
		return statements;
	}

	public void setStatements(Set<PreferenceStatement> statements) {
		this.statements = statements;
	}

	public Set<String> getVariableNames() {
		Set<String> names = new HashSet<String>();
		for(PreferenceVariable var : variables) {
			names.add(var.getVariableName());
		}
		return names;
	}
	
	public ListMultimap<String, String> getVariablesWithDomainsAsMultimap() {
		ListMultimap<String, String> variablesWithDomains = ArrayListMultimap.create();
		for(PreferenceVariable var : variables) {
			variablesWithDomains.putAll(var.getVariableName(),var.getDomainValues());
		}
		return variablesWithDomains;
	}
	
	public PreferenceVariable getPreferenceVariable(String variableName) {
		for(PreferenceVariable var : variables) {
			if(variableName.equals(var.getVariableName())) {
				return var;
			}
		}
		throw new RuntimeException("Variable " + variableName + " not found.");
	}
	
	public void makeValid() {
		if(variables == null) {
			variables = new HashSet<PreferenceVariable>();
		} else {
			for(PreferenceVariable var : variables) {
				var.makeValid();
			}
		}
		
		if(statements == null) {
			statements = new HashSet<PreferenceStatement>();
		} else {
			for(PreferenceStatement stmt : statements) {
				stmt.makeValid();
			}
		}
	}
}
