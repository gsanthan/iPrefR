package model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ASSIGNMENT")
public class Assignment {

		@XStreamAlias("PREFERENCE-VARIABLE")
		String variableName;
		
		@XStreamAlias("VALUATION")
		String variableValuation;
		
		public Assignment() {
			// TODO Auto-generated constructor stub
		}

		public Assignment(String var, String val) {
			variableName = var;
			variableValuation = val;
		}
		
		public String getVariableName() {
			return variableName;
		}

		public void setVariableName(String variableName) {
			this.variableName = variableName;
		}

		public String getVariableValuation() {
			return variableValuation;
		}

		public void setVariableValuation(String variableValuation) {
			this.variableValuation = variableValuation;
		}

		public void print() {
			System.out.println(variableName+" = "+variableValuation);
		}
		
}
