package model;

public abstract class PreferenceQuery {

		public enum QueryType {DOMINANCE,CONSISTENCY}; 
		PreferenceSpecification prefSpec;
		PreferenceMetaData prefMetaData;
		String xmlQueryFileName;
		
		public PreferenceQuery(PreferenceSpecification prefSpec, String xmlQueryFileName) {
			this.prefSpec = prefSpec;
			this.xmlQueryFileName = xmlQueryFileName;
			parseXMLQuery();
		}
		
		public abstract void parseXMLQuery();
		
		public String getXmlQueryFileName() {
			return xmlQueryFileName;
		}
		public void setXmlQueryFileName(String xmlQueryFileName) {
			this.xmlQueryFileName = xmlQueryFileName;
		}
		public PreferenceSpecification getPrefSpec() {
			return prefSpec;
		}
		public void setPrefSpec(PreferenceSpecification prefSpec) {
			this.prefSpec = prefSpec;
		}
		public PreferenceMetaData getPrefMetaData() {
			return prefMetaData;
		}
		public void setPrefMetaData(PreferenceMetaData prefMetaData) {
			this.prefMetaData = prefMetaData;
		}
		
}
