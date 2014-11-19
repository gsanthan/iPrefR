package model;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("PREFERENCE-VARIABLE")
public class PreferenceVariable {
	
	@XStreamAlias("VARIABLE-NAME")
	String variableName;
	
	@XStreamImplicit(itemFieldName="DOMAIN-VALUE")
	Set<String> domainValues;
	
	public PreferenceVariable(String name, Set<String> domain) {
		variableName = name;
		domainValues = domain;
	}
	
	/*public PreferenceVariable(String variableName, boolean initializeWithBinary01Domain) {
		this.variableName = variableName;
		domainValues = new HashSet<String>();
		if(initializeWithBinary01Domain) {
			domainValues.add("0");
			domainValues.add("1");
		} else {
			OutputUtil.println("Domain Values not specified for variable "+variableName+"; used an empty domain");
		}
	}*/
	
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	public Set<String> getDomainValues() {
		return domainValues;
	}
	public void setDomainValues(Set<String> domainValues) {
		this.domainValues = domainValues;
	}
	
	public void makeValid() {
		if(domainValues == null) {
			domainValues = new HashSet<String>();
		} 
	}

	@Override
	public String toString() {
		return variableName;
	}
}
