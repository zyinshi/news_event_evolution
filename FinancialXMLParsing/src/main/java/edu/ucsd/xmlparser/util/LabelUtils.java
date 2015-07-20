package edu.ucsd.xmlparser.util;

import edu.ucsd.xmlparser.entity.SpecialCharacters;

public abstract class LabelUtils {
	private static String LABELSTRATEGY_PREFIX = "_";
	
	public static String createLabel(String userSuppliedLabel) {
		return LABELSTRATEGY_PREFIX + userSuppliedLabel;
	}
	
	public static String labelPOS(String posInformation) {
		return "POS{" + posInformation + "}";
	}
	
	public static String labelNE(String neInformation) {
		return "NE{" + neInformation + "}";
	}

	/**
	 * If nodeName starts with "#" remove it, because it's a special character in Neo4J
	 * and then we want to uppercase the first character of the Node Name
	 * 
	 * @param nodeName
	 * @return
	 */
	public static String createLabelFromNodeName(String nodeName) {
		String label = nodeName;
		
		if(nodeName.startsWith(SpecialCharacters.HASH.getCharacter())) {
			label = nodeName.substring(1);
		}
		
		return label.substring(0, 1).toUpperCase().concat(label.substring(1));
	}
}
