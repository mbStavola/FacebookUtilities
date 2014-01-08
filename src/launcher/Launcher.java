package launcher;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import facebook_scraper.FacebookBrandsScraper;
import facebook_token_refresher.TokenGenerator;


public class Launcher {
	public static void main(String[] args) throws ConfigurationException, IOException {
		if (args.length == 1) {
			TokenGenerator.run(args[0]);
		}
		else if (args.length == 3) {
			FacebookBrandsScraper.run(args[0], args[1], args[2]);
		}
		else {
			System.out.println("ERROR: Invalid number of parameters passed");
			System.out.println("Please invoke this program either in the form:");
			System.out.println("\"java -jar ProgramName.jar <configXmlFile>\" to print a new access token to stdout");
			System.out.println("OR");
			System.out.println("\"java -jar ProgramName.jar <configXmlFile> <tokenTxtFile> <brandsTxtFile>\" to scrape Facebook for the requested brands");
		}
	}
}
