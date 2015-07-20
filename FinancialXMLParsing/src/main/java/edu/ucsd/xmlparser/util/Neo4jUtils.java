package edu.ucsd.xmlparser.util;

public class Neo4jUtils {
	public static String likeInput(String input) {
		return "(?i).*" + input + ".*";
	}
}
