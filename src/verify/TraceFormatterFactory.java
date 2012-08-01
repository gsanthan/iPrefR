package verify;

import util.Constants;

/**
 * Factory class that provides CadenceSMVTraceFormatter or NuSMVTraceFormatter (for now) implementations 
 * @author gsanthan
 *
 */
public class TraceFormatterFactory {

	/**
	 * Returns a new TraceFormatter implementation based on the current model checker  
	 * @return A TraceFormatter implementation
	 */
	public static TraceFormatter createTraceFormatter() {
		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
			return new CadenceSMVTraceFormatter();
		}
		return new NuSMVTraceFormatter();
	}
}
