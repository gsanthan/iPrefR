package util;

public class ExceptionUtil {

	public static String getStackTraceAsString(Throwable cause) {
		String error = "";
		StackTraceElement elements[] = cause.getStackTrace();
		for (int i = 0, n = elements.length; i < n; i++) {
			error += elements[i].getFileName() + ":"
					+ elements[i].getLineNumber() + ">> "
					+ elements[i].getMethodName() + "()";
		}
		return error;
	}
}
