package edu.ucsd.xmlparser.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * We can't really use a more specialized class because we may encounter a situation where a tag is unrecognized and we'll have to 
 * 
 * 
 * @author rogertan
 *
 */
public class GraphNode {
	private Long id;
	private Map<String, Object> propertyNameToValues = new HashMap<String, Object>();
	
	public final Long getId() {
		return this.id;
	}
	
	/**
	 * Will be set through reflection 
	 * 
	 * @param id
	 */
	@SuppressWarnings("unused")
	private final void setId(Long id) {
		this.id = id;
	}
	
	public void setProperty(String propertyName, Object propertyValue) {
		if(propertyName == null) {
			throw new IllegalArgumentException("Property Name for a Graph Node can not be null.");
		}
		
		this.propertyNameToValues.put(propertyName, propertyValue);
	}
	
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(this.propertyNameToValues);
	}
}
