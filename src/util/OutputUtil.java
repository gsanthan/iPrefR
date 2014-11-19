package util;

public class OutputUtil {
	
	public static void print(Object object) {
		if(Constants.LOG_OUTPUT) {
			System.out.println(object);
		}
	}
	
	public static void println(Object object) {
		if(Constants.LOG_OUTPUT) {
			System.out.println(object);
		}
	}
	
	public static void println() {
		if(Constants.LOG_OUTPUT) {
			System.out.println();
		}
	}
}
