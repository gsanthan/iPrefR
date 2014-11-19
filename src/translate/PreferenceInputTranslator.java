package translate;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import test.CPTheoryDominanceExperimentDriver.REASONING_TASK;

/**
 * Translates preference input of the type (T)CP-nets with unconditional relative importance and CI-nets into models suitable for model checking by (presently) CadenceSMV, NuSMV.
 * @author gsanthan
 *
 */
public interface PreferenceInputTranslator {

	/**
	 * Parses preference input file and saves a file fit for model checking by a model checker.
	 * Presently supported preference input : (T)CP-nets with unconditional relative importance and CI-nets.
	 * Presently supported model checkers : CadenceSMV, NuSMV.
   	 * Translation is based on the theory from the paper "Dominance Testing via Model Checking" Santhanam et al. AAAI 2010 which contains the precise translation rules.
	 * Also generates random 'sampleSize' specs, i.e., dominance test cases and adds to the SMV file.
	 * 
	 * Note: Output file syntax is switched according to value set in the constant Constants.CURRENT_MODEL_CHECKER
	 * 
	 * @see translate.CINetToSMVTranslator
	 * @see translate.TCPNetToSMVTranslator 
	 * 
	 * @param preferenceInputFile
	 * @param sampleSize
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public String convertToSMV(String preferenceInputFile, REASONING_TASK task, int sampleSize) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException;

}