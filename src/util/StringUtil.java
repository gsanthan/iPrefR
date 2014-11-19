package util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	public static String commaSeparated(Collection<String> strings) {
		String formattedString = new String();
		for(String v : strings) {
			if(!formattedString.isEmpty()) {
				formattedString = formattedString + ",";
			}
			formattedString = formattedString + v;
		}
		return formattedString;
	}
	
	/**
	 * Returns a comma separated list of Strings in a List, adding a prefix to each string
	 * @param strings
	 * @return Comma separated List of Strings
	 */
	public static String commaSeparatedPrefixed(Collection<String> strings, String prefix) {
		String formattedString = new String();
		for(String v : strings) {
			if(!formattedString.isEmpty()) {
				formattedString = formattedString + ",";
			}
			formattedString = formattedString + prefix + v;
		}
		return formattedString;
	}
	
	/**
	 * Returns a comma separated list of Strings in a List, adding a suffix to each string
	 * @param strings
	 * @return Comma separated List of Strings
	 */
	public static String commaSeparatedSuffixed(Collection<String> strings, String suffix) {
		String formattedString = new String();
		for(String v : strings) {
			if(!formattedString.isEmpty()) {
				formattedString = formattedString + ",";
			}
			formattedString = formattedString + v + suffix;
		}
		return formattedString;
	}
	/**
	 * Returns a comma separated list of each item's toString() in the Collection
	 * @param <K>
	 * @param strings
	 * @return Comma separated List of Strings
	 */
	public static <K> String commaSeparatedForCollectionAsString(Collection<K> objects) {
		String formattedString = new String();
		for(K v : objects) {
			if(!formattedString.isEmpty()) {
				formattedString = formattedString + ",";
			}
			formattedString = formattedString + v.toString();
		}
		return formattedString;
	}

	public static Set<String> parseToStringSet(String domain) {
		Set<String> setOfStrings = new HashSet<String>();
		domain = domain.trim();
		if(domain.startsWith("{") && (domain.endsWith("}"))) {
			domain = domain.substring(1, domain.length()-1);
			setOfStrings = new HashSet<String>(Arrays.asList(domain.split(",")));
		} else if(domain.startsWith("{") && (domain.endsWith("};"))) {
			domain = domain.substring(1, domain.length()-2);
			setOfStrings = new HashSet<String>(Arrays.asList(domain.split(",")));
		} else if(domain.equalsIgnoreCase("boolean")){
			setOfStrings.add("0");
			setOfStrings.add("1");
		}
		return setOfStrings;
	}
}
