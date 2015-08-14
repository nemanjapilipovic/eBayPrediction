package data;

public abstract class EbayDriver {

	public abstract String createAddress(String tag);

	public abstract String processResponse(String response) throws Exception;

	public abstract String getData() throws Exception;

	public String run(String tag) throws Exception {

		String address = createAddress(tag);
		// print("sending request to :: ", address);
		String response = URLReader.read(address);
		// print("response :: ", response);
		// process xml dump returned from EBAY
		return processResponse(response);

	}

}
