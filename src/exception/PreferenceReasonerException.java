package exception;

public class PreferenceReasonerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String exceptionCode;
	
	public PreferenceReasonerException(String string) {
		// TODO Auto-generated constructor stub
		exceptionCode = string;
	}

	public String getExceptionCode() {
		return exceptionCode;
	}

	public void setExceptionCode(String exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
