package edu.ucsd.xmlparser.entity;

public enum ReferenceType {
	DOCUMENT("Document"), SECTION("Section");
	
	private String inString;
	
	ReferenceType(String inString) {
		this.inString = inString;
	}
	
	public String getFriendlyString() {
		return inString;
	}
}
