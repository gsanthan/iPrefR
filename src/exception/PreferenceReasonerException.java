package exception;

public class PreferenceReasonerException extends Exception {

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
