package facebook_token_refresher;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

// Generates a new token and prints it to stdout. This token should be piped into a plaintext file so that it can be used elsewhere
public class TokenGenerator {

	public static void run(String configXmlFile) throws ConfigurationException, IOException {
		// Create a UserTokenRefresher object and get a new token using this object
		UserTokenRefresher refresher = new UserTokenRefresher(configXmlFile);
		String token = refresher.getFacebookAccessToken();

		// Check the token's validity.
		// If the token is valid, print it to stdout.
		// If the token is not valid, print an error to stderr.
		if (UserTokenRefresher.isTokenValid(token)) {
			System.out.println(token);
			System.err.println("Token generated successfully");
		}
		else {
			System.err.println("ERROR: The generated token was invalid");
		}
	}
}
