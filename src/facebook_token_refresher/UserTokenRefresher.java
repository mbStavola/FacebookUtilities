package facebook_token_refresher;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.User;

// TODO: Add support for accounts with 2-factor authentication
/*
 * How to use:
 * 1) Use the TokenRefresher() constructor to create a new token. You will be prompted in the console for your username and password
 * 2) Use the TokenRefresher(username, password) constructor to create a new token with no further interaction required.
 */

public class UserTokenRefresher {
	
	// For configuration
	private XMLConfiguration config;
	
	// User's username and password
	private String username;
	private String password;
	
	private final String configLogins = "facebook-login.";
	private final String configUrls = "token-refresher.urls.";
	private final String configHtmlElementIds = "token-refresher.html-element-ids.";
	private final String configParameters = "token-refresher.parameters.";
	

	// The constructor initializes the config file, the username field, and the password field
	public UserTokenRefresher(String configFilePath) throws ConfigurationException, IOException {
		// Create the default config file if ours is missing
		File configFile = new File(configFilePath);
		if (!configFile.exists()) {
			createDefaultConfigFile(configFile);
		}
		// Setup new PropertiesConfiguration object
		config = new XMLConfiguration(configFile);
		
		// Sets username and password
		username = config.getString(configLogins + "username");
		password = config.getString(configLogins +"password");
	}
	// Runs when there is no config file in the running directory, and creates that file. Note that this file may not work in the future.
	public void createDefaultConfigFile(File configFile) throws IOException, ConfigurationException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
		writer.flush();
		writer.close();
	}

	// Returns a string containing the FB access token
	@SuppressWarnings("deprecation")
	public String getFacebookAccessToken() throws FailingHttpStatusCodeException, MalformedURLException, IOException, ConfigurationException {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); // Suppress HtmlUnit's warnings

		// Initialize the web client and go to the OAuth URL, where you will be prompted to login
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6); // FF 3.6 is the only browser agent that seems to work.
		HtmlPage facebookPage = webClient.getPage(config.getString(configUrls + "initial-url"));


		// Target the login form and have the user enter their username and password
		final HtmlForm loginForm = (HtmlForm) facebookPage.getElementById(config.getString(configHtmlElementIds + "login-form")); // FB login form

		final HtmlTextInput loginFormEmail = loginForm.getElementById(config.getString(configHtmlElementIds + "login-form-email")); // FB login form's email field
		loginFormEmail.setValueAttribute(username);

		final HtmlPasswordInput loginFormPassword = loginForm.getElementById(config.getString(configHtmlElementIds + "login-form-password")); // FB login form's password field
		loginFormPassword.setValueAttribute(password);
		
		final HtmlSubmitInput loginFormButton = loginForm.getElementById(config.getString(configHtmlElementIds + "login-form-submit")); // FB login form's submit button
		facebookPage = loginFormButton.click(); // Click "Log In"


		// Now we should be redirected to the Wunderman page, which contains our code in the URL, which we will parse into a String
		String wundermanURL = facebookPage.getUrl().toString();
		String wundermanCode = parseParameterFromURL(wundermanURL, config.getString(configParameters + "code"), false); // Get "code" parameter

		String facebookOAuthTextPage = ((TextPage) webClient.getPage(config.getString(configUrls + "oauth-url")+ wundermanCode)).getContent(); // The page returned is a simple text file containing the token, which is printed to stdout

		return parseParameterFromURL(
				facebookOAuthTextPage, // Text returned by Facebook will be in the form of parameters
				config.getString(configParameters + "access-token"), // Parse out the access token's parameter
				false); // Do not include the parameter's label in the return string
	}
	
	public static boolean isTokenValid(String token) {
		try {
			// Try to retrieve your own profile using the access token. If it doesn't work, it will throw an exception.
			FacebookClient client = new DefaultFacebookClient(token);
			client.fetchObject("me", User.class);
		}
		catch (FacebookOAuthException e) {
			return false;
		}
		return true;
	}
	// Takes a String representing the URL you want to parse, a parameter to find, and a boolean. The boolean controls whether the method returns "parameter=value" or "value"
	public String parseParameterFromURL(String URL, String parameter, boolean includeParameterLabel) {
		String symbol;
		if (URL.contains("&" + parameter + "="))
			symbol = "&";
		else if (URL.contains("?" + parameter + "="))
			symbol = "?";
		else if (URL.contains(parameter + "="))
			symbol = "";
		else
			return "";
		
		if (includeParameterLabel)
			URL = URL.substring(URL.indexOf(symbol + parameter)+symbol.length());
		else
			URL = URL.substring(URL.indexOf(symbol + parameter)+(symbol + parameter).length()+1);
		
		if (URL.contains("&"))
			URL = URL.substring(0, URL.indexOf("&"));
		else if (URL.contains("/"))
			URL = URL.substring(0, URL.indexOf("/"));
		
		return URL;
	}
	public XMLConfiguration getConfig() {
		return config;
	}
}
