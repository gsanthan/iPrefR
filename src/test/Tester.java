package test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import translate.CPTheoryToSMVTranslator;
import model.PreferenceQuery.QueryType;
import model.PreferenceSpecification;
import model.Query;

import com.thoughtworks.xstream.XStream;

public class Tester {

	public static void main(String[] args) throws FileNotFoundException {
//		Query q = parsePreferenceQuery("C:\\Users\\gsanthan.IASTATE\\Copy\\AAAI2014\\output\\query.xml");
//		q.print();
		String preferenceSpecificationFileName = "C:\\Users\\gsanthan.IASTATE\\Copy\\AAAI2014\\output\\sample1.xml";
		PreferenceSpecification ps = CPTheoryToSMVTranslator.parsePreferenceSpecification(preferenceSpecificationFileName);
		System.out.println(ps.getVariablesWithDomainsAsMultimap());
	}
	
	public static Query parsePreferenceQuery(String xmlFile)
			throws FileNotFoundException {
		XStream xStream = new XStream();
		xStream.autodetectAnnotations(true);
		FileReader reader = new FileReader(xmlFile);
		xStream.toXML(new Query(null,null,null)); // Don't know why, but fromXML throws Exception if we don't do toXML first!
		Query q = (Query) xStream.fromXML(reader);
		return q;
	}
	
}
