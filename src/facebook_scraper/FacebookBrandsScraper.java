package facebook_get;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

public class FacebookBrandsScraper {
	public static void main(String[] args) throws ConfigurationException, IOException {
		FacebookBatchScraper scraper = new FacebookBatchScraper(new File(args[0]), new File(args[1]));
		String allScrapedData = scraper.getAllFieldInfoFromBrands(true);
		System.out.println(allScrapedData);
	}
}
