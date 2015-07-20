package edu.ucsd.xmlparser.entity;

public enum NodeAttributes {
	VALUE("nodeValue"), ORIGINAL_TAG("originalTag");
	
	private String attributeName;
	
	NodeAttributes(String attributeName) {
		if(attributeName == null) {
			throw new IllegalArgumentException("Attribute Name can not be null.");
		}
		this.attributeName = attributeName;
	}
	
	public String getAttributeName() {
		return this.attributeName;
	}
}
