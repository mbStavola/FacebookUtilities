package facebook_scraper;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

public class FacebookBrandsScraper {
	public static void run(String configXmlFile, String tokenTextFile, String brandsTextFile) throws ConfigurationException, IOException {
		FacebookBatchScraper scraper = new FacebookBatchScraper(configXmlFile, tokenTextFile, brandsTextFile);
		String allScrapedData = scraper.getAllFieldInfoFromBrands(true);
		System.out.println(allScrapedData);
	}
}
