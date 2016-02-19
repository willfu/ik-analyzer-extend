package org.wltea.analyzer.help;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by masonqwli on 16/2/19.
 */
public class PatternUtil {
	private static String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static Pattern urlPattern = Pattern.compile(regex);

	public static boolean isUrl(String text) {
		Matcher matcher = urlPattern.matcher(text);
		return matcher.find();
	}
}
