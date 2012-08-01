package translate;


/**
 * Factory class that provides (presently) CINetToSMVTranslator or TCPNetToSMVTranslator implementations 
 * @author gsanthan
 *
 */
public class PreferenceInputTranslatorFactory {

	/**
	 * Returns a new PreferenceInputTranslator implementation based on the PreferenceInputType
	 * 
	 * @param type Preference input language type
	 * @return A PreferenceInputTranslator implementation
	 */
	public static PreferenceInputTranslator createTranslator(PreferenceInputType type) {
		if(type == PreferenceInputType.CInet) {
			return (PreferenceInputTranslator)new CINetToSMVTranslator();
		} else if(type == PreferenceInputType.TCPnet) {
			return (PreferenceInputTranslator)new TCPNetToSMVTranslator();
		} else {
			throw new RuntimeException("Preference Input type not supported!");
		}
	}
}
