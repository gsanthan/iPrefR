package reasoner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

import model.Outcome;
import model.PreferenceQuery.QueryType;
import model.Query;

import com.thoughtworks.xstream.XStream;

import exception.PreferenceReasonerException;

public class PreferenceQueryParser {

	public static Query parsePreferenceQuery(String xmlFile) throws PreferenceReasonerException, FileNotFoundException {
		try {
			XStream xStream = new XStream();
			xStream.autodetectAnnotations(true);
			FileReader reader = new FileReader(xmlFile);
			xStream.toXML(new Query(QueryType.DOMINANCE,"",new HashSet<Outcome>())); // Don't know why, but fromXML throws Exception if we don't do toXML first!
			Query q = (Query) xStream.fromXML(reader);
			return q; 
		} catch(Exception e) {
			throw new PreferenceReasonerException("Error parsing query file.");
		}
	}
}
