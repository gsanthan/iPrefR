package util;

import java.util.List;

/**
 * Provides basic String utility methods
 * @author gsanthan
 *
 */
public class StringUtil {

	/**
	 * Returns the input string expanded to the given length padded with spaces on the left (before the string).
	 * Useful for formatting strings to fit fixed width columns with right justification.  
	 * @param string 
	 * @param length
	 * @return Formatted String
	 */
	public static String padWithSpace(String string, int length) {

		if(string.length()>length) {
			return string;
		}
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < length; i++) {
			b.append(" ");
		}
		return b.substring(0, length - (string+"").length()) + string;
	}
	
	/**
	 * Returns the input string expanded to the given length padded with spaces on the right (after the string).
	 * Useful for formatting strings to fit fixed width columns with left justification. 
	 * @param string
	 * @param length
	 * @return Formatted string
	 */
	public static String padWithRightSpace(String string, int length) {

		if(string.length()>length) {
			return string;
		}
		StringBuffer b = new StringBuffer(string);
		for (int i = string.length(); i < length; i++) {
			b.append(" ");
		}
		return b.toString();
	}
	
	/**
	 * Returns a comma separated list of Strings in a List
	 * @param strings
	 * @return Comma separated List of Strings
	 */
	public static String commaSeparated(List<String> strings) {
		String formattedString = new String();
		for(String v : strings) {
			if(!formattedString.isEmpty()) {
				formattedString = formattedString + ",";
			}
			formattedString = formattedString + v;
		}
		return formattedString;
	}
}
