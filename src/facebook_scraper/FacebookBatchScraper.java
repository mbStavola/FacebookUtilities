package facebook_scraper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.batchfb.Batcher;
import com.googlecode.batchfb.FacebookBatcher;
import com.googlecode.batchfb.Later;


public class FacebookBatchScraper {
	private XMLConfiguration config;
	private String delimiter;
	private final List<String> fields;
	private final List<String> headers;
	private final String headerString;
	private final Batcher batcher;
	private final Map<String, Later<JsonNode>> allBrandsMap;
	
	// Constructor sets up the config file, access token, batcher, stdout header, and 
	public FacebookBatchScraper(String configXmlFile, String tokenTextFile, String brandsTextFile) throws ConfigurationException, IOException {
		config = new XMLConfiguration(configXmlFile); // Gets the config file
		
		batcher = new FacebookBatcher(FacebookBatchUtils.getToken(tokenTextFile)); // The Facebook batch retriever object

		// This data will be shown on the top of stdout
		headers = FacebookBatchUtils.convertToStringList(config.getList("fields.field.human-readable-name")); // Gets the human-readable names for the aforementioned fields that we will use as the header for stdout
		delimiter = config.getString("delimiter"); // Gets the delimiter that goes between fields
		headerString = FacebookBatchUtils.getDelimitedList(headers, delimiter);
		

		// This map holds each brand. The delimiter will be used to separate each field.
		// Keys are the brands we want, Values are their corresponding BatchFB objects
		allBrandsMap = FacebookBatchUtils.getMapOfBrands(batcher, brandsTextFile);
		
		fields = FacebookBatchUtils.convertToStringList(config.getList("fields.field.facebook-graph-name")); // Gets the fields that we will request from BatchFB in a list
	}

	// Returns every brand's info into a single String, with each brand separated by a newline
	public String getAllFieldInfoFromBrands(boolean appendHeaderAtStart) throws IOException, ConfigurationException {
		StringBuilder sb = new StringBuilder();
		if (appendHeaderAtStart) { // Append the header if needed
			sb.append(headerString);
			sb.append("\n");
		}

		for (Map.Entry<String, Later<JsonNode>> brand: allBrandsMap.entrySet()) { // Iterates through each brand in the Map
			String row = FacebookBatchUtils.getDelimitedElementsFromBrand(brand, delimiter, fields); // Get the delimited fields you request from this brand
			if (row != null) { // If the row has data, append it and a newline
				sb.append(row);
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
