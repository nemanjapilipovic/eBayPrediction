package data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EbayDriverFindingAPI extends EbayDriver{

	public final static String EBAY_APP_ID = "Facultyo-809c-42bd-b1ab-8b4625fdea4a";
	public final static String EBAY_FINDING_SERVICE_URI = "http://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME="
			+ "{operation}&SERVICE-VERSION={version}&SECURITY-APPNAME="
			+ "{applicationId}&GLOBAL-ID={globalId}&categoryId={categoryId}"
			+ "&paginationInput.entriesPerPage={maxresults}";
	public static final String SERVICE_VERSION = "1.0.0";
	public static final String OPERATION_NAME = "findItemsByCategory";
	public static final String GLOBAL_ID = "EBAY-US";
	public final static int MAX_RESULTS = 100;

	private LinkedList<String> listOfIds = new LinkedList<String>();

	@Override
	public String createAddress(String tag) {

		// substitute token
		String address = EbayDriverFindingAPI.EBAY_FINDING_SERVICE_URI;
		address = address.replace("{version}",
				EbayDriverFindingAPI.SERVICE_VERSION);
		address = address.replace("{operation}",
				EbayDriverFindingAPI.OPERATION_NAME);
		address = address.replace("{globalId}", EbayDriverFindingAPI.GLOBAL_ID);
		address = address.replace("{applicationId}",
				EbayDriverFindingAPI.EBAY_APP_ID);
		address = address.replace("{categoryId}", tag);
		address = address.replace("{maxresults}", "" + MAX_RESULTS);

		return address;

	}

	@Override
	public String processResponse(String response) throws Exception {

		XPath xpath = XPathFactory.newInstance().newXPath();
		InputStream is = new ByteArrayInputStream(response.getBytes("UTF-8"));
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();

		Document doc = builder.parse(is);
		XPathExpression ackExpression = xpath
				.compile("//findItemsByCategoryResponse/ack");
		XPathExpression itemExpression = xpath
				.compile("//findItemsByCategoryResponse/searchResult/item");

		String ackToken = (String) ackExpression.evaluate(doc,
				XPathConstants.STRING);
		if (!ackToken.equals("Success")) {
			throw new Exception(" service returned an error");
		}

		NodeList nodes = (NodeList) itemExpression.evaluate(doc,
				XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); i++) {

			Node node = nodes.item(i);

			String itemId = (String) xpath.evaluate("itemId", node,
					XPathConstants.STRING);

			listOfIds.add(itemId);

		}

		String ids = listOfIds.get(0);
		for (int i = 1; i < listOfIds.size(); i++) {
			ids = ids + "," + listOfIds.get(i);
		}

		is.close();

		return ids;

	}

	@Override
	public String getData() throws Exception {
		EbayDriverFindingAPI driver = new EbayDriverFindingAPI();
		String tag = "111422";
		return driver.run(java.net.URLEncoder.encode(tag, "UTF-8"));

	}
	
	public static void main(String[] args) throws Exception {
		EbayDriverFindingAPI driver = new EbayDriverFindingAPI();
		String tag = "111422";
		System.out.println(driver.run(java.net.URLEncoder.encode(tag, "UTF-8")));
	}

}