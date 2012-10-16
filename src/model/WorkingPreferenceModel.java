package model;

/**
 * Stores PreferenceMetaData about the current preference specification used by the PreferenceReasoner.
 * 
 * @author gsanthan
 *
 */
public class WorkingPreferenceModel {
	
	/**
	 * PreferenceMetaData about the current preference specification used by the PreferenceReasoner.
	 */
	public static PreferenceMetaData prefMetaData = new PreferenceMetaData();
	
	public static PreferenceMetaData prefMetaDataReverse = new PreferenceMetaData();

	public static PreferenceMetaData getPrefMetaDataReverse() {
		return prefMetaDataReverse;
	}

	public static void setPrefMetaDataReverse(PreferenceMetaData prefMetaDataReverse) {
		WorkingPreferenceModel.prefMetaDataReverse = prefMetaDataReverse;
	}

	public static PreferenceMetaData getPrefMetaData() {
		return prefMetaData;
	}

	public static void setPrefMetaData(PreferenceMetaData prefMetaData) {
		WorkingPreferenceModel.prefMetaData = prefMetaData;
	}
}
