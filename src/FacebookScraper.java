import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookGraphException;
import com.restfb.types.Page;


public class FacebookScraper {
	
/*
 * Parameters needed:
 * arg[0] = userToken
 * arg[1] = brandsFilePath
 */
	
	// START SINGLETON
	private static FacebookScraper instance;
	private FacebookScraper() {}
	public static synchronized FacebookScraper getFacebookScraper() {
		if (instance == null) {
			instance = new FacebookScraper();
		}
		return instance;
	}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}
	// END SINGLETON

	
	private Path brandsFilePath; // The path to the txt file containing the brands
	private List<String> brands; // A list where each element is one brand to scrape
	
	private final String delimiter = "`~!"; // The delimiter between fields in our output
	private FacebookClient facebookClient; // The actual FB client that will be used to scrape data
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Number of parameters is incorrect! Please pass your user-token and your txt file path only");
			System.exit(1);
		}
		getFacebookScraper().facebookClient = new DefaultFacebookClient(args[0]); // Creates the FB client using your access token
		getFacebookScraper().brandsFilePath = Paths.get(args[1]); // Sets the brands filepath
		
		getFacebookScraper().brands = Files.readAllLines(getFacebookScraper().brandsFilePath, Charset.defaultCharset()); // Gets each line from the file and puts it into the list
		
		getFacebookScraper().scrapeBrands(getFacebookScraper().brands); // Scrape the data for every brand
	}
	
	public void scrapeBrands(List<String> names) throws FacebookGraphException {
		String company;
		Page page;
		Scanner sc = new Scanner(System.in);
		System.out.println("ID|FBID|NAME|USERNAME|LINK|CATEGORY|LIKES|CHECKINS|TALKING_ABOUT_COUNT|FOUNDED|PRODUCTS|COMPANY_OVERVIEW|MISSION|DESCRIPTION");
		for(String name:names){
			try{
				page = facebookClient.fetchObject(name.replaceAll("[\n\r]",""), Page.class);
				company = (name + delimiter + page.getId() +delimiter + page.getName() + delimiter + page.getUsername() + delimiter +page.getLink() + delimiter + page.getCategory() + delimiter + page.getLikes() + delimiter + page.getCheckins() + delimiter + page.getTalkingAboutCount() + delimiter + page.getFounded() + delimiter + page.getProducts() + delimiter + page.getCompanyOverview() + delimiter + page.getMission() + delimiter + page.getDescription());
				company = company.replaceAll("[\n\r]","").replaceAll("\\|", " ").replaceAll("`~!", "|").replaceAll("null", "");
				System.out.println(company);
			} catch (FacebookGraphException e) {
				System.err.println("could not find " + name);
			}
		}
		sc.close();
	}

}
