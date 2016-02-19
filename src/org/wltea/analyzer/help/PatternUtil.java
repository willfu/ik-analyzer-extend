package org.wltea.analyzer.help;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by masonqwli on 16/2/19.
 */
public class PatternUtil {
	private static String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static String EMAIL_REGEX = "^(?:.*?<)?[-A-Za-z\\d.+_=]+@[-A-Za-z\\d._]+\\.[-A-Za-z\\d._]+>?$";
	private static Pattern urlPattern = Pattern.compile(URL_REGEX);
	private static Pattern emailPattern = Pattern.compile(EMAIL_REGEX);


	public static boolean isEmail(String text) {
		Matcher matcher = emailPattern.matcher(text);
		return matcher.find();
	}

	public static boolean isUrl(String text) {
		Matcher matcher = urlPattern.matcher(text);
		return matcher.find();
	}
}
