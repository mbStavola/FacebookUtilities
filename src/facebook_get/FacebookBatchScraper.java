package facebook_get;

import java.io.File;
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
	private final String token;
	private final String headerString;
	private final Batcher batcher;
	private final Map<String, Later<JsonNode>> allBrandsMap;
	
	public FacebookBatchScraper(File configXmlFilePath, File brandsTextFilePath) throws ConfigurationException, IOException {
		config = new XMLConfiguration(configXmlFilePath); // Gets the config file
		
		token = config.getString("token"); // The access token
		batcher = new FacebookBatcher(token); // The Facebook batch retriever object

		// This data will be shown on the top of stdout
		headers = FacebookBatchUtils.convertToStringList(config.getList("fields.field.human-readable-name")); // Gets the human-readable names for the aforementioned fields that we will use as the header for stdout
		delimiter = config.getString("delimiter"); // Gets the delimiter that goes between fields
		headerString = FacebookBatchUtils.getDelimitedList(headers, delimiter);
		

		// This map holds each brand. The delimiter will be used to separate each field.
		allBrandsMap = FacebookBatchUtils.getMapOfBrands(batcher, brandsTextFilePath); // Keys are the brands we want, Values are their corresponding BatchFB objects
		
		fields = FacebookBatchUtils.convertToStringList(config.getList("fields.field.facebook-graph-name")); // Gets the fields that we will request from BatchFB in a list
	}

	public String getAllFieldInfoFromBrands(boolean appendHeaderAtStart) throws IOException, ConfigurationException {
		StringBuilder sb = new StringBuilder();
		if (appendHeaderAtStart) // Append the header if needed
			sb.append(headerString);
		sb.append("\n");
		// Iterates through the now brand-filled Map, getting the required data from each brand
		for (Map.Entry<String, Later<JsonNode>> brand: allBrandsMap.entrySet()) {
			String row = FacebookBatchUtils.getDelimitedElementsFromBrand(brand, delimiter, fields);
			if (row != null) {
				sb.append(row);
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
