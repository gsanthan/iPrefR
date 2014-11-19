package model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("PREFERENCE-STATEMENT")
public class PreferenceStatement {

	@XStreamAlias("STATEMENT-ID")
	String statementId;
	
	@XStreamAlias("PREFERENCE-VARIABLE")
	String variableName;
	
	@XStreamImplicit(itemFieldName="CONDITION")
	List<String> parentAssignments;
	
	@XStreamImplicit(itemFieldName="PREFERENCE")
	List<String> intravarPreferences;
	
	@XStreamImplicit(itemFieldName="REGARDLESS-OF")
	List<String> lessImpVariables;
	
	public PreferenceStatement() {
		parentAssignments = new ArrayList<String>();
		intravarPreferences = new ArrayList<String>();
		lessImpVariables = new ArrayList<String>();
	}
	
	public PreferenceStatement(String statementId, String variableName,
			List<String> parentAssignments,
			List<String> intravarPreferences, List<String> lessImpVariables) {
		this.statementId = statementId;
		this.variableName = variableName;
		this.parentAssignments = parentAssignments;
		this.intravarPreferences = intravarPreferences;
		this.lessImpVariables = lessImpVariables;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public List<String> getParentAssignments() {
		return parentAssignments;
	}

	public void setParentAssignments(List<String> parentAssignments) {
		this.parentAssignments = parentAssignments;
	}

	public List<String> getIntravarPreferences() {
		return intravarPreferences;
	}

	public void setIntravarPreferences(List<String> intravarPreferences) {
		this.intravarPreferences = intravarPreferences;
	}

	public List<String> getLessImpVariables() {
		return lessImpVariables;
	}

	public void setLessImpVariables(List<String> lessImpVariables) {
		this.lessImpVariables = lessImpVariables;
	}

	public String getStatementId() {
		return statementId;
	}

	public void setStatementId(String statementId) {
		this.statementId = statementId;
	}
	
	public void makeValid() {
		if(lessImpVariables == null) {
			lessImpVariables = new ArrayList<String>();
		} else if(intravarPreferences == null) {
			intravarPreferences = new ArrayList<String>();
		} else if(parentAssignments == null) {
			parentAssignments = new ArrayList<String>();
		}
	}
}
