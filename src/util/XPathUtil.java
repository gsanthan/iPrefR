package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Uses a DOM model based XPath API implementation to perform XPath query evaluation on XML documents.
 * @author gsanthan
 *
 */
public class XPathUtil {

  /**
   * Evaluates an XPath query specified by xpathExpr against the XML content specified by the Document object passed as input.
   * Used when the result is a list of nodes in the XML document.
   * Returns the result of the XPath query evaluation as a List of Strings corresponding to the text content in the nodes in the computed result.
   * Note: The document object should not be null
   * @param xpathExpr
   * @param document
   * @return Result of the XPath query evaluation as a List of Strings
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws XPathExpressionException
   */
  public static List<String> evaluateListExpr(String xpathExpr, Document document) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

	    XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    XPathExpression expr = xpath.compile(xpathExpr);

	    List<String> list = new ArrayList<String>();
	    Object result = expr.evaluate(document, XPathConstants.NODESET);
	    NodeList nodes = (NodeList) result;
	    for (int i = 0; i < nodes.getLength(); i++) {
	    	list.add(nodes.item(i).getTextContent());
	    }
	    return list;
  }
  
  /**
   * Evaluates an XPath query specified by xpathExpr against the XML content specified by the XML file name or Document object passed as input.
   * Used when the result is a list of nodes in the XML document.
   * If the Document object input is not null, the XPath query is performed against this object; otherwise, a DOM model is built from the XML file name input.
   * Returns the result of the XPath query evaluation as a List of Strings corresponding to the text content in the nodes in the computed result.
   * @param fileName Input XML file name, used for the XPath query evaluation if document object parameter is null
   * @param xpathExpr XPath query to be evaluated 
   * @param document Document object containing the DOM model against which the xpathExpr query has to be evaluated
   * @return List of Strings corresponding to the text content in the nodes in the computed result
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws XPathExpressionException
   */
  public static List<String> evaluateListExpr(String fileName, String xpathExpr, Document document) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

	  	Document doc = null; 
	  
	  	if(document != null) {
	  		doc = document;
	  	} else {
		  	doc = makeDocument(fileName);
	  	}
	  	
	    XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    XPathExpression expr = xpath.compile(xpathExpr);

	    List<String> list = new ArrayList<String>();
	    Object result = expr.evaluate(doc, XPathConstants.NODESET);
	    NodeList nodes = (NodeList) result;
	    for (int i = 0; i < nodes.getLength(); i++) {
	    	list.add(nodes.item(i).getTextContent());
	    }
	    return list;
  }

  /**
   * Evaluates an XPath query specified by xpathExpr against the XML content specified by the Document object passed as input.
   * Returns the result of the XPath query evaluation.
   * Note: The document object should not be null
   * @param xpathExpr XPath query to be evaluated 
   * @param document Document object containing the DOM model against which the xpathExpr query has to be evaluated
   * @return The result of evaluating the xpathExpr 
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws XPathExpressionException
   */
  public static String evaluateExpr(String xpathExpr, Document document) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

	  	XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    XPathExpression expr = xpath.compile(xpathExpr);

	    Object result = expr.evaluate(document, XPathConstants.NODE);
	    Node node = (Node)result;
	    return node.getTextContent();
  }
  
  /**
   * Evaluates an XPath query specified by xpathExpr against the XML content specified by the XML file name or Document object passed as input.
   * If the Document object input is not null, the XPath query is performed against this object; otherwise, a DOM model is built from the XML file name input.
   * Returns the result of the XPath query evaluation
   * @param fileName Input XML file name, used for the XPath query evaluation if document object parameter is null
   * @param xpathExpr XPath query to be evaluated 
   * @param document Document object containing the DOM model against which the xpathExpr query has to be evaluated
   * @return The result of evaluating the xpathExpr 
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws XPathExpressionException
   */
  public static String evaluateExpr(String fileName, String xpathExpr, Document document) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

	  	Document doc = null; 
		  
	  	if(document != null) {
	  		doc = document;
	  	} else {
		  	doc = makeDocument(fileName);
	  	}
	  	
	  	XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    XPathExpression expr = xpath.compile(xpathExpr);

	    Object result = expr.evaluate(doc, XPathConstants.NODE);
	    Node node = (Node)result;
	    return node.getTextContent();
  }
  
  /**
   * Builds a DOM model of the entire input XML file and returns the Document object   
   * @param fileName Input XML file name
   * @return Document object containing the DOM model
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static Document makeDocument(String fileName) throws ParserConfigurationException, SAXException, IOException {
	  
	  	Document doc = null; 
	  	DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    doc = builder.parse(fileName);
	    return doc;
  }
  
  public static NodeList eval(final Document doc, final String pathStr) 
	        throws XPathExpressionException  {
	    final XPath xpath = XPathFactory.newInstance().newXPath();
	    final XPathExpression expr = xpath.compile(pathStr);
	    return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
	}

  public static List<ListMultimap<String, String>> nodeListOfNodeListsAsListOfMultimaps(final NodeList nodes) {
	  	
	    List<ListMultimap<String, String>> out = new ArrayList<ListMultimap<String, String>>(); 
	    int len = (nodes != null) ? nodes.getLength() : 0;
	    for (int i = 0; i < len; i++) {
	    	ListMultimap<String, String> multimap = ArrayListMultimap.create();
	        NodeList children = nodes.item(i).getChildNodes();
	        for (int j = 0; j < children.getLength(); j++) {
	            Node child = children.item(j);
                multimap.put(child.getNodeName(), child.getTextContent());
	        }
	        out.add(multimap);
	    }
	    return out;
	}
  
  public static ListMultimap<String, String> nodeListAsMultimap(final NodeList nodeList) {
	  	
    	ListMultimap<String, String> multimap = ArrayListMultimap.create();
        for (int j = 0; j < nodeList.getLength(); j++) {
        	Node child = nodeList.item(j);
        	multimap.put(child.getNodeName(), child.getTextContent());
        }
	    return multimap;
	}
}