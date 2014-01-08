package facebook_scraper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.batchfb.Batcher;
import com.googlecode.batchfb.Later;


public class FacebookBatchUtils {
	
	// Detects if the passed String is a file. If it is, it returns the contents of the file (as this is presumably a token). If not, it retr
	public static String getToken(String tokenString) throws IOException {
		Path tokenPath = Paths.get(tokenString);
		if (Files.exists(tokenPath)) { // If the token was passed as a file
			File tokenFile = new File(tokenString);
			BufferedReader reader = new BufferedReader(new FileReader(tokenFile));
			String token = reader.readLine();
			reader.close();
			return token;
		}
		else {
			return tokenString;
		}
	}
	
	// Takes a String to a file where each newline is a brand's name, and makes a map. The map's key is the brand's name from the BufferedReader, and the value is a corresponding BatchFB object.
	public static Map<String, Later<JsonNode>> getMapOfBrands(Batcher batcher, String brandNames) throws IOException {
		File file = new File(brandNames);
		return getMapOfBrands(batcher, file);
	}
	// Takes a file where each newline is a brand's name, and makes a map. The map's key is the brand's name from the BufferedReader, and the value is a corresponding BatchFB object.
	public static Map<String, Later<JsonNode>> getMapOfBrands(Batcher batcher, File brandNames) throws IOException {
		Map<String, Later<JsonNode>> map = new HashMap<String, Later<JsonNode>>();
		BufferedReader brandsReader = new BufferedReader(new FileReader(brandNames)); // Reader that will read in each line of the brands file
		
		String aBrandName = null;
		while ((aBrandName = brandsReader.readLine()) != null) {
			map.put(aBrandName, batcher.graph(aBrandName));
		}
		
		brandsReader.close();
		return map;
		
	}
	// Takes a BufferedReader where each newline is a brand's name, and makes a map. The map's key is the brand's name from the file, and the value is a corresponding BatchFB object.
	public static Map<String, Later<JsonNode>> getMapOfBrands(Batcher batcher, BufferedReader brandNames) throws IOException {
		Map<String, Later<JsonNode>> map = new HashMap<String, Later<JsonNode>>();
		String aBrandName = null;
		while ((aBrandName = brandNames.readLine()) != null) {
			map.put(aBrandName, batcher.graph(aBrandName));
		}
		
		brandNames.close();
		return map;
	}
	// Takes a list of brand names, and makes a map. The map's key is the brand's name from the List, and the value is a corresponding BatchFB object.
	public static Map<String, Later<JsonNode>> getMapOfBrands(Batcher batcher, List<String> brandNames) throws IOException {
		Map<String, Later<JsonNode>> map = new HashMap<String, Later<JsonNode>>();
		for (String aBrandName : brandNames) {
			map.put(aBrandName, batcher.graph(aBrandName));
		}
		return map;
	}

	// Pass a List and delimiter and get a delimited String
	public static String getDelimitedList(List<String> fields, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (String field : fields) {
			sb.append(field);
			sb.append(delimiter);
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	// Pass a Map Entry (with its key as its name and its value as its BatchFB object), a delimiter, and the elements you want from that BatchFB object and get a delimited String of those elements only
	public static String getDelimitedElementsFromBrand(Map.Entry<String, Later<JsonNode>> aBrand, String delimiter, List<String> elements) {
		String brandName = aBrand.getKey(); // This brand's name as it was entered into our imported text file
		JsonNode brand; // The data that BatchFB will retrieve for the current brand it is up to will be stored here

		StringBuilder sb = new StringBuilder(brandName); // All of our data will be concatenated onto this string (along with delimiters) and then returned

		// Try to get this brand. This brand may not exist, in which case we will print a message to stderr and continue on
		try {
			brand = aBrand.getValue().get();
		}
		catch (Exception e) {
			System.err.println("could not find " + brandName);
			return null;
		}

		// Now that we have the brand, iterate through each element that we wanted and put it into the returned String.
		// Some brands are missing some data, so they might throw an exception. That's okay, we will skip those fields that they don't have and continue getting the rest of their data
		for (String element : elements) {
				if (element != null) { // If the element is null, we will skip the entire process of concatenating to the StringBuilder
					try {
						String brandField = brand.path(element).asText(); // The field of this element returned from this brand
						brandField = brandField
							.replaceAll(delimiter, "") // Replace all delimiters with spaces
							.replaceAll("\n", "") // Remove newlines
							.replaceAll("\r", "") // Remove carriage returns
							.replaceAll("null", ""); // Removes "null" in Strings
						sb.append(brandField); // Append the element that we got from the current page
					}
					catch (Exception e) {}
				}
				sb.append(delimiter); // Append a delimiter at the end of each element, whether or not we got that element, to keep things in consistent "columns"
		}
		sb.setLength(sb.length() - 1); // Remove the last delimiter because it is just floating at the end of the line now
		return sb.toString();
	}

	// Takes a 2D array of any type and the desired column and returns a List containing just that column
	public static <T> List<T> get2DArrayColumn(T[][] array, int column) {
		List<T> list = new ArrayList<>();
		for (T[] row : array)
			list.add(row[column]);
		return list;
	}
	
	// Takes a generic list and converts it to a String list
	public static List<String> convertToStringList(List<?> list) {
		List<String> strings = new ArrayList<String>();
		for (Object object : list) {
			strings.add(object != null ? object.toString() : null);
		}
		return strings;
	}
}
