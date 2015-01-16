package reasoner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

import model.Outcome;
import model.Query;
import model.QueryResult;
import model.QueryType;

import com.thoughtworks.xstream.XStream;

import exception.PreferenceReasonerException;

public class PreferenceQueryParser {

	public static Query parseQuery(String xmlFile) throws PreferenceReasonerException, FileNotFoundException {
		try {
			XStream xStream = new XStream();
			xStream.autodetectAnnotations(true);
			FileReader reader = new FileReader(xmlFile);
			xStream.toXML(new Query(QueryType.DOMINANCE,"",new HashSet<Outcome>())); // Don't know why, but fromXML throws Exception if we don't do toXML first!
			Query q = (Query) xStream.fromXML(reader);
			q.setQueryFileName(xmlFile);
			return q; 
		} catch(Exception e) {
			e.printStackTrace();
			throw new PreferenceReasonerException("Error parsing query file.");
		}
	}
	
	public static void saveQueryToFile(Query query) throws PreferenceReasonerException {
		try {
			XStream xStream = new XStream();
			xStream.autodetectAnnotations(true);
			FileWriter writer = new FileWriter(query.getQueryFileName());
			xStream.toXML(query, writer);
		} catch(Exception e) {
			e.printStackTrace();
			throw new PreferenceReasonerException("Error saving query object to file.");
		}
	}
	
	public static void saveQueryResultToFile(QueryResult result) throws PreferenceReasonerException, FileNotFoundException {
		try {
			XStream xStream = new XStream();
			xStream.autodetectAnnotations(true);
			if(result.getQueryFileName() != null) {
				FileWriter writer = new FileWriter(result.getQueryFileName()+"-result.xml");
				xStream.toXML(result, writer);
			} else {
				throw new PreferenceReasonerException("File name for saving QueryResult not specified");
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new PreferenceReasonerException("Error saving query object to file.");
		}
	}
}
