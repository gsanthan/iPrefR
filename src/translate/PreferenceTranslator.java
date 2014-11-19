package translate;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.PreferenceVariable;
import util.FileUtil;
import util.StringUtil;

public class PreferenceTranslator {

	public static List<PreferenceVariable> getVariablesFromSMVModel(String smvFile)	throws IOException {
		List<PreferenceVariable> variables = new ArrayList<PreferenceVariable>();
		BufferedReader r = FileUtil.openFileForRead(smvFile);
		
		try{
		String nextLine = null;
		
		do {
			nextLine = r.readLine();
		} while (nextLine != null && !nextLine.trim().equalsIgnoreCase("VAR"));
		
		if(nextLine == null) {
			//VAR declaration not present - Error in SMV file
			throw new RuntimeException("No VARS declaration line in SMV file");
		}
		
		do {
			nextLine = r.readLine();
			if(nextLine != null) {
				nextLine = nextLine.trim();
				if(nextLine.contains(":")) {
					String name = nextLine.substring(0,nextLine.indexOf(":")-1).trim();
					String domain = nextLine.substring(nextLine.indexOf(":")+1,nextLine.length()).trim();
					Set<String> varDomain = StringUtil.parseToStringSet(domain);
					if(! name.contains("ch") && ! name.contains("used") && ! name.endsWith("_0") && ! name.equalsIgnoreCase("start")) {
						PreferenceVariable prefVar = new PreferenceVariable(name, varDomain);
						variables.add(prefVar);
					}
				}
			}
		} while (nextLine != null && !nextLine.trim().equalsIgnoreCase("ASSIGN"));
		} 
		finally { 
			r.close();
		}
		return variables;
	}

	public PreferenceTranslator() {
		super();
	}

}