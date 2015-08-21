package data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EbayDriverShoppingAPI extends EbayDriver {

	public final static String EBAY_FINDING_SERVICE_URI = "http://open.api.ebay.com/Shopping?callname="
			+ "{callname}&appid={appid}"
			+ "&version={version}"
			+ "&siteid={siteid}"
			+ "&ItemID={itemid}"
			+ "&IncludeSelector=Description,ItemSpecifics";
	public static final String CALLNAME = "GetSingleItem";
	public static final String APPID = "Facultyo-0417-4b42-9fa4-0d78a268f58a";
	public final static String VERSION = "515";
	public final static String SITEID = "0";
	public static String stringForData = null;

	@Override
	public String createAddress(String tag) {

		// substitute token
		String address = EbayDriverShoppingAPI.EBAY_FINDING_SERVICE_URI;
		address = address.replace("{callname}", EbayDriverShoppingAPI.CALLNAME);
		address = address.replace("{appid}", EbayDriverShoppingAPI.APPID);
		address = address.replace("{version}", EbayDriverShoppingAPI.VERSION);
		address = address.replace("{siteID}", EbayDriverShoppingAPI.SITEID);
		address = address.replace("{itemid}", tag);

		return address;

	}

	@Override
	public String processResponse(String response) throws Exception {
		String productFamily = null;
		String screenSize = null;
		String processorType = null;
		String operatingSystem = null;
		double productPrice = 0;
		String memory = null;
		String hardDriveCapacity = null;
		String processorSpeed = null;
		String price = null;

		XPath xpath = XPathFactory.newInstance().newXPath();
		InputStream is = new ByteArrayInputStream(response.getBytes("UTF-8"));
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();

		Document doc = builder.parse(is);
		XPathExpression ackExpression = xpath
				.compile("//GetSingleItemResponse/Ack");
		XPathExpression itemExpression = xpath
				.compile("//GetSingleItemResponse/Item");
		XPathExpression itemSpecifics = xpath
				.compile("//GetSingleItemResponse/Item/ItemSpecifics/NameValueList");

		String ackToken = (String) ackExpression.evaluate(doc,
				XPathConstants.STRING);
		// print("ACK from ebay API :: ", ackToken);
		if (!ackToken.equals("Success")) {
			throw new Exception(" service returned an error");
		}

		NodeList nodes = (NodeList) itemExpression.evaluate(doc,
				XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); i++) {

			Node node = nodes.item(i);

			price = (String) xpath.evaluate("ConvertedCurrentPrice", node,
					XPathConstants.STRING);

			productPrice = Double.parseDouble(price);

			NodeList nodesSpecifics = (NodeList) itemSpecifics.evaluate(doc,
					XPathConstants.NODESET);

			for (int j = 0; j < nodesSpecifics.getLength(); j++) {

				Node nodeSpecific = nodesSpecifics.item(j);

				String name = (String) xpath.evaluate("Name", nodeSpecific,
						XPathConstants.STRING);
				String value = (String) xpath.evaluate("Value", nodeSpecific,
						XPathConstants.STRING);

				if (name.equals("Product Family")) {
					productFamily = value;
					productFamily = productFamily.replaceAll(" ", "");
				}
				if (name.equals("Screen Size")) {
					screenSize = value;
					screenSize = screenSize.replace("\"", "");
					screenSize = screenSize.replaceAll("'", "");
				}
				if (name.equals("Processor Type")) {
					processorType = value;
					processorType = processorType.replaceAll(" ", "");
				}
				if (name.equals("Operating System")) {
					operatingSystem = value;
					operatingSystem = operatingSystem.replace(",", "-");
					operatingSystem = operatingSystem.replaceAll(" ", "");
					operatingSystem = operatingSystem.replaceAll("\"", "");

				}
				if (name.equals("Memory")) {
					memory = value;
					if (memory.length() > 5) {
						String[] mem = memory.split(" ");
						memory = mem[0];
					}
					memory = memory.replaceAll("\\D+", "");
				}
				if (name.equals("Hard Drive Capacity")) {
					hardDriveCapacity = value;
					hardDriveCapacity = hardDriveCapacity
							.replaceAll("\\D+", "");
				}
				if (name.equals("Processor Speed")) {
					processorSpeed = value;
					processorSpeed = processorSpeed.replaceAll("\\D+", "");
				}

			}

		}

		if (productFamily != null && screenSize != null
				&& operatingSystem != null && processorType != null
				&& !screenSize.contains(" ") && memory != null
				&& hardDriveCapacity != null && processorSpeed != null) {
			stringForData = productFamily + ",,," + operatingSystem + ",,,"
					+ processorType + ",,," + screenSize + ",,," + memory
					+ ",,," + hardDriveCapacity + ",,," + processorSpeed
					+ ",,," + productPrice;

		}

		is.close();
		return ackToken;

	}

	public String getData() throws Exception {
		EbayDriverFindingAPI ebdf = new EbayDriverFindingAPI();
		String ids = ebdf.getData();
		String[] listOfIds = ids.split(",");

		String itemsData = "";

		for (int i = 0; i < listOfIds.length; i++) {
			EbayDriverShoppingAPI driver = new EbayDriverShoppingAPI();
			driver.run(java.net.URLEncoder.encode(listOfIds[i], "UTF-8"));
			itemsData = itemsData + stringForData + "//";
		}

		return itemsData;

	}

}