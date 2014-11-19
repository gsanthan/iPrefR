package translate;


/**
 * Factory class that provides (presently) CINetToSMVTranslator or TCPNetToSMVTranslator implementations 
 * @author gsanthan
 *
 */
public class PreferenceInputTranslatorFactory {

	/**
	 * Returns a new PreferenceInputTranslator implementation based on the PreferenceLanguage
	 * 
	 * @param type Preference input language type
	 * @return A PreferenceInputTranslator implementation
	 */
	public static PreferenceInputTranslator createTranslator(PreferenceLanguage type) {
		if(type == PreferenceLanguage.CInet) {
			return (PreferenceInputTranslator)new CINetToSMVTranslator();
		} else if(type == PreferenceLanguage.TCPnet) {
			return (PreferenceInputTranslator)new TCPNetToSMVTranslator();
		} else if(type == PreferenceLanguage.CPTheory) {
			return (PreferenceInputTranslator)new CPTheoryToSMVTranslator();
		} else {
			throw new RuntimeException("Preference Input type not supported!");
		}
	}
}
