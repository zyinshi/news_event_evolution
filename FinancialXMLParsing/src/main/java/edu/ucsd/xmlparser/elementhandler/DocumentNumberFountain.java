package edu.ucsd.xmlparser.elementhandler;

import java.util.HashMap;
import java.util.Map;

/**
 * We need to number elements within a document for ease of reference among other things. In other words we are keeping a counter. 
 * 
 * @author rogertan
 *
 */
public class DocumentNumberFountain {
	private Map<String, Integer> elementNameAndNumber = new HashMap<String, Integer>();
	
	public Integer next(String elementName) {
		if(elementName == null) {
			throw new IllegalArgumentException("Element name can not be null.");
		}
		
		if(!elementNameAndNumber.containsKey(elementName)) {
			elementNameAndNumber.put(elementName, 0);
		}
		
		Integer nextNumber = elementNameAndNumber.get(elementName);
		elementNameAndNumber.put(elementName, nextNumber++);
		
		return nextNumber;
	}
}
